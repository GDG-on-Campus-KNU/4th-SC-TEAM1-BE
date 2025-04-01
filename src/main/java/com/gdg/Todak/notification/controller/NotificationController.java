package com.gdg.Todak.notification.controller;

import com.gdg.Todak.common.domain.ApiResponse;
import com.gdg.Todak.member.domain.AuthenticateUser;
import com.gdg.Todak.member.resolver.Login;
import com.gdg.Todak.notification.controller.dto.AckRequest;
import com.gdg.Todak.notification.entity.Notification;
import com.gdg.Todak.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
@RestController
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/create")
    @Operation(summary = "SSE 연결 생성", description = "알림 전달을 위한 SSE 연결을 생성한다.")
    public SseEmitter notificationSubscribe(@Parameter(hidden = true) @Login AuthenticateUser user) {
        return notificationService.createEmitter(user.getUserId());
    }

    @PostMapping("/ack")
    @Operation(summary = "읽은 알림 삭제", description = "읽은 알림을 삭제한다.")
    public ApiResponse notificationAck(@Parameter(hidden = true) @Login AuthenticateUser user, @RequestBody AckRequest request) {
        return ApiResponse.ok(notificationService.deleteAckNotification(user.getUserId(), request.getNotificationId()));
    }

    @GetMapping("/unchecked-notifications")
    @Operation(summary = "읽지 않은 알림들 조회", description = "읽지 않은 알림(읽음 확인 되지 않은 알림)들을 조회한다.")
    public ApiResponse<List<Notification>> getUncheckedNotifications(@Parameter(hidden = true) @Login AuthenticateUser user) {
        return ApiResponse.ok(notificationService.getStoredMessages(user.getUserId()));
    }
}
