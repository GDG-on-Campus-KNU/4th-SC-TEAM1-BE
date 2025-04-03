package com.gdg.Todak.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdg.Todak.friend.FriendStatus;
import com.gdg.Todak.friend.entity.Friend;
import com.gdg.Todak.friend.repository.FriendRepository;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.DefaultStringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationService {

    private final RedisSubscriberService redisSubscriberService;
    private final RedisPublisherService redisPublisherService;
    private final FriendRepository friendRepository;
    private final RedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public SseEmitter createEmitter(String userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        redisSubscriberService.addEmitter(userId, emitter);

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> sendHeartbeat(userId, emitter, executor), 0, 15, TimeUnit.SECONDS);

        setEmitterCallbacks(userId, emitter, executor);
        return emitter;
    }

    // todo: 오프라인 상태였던 동안 받지 못했던 알림들 받아오기
    public List<Notification> getStoredMessages(String userId) {
        Set<String> notificationIds = redisTemplate.opsForSet().members(userId);

        List<String> notificationsStrings = redisTemplate.executePipelined(
                (RedisCallback<Object>) connection -> {
                    DefaultStringRedisConnection stringRedisConn = new DefaultStringRedisConnection(connection);
                    notificationIds.stream()
                            .map(stringRedisConn::get)
                            .collect(Collectors.toList());
                    return null;
                });

        return notificationsStrings.stream()
                .map(s -> {
                    try {
                        return objectMapper.readValue(s, Notification.class);
                    } catch (JsonProcessingException e) {
                        return null;
                    }
                })
                .filter(obj -> obj != null)
                .collect(Collectors.toList());
    }

    private void sendHeartbeat(String userId, SseEmitter emitter, ScheduledExecutorService executor) {
        try {
            emitter.send(SseEmitter.event()
                    .name("heartbeat")
                    .data("heartbeat"));
        } catch (IOException e) {
            log.warn("Error sending heartbeat, connection might be closed. Removing emitter and shutting down executor.", e);
            redisSubscriberService.removeEmitter(userId, emitter);
            emitter.completeWithError(e);
            executor.shutdown();
        }
    }

    private void setEmitterCallbacks(String userId, SseEmitter emitter, ScheduledExecutorService executor) {
        emitter.onCompletion(() -> {
            redisSubscriberService.removeEmitter(userId, emitter);
            log.info("Emitter completed for user: {}", userId);
            executor.shutdown();
        });

        emitter.onTimeout(() -> {
            redisSubscriberService.removeEmitter(userId, emitter);
            log.info("Emitter timed out for user: {}", userId);
            executor.shutdown();
        });

        emitter.onError((Throwable t) -> {
            redisSubscriberService.removeEmitter(userId, emitter);
            log.error("Emitter error for user: {}", userId, t);
            executor.shutdown();
        });
    }

    /**
     * @param objectId : postId
     * @param type     : "post" (새 글 알림)
     */
    public void publishPostNotifications(String senderId, String type, Long objectId) {
        List<Friend> friends = friendRepository.findAllByAccepterUserIdAndFriendStatusOrRequesterUserIdAndFriendStatus(
                senderId, FriendStatus.ACCEPTED, // requester
                senderId, FriendStatus.ACCEPTED // acceptor
        );

        friends.stream()
                .forEach(friend -> {
                    Member friendInfo = friend.getFriend(senderId);
                    publishEventToRedis(objectId, senderId, friendInfo.getUserId(), type);
                });
    }

    /**
     * @param objectId : postId
     * @param type     : "comment" (댓글 알림)
     */
    public void publishCommentNotification(String senderId, String receiverId, String type, Long objectId) {
        publishEventToRedis(objectId, senderId, receiverId, type);
    }

    private void publishEventToRedis(Long objectId, String senderId, String receiverId, String type) {
        String notificationKey = UUID.randomUUID().toString();
        Instant timestamp = Instant.now();

        Notification notification = Notification.builder()
                .id(notificationKey)
                .objectId(objectId)
                .senderUserId(senderId)
                .receiverUserId(receiverId)
                .type(type)
                .createdAt(timestamp)
                .build();

        log.info("About to save notification: {}", notificationKey);
        redisPublisherService.saveNotificationWithTTL(notificationKey, notification, 3, TimeUnit.DAYS);
        log.info("Finish saving notification: {}", notificationKey);

        redisTemplate.opsForSet().add(receiverId, notificationKey);

        redisPublisherService.publish("notification", notificationKey);
    }

    public String deleteAckNotification(String userId, String notificationId) {
        redisTemplate.opsForSet().remove(userId, notificationId);
        redisTemplate.delete(notificationId);
        return "notification deleted";
    }
}
