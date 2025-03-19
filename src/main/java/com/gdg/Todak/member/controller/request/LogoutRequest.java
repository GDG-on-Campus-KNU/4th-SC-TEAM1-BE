package com.gdg.Todak.member.controller.request;

import com.gdg.Todak.member.service.request.LogoutServiceRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LogoutRequest {

    private String refreshToken;

    @Builder
    public LogoutRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public LogoutServiceRequest toServiceRequest() {
        return LogoutServiceRequest.builder()
                .refreshToken(refreshToken)
                .build();
    }
}
