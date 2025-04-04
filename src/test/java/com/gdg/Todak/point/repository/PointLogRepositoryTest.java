package com.gdg.Todak.point.repository;

import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.repository.MemberRepository;
import com.gdg.Todak.point.PointStatus;
import com.gdg.Todak.point.PointType;
import com.gdg.Todak.point.entity.PointLog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PointLogRepositoryTest {

    @Autowired
    PointLogRepository pointLogRepository;

    @Autowired
    MemberRepository memberRepository;

    @DisplayName("PointLog 저장 테스트")
    @Test
    void pointLogSaveTest() {
        // given
        Member member = memberRepository.save(new Member("user1", "email1", "pw", "010-1234-5678", "nickname"));

        PointLog log = PointLog.builder()
                .member(member)
                .point(100)
                .pointType(PointType.DIARY)
                .pointStatus(PointStatus.EARNED)
                .build();

        // when
        PointLog savedLog = pointLogRepository.save(log);

        // then
        assertThat(savedLog.getId()).isNotNull();
        assertThat(savedLog.getPoint()).isEqualTo(100);
        assertThat(savedLog.getMember()).isEqualTo(member);
    }

    @DisplayName("findAllByMember 테스트 - 페이징으로 포인트 로그 조회")
    @Test
    void findAllByMemberTest() {
        // given
        Member member = memberRepository.save(new Member("user2", "email2", "pw", "010-0000-0000", "nickname"));
        for (int i = 0; i < 5; i++) {
            PointLog log = PointLog.builder()
                    .member(member)
                    .point(10 * i)
                    .pointType(PointType.DIARY)
                    .pointStatus(PointStatus.EARNED)
                    .build();
            pointLogRepository.save(log);
        }

        // when
        Page<PointLog> page = pointLogRepository.findAllByMember(member, PageRequest.of(0, 3));

        // then
        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
    }

    @DisplayName("existsByCreatedAtBetweenAndMemberAndPointTypeIn 테스트")
    @Test
    void existsByCreatedAtBetweenAndMemberAndPointTypeInTest() {
        // given
        Member member = memberRepository.save(new Member("user3", "email3", "pw", "010-0000-0000", "nickname"));
        PointLog log = PointLog.builder()
                .member(member)
                .point(30)
                .pointType(PointType.DIARY)
                .pointStatus(PointStatus.EARNED)
                .build();

        Instant now = Instant.now();
        ReflectionTestUtils.setField(log, "createdAt", now);
        pointLogRepository.save(log);

        Instant start = now.minusSeconds(60);
        Instant end = now.plusSeconds(60);
        List<PointType> types = List.of(PointType.DIARY);

        // when
        boolean exists = pointLogRepository.existsByCreatedAtBetweenAndMemberAndPointTypeIn(start, end, member, types);

        // then
        assertThat(exists).isTrue();
    }

    @DisplayName("existsByCreatedAtBetweenAndMemberAndPointTypeIn - 존재하지 않을 경우 false 반환")
    @Test
    void existsByCreatedAtBetweenAndMemberAndPointTypeIn_NotFoundTest() {
        // given
        Member member = memberRepository.save(new Member("user4", "email4", "pw", "010-0000-0000", "nickname"));

        Instant start = Instant.now().minusSeconds(60);
        Instant end = Instant.now().plusSeconds(60);
        List<PointType> types = List.of(PointType.DIARY);

        // when
        boolean exists = pointLogRepository.existsByCreatedAtBetweenAndMemberAndPointTypeIn(start, end, member, types);

        // then
        assertThat(exists).isFalse();
    }
}
