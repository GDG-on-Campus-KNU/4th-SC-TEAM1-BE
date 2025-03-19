package com.gdg.Todak.member.service.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginServiceRequest {

    private String username;
    private String password;

    @Builder
    public LoginServiceRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
