package com.gdg.Todak.member.service.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LogoutServiceRequest {

    private String refreshToken;

    @Builder
    public LogoutServiceRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
