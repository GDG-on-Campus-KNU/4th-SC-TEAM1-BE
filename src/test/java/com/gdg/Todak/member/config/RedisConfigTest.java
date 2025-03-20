package com.gdg.Todak.member.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RedisConfigTest {

    @Autowired
    RedisTemplate redisTemplate;

    Long memberId = 1L;
    String refreshToken = "abcd";
    String nonExistentRefreshToken = "ABCD";

    @AfterEach
    void tearDown() {
        redisTemplate.delete(refreshToken);
        redisTemplate.delete(nonExistentRefreshToken);
    }

    @DisplayName("레디스에 refreshToken을 key, memberId를 value로 저장 후 조회한다.")
    @Test
    void redisRetrieveTest() {
        // given
        redisTemplate.opsForValue().set(refreshToken, memberId, 7, TimeUnit.DAYS);

        // when
        Long findMemberId = (Long) redisTemplate.opsForValue().get(refreshToken);

        // then
        assertThat(findMemberId).isEqualTo(memberId);
    }

    @DisplayName("레디스에 존재하지 않는 key로 조회시 null이 반환된다.")
    @Test
    void redisRetrieveErrorTest() {
        // given
        Long memberId = 1L;
        String refreshToken = "abcd";

        redisTemplate.opsForValue().set(refreshToken, memberId, 7, TimeUnit.DAYS);

        // when
        Long findMemberId = (Long) redisTemplate.opsForValue().get(nonExistentRefreshToken);

        // then
        assertThat(findMemberId).isNull();
    }
}
