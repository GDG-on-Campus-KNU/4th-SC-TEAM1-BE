package com.gdg.Todak.guestbook.controller.dto;

import com.gdg.Todak.guestbook.entity.Guestbook;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
public class AddGuestbookResponse {

    private Long id;
    private String nickname;
    private String content;
    private Instant createdAt;

    @Builder
    public AddGuestbookResponse(Long id, String nickname, Instant createdAt, String content) {
        this.id = id;
        this.nickname = nickname;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static AddGuestbookResponse from(Guestbook guestbook) {
        return AddGuestbookResponse.builder()
            .id(guestbook.getId())
            .nickname(guestbook.getSender().getNickname())
            .content(guestbook.getContent())
            .createdAt(guestbook.getCreatedAt())
            .build();
    }
}
