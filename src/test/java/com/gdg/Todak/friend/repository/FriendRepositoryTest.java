package com.gdg.Todak.friend.repository;

import com.gdg.Todak.friend.FriendStatus;
import com.gdg.Todak.friend.entity.Friend;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FriendRepositoryTest {

    @Autowired
    FriendRepository friendRepository;

    @Autowired
    MemberRepository memberRepository;

    @DisplayName("friend 객체 정상 저장 테스트")
    @Test
    void friendSaveTest() {
        // given
        Member requester = new Member("user1", "test1", "test1", "test1", "test1");
        Member accepter = new Member("user2", "test2", "test2", "test2", "test2");

        memberRepository.save(requester);
        memberRepository.save(accepter);

        Friend newFriend = Friend.builder()
                .requester(requester)
                .accepter(accepter)
                .friendStatus(FriendStatus.PENDING)
                .build();

        // when
        Friend savedFriend = friendRepository.save(newFriend);

        // then
        assertThat(savedFriend.getId()).isNotNull();
        assertThat(savedFriend.getRequester().getId()).isNotNull();
        assertThat(savedFriend.getAccepter().getId()).isNotNull();
        assertThat(savedFriend.getFriendStatus()).isEqualTo(FriendStatus.PENDING);
    }

    @DisplayName("existsByRequesterAndAccepter() 테스트 - 친구 관계 존재 여부 확인")
    @Test
    void existsByRequesterAndAccepterTest() {
        // given
        Member requester = memberRepository.save(new Member("user1", "test1", "test1", "test1", "test1"));
        Member accepter = memberRepository.save(new Member("user2", "test2", "test2", "test2", "test2"));

        friendRepository.save(Friend.builder()
                .requester(requester)
                .accepter(accepter)
                .friendStatus(FriendStatus.ACCEPTED)
                .build());

        // when
        boolean exists = friendRepository.existsByRequesterAndAccepter(requester, accepter);

        // then
        assertThat(exists).isTrue();
    }

    @DisplayName("findAllByAccepterUserIdAndFriendStatusOrRequesterUserIdAndFriendStatus() 테스트 - 친구 찾기 정상 작동 확인")
    @Test
    void findAllByAccepterOrRequesterAndStatusTest() {
        // given
        Member requester = memberRepository.save(new Member("user1", "test1", "test1", "test1", "test1"));
        Member accepter = memberRepository.save(new Member("user2", "test2", "test2", "test2", "test2"));
        Member other = memberRepository.save(new Member("user3", "test3", "test3", "test3", "test3"));

        friendRepository.save(Friend.builder()
                .requester(requester)
                .accepter(accepter)
                .friendStatus(FriendStatus.ACCEPTED)
                .build());

        friendRepository.save(Friend.builder()
                .requester(accepter)
                .accepter(other)
                .friendStatus(FriendStatus.ACCEPTED)
                .build());

        // when
        List<Friend> friends = friendRepository.findAllByAccepterUserIdAndFriendStatusOrRequesterUserIdAndFriendStatus(
                "user2", FriendStatus.ACCEPTED, "user2", FriendStatus.ACCEPTED
        );

        // then
        assertThat(friends).hasSize(2);
    }

    @DisplayName("countByRequesterAndStatusIn() 테스트 - requester 기준으로 특정 상태의 친구요청 개수 확인")
    @Test
    void countByRequesterAndStatusInTest() {
        // given
        Member requester = memberRepository.save(new Member("user1", "test1", "test1", "test1", "test1"));
        Member accepter1 = memberRepository.save(new Member("user2", "test2", "test2", "test2", "test2"));
        Member accepter2 = memberRepository.save(new Member("user3", "test3", "test3", "test3", "test3"));

        friendRepository.save(Friend.builder()
                .requester(requester)
                .accepter(accepter1)
                .friendStatus(FriendStatus.PENDING)
                .build());

        friendRepository.save(Friend.builder()
                .requester(requester)
                .accepter(accepter2)
                .friendStatus(FriendStatus.ACCEPTED)
                .build());

        // when
        long count = friendRepository.countByRequesterAndStatusIn(requester, List.of(FriendStatus.PENDING, FriendStatus.ACCEPTED));

        // then
        assertThat(count).isEqualTo(2);
    }

    @DisplayName("countByAccepterAndStatusIn() 테스트 - accepter 기준으로 특정 상태의 친구요청 개수 확인")
    @Test
    void countByAccepterAndStatusInTest() {
        // given
        Member requester1 = memberRepository.save(new Member("user1", "test1", "test1", "test1", "test1"));
        Member requester2 = memberRepository.save(new Member("user2", "test2", "test2", "test2", "test2"));
        Member accepter = memberRepository.save(new Member("user3", "test3", "test3", "test3", "test3"));

        friendRepository.save(Friend.builder()
                .requester(requester1)
                .accepter(accepter)
                .friendStatus(FriendStatus.PENDING)
                .build());

        friendRepository.save(Friend.builder()
                .requester(requester2)
                .accepter(accepter)
                .friendStatus(FriendStatus.ACCEPTED)
                .build());

        // when
        long count = friendRepository.countByAccepterAndStatusIn(accepter, List.of(FriendStatus.PENDING, FriendStatus.ACCEPTED));

        // then
        assertThat(count).isEqualTo(2);
    }

    @DisplayName("findAllByAccepterUserIdAndFriendStatusIn() 테스트 - accepter 기준으로 특정 상태의 친구요청 조회")
    @Test
    void findAllByAccepterUserIdAndFriendStatusInTest() {
        // given
        Member requester1 = memberRepository.save(new Member("user1", "test1", "test1", "test1", "test1"));
        Member requester2 = memberRepository.save(new Member("user2", "test2", "test2", "test2", "test2"));
        Member accepter = memberRepository.save(new Member("user3", "test3", "test3", "test3", "test3"));

        Friend pendingFriend = friendRepository.save(Friend.builder()
                .requester(requester1)
                .accepter(accepter)
                .friendStatus(FriendStatus.PENDING)
                .build());

        Friend declinedFriend = friendRepository.save(Friend.builder()
                .requester(requester2)
                .accepter(accepter)
                .friendStatus(FriendStatus.DECLINED)
                .build());

        // 다른 상태의 친구 요청도 추가
        Member otherAccepter = memberRepository.save(new Member("user4", "test4", "test4", "test4", "test4"));
        friendRepository.save(Friend.builder()
                .requester(requester1)
                .accepter(otherAccepter)
                .friendStatus(FriendStatus.ACCEPTED)
                .build());

        // when
        List<Friend> friends = friendRepository.findAllByAccepterUserIdAndFriendStatusIn("user3",
                List.of(FriendStatus.PENDING, FriendStatus.DECLINED));

        // then
        assertThat(friends).hasSize(2);
        assertThat(friends.stream().map(Friend::getFriendStatus))
                .containsExactlyInAnyOrder(FriendStatus.PENDING, FriendStatus.DECLINED);
    }

    @DisplayName("findAllByRequesterUserIdAndFriendStatusIn() 테스트 - requester 기준으로 특정 상태의 친구요청 조회")
    @Test
    void findAllByRequesterUserIdAndFriendStatusInTest() {
        // given
        Member requester = memberRepository.save(new Member("user1", "test1", "test1", "test1", "test1"));
        Member accepter1 = memberRepository.save(new Member("user2", "test2", "test2", "test2", "test2"));
        Member accepter2 = memberRepository.save(new Member("user3", "test3", "test3", "test3", "test3"));

        Friend pendingFriend = friendRepository.save(Friend.builder()
                .requester(requester)
                .accepter(accepter1)
                .friendStatus(FriendStatus.PENDING)
                .build());

        Friend declinedFriend = friendRepository.save(Friend.builder()
                .requester(requester)
                .accepter(accepter2)
                .friendStatus(FriendStatus.DECLINED)
                .build());

        // 다른 상태의 친구 요청도 추가
        Member otherRequester = memberRepository.save(new Member("user4", "test4", "test4", "test4", "test4"));
        friendRepository.save(Friend.builder()
                .requester(otherRequester)
                .accepter(accepter1)
                .friendStatus(FriendStatus.ACCEPTED)
                .build());

        // when
        List<Friend> friends = friendRepository.findAllByRequesterUserIdAndFriendStatusIn("user1",
                List.of(FriendStatus.PENDING, FriendStatus.DECLINED));

        // then
        assertThat(friends).hasSize(2);
        assertThat(friends.stream().map(Friend::getFriendStatus))
                .containsExactlyInAnyOrder(FriendStatus.PENDING, FriendStatus.DECLINED);
    }

    @DisplayName("findByRequesterAndAccepter() 테스트 - 특정 requester와 accepter 사이의 친구 관계 조회")
    @Test
    void findByRequesterAndAccepterTest() {
        // given
        Member requester = memberRepository.save(new Member("user1", "test1", "test1", "test1", "test1"));
        Member accepter = memberRepository.save(new Member("user2", "test2", "test2", "test2", "test2"));

        Friend friend = friendRepository.save(Friend.builder()
                .requester(requester)
                .accepter(accepter)
                .friendStatus(FriendStatus.PENDING)
                .build());

        // when
        Optional<Friend> foundFriend = friendRepository.findByRequesterAndAccepter(requester, accepter);

        // then
        assertThat(foundFriend).isPresent();
        assertThat(foundFriend.get().getRequester().getUserId()).isEqualTo("user1");
        assertThat(foundFriend.get().getAccepter().getUserId()).isEqualTo("user2");
        assertThat(foundFriend.get().getFriendStatus()).isEqualTo(FriendStatus.PENDING);
    }
}
