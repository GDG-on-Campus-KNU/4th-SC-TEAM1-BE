package com.gdg.Todak.member.service.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginResponse {
    private String userId;
    private String nickname;
    private String accessToken;
    private String refreshToken;

    @Builder
    public LoginResponse(String userId, String nickname, String accessToken, String refreshToken) {
        this.userId = userId;
        this.nickname = nickname;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public static LoginResponse of(String userId, String nickname, String accessToken, String refreshToken) {
        return LoginResponse.builder()
                .userId(userId)
                .nickname(nickname)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
