package com.gdg.Todak.member.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Jwt {

    private String accessToken;
    private String refreshToken;

    @Builder
    public Jwt(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public static Jwt of(String accessToken, String refreshToken) {
        return Jwt.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
