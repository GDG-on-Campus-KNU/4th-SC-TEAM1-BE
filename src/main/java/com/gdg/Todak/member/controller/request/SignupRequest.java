package com.gdg.Todak.member.controller.request;

import com.gdg.Todak.member.service.request.SignupServiceRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequest {

    @NotBlank
    @Size(min = 3, max = 20)
    private String userId;

    @NotBlank
    @Size(min = 8, max = 16)
    private String password;
    private String passwordCheck;

    @Size(min = 3, max = 20)
    private String nickname;

    @Builder
    public SignupRequest(String userId, String password, String passwordCheck, String nickname) {
        this.userId = userId;
        this.password = password;
        this.passwordCheck = passwordCheck;
        this.nickname = nickname;
    }

    public SignupServiceRequest toServiceRequest() {
        return SignupServiceRequest.builder()
                .userId(userId)
                .password(password)
                .passwordCheck(passwordCheck)
                .nickname(nickname)
                .build();
    }
}
