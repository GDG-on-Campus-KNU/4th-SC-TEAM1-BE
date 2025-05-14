package com.gdg.Todak.guestbook.controller.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AddGuestbookRequest {
    private String userId;
    private String content;

    @Builder
    public AddGuestbookRequest(String userId, String content) {
        this.userId = userId;
        this.content = content;
    }

    public static AddGuestbookRequest of(String userId, String content) {
        return AddGuestbookRequest.builder()
            .userId(userId)
            .content(content)
            .build();
    }
}
