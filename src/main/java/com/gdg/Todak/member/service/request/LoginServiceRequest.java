package com.gdg.Todak.member.service.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginServiceRequest {

    private String userId;
    private String password;

    @Builder
    public LoginServiceRequest(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }
}
