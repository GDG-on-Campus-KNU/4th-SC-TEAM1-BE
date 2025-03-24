package com.gdg.Todak.friend.service;

import com.gdg.Todak.friend.FriendStatus;
import com.gdg.Todak.friend.dto.FriendCountResponse;
import com.gdg.Todak.friend.dto.FriendNameRequest;
import com.gdg.Todak.friend.dto.FriendRequestResponse;
import com.gdg.Todak.friend.dto.FriendResponse;
import com.gdg.Todak.friend.entity.Friend;
import com.gdg.Todak.friend.exception.BadRequestException;
import com.gdg.Todak.friend.exception.NotFoundException;
import com.gdg.Todak.friend.exception.UnauthorizedException;
import com.gdg.Todak.friend.repository.FriendRepository;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendService {

    private final FriendRepository friendRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void makeFriendRequest(String memberName, FriendNameRequest friendNameRequest) {
        Member requesterMember = getMember(memberName);
        Member accepterMember = memberRepository.findByUsername(friendNameRequest.friendName())
                .orElseThrow(() -> new NotFoundException("friendName에 해당하는 멤버가 없습니다."));

        if (friendRepository.existsByRequesterAndAccepter(requesterMember, accepterMember) || friendRepository.existsByRequesterAndAccepter(accepterMember, requesterMember)) {
            throw new BadRequestException("이미 친구이거나, 대기 또는 거절된 친구요청이 존재합니다.");
        }

        if (requesterMember.equals(accepterMember)) {
            throw new BadRequestException("본인에게는 친구 요청을 할 수 없습니다");
        }

        long requesterFriendCount = friendRepository.countByRequesterAndStatusIn(requesterMember, List.of(FriendStatus.PENDING, FriendStatus.ACCEPTED));
        if (requesterFriendCount >= 20) {
            throw new BadRequestException("친구 요청 개수를 초과하였습니다. (최대 20개)");
        }

        long accepterFriendCount = friendRepository.countByAccepterAndStatusIn(accepterMember, List.of(FriendStatus.PENDING, FriendStatus.ACCEPTED));
        if (accepterFriendCount >= 20) {
            throw new BadRequestException("상대방이 더 이상 친구 요청을 받을 수 없습니다. (최대 20개)");
        }

        friendRepository.save(Friend.builder()
                .requester(requesterMember)
                .accepter(accepterMember)
                .friendStatus(FriendStatus.PENDING)
                .build());
    }

    public List<FriendResponse> getAllFriend(String memberName) {
        return friendRepository.findAllByAccepterUsernameAndFriendStatusOrRequesterUsernameAndFriendStatus(memberName, FriendStatus.ACCEPTED, memberName, FriendStatus.ACCEPTED)
                .stream().map(friend -> {
                    if (friend.getRequester().getUsername().equals(memberName)) {
                        return new FriendResponse(
                                friend.getId(),
                                friend.getAccepter().getUsername()
                        );
                    } else {
                        return new FriendResponse(
                                friend.getId(),
                                friend.getRequester().getUsername()
                        );
                    }
                })
                .collect(Collectors.toList());
    }

    public List<FriendRequestResponse> getAllDeclinedFriends(String memberName) {
        return friendRepository.findAllByAccepterUsernameAndFriendStatusOrRequesterUsernameAndFriendStatus(memberName, FriendStatus.DECLINED, memberName, FriendStatus.DECLINED)
                .stream().map(
                        Friend -> new FriendRequestResponse(
                                Friend.getId(),
                                Friend.getRequester().getUsername(),
                                Friend.getAccepter().getUsername()
                        ))
                .collect(Collectors.toList());
    }

    public List<FriendRequestResponse> getAllFriendRequests(String memberName) {
        return friendRepository.findAllByAccepterUsernameAndFriendStatusOrRequesterUsernameAndFriendStatus(memberName, FriendStatus.PENDING, memberName, FriendStatus.PENDING)
                .stream().map(
                        Friend -> new FriendRequestResponse(
                                Friend.getId(),
                                Friend.getRequester().getUsername(),
                                Friend.getAccepter().getUsername()
                        ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void acceptFriendRequest(String memberName, Long friendRequestId) {
        Member member = getMember(memberName);

        Friend friendRequest = friendRepository.findById(friendRequestId)
                .orElseThrow(() -> new NotFoundException("friendRequestId에 해당하는 친구요청이 없습니다."));

        if (friendRequest.checkMemberIsNotAccepter(member)) {
            throw new UnauthorizedException("친구 요청을 수락할 권한이 없습니다. 요청받은 사람만 수락할 수 있습니다.");
        }

        friendRequest.acceptFriendRequest();
    }

    @Transactional
    public void declineFriendRequest(String memberName, Long friendRequestId) {
        Member member = getMember(memberName);

        Friend friendRequest = friendRepository.findById(friendRequestId)
                .orElseThrow(() -> new NotFoundException("friendRequestId에 해당하는 친구요청이 없습니다."));

        if (friendRequest.checkMemberIsNotAccepter(member)) {
            throw new UnauthorizedException("친구 요청을 거절할 권한이 없습니다. 요청받은 사람만 거절할 수 있습니다.");
        }

        friendRequest.declinedFriendRequest();
    }

    @Transactional
    public void deleteFriend(String memberName, Long friendRequestId) {
        Member member = getMember(memberName);

        Friend friendRequest = friendRepository.findById(friendRequestId)
                .orElseThrow(() -> new NotFoundException("friendRequestId에 해당하는 친구요청이 없습니다."));

        if (friendRequest.checkMemberIsNotRequester(member) && friendRequest.checkMemberIsNotAccepter(member)) {
            throw new UnauthorizedException("친구를 삭제할 권한이 없습니다. 당사자들만 삭제할 수 있습니다.");
        }

        friendRepository.deleteById(friendRequestId);
    }

    public List<FriendCountResponse> getMyFriendCountByStatus(String memberName) {
        Member member = getMember(memberName);

        long PendingFriendCount = friendRepository.countByRequesterAndStatusIn(member, List.of(FriendStatus.PENDING));
        long AcceptedFriendCount = friendRepository.countByRequesterAndStatusIn(member, List.of(FriendStatus.ACCEPTED))
                + friendRepository.countByAccepterAndStatusIn(member, List.of(FriendStatus.ACCEPTED));

        return List.of(
                new FriendCountResponse(FriendStatus.PENDING, PendingFriendCount),
                new FriendCountResponse(FriendStatus.ACCEPTED, AcceptedFriendCount)
        );
    }

    private Member getMember(String memberName) {
        return memberRepository.findByUsername(memberName)
                .orElseThrow(() -> new NotFoundException("memberName에 해당하는 멤버가 없습니다."));
    }
}
