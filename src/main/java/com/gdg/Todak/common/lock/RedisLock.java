package com.gdg.Todak.common.lock;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

@RequiredArgsConstructor
public class RedisLock implements Lock {
    private final RLock redisLock;

    public boolean isHeldByCurrentThread() {
        return redisLock.isHeldByCurrentThread();
    }

    @Override
    public void lock() {
        redisLock.lock();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        redisLock.lockInterruptibly();
    }

    @Override
    public boolean tryLock() {
        return redisLock.tryLock();
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return redisLock.tryLock(time, unit);
    }

    @Override
    public void unlock() {
        redisLock.unlock();
    }

    @Override
    public Condition newCondition() {
        return redisLock.newCondition();
    }
}
