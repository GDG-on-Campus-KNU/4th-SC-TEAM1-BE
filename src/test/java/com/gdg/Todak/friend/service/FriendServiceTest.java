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
        requester = memberRepository.save(new Member("requesterUser", "test1", "test1","test1", "test1"));
        accepter = memberRepository.save(new Member("accepterUser", "test2", "test2","test2", "test2"));
    }

    @Test
    @DisplayName("친구 요청 성공")
    void makeFriendRequestSuccessfullyTest() {
        //given
        FriendNameRequest request = new FriendNameRequest(accepter.getUserId());

        //when
        friendService.makeFriendRequest(requester.getUserId(), request);

        //then
        Optional<Friend> friendRequest = friendRepository.findByRequesterAndAccepter(requester, accepter);
        assertThat(friendRequest).isPresent();
        assertThat(friendRequest.get().getFriendStatus()).isEqualTo(FriendStatus.PENDING);
    }

    @Test
    @DisplayName("본인에게 친구 요청 불가")
    void notAllowSelfFriendRequestTest() {
        //given
        FriendNameRequest request = new FriendNameRequest(requester.getUserId());

        //when & then
        assertThatThrownBy(() -> friendService.makeFriendRequest(requester.getUserId(), request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("본인에게는 친구 요청을 할 수 없습니다");
    }

    @Test
    @DisplayName("중복 친구 요청 불가")
    void notAllowDuplicateFriendRequestTest() {
        //given
        FriendNameRequest request = new FriendNameRequest(accepter.getUserId());

        //when
        friendService.makeFriendRequest(requester.getUserId(), request);

        //then
        assertThatThrownBy(() -> friendService.makeFriendRequest(requester.getUserId(), request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이미 친구이거나, 대기 또는 거절된 친구요청이 존재합니다.");
    }

    @Test
    @DisplayName("친구 요청 수락")
    void acceptFriendRequestTest() {
        //given
        Friend friend = friendRepository.save(Friend.builder().requester(requester).accepter(accepter).friendStatus(FriendStatus.PENDING).build());

        //when
        friendService.acceptFriendRequest(accepter.getUserId(), friend.getId());

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
        friendService.declineFriendRequest(accepter.getUserId(), friend.getId());

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
        friendService.deleteFriend(requester.getUserId(), friend.getId());

        //then
        Optional<Friend> deletedFriend = friendRepository.findById(friend.getId());
        assertThat(deletedFriend).isEmpty();
    }

    @Test
    @DisplayName("친구 요청 개수를 초과한 경우 예외 발생")
    void exceedFriendRequestLimitTest() {
        // given
        FriendNameRequest request = new FriendNameRequest(accepter.getUserId());

        for (int i = 0; i < 20; i++) {
            Member accepterMember = Member.builder()
                    .userId("accepter" + i)
                    .password("password")
                    .salt("salt")
                    .imageUrl("imageUrl")
                    .build();

            memberRepository.save(accepterMember);

            Friend friendRequest = Friend.builder()
                    .requester(requester)
                    .accepter(accepterMember)
                    .friendStatus(FriendStatus.PENDING)
                    .build();

            friendRepository.save(friendRequest);
        }

        // when & then
        assertThatThrownBy(() -> friendService.makeFriendRequest(requester.getUserId(), request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("친구 요청 개수를 초과하였습니다. (최대 20개)");
    }

    @Test
    @DisplayName("상대방이 더 이상 친구 요청을 받을 수 없을 경우 예외 발생")
    void exceedAccepterFriendRequestLimitTest() {
        // given
        FriendNameRequest request = new FriendNameRequest(accepter.getUserId());

        for (int i = 0; i < 20; i++) {
            Member requesterMember = Member.builder()
                    .userId("requester" + i)
                    .password("password")
                    .salt("salt")
                    .imageUrl("imageUrl")
                    .build();

            memberRepository.save(requesterMember);

            Friend friendRequest = Friend.builder()
                    .requester(requesterMember)
                    .accepter(accepter)
                    .friendStatus(FriendStatus.PENDING)
                    .build();

            friendRepository.save(friendRequest);
        }

        // when & then
        assertThatThrownBy(() -> friendService.makeFriendRequest(requester.getUserId(), request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("상대방이 더 이상 친구 요청을 받을 수 없습니다. (최대 20개)");
    }
}
