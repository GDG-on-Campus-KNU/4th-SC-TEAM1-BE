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
    private String username;
    @NotBlank
    @Size(min = 8, max = 16)
    private String password;

    @Builder
    public SignupRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public SignupServiceRequest toServiceRequest() {
        return SignupServiceRequest.builder()
                .username(username)
                .password(password)
                .build();
    }
}
