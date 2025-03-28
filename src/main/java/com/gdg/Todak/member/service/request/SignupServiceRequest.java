package com.gdg.Todak.member.service.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupServiceRequest {

    private String userId;
    private String password;
    private String passwordCheck;
    private String nickname;

    @Builder
    public SignupServiceRequest(String userId, String password, String passwordCheck, String nickname) {
        this.userId = userId;
        this.password = password;
        this.passwordCheck = passwordCheck;
        this.nickname = nickname;
    }
}
