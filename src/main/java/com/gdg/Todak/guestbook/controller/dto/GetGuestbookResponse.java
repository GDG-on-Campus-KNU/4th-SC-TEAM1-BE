package com.gdg.Todak.guestbook.controller.dto;

import com.gdg.Todak.guestbook.entity.Guestbook;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@NoArgsConstructor
@Getter
public class GetGuestbookResponse {
    private Long id;
    private String nickname;
    private String content;
    private Instant createdAt;

    @Builder
    public GetGuestbookResponse(Long id, String nickname, String content, Instant createdAt) {
        this.id = id;
        this.nickname = nickname;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static GetGuestbookResponse from(Guestbook guestbook, String nickname) {
        return GetGuestbookResponse.builder()
            .id(guestbook.getId())
            .nickname(nickname)
            .content(guestbook.getContent())
            .createdAt(guestbook.getCreatedAt())
            .build();
    }
}
