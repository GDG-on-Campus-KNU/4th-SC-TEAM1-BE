package com.gdg.Todak.notification.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Instant;

@Getter
@ToString
@NoArgsConstructor
public class Notification {
    private String id;
    private Long objectId;
    private String senderUserId;
    private String receiverUserId;
    private String type;
    private Instant diaryCreatedAt;
    private Instant createdAt;

    @Builder
    public Notification(String id, Long objectId, String senderUserId, String receiverUserId, String type, Instant diaryCreatedAt, Instant createdAt) {
        this.id = id;
        this.objectId = objectId;
        this.senderUserId = senderUserId;
        this.receiverUserId = receiverUserId;
        this.type = type;
        this.diaryCreatedAt = diaryCreatedAt;
        this.createdAt = createdAt;
    }
}
