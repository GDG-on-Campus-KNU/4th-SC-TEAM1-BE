package com.gdg.Todak.point.service;

import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.point.entity.Point;
import com.gdg.Todak.point.exception.ConflictException;
import com.gdg.Todak.point.repository.PointLogRepository;
import com.gdg.Todak.point.repository.PointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class PointServiceLockTest {

    private PointRepository pointRepository;
    private PointLogRepository pointLogRepository;
    private RedissonClient redissonClient;
    private RLock rLock;
    private PointLogService pointLogService;
    private PointService pointService;

    @BeforeEach
    void setUp() {
        pointRepository = mock(PointRepository.class);
        pointLogRepository = mock(PointLogRepository.class);
        redissonClient = mock(RedissonClient.class);
        rLock = mock(RLock.class);
        pointLogService = mock(PointLogService.class);

        pointService = new PointService(
                pointRepository,
                null,
                pointLogRepository,
                pointLogService,
                redissonClient
        );
    }

    @Test
    @DisplayName("락 정상 획득 테스트")
    void acquireLockSuccessfullyTest() throws InterruptedException {
        // given
        Member member = Member.builder().userId("test").salt("test").password("test").nickname("test").build();
        Point point = Point.builder().member(member).build();

        when(redissonClient.getLock(any())).thenReturn(rLock);
        when(rLock.tryLock(5, 2, TimeUnit.SECONDS)).thenReturn(true);
        when(pointRepository.findByMember(member)).thenReturn(Optional.of(point));
        when(pointLogRepository.existsByCreatedAtBetweenAndMemberAndPointTypeIn(any(), any(), eq(member), any())).thenReturn(false);
        given(rLock.isHeldByCurrentThread()).willReturn(true);

        // when
        pointService.earnAttendancePointPerDay(member);

        // then
        verify(rLock, times(1)).unlock();
        verify(pointLogService, times(1)).createPointLog(any());
    }

    @Test
    @DisplayName("락 획득 실패 시 예외 발생 테스트")
    void throwExceptionWhenLockFailsTest() throws InterruptedException {
        // given
        Member member = Member.builder().userId("test").salt("test").password("test").nickname("test").build();

        when(redissonClient.getLock(any())).thenReturn(rLock);
        when(rLock.tryLock(5, 2, TimeUnit.SECONDS)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> pointService.earnAttendancePointPerDay(member))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("포인트 적립 락 획득 실패");
    }

    @Test
    @DisplayName("락 획득 중 예외 발생시 예외 발생 테스트")
    void throwConflictExceptionWhenInterruptedTest() throws InterruptedException {
        // given
        Member member = Member.builder().userId("test").salt("test").password("test").nickname("test").build();

        when(redissonClient.getLock(any())).thenReturn(rLock);
        when(rLock.tryLock(5, 2, TimeUnit.SECONDS)).thenThrow(new InterruptedException("interrupted"));

        // when & then
        assertThatThrownBy(() -> pointService.earnAttendancePointPerDay(member))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("포인트 적립 중 락 에러");
    }

    @Test
    @DisplayName("동시에 여러 요청이 들어와도 포인트는 한 번만 적립되는지 테스트")
    void earnPointOnlyOnceWhenMultipleRequestsComeTest() throws InterruptedException {
        // given
        Member member = Member.builder().userId("test").salt("test").password("test").nickname("test").build();
        Point point = Point.builder().member(member).build();

        when(pointRepository.findByMember(any())).thenReturn(Optional.of(point));
        when(pointLogRepository.existsByCreatedAtBetweenAndMemberAndPointTypeIn(any(), any(), eq(member), any()))
                .thenReturn(false);
        when(redissonClient.getLock(any())).thenReturn(rLock);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        when(rLock.tryLock(anyLong(), anyLong(), any()))
                .thenAnswer(new Answer<Boolean>() {
                    private boolean locked = false;

                    @Override
                    public synchronized Boolean answer(InvocationOnMock invocation) {
                        if (!locked) {
                            locked = true;
                            return true;
                        }
                        return false;
                    }
                });

        doNothing().when(pointLogService).createPointLog(any());

        int threadCount = 5;
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                try {
                    pointService.earnAttendancePointPerDay(member);
                } catch (Exception ignored) {
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // then
        verify(pointLogService, times(1)).createPointLog(any());
    }

    @DisplayName("여러 명의 유저가 동시에 요청해도 각각 한번만 포인트가 적립되는지 테스트")
    @Test
    void earnPointOncePerUserWhenMultipleUsersRequestConcurrentlyTest() throws InterruptedException {
        int userCount = 5;
        Thread[] threads = new Thread[userCount];

        Member[] members = new Member[userCount];
        Point[] points = new Point[userCount];

        for (int i = 0; i < userCount; i++) {
            Member member = Member.builder()
                    .userId("user" + i)
                    .salt("salt")
                    .password("pw")
                    .nickname("nick" + i)
                    .build();
            Point point = Point.builder().member(member).build();

            members[i] = member;
            points[i] = point;

            when(pointRepository.findByMember(eq(member))).thenReturn(Optional.of(point));
            when(pointLogRepository.existsByCreatedAtBetweenAndMemberAndPointTypeIn(any(), any(), eq(member), any()))
                    .thenReturn(false);

            RLock mockLock = mock(RLock.class);
            when(redissonClient.getLock(any())).thenReturn(mockLock);
            when(mockLock.tryLock(anyLong(), anyLong(), any())).thenReturn(true);
            when(mockLock.isHeldByCurrentThread()).thenReturn(true);
        }

        doNothing().when(pointLogService).createPointLog(any());

        for (int i = 0; i < userCount; i++) {
            int finalI = i;
            threads[i] = new Thread(() -> {
                try {
                    pointService.earnAttendancePointPerDay(members[finalI]);
                } catch (Exception ignored) {
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // then
        verify(pointLogService, times(userCount)).createPointLog(any());
    }

}
