package com.gdg.Todak.member.service.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupServiceRequest {

    private String username;
    private String password;

    @Builder
    public SignupServiceRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
