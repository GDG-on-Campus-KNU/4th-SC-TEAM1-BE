package com.gdg.Todak.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class RedissonConfig {
    private static final Logger log = LoggerFactory.getLogger(RedissonConfig.class);

    @Value("${REDIS_HOST}")
    private String host;

    @Value("${REDIS_PORT}")
    private int port;

    @Value("${REDIS_PASSWORD}")
    private String password;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        String redisAddress = "redis://" + host + ":" + port;
        log.info("Configuring Redisson with Redis address: {}", redisAddress);
        
        try {
            if (password == null || password.trim().isEmpty()) {
                log.info("Redis connection with no password");
                config.useSingleServer()
                    .setAddress(redisAddress)
                    .setConnectionPoolSize(10)
                    .setConnectionMinimumIdleSize(1)
                    .setConnectTimeout(10000)
                    .setTimeout(3000)             
                    .setRetryAttempts(5)  
                    .setRetryInterval(1500);  
            } else {
                log.info("Redis connection with password");
                config.useSingleServer()
                    .setAddress(redisAddress)
                    .setPassword(password)
                    .setConnectionPoolSize(10)
                    .setConnectionMinimumIdleSize(1)
                    .setConnectTimeout(10000)
                    .setTimeout(3000)
                    .setRetryAttempts(5)
                    .setRetryInterval(1500);
            }
            
            log.info("Redisson configuration complete");
            return Redisson.create(config);
        } catch (Exception e) {
            log.error("Error creating RedissonClient", e);
            throw e;
        }
    }
}
