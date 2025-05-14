package com.gdg.Todak.guestbook.controller.dto;

import com.gdg.Todak.guestbook.entity.Guestbook;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@NoArgsConstructor
@Getter
public class GetGuestbookResponse {
    private Long guestbookId;
    private String senderNickname;
    private String senderUserId;
    private String content;
    private Instant createdAt;

    @Builder
    public GetGuestbookResponse(Long id, String senderNickname, String senderUserId, String content, Instant createdAt) {
        this.guestbookId = id;
        this.senderNickname = senderNickname;
        this.senderUserId = senderUserId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static GetGuestbookResponse of(Guestbook guestbook) {
        return GetGuestbookResponse.builder()
            .id(guestbook.getId())
            .senderNickname(guestbook.getSender().getNickname())
            .senderUserId(guestbook.getSender().getUserId())
            .content(guestbook.getContent())
            .createdAt(guestbook.getCreatedAt())
            .build();
    }
}
