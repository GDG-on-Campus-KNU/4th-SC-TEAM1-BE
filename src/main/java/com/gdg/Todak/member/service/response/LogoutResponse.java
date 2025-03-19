package com.gdg.Todak.member.service.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LogoutResponse {

    private String message;

    @Builder
    public LogoutResponse(String message) {
        this.message = message;
    }

    public static LogoutResponse of(String message) {
        return LogoutResponse.builder()
                .message(message)
                .build();
    }
}
