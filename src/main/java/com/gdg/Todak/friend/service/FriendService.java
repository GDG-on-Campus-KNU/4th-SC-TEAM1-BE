package com.gdg.Todak.friend.service;

import com.gdg.Todak.friend.FriendStatus;
import com.gdg.Todak.friend.dto.*;
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
    public void makeFriendRequest(String userId, FriendIdRequest friendIdRequest) {
        Member requesterMember = getMember(userId);
        Member accepterMember = memberRepository.findByUserId(friendIdRequest.friendId())
                .orElseThrow(() -> new NotFoundException("friendId에 해당하는 멤버가 없습니다."));

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

    public List<FriendResponse> getAllFriend(String userId) {
        return friendRepository.findAllByAccepterUserIdAndFriendStatusOrRequesterUserIdAndFriendStatus(userId, FriendStatus.ACCEPTED, userId, FriendStatus.ACCEPTED)
                .stream().map(friend -> {
                    if (friend.getRequester().getUserId().equals(userId)) {
                        return new FriendResponse(
                                friend.getId(),
                                friend.getAccepter().getUserId()
                        );
                    } else {
                        return new FriendResponse(
                                friend.getId(),
                                friend.getRequester().getUserId()
                        );
                    }
                })
                .collect(Collectors.toList());
    }

    public List<FriendRequestWithStatusResponse> getAllPendingAndDeclinedFriendRequestByRequester(String userId) {
        return friendRepository.findAllByRequesterUserIdAndFriendStatusIn(userId, List.of(FriendStatus.PENDING, FriendStatus.DECLINED))
                .stream().map(
                        Friend -> new FriendRequestWithStatusResponse(
                                Friend.getId(),
                                Friend.getRequester().getUserId(),
                                Friend.getAccepter().getUserId(),
                                Friend.getFriendStatus()
                        ))
                .toList();
    }

    public List<FriendRequestWithStatusResponse> getAllPendingAndDeclinedFriendRequestByAccepter(String userId) {
        return friendRepository.findAllByAccepterUserIdAndFriendStatusIn(userId, List.of(FriendStatus.PENDING, FriendStatus.DECLINED))
                .stream().map(
                        Friend -> new FriendRequestWithStatusResponse(
                                Friend.getId(),
                                Friend.getRequester().getUserId(),
                                Friend.getAccepter().getUserId(),
                                Friend.getFriendStatus()
                        ))
                .toList();
    }

    public List<FriendRequestResponse> getAllFriendRequests(String userId) {
        return friendRepository.findAllByAccepterUserIdAndFriendStatusOrRequesterUserIdAndFriendStatus(userId, FriendStatus.PENDING, userId, FriendStatus.PENDING)
                .stream().map(
                        Friend -> new FriendRequestResponse(
                                Friend.getId(),
                                Friend.getRequester().getUserId(),
                                Friend.getAccepter().getUserId()
                        ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void acceptFriendRequest(String userId, Long friendRequestId) {
        Member member = getMember(userId);

        Friend friendRequest = friendRepository.findById(friendRequestId)
                .orElseThrow(() -> new NotFoundException("friendRequestId에 해당하는 친구요청이 없습니다."));

        if (friendRequest.checkMemberIsNotAccepter(member)) {
            throw new UnauthorizedException("친구 요청을 수락할 권한이 없습니다. 요청받은 사람만 수락할 수 있습니다.");
        }

        friendRequest.acceptFriendRequest();
    }

    @Transactional
    public void declineFriendRequest(String userId, Long friendRequestId) {
        Member member = getMember(userId);

        Friend friendRequest = friendRepository.findById(friendRequestId)
                .orElseThrow(() -> new NotFoundException("friendRequestId에 해당하는 친구요청이 없습니다."));

        if (friendRequest.checkMemberIsNotAccepter(member)) {
            throw new UnauthorizedException("친구 요청을 거절할 권한이 없습니다. 요청받은 사람만 거절할 수 있습니다.");
        }

        friendRequest.declinedFriendRequest();
    }

    @Transactional
    public void deleteFriend(String userId, Long friendRequestId) {
        Member member = getMember(userId);

        Friend friendRequest = friendRepository.findById(friendRequestId)
                .orElseThrow(() -> new NotFoundException("friendRequestId에 해당하는 친구요청이 없습니다."));

        if (friendRequest.checkMemberIsNotRequester(member) && friendRequest.checkMemberIsNotAccepter(member)) {
            throw new UnauthorizedException("친구를 삭제할 권한이 없습니다. 당사자들만 삭제할 수 있습니다.");
        }

        friendRepository.deleteById(friendRequestId);
    }

    public List<FriendCountResponse> getMyFriendCountByStatus(String userId) {
        Member member = getMember(userId);

        long PendingFriendCountByAccepter = friendRepository.countByRequesterAndStatusIn(member, List.of(FriendStatus.PENDING));
        long PendingFriendCountByRequester = friendRepository.countByAccepterAndStatusIn(member, List.of(FriendStatus.PENDING));
        long AcceptedFriendCount = friendRepository.countByRequesterAndStatusIn(member, List.of(FriendStatus.ACCEPTED))
                + friendRepository.countByAccepterAndStatusIn(member, List.of(FriendStatus.ACCEPTED));

        return List.of(
                new FriendCountResponse(FriendStatus.PENDING, "Accepter", PendingFriendCountByAccepter),
                new FriendCountResponse(FriendStatus.PENDING, "Requester", PendingFriendCountByRequester),
                new FriendCountResponse(FriendStatus.ACCEPTED, "Accepter or Requester", AcceptedFriendCount)
        );
    }

    private Member getMember(String userId) {
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("userId에 해당하는 멤버가 없습니다."));
    }
}
