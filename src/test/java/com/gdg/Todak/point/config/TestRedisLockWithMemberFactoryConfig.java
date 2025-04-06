package com.gdg.Todak.point.config;

import com.gdg.Todak.common.lock.LockWithMemberFactory;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
@RequiredArgsConstructor
public class TestRedisLockWithMemberFactoryConfig {

    private final RedissonClient redissonClient;

    @Bean
    public LockWithMemberFactory lockWithMemberFactory() {
        return new TestRedisLockWithMemberFactory(redissonClient);
    }
}
