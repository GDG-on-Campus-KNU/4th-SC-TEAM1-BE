package com.gdg.Todak.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdg.Todak.notification.entity.Notification;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


@RequiredArgsConstructor
@Slf4j
@Service
public class RedisSubscriberService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private Map<String, List<SseEmitter>> emitters;
    private ScheduledExecutorService scheduledExecutorService;

    @PostConstruct
    public void init() {
        this.emitters = new ConcurrentHashMap<>();
        this.scheduledExecutorService = Executors.newScheduledThreadPool(10);
    }

    /**
     * @param channel : 레디스 알림 채널
     * @param message : 레디스 키
     */
    public void onMessage(String channel, String message) {
        log.info("Received message from channel: [{}] at time: {} with message: {}", channel, Instant.now(), message);
        processMessage(message, 5);
    }

    private void processMessage(String key, int retries) {
        scheduledExecutorService.submit(() -> {
            try {
                String notificationString = null;
                for (int attempt = 1; attempt <= retries; attempt++) {
                    notificationString = (String) redisTemplate.opsForValue().get(key);
                    if (notificationString != null) {
                        break;
                    }
                    log.debug("Retrying to get key: {}. Attempt: {}", key, attempt);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        log.error("InterruptedException during sleep", e);
                        Thread.currentThread().interrupt();
                        return;
                    }
                }

                if (notificationString != null) {
                    Notification notification = objectMapper.readValue(notificationString, Notification.class);
                    log.info("Notification: {}", notification);
                    sendNotificationToEmitters(notification);
                } else {
                    log.warn("No notification found in Redis for key: {} after maximum retries", key);
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void sendNotificationToEmitters(Notification notification) {
        String receiverUserId = notification.getReceiverUserId();
        List<SseEmitter> sseEmitters = emitters.get(receiverUserId);

        if (sseEmitters != null && !sseEmitters.isEmpty()) {
            List<SseEmitter> deadEmitters = new ArrayList<>();

            for (SseEmitter emitter : sseEmitters) {
                try {
                    emitter.send(
                            SseEmitter.event()
                                    .name("notification")
                                    .data(notification)
                    );
                    log.info("Sent SSE to user: {} with notification: {} at time: {}", receiverUserId, notification, Instant.now());
                } catch (IOException e) {
                    log.error("Error sending SSE to user: {} with message: {}", receiverUserId, e.getMessage());
                    deadEmitters.add(emitter);
                }
            }
        } else {
            log.warn("No emitters found for user: {}", receiverUserId);
        }
    }

    public void addEmitter(String userId, SseEmitter emitter) {
        emitters.computeIfAbsent(userId, k -> new ArrayList<>()).add(emitter);
        log.info("Emitter added for user: {}", userId);
    }

    public void removeEmitter(String userId, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters != null) {
            userEmitters.remove(emitter);
            if (userEmitters.isEmpty()) {
                emitters.remove(userId);
            }
        }
        log.info("Emitter removed for user: {}", userId);
    }
}
