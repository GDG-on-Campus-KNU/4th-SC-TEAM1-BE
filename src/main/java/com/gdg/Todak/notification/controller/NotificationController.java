package com.gdg.Todak.notification.controller;

import com.gdg.Todak.common.domain.ApiResponse;
import com.gdg.Todak.member.domain.AuthenticateUser;
import com.gdg.Todak.member.resolver.Login;
import com.gdg.Todak.notification.controller.dto.AckRequest;
import com.gdg.Todak.notification.entity.Notification;
import com.gdg.Todak.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
@RestController
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public SseEmitter notificationSubscribe(@Login AuthenticateUser user) {
        return notificationService.createEmitter(user.getUserId());
    }

    @PostMapping
    public ApiResponse notificationAck(@Login AuthenticateUser user, @RequestBody AckRequest request) {
        return ApiResponse.ok(notificationService.deleteAckNotification(user.getUserId(), request.getNotificationId()));
    }

    @GetMapping("/unchecked-notifications")
    public ApiResponse<List<Notification>> getUncheckedNotifications(@Login AuthenticateUser user) {
        return ApiResponse.ok(notificationService.getStoredMessages(user.getUserId()));
    }
}
