package com.gdg.Todak.friend.entity;

import com.gdg.Todak.friend.FriendStatus;
import com.gdg.Todak.friend.exception.BadRequestException;
import com.gdg.Todak.member.domain.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FriendTest {

    private Friend friend;
    private Member requester;
    private Member accepter;

    @BeforeEach
    void setUp() {
        requester = new Member("user1", "test1", "test1","test1", "test1");
        accepter = new Member("user2", "test2", "test2","test2", "test2");

        friend = Friend.builder().requester(requester).accepter(accepter).friendStatus(FriendStatus.PENDING).build();
    }

    @DisplayName("Friend 객체 생성 테스트")
    @Test
    void constructorTest() {
        assertThat(friend).isNotNull();
        assertThat(friend.getRequester()).isEqualTo(requester);
        assertThat(friend.getAccepter()).isEqualTo(accepter);
        assertThat(friend.getFriendStatus()).isEqualTo(FriendStatus.PENDING);
    }

    @DisplayName("친구 요청을 수락하면 상태가 ACCEPTED로 변경되어야 한다")
    @Test
    void acceptFriendRequestTest() {
        // when
        friend.acceptFriendRequest();

        // then
        assertThat(friend.getFriendStatus()).isEqualTo(FriendStatus.ACCEPTED);
    }

    @DisplayName("이미 ACCEPTED 상태인 친구 요청을 다시 수락하면 예외 발생")
    @Test
    void acceptFriendRequest_alreadyAcceptedTest() {
        // given
        friend.acceptFriendRequest();

        // when & then
        assertThatThrownBy(() -> friend.acceptFriendRequest())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("대기중인 친구 요청이 아닙니다.");
    }

    @DisplayName("친구 요청을 거절하면 상태가 DECLINED로 변경되어야 한다")
    @Test
    void declinedFriendRequestTest() {
        // when
        friend.declinedFriendRequest();

        // then
        assertThat(friend.getFriendStatus()).isEqualTo(FriendStatus.DECLINED);
    }

    @DisplayName("이미 DECLINED 상태인 친구 요청을 다시 거절하면 예외 발생")
    @Test
    void declinedFriendRequest_alreadyDeclinedTest() {
        // given
        friend.declinedFriendRequest();

        // when & then
        assertThatThrownBy(() -> friend.declinedFriendRequest())
                .isInstanceOf(BadRequestException.class)
                .hasMessage("대기중인 친구 요청이 아닙니다.");
    }

    @DisplayName("요청자가 아닌 사용자가 checkMemberIsNotRequester 호출 시 true 반환")
    @Test
    void checkMemberIsNotRequesterTest() {
        Member otherUser = new Member("user3", "test3", "test3","test3", "test3");

        assertThat(friend.checkMemberIsNotRequester(otherUser)).isTrue();
        assertThat(friend.checkMemberIsNotRequester(requester)).isFalse();
    }

    @DisplayName("수락자가 아닌 사용자가 checkMemberIsNotAccepter 호출 시 true 반환")
    @Test
    void checkMemberIsNotAccepterTest() {
        Member otherUser = new Member("user3", "test3", "test3","test3", "test3");

        assertThat(friend.checkMemberIsNotAccepter(otherUser)).isTrue();
        assertThat(friend.checkMemberIsNotAccepter(accepter)).isFalse();
    }
}
