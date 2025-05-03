package com.gdg.Todak.common.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RedissonConfigTest {

    @Autowired
    private RedissonClient redissonClient;

    private final String TEST_PREFIX = "test:redisson:";

    @BeforeEach
    void setUp() {
        redissonClient.getKeys().getKeysByPattern(TEST_PREFIX + "*").forEach(key -> {
            redissonClient.getBucket(key).delete();
        });
    }

    @AfterEach
    void tearDown() {
        redissonClient.getKeys().getKeysByPattern(TEST_PREFIX + "*").forEach(key -> {
            redissonClient.getBucket(key).delete();
        });
    }

    @Test
    void redissonClientShouldConnectSuccessfully() {
        // Given
        String key = TEST_PREFIX + "key";
        String value = "Hello, Redis!";

        // When
        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.set(value);

        // Then
        assertThat(bucket.isExists()).isTrue();
        assertThat(bucket.get()).isEqualTo(value);
    }

    @Test
    void redissonClientShouldHandleNonExistingKey() {
        // Given
        String nonExistingKey = TEST_PREFIX + "non-existing-key";

        // When
        RBucket<String> bucket = redissonClient.getBucket(nonExistingKey);

        // Then
        assertThat(bucket.isExists()).isFalse();
        assertThat(bucket.get()).isNull();
    }

    @Test
    void redissonClientShouldSupportDifferentDataTypes() {
        // Given
        String stringKey = TEST_PREFIX + "string";
        String stringValue = "string value";

        String numberKey = TEST_PREFIX + "number";
        int numberValue = 42;

        // When
        redissonClient.getBucket(stringKey).set(stringValue);
        redissonClient.getBucket(numberKey).set(numberValue);

        // Then
        assertThat(redissonClient.<String>getBucket(stringKey).get()).isEqualTo(stringValue);
        assertThat(redissonClient.<Integer>getBucket(numberKey).get()).isEqualTo(numberValue);
    }
}
