package com.gdg.Todak.point.service;

import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.repository.MemberRepository;
import com.gdg.Todak.point.PointStatus;
import com.gdg.Todak.point.PointType;
import com.gdg.Todak.point.dto.PointLogRequest;
import com.gdg.Todak.point.dto.PointRequest;
import com.gdg.Todak.point.dto.PointResponse;
import com.gdg.Todak.point.entity.Point;
import com.gdg.Todak.point.exception.BadRequestException;
import com.gdg.Todak.point.exception.ConflictException;
import com.gdg.Todak.point.exception.NotFoundException;
import com.gdg.Todak.point.repository.PointLogRepository;
import com.gdg.Todak.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PointService {
    private final static int ATTENDANCE_BASE_POINT = 10;
    private final static int ATTENDANCE_BONUS_2_DAYS = 15;
    private final static int ATTENDANCE_BONUS_3_DAYS = 20;
    private final static int ATTENDANCE_BONUS_4_DAYS = 25;
    private final static int ATTENDANCE_BONUS_5_DAYS = 30;
    private final static int DIARY_WRITE_POINT = 15;
    private final static int COMMENT_WRITE_POINT = 10;
    private final static List<PointType> ATTENDANCE_LISTS = Arrays.asList(
            PointType.ATTENDANCE_DAY_1,
            PointType.ATTENDANCE_DAY_2,
            PointType.ATTENDANCE_DAY_3,
            PointType.ATTENDANCE_DAY_4,
            PointType.ATTENDANCE_DAY_5_OR_MORE
    );

    private final PointRepository pointRepository;
    private final MemberRepository memberRepository;
    private final PointLogRepository pointLogRepository;
    private final PointLogService pointLogService;

    @Transactional
    public void createPoint(Member member) {
        if (pointRepository.existsByMember(member)) {
            throw new ConflictException("이미 해당 멤버의 point 객체가 존재합니다.");
        }

        Point point = Point.builder().member(member).build();
        pointRepository.save(point);
    }

    public PointResponse getPoint(String userId) {
        Member member = getMember(userId);
        Point point = getPoint(member);

        return new PointResponse(point.getPoint());
    }

    @Transactional
    public void earnAttendancePointPerDay(Member member) {
        Point point = getPoint(member);

        Instant today = Instant.now();

        if (!pointLogRepository.existsByCreatedAtAndMemberAndPointTypeIn(today, member, ATTENDANCE_LISTS)) {
            int consecutiveDays = calculateConsecutiveAttendanceDays(member);

            int totalPoints = ATTENDANCE_BASE_POINT + calculateBonusPoints(consecutiveDays);

            PointType attendanceType = getAttendanceType(consecutiveDays);

            pointLogService.createPointLog(new PointLogRequest(member, totalPoints, attendanceType, PointStatus.EARNED));

            point.earnPoint(totalPoints);
        }
    }

    private int calculateConsecutiveAttendanceDays(Member member) {
        ZoneId zone = ZoneId.systemDefault();
        Instant now = Instant.now().atZone(zone).truncatedTo(ChronoUnit.DAYS).toInstant();

        int consecutiveDays = 0;
        for (int i = 1; i <= 5; i++) {
            Instant startOfDay = now.minus(i, ChronoUnit.DAYS);
            Instant endOfDay = startOfDay.plus(1, ChronoUnit.DAYS).minusMillis(1);

            if (pointLogRepository.existsByCreatedAtBetweenAndMemberAndPointTypeIn(startOfDay, endOfDay, member, ATTENDANCE_LISTS)) {
                consecutiveDays++;
            } else {
                break;
            }
        }
        return consecutiveDays;
    }

    private int calculateBonusPoints(int consecutiveDays) {
        return switch (consecutiveDays) {
            case 2 -> ATTENDANCE_BONUS_2_DAYS;
            case 3 -> ATTENDANCE_BONUS_3_DAYS;
            case 4 -> ATTENDANCE_BONUS_4_DAYS;
            case 5 -> ATTENDANCE_BONUS_5_DAYS;
            default -> 0;
        };
    }

    private PointType getAttendanceType(int consecutiveDays) {
        return switch (consecutiveDays) {
            case 2 -> PointType.ATTENDANCE_DAY_2;
            case 3 -> PointType.ATTENDANCE_DAY_3;
            case 4 -> PointType.ATTENDANCE_DAY_4;
            case 5 -> PointType.ATTENDANCE_DAY_5_OR_MORE;
            default -> PointType.ATTENDANCE_DAY_1;
        };
    }

    @Transactional
    public void earnPointByType(PointRequest pointRequest) {
        Point point = getPoint(pointRequest.member());

        ZoneId zone = ZoneId.systemDefault();
        Instant now = Instant.now();
        Instant startOfDay = now.atZone(zone).truncatedTo(ChronoUnit.DAYS).toInstant();
        Instant endOfDay = startOfDay.plus(1, ChronoUnit.DAYS).minusMillis(1);

        int pointByType = getPointByType(pointRequest.pointType());

        if (!pointLogRepository.existsByCreatedAtBetweenAndMemberAndPointTypeIn(startOfDay, endOfDay, pointRequest.member(), List.of(pointRequest.pointType()))) {
            pointLogService.createPointLog(new PointLogRequest(pointRequest.member(), pointByType, pointRequest.pointType(), PointStatus.EARNED));

            point.earnPoint(pointByType);
        }
    }

    private int getPointByType(PointType pointType) {
        return switch (pointType) {
            case DIARY -> DIARY_WRITE_POINT;
            case COMMENT -> COMMENT_WRITE_POINT;
            default -> throw new BadRequestException("해당하는 pointType이 없습니다");
        };
    }

    private Point getPoint(Member member) {
        return pointRepository.findByMember(member)
                .orElseThrow(() -> new NotFoundException("member의 point 객체가 없습니다."));
    }

    private Member getMember(String userId) {
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("userId에 해당하는 멤버가 없습니다."));
    }
}
