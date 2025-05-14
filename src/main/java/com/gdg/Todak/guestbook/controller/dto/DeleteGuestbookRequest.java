package com.gdg.Todak.guestbook.controller.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class DeleteGuestbookRequest {

    private Long guestbookId;

    @Builder
    public DeleteGuestbookRequest(Long guestbookId) {
        this.guestbookId = guestbookId;
    }

    public static DeleteGuestbookRequest of(Long guestbookId) {
        return DeleteGuestbookRequest.builder()
            .guestbookId(guestbookId)
            .build();
    }
}
