package com.gdg.Todak.friend.service;

import com.gdg.Todak.friend.FriendStatus;
import com.gdg.Todak.friend.dto.FriendNameRequest;
import com.gdg.Todak.friend.entity.Friend;
import com.gdg.Todak.friend.exception.BadRequestException;
import com.gdg.Todak.friend.repository.FriendRepository;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class FriendServiceTest {

    @Autowired
    private FriendService friendService;

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member requester;
    private Member accepter;

    @BeforeEach
    void setUp() {
        requester = memberRepository.save(new Member("requesterUser", "test1", "test1", "test1"));
        accepter = memberRepository.save(new Member("accepterUser", "test2", "test2", "test2"));
    }

    @Test
    @DisplayName("친구 요청 성공")
    void makeFriendRequestSuccessfullyTest() {
        //given
        FriendNameRequest request = new FriendNameRequest(accepter.getUsername());

        //when
        friendService.makeFriendRequest(requester.getUsername(), request);

        //then
        Optional<Friend> friendRequest = friendRepository.findByRequesterAndAccepter(requester, accepter);
        assertThat(friendRequest).isPresent();
        assertThat(friendRequest.get().getFriendStatus()).isEqualTo(FriendStatus.PENDING);
    }

    @Test
    @DisplayName("본인에게 친구 요청 불가")
    void notAllowSelfFriendRequestTest() {
        //given
        FriendNameRequest request = new FriendNameRequest(requester.getUsername());

        //when & then
        assertThatThrownBy(() -> friendService.makeFriendRequest(requester.getUsername(), request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("본인에게는 친구 요청을 할 수 없습니다");
    }

    @Test
    @DisplayName("중복 친구 요청 불가")
    void notAllowDuplicateFriendRequestTest() {
        //given
        FriendNameRequest request = new FriendNameRequest(accepter.getUsername());

        //when
        friendService.makeFriendRequest(requester.getUsername(), request);

        //then
        assertThatThrownBy(() -> friendService.makeFriendRequest(requester.getUsername(), request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이미 친구이거나, 대기 또는 거절된 친구요청이 존재합니다.");
    }

    @Test
    @DisplayName("친구 요청 수락")
    void acceptFriendRequestTest() {
        //given
        Friend friend = friendRepository.save(Friend.builder().requester(requester).accepter(accepter).friendStatus(FriendStatus.PENDING).build());

        //when
        friendService.acceptFriendRequest(accepter.getUsername(), friend.getId());

        //then
        Friend updatedFriend = friendRepository.findById(friend.getId()).orElseThrow();
        assertThat(updatedFriend.getFriendStatus()).isEqualTo(FriendStatus.ACCEPTED);
    }

    @Test
    @DisplayName("친구 요청 거절")
    void declineFriendRequestTest() {
        //given
        Friend friend = friendRepository.save(Friend.builder().requester(requester).accepter(accepter).friendStatus(FriendStatus.PENDING).build());

        //when
        friendService.declineFriendRequest(accepter.getUsername(), friend.getId());

        //then
        Friend updatedFriend = friendRepository.findById(friend.getId()).orElseThrow();
        assertThat(updatedFriend.getFriendStatus()).isEqualTo(FriendStatus.DECLINED);
    }

    @Test
    @DisplayName("친구 삭제")
    void deleteFriendTest() {
        //given
        Friend friend = friendRepository.save(Friend.builder().requester(requester).accepter(accepter).friendStatus(FriendStatus.ACCEPTED).build());

        //when
        friendService.deleteFriend(requester.getUsername(), friend.getId());

        //then
        Optional<Friend> deletedFriend = friendRepository.findById(friend.getId());
        assertThat(deletedFriend).isEmpty();
    }
}
