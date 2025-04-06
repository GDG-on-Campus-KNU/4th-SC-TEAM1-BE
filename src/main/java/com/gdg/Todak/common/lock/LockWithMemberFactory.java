package com.gdg.Todak.common.lock;

import com.gdg.Todak.member.domain.Member;

import java.util.concurrent.locks.Lock;

public interface LockWithMemberFactory {

    Lock tryLock(Member member, String lockKey, long waitTime, long leaseTIme);

    void unlock(Member member, Lock lock);
}
