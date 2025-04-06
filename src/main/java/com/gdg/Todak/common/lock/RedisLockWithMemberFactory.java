package com.gdg.Todak.common.lock;

import com.gdg.Todak.common.lock.exception.LockException;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.point.service.PointLogService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@Component
@RequiredArgsConstructor
public class RedisLockWithMemberFactory implements LockWithMemberFactory {
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    private final RedissonClient redissonClient;
    private final PointLogService pointLogService;

    @Override
    public Lock tryLock(Member member, String lockKey, long waitTime, long leaseTime) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(waitTime, leaseTime, TIME_UNIT)) {
                throw new LockException("획득 실패한 락: " + lockKey);
            }
            return new RedisLock(lock);
        } catch (Exception e) {
            pointLogService.saveLockErrorLogToServer(member, "[Redis lock 획득 에러] Redis lock 획득 실패, errorMessage: " + e.getMessage());
            throw new LockException("획득 실패한 락: " + lockKey);
        }
    }

    @Override
    public void unlock(Member member, Lock lock) {
        try {
            if (lock instanceof RedisLock redisLock) {
                if (redisLock.isHeldByCurrentThread()) {
                    redisLock.unlock();
                } else {
                    pointLogService.saveLockErrorLogToServer(member, "[Redis lock 해제 에러] 현재 스레드는 락을 보유하고 있지 않습니다.");
                }
            } else {
                pointLogService.saveLockErrorLogToServer(member, "[Redis lock 해제 에러] 잘못된 락 객체 전달: " + lock.getClass().getName());
            }
        } catch (Exception e) {
            pointLogService.saveLockErrorLogToServer(member, "[Redis lock 해제 에러] 해제 실패: " + lock.getClass().getName());
        }
    }
}
