package com.gdg.Todak.point.config;

import com.gdg.Todak.common.lock.LockWithMemberFactory;
import com.gdg.Todak.common.lock.RedisLock;
import com.gdg.Todak.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@RequiredArgsConstructor
public class TestRedisLockWithMemberFactory implements LockWithMemberFactory {

    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;
    private final RedissonClient redissonClient;

    @Override
    public Lock tryLock(Member member, String lockKey, long waitTime, long leaseTime) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(waitTime, leaseTime, TIME_UNIT)) {
                throw new RuntimeException("테스트 락 획득 실패: " + lockKey);
            }
            return new RedisLock(lock);
        } catch (Exception e) {
            throw new RuntimeException("테스트 락 예외: " + e.getMessage());
        }
    }

    @Override
    public void unlock(Member member, Lock lock) {
        try {
            if (lock instanceof RedisLock redisLock && redisLock.isHeldByCurrentThread()) {
                redisLock.unlock();
            }
        } catch (Exception ignored) {
        }
    }
}
