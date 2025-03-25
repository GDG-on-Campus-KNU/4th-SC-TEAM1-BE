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
        Member requester = new Member("user1", "test1", "test1", "test1");
        Member accepter = new Member("user2", "test2", "test2", "test2");

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
        Member requester = memberRepository.save(new Member("user1", "test1", "test1", "test1"));
        Member accepter = memberRepository.save(new Member("user2", "test2", "test2", "test2"));

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

    @DisplayName("findAllByAccepterUsernameAndFriendStatusOrRequesterUsernameAndFriendStatus() 테스트 - 친구 찾기 정상 작동 확인")
    @Test
    void findAllByAccepterOrRequesterAndStatusTest() {
        // given
        Member requester = memberRepository.save(new Member("user1", "test1", "test1", "test1"));
        Member accepter = memberRepository.save(new Member("user2", "test2", "test2", "test2"));
        Member other = memberRepository.save(new Member("user3", "test3", "test3", "test3"));

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
        List<Friend> friends = friendRepository.findAllByAccepterUsernameAndFriendStatusOrRequesterUsernameAndFriendStatus(
                "user2", FriendStatus.ACCEPTED, "user2", FriendStatus.ACCEPTED
        );

        // then
        assertThat(friends).hasSize(2);
    }

    @DisplayName("countByRequesterAndStatusIn() 테스트 - requester 기준으로 특정 상태의 친구요청 개수 확인")
    @Test
    void countByRequesterAndStatusInTest() {
        // given
        Member requester = memberRepository.save(new Member("user1", "test1", "test1", "test1"));
        Member accepter1 = memberRepository.save(new Member("user2", "test2", "test2", "test2"));
        Member accepter2 = memberRepository.save(new Member("user3", "test3", "test3", "test3"));

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
        Member requester1 = memberRepository.save(new Member("user1", "test1", "test1", "test1"));
        Member requester2 = memberRepository.save(new Member("user2", "test2", "test2", "test2"));
        Member accepter = memberRepository.save(new Member("user3", "test3", "test3", "test3"));

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
}
