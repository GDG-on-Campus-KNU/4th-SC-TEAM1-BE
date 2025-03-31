package com.gdg.Todak.friend.service;

import com.gdg.Todak.friend.FriendStatus;
import com.gdg.Todak.friend.entity.Friend;
import com.gdg.Todak.friend.exception.NotFoundException;
import com.gdg.Todak.friend.repository.FriendRepository;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FriendCheckService {

    private final MemberRepository memberRepository;
    private final FriendRepository friendRepository;

    public List<Member> getFriendMembers(String userId) {
        Member member = getMember(userId);

        List<Friend> acceptedFriends = friendRepository.findAllByAccepterUserIdAndFriendStatusOrRequesterUserIdAndFriendStatus(
                userId, FriendStatus.ACCEPTED, userId, FriendStatus.ACCEPTED);

        return acceptedFriends.stream()
                .map(friend -> friend.getAccepter().equals(member) ? friend.getRequester() : friend.getAccepter())
                .toList();
    }

    private Member getMember(String userId) {
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("userId에 해당하는 멤버가 없습니다."));
    }
}
