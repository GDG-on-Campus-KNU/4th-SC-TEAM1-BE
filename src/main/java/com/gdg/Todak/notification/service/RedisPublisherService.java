package com.gdg.Todak.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdg.Todak.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
@Service
public class RedisPublisherService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void publish(String channel, Object message) {
        log.info("Publishing message to channel: [{}] at time: {} with message: {}", channel, Instant.now(), message);
        redisTemplate.convertAndSend(channel, message);
        log.info("Published message to channel: [{}] at time: {} with message: {}", channel, Instant.now(), message);
    }

    public void saveNotificationWithTTL(String key, Notification notification, long ttl, TimeUnit timeUnit) {
        try {
            String notificationString = objectMapper.writeValueAsString(notification);
            redisTemplate.opsForValue().set(key, notificationString, ttl, timeUnit);
            log.debug("Saved notification with key: {} and TTL: {} {}", key, ttl, timeUnit);
        } catch (JsonProcessingException e) {
            log.error("Error saving notification with key: {} and TTL: {} {}", key, ttl, timeUnit, e);
        }
    }
}
