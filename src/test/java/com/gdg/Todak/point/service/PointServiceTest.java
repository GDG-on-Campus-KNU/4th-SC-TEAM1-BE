package com.gdg.Todak.point.service;

import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.repository.MemberRepository;
import com.gdg.Todak.point.PointStatus;
import com.gdg.Todak.point.PointType;
import com.gdg.Todak.point.dto.PointRequest;
import com.gdg.Todak.point.entity.Point;
import com.gdg.Todak.point.exception.BadRequestException;
import com.gdg.Todak.point.exception.ConflictException;
import com.gdg.Todak.point.exception.NotFoundException;
import com.gdg.Todak.point.repository.PointLogRepository;
import com.gdg.Todak.point.repository.PointRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class PointServiceTest {
    private final static int ATTENDANCE_BASE_POINT = 10;
    private final static int ATTENDANCE_BONUS_2_DAYS = 15;
    private final static int ATTENDANCE_BONUS_3_DAYS = 20;
    private final static int DIARY_WRITE_POINT = 15;
    private final static int COMMENT_WRITE_POINT = 10;

    @Autowired
    private PointService pointService;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PointLogRepository pointLogRepository;

    @Autowired
    private EntityManager entityManager;

    private Member member;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(new Member("testUser", "test", "test", "test", "test"));
    }

    @Test
    @DisplayName("포인트 객체 생성 성공")
    void createPointSuccessfullyTest() {
        // when
        pointService.createPoint(member);

        // then
        assertThat(pointRepository.existsByMember(member)).isTrue();
    }

    @Test
    @DisplayName("포인트 객체 중복 생성 시 예외 발생")
    void createPointFailedByDuplicatedTest() {
        // given
        pointService.createPoint(member);

        // when & then
        assertThatThrownBy(() -> pointService.createPoint(member))
                .isInstanceOf(ConflictException.class)
                .hasMessage("이미 해당 멤버의 point 객체가 존재합니다.");
    }

    @Test
    @DisplayName("포인트 조회 성공")
    void getPointSuccessfullyTest() {
        // given
        pointService.createPoint(member);

        // when
        int point = pointService.getPoint(member.getUserId()).point();

        // then
        assertThat(point).isEqualTo(0);
    }

    @Test
    @DisplayName("포인트 조회 실패 (해당 멤버의 포인트 객체가 없는 경우)")
    void getPointFailedByNotFoundTest() {
        // when & then
        assertThatThrownBy(() -> pointService.getPoint(member.getUserId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("member의 point 객체가 없습니다.");
    }

    @Test
    @DisplayName("출석 포인트 획득 성공 (ATTENDANCE_DAY_1)")
    void earnAttendancePointPerDaySuccessfullyTest() {
        // given
        pointService.createPoint(member);

        // when
        pointService.earnAttendancePointPerDay(member);

        // then
        Point point = pointRepository.findByMember(member).orElseThrow();
        assertThat(point.getPoint()).isEqualTo(ATTENDANCE_BASE_POINT);
        assertThat(pointLogRepository.existsByMemberAndPointType(member, PointType.ATTENDANCE_DAY_1)).isTrue();
    }

    @Test
    @DisplayName("출석 포인트 획득 성공 (ATTENDANCE_DAY_2)")
    void earnAttendancePointPerDaySuccessfullyTest2() {
        // given
        pointService.createPoint(member);

        entityManager.createNativeQuery(
                        "INSERT INTO point_log (member_id, point_type, point, point_status, created_at, updated_at) " +
                                "VALUES (?, ?, ?, ?, ?, ?)")
                .setParameter(1, member.getId())
                .setParameter(2, PointType.ATTENDANCE_DAY_1.name())
                .setParameter(3, ATTENDANCE_BASE_POINT)
                .setParameter(4, PointStatus.EARNED.name())
                .setParameter(5, Instant.now().minus(1, ChronoUnit.DAYS))
                .setParameter(6, Instant.now())
                .executeUpdate();

        entityManager.flush();
        entityManager.clear();

        // when
        pointService.earnAttendancePointPerDay(member);

        // then
        Point point = pointRepository.findByMember(member).orElseThrow();
        assertThat(point.getPoint()).isEqualTo(ATTENDANCE_BASE_POINT + ATTENDANCE_BONUS_2_DAYS);
        assertThat(pointLogRepository.existsByMemberAndPointType(member, PointType.ATTENDANCE_DAY_2)).isTrue();
    }

    @Test
    @DisplayName("출석 포인트 획득 성공 (ATTENDANCE_DAY_3)")
    void earnAttendancePointPerDaySuccessfullyTest3() {
        // given
        pointService.createPoint(member);
        entityManager.createNativeQuery(
                        "INSERT INTO point_log (member_id, point_type, point, point_status, created_at, updated_at) " +
                                "VALUES (?, ?, ?, ?, ?, ?)")
                .setParameter(1, member.getId())
                .setParameter(2, PointType.ATTENDANCE_DAY_1.name())
                .setParameter(3, ATTENDANCE_BASE_POINT)
                .setParameter(4, PointStatus.EARNED.name())
                .setParameter(5, Instant.now().minus(2, ChronoUnit.DAYS))
                .setParameter(6, Instant.now())
                .executeUpdate();

        entityManager.createNativeQuery(
                        "INSERT INTO point_log (member_id, point_type, point, point_status, created_at, updated_at) " +
                                "VALUES (?, ?, ?, ?, ?, ?)")
                .setParameter(1, member.getId())
                .setParameter(2, PointType.ATTENDANCE_DAY_2.name())
                .setParameter(3, ATTENDANCE_BASE_POINT + ATTENDANCE_BONUS_2_DAYS)
                .setParameter(4, PointStatus.EARNED.name())
                .setParameter(5, Instant.now().minus(1, ChronoUnit.DAYS))
                .setParameter(6, Instant.now())
                .executeUpdate();

        entityManager.flush();
        entityManager.clear();

        // when
        pointService.earnAttendancePointPerDay(member);

        // then
        Point point = pointRepository.findByMember(member).orElseThrow();
        assertThat(point.getPoint()).isEqualTo(ATTENDANCE_BASE_POINT + ATTENDANCE_BONUS_3_DAYS);
        assertThat(pointLogRepository.existsByMemberAndPointType(member, PointType.ATTENDANCE_DAY_3)).isTrue();
    }

    @Test
    @DisplayName("하루에 출석 포인트 중복 획득 불가")
    void earnAttendancePointPerDayFailedByDuplicatedTest() {
        // given
        pointService.createPoint(member);
        pointService.earnAttendancePointPerDay(member);

        // when
        pointService.earnAttendancePointPerDay(member);

        // then
        Point point = pointRepository.findByMember(member).orElseThrow();
        assertThat(point.getPoint()).isEqualTo(ATTENDANCE_BASE_POINT);
    }

    @Test
    @DisplayName("일기 작성 포인트 획득 성공")
    void earnDiaryPointSuccessfullyTest() {
        // given
        pointService.createPoint(member);
        PointRequest pointRequest = new PointRequest(member, PointType.DIARY);

        // when
        pointService.earnPointByType(pointRequest);

        // then
        Point point = pointRepository.findByMember(member).orElseThrow();
        assertThat(point.getPoint()).isEqualTo(DIARY_WRITE_POINT);
        assertThat(pointLogRepository.existsByMemberAndPointType(member, PointType.DIARY)).isTrue();
    }

    @Test
    @DisplayName("댓글 작성 포인트 획득 성공")
    void earnCommentPointSuccessfullyTest() {
        // given
        pointService.createPoint(member);
        PointRequest pointRequest = new PointRequest(member, PointType.COMMENT);

        // when
        pointService.earnPointByType(pointRequest);

        // then
        Point point = pointRepository.findByMember(member).orElseThrow();
        assertThat(point.getPoint()).isEqualTo(COMMENT_WRITE_POINT);
        assertThat(pointLogRepository.existsByMemberAndPointType(member, PointType.COMMENT)).isTrue();
    }

    @Test
    @DisplayName("잘못된 메서드로 포인트 획득 시도시 예외 발생")
    void earnPointByNotExistingPointTypeTest() {
        // given
        pointService.createPoint(member);
        PointRequest pointRequest = new PointRequest(member, PointType.ATTENDANCE_DAY_1);

        // when & then
        assertThatThrownBy(() -> pointService.earnPointByType(pointRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("해당하는 pointType이 없습니다");
    }

    @Test
    @DisplayName("하루에 같은 타입의 포인트 중복 획득 불가")
    void earnPointByTypeFailedByDuplicatedTest() {
        // given
        pointService.createPoint(member);
        PointRequest pointRequest = new PointRequest(member, PointType.DIARY);
        pointService.earnPointByType(pointRequest);

        // when
        pointService.earnPointByType(pointRequest);

        // then
        Point point = pointRepository.findByMember(member).orElseThrow();
        assertThat(point.getPoint()).isEqualTo(DIARY_WRITE_POINT);
    }
}
