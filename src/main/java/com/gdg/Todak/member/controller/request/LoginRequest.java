package com.gdg.Todak.member.controller.request;

import com.gdg.Todak.member.service.request.LoginServiceRequest;

public record LoginRequest(
        String username,
        String password
) {

    public LoginServiceRequest toServiceRequest() {
        return LoginServiceRequest.builder()
                .username(username)
                .password(password)
                .build();
    }
}
