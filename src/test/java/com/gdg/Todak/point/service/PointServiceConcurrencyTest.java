package com.gdg.Todak.point.service;

import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.repository.MemberRepository;
import com.gdg.Todak.point.PointType;
import com.gdg.Todak.point.config.TestRedisLockWithMemberFactoryConfig;
import com.gdg.Todak.point.dto.PointRequest;
import com.gdg.Todak.point.exception.FileException;
import com.gdg.Todak.tree.domain.GrowthButton;
import com.gdg.Todak.tree.domain.TreeConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import({TestRedisLockWithMemberFactoryConfig.class})
public class PointServiceConcurrencyTest {

    private static final int NUMBER_OF_THREADS = 5;
    private static final int NUMBER_OF_MEMBERS = 3;
    private final static int ATTENDANCE_BASE_POINT = 10;
    private final static int DIARY_WRITE_POINT = 15;
    @Autowired
    private PointService pointService;
    @Autowired
    private MemberRepository memberRepository;
    private List<Member> usedMembers = new ArrayList<>();

    @AfterEach
    void tearDown() {
        memberRepository.deleteAllInBatch(usedMembers);
        usedMembers.clear();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    @DisplayName("동시에 여러명의 사람이 출석포인트 요청을 해도 정상 처리 테스트")
    public void earnAttendancePoint_whenConcurrency5Members_getAttendancePointSuccessfully() throws InterruptedException {
        // given
        List<Member> members = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_MEMBERS; i++) {
            String uniqueId = "test_attendance_" + i + "_" + System.currentTimeMillis();
            Member member = memberRepository.save(Member.builder()
                    .salt("test")
                    .userId(uniqueId)
                    .nickname(uniqueId)
                    .imageUrl("test")
                    .password("test").build());
            pointService.createPoint(member);
            members.add(member);
            usedMembers.add(member);
        }

        List<Integer> initialPoints = new ArrayList<>();
        for (Member member : members) {
            initialPoints.add(pointService.getPoint(member.getUserId()).point());
        }

        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        CountDownLatch latch = new CountDownLatch(NUMBER_OF_THREADS * NUMBER_OF_MEMBERS);

        // when
        for (Member member : members) {
            for (int i = 0; i < NUMBER_OF_THREADS; i++) {
                final int memberIndex = members.indexOf(member);
                executorService.execute(() -> {
                    try {
                        pointService.earnAttendancePointPerDay(member);
                    } catch (Exception e) {
                        System.err.println("Error processing member " + memberIndex + ": " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }

        latch.await();
        executorService.shutdown();

        if (!executorService.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
            executorService.shutdownNow();
        }

        // then
        for (int i = 0; i < NUMBER_OF_MEMBERS; i++) {
            Member member = members.get(i);
            int finalPoint = pointService.getPoint(member.getUserId()).point();
            assertThat(finalPoint).isEqualTo(initialPoints.get(i) + ATTENDANCE_BASE_POINT);
        }
    }

    @Test
    @DisplayName("동시에 여러명의 사람이 특정 포인트 타입으로 포인트 요청을 해도 정상 처리 테스트")
    public void earnPointByType_whenConcurrency5Members_getPointByTypeSuccessfully() throws InterruptedException {
        // given
        List<Member> members = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_MEMBERS; i++) {
            String uniqueId = "test_earn_" + i + "_" + System.currentTimeMillis();
            Member member = memberRepository.save(Member.builder()
                    .salt("test")
                    .userId(uniqueId)
                    .nickname(uniqueId)
                    .imageUrl("test")
                    .password("test").build());
            pointService.createPoint(member);
            members.add(member);
            usedMembers.add(member);
        }

        List<Integer> initialPoints = new ArrayList<>();
        for (Member member : members) {
            initialPoints.add(pointService.getPoint(member.getUserId()).point());
        }

        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        CountDownLatch latch = new CountDownLatch(NUMBER_OF_THREADS * NUMBER_OF_MEMBERS);

        // when
        for (Member member : members) {
            for (int i = 0; i < NUMBER_OF_THREADS; i++) {
                executorService.execute(() -> {
                    try {
                        pointService.earnPointByType(new PointRequest(member, PointType.DIARY));
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }

        latch.await();
        executorService.shutdown();

        if (!executorService.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
            executorService.shutdownNow();
        }

        // then
        for (int i = 0; i < NUMBER_OF_MEMBERS; i++) {
            Member member = members.get(i);
            int finalPoint = pointService.getPoint(member.getUserId()).point();
            assertThat(finalPoint).isEqualTo(initialPoints.get(i) + DIARY_WRITE_POINT);
        }
    }
}
