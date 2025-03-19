package com.gdg.Todak.member.controller.request;


import com.gdg.Todak.member.service.request.UpdateAccessTokenServiceRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateAccessTokenRequest {

    private String refreshToken;

    @Builder
    public UpdateAccessTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public UpdateAccessTokenServiceRequest toServiceRequest() {
        return UpdateAccessTokenServiceRequest.builder()
                .refreshToken(refreshToken)
                .build();
    }
}
