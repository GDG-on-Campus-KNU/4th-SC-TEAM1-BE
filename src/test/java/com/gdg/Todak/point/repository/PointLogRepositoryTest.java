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
import java.time.LocalDate;
import java.time.ZoneId;
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

    @DisplayName("member의 userId에 해당하는 PointLog를 반환한다.")
    @Test
    void findAllByMember_UserIdTest() {
        // given
        String userId = "user1";
        Member member1 = memberRepository.save(new Member(userId, "email4", "pw", "010-0000-0000", "nickname"));
        Member member2 = memberRepository.save(new Member("user2", "email4", "pw", "010-0000-0000", "nickname"));

        PointLog log1 = PointLog.builder()
                .member(member1)
                .point(30)
                .pointType(PointType.DIARY)
                .pointStatus(PointStatus.EARNED)
                .build();
        PointLog log2 = PointLog.builder()
                .member(member2)
                .point(30)
                .pointType(PointType.DIARY)
                .pointStatus(PointStatus.EARNED)
                .build();

        pointLogRepository.saveAll(List.of(log1, log2));

        // when
        List<PointLog> findPointLogs = pointLogRepository.findAllByMember_UserId(userId);

        // then
        assertThat(findPointLogs).hasSize(1);
    }

    @DisplayName("pointType에 해당하는 PointLog를 반환한다.")
    @Test
    void findAllByPointTypeTest() {
        // given
        PointType pointType = PointType.ATTENDANCE_DAY_2;
        Member member = memberRepository.save(new Member("user1", "email4", "pw", "010-0000-0000", "nickname"));

        PointLog log1 = PointLog.builder()
                .member(member)
                .point(30)
                .pointType(pointType)
                .pointStatus(PointStatus.EARNED)
                .build();
        PointLog log2 = PointLog.builder()
                .member(member)
                .point(30)
                .pointType(PointType.ATTENDANCE_DAY_1)
                .pointStatus(PointStatus.EARNED)
                .build();

        Instant now = Instant.now();
        ReflectionTestUtils.setField(log1, "createdAt", now);
        ReflectionTestUtils.setField(log2, "createdAt", now);
        pointLogRepository.saveAll(List.of(log1, log2));

        // when
        List<PointLog> findPointLogs = pointLogRepository.findAllByPointType(pointType);

        // then
        assertThat(findPointLogs).hasSize(1);
    }

    @DisplayName("pointStatus에 해당하는 PointLog를 반환한다.")
    @Test
    void findAllByPointStatusTest() {
        // given
        PointStatus pointStatus = PointStatus.CONSUMED;
        Member member = memberRepository.save(new Member("user1", "email4", "pw", "010-0000-0000", "nickname"));

        PointLog log1 = PointLog.builder()
                .member(member)
                .point(30)
                .pointType(PointType.DIARY)
                .pointStatus(pointStatus)
                .build();
        PointLog log2 = PointLog.builder()
                .member(member)
                .point(30)
                .pointType(PointType.ATTENDANCE_DAY_1)
                .pointStatus(PointStatus.EARNED)
                .build();

        pointLogRepository.saveAll(List.of(log1, log2));

        // when
        List<PointLog> findPointLogs = pointLogRepository.findAllByPointStatus(pointStatus);

        // then
        assertThat(findPointLogs).hasSize(1);
    }

    @DisplayName("해당 날짜에 해당하는 PointLog를 반환한다.")
    @Test
    void findAllByCreatedAtBetweenTest() {
        // given
        Member member1 = memberRepository.save(new Member("user1", "email4", "pw", "010-0000-0000", "nickname"));
        Member member2 = memberRepository.save(new Member("user2", "email4", "pw", "010-0000-0000", "nickname"));

        PointLog log1 = PointLog.builder()
                .member(member1)
                .point(30)
                .pointType(PointType.DIARY)
                .pointStatus(PointStatus.EARNED)
                .build();
        PointLog log2 = PointLog.builder()
                .member(member2)
                .point(30)
                .pointType(PointType.DIARY)
                .pointStatus(PointStatus.EARNED)
                .build();

        LocalDate localDate = LocalDate.of(2020, 1, 1);
        ReflectionTestUtils.setField(log1, "createdAt", localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        ReflectionTestUtils.setField(log2, "createdAt", LocalDate.of(2010, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        pointLogRepository.saveAll(List.of(log1, log2));

        // when
        Instant start = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = localDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        List<PointLog> findPointLogs = pointLogRepository.findAllByCreatedAtBetween(start, end);

        // then
        assertThat(findPointLogs).hasSize(1);
    }
}
