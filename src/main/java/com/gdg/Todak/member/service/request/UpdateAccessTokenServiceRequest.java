package com.gdg.Todak.member.service.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateAccessTokenServiceRequest {

    private String refreshToken;

    @Builder
    public UpdateAccessTokenServiceRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
