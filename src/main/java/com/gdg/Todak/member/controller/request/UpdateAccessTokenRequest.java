package com.gdg.Todak.member.controller.request;


import com.gdg.Todak.member.service.request.UpdateAccessTokenServiceRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateAccessTokenRequest {

    private String accessToken;
    private String refreshToken;

    @Builder
    public UpdateAccessTokenRequest(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public UpdateAccessTokenServiceRequest toServiceRequest() {
        return UpdateAccessTokenServiceRequest.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
    }
}
