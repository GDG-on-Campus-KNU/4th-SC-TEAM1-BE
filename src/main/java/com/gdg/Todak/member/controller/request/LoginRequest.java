package com.gdg.Todak.member.controller.request;

import com.gdg.Todak.member.service.request.LoginServiceRequest;

public record LoginRequest(
        String userId,
        String password
) {

    public LoginServiceRequest toServiceRequest() {
        return LoginServiceRequest.builder()
                .userId(userId)
                .password(password)
                .build();
    }
}
