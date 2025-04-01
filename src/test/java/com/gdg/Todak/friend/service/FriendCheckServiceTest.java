package com.gdg.Todak.friend.service;

import com.gdg.Todak.friend.FriendStatus;
import com.gdg.Todak.friend.entity.Friend;
import com.gdg.Todak.friend.repository.FriendRepository;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
class FriendCheckServiceTest {

    @Autowired
    private FriendCheckService friendCheckService;

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member requester;
    private Member accepter;

    @BeforeEach
    void setUp() {
        requester = memberRepository.save(new Member("requesterUser", "test1", "test1", "test1", "test1"));
        accepter = memberRepository.save(new Member("accepterUser", "test2", "test2", "test2", "test2"));
    }

    @Test
    @DisplayName("친구 조회 테스트")
    void getFriendMembersTest() {
        //given
        friendRepository.save(Friend.builder().requester(requester).accepter(accepter).friendStatus(FriendStatus.ACCEPTED).build());

        //when
        List<Member> friendFindByRequester = friendCheckService.getFriendMembers(requester.getUserId());
        List<Member> friendFindByAccepter = friendCheckService.getFriendMembers(accepter.getUserId());

        //then
        assertThat(friendFindByRequester).isNotNull();
        assertThat(friendFindByRequester.size()).isEqualTo(1);
        assertThat(friendFindByRequester.getFirst().getUserId()).isEqualTo("accepterUser");

        assertThat(friendFindByAccepter).isNotNull();
        assertThat(friendFindByAccepter.size()).isEqualTo(1);
        assertThat(friendFindByAccepter.getFirst().getUserId()).isEqualTo("requesterUser");


    }
}
