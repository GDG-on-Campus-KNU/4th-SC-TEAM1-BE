package com.gdg.Todak.member.controller.request;

import com.gdg.Todak.member.service.request.CheckUserIdServiceRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CheckUserIdRequest {

    private String userId;

    @Builder
    public CheckUserIdRequest(String userId) {
        this.userId = userId;
    }

    public CheckUserIdServiceRequest toServiceRequest() {
        return CheckUserIdServiceRequest.builder()
                .userId(userId)
                .build();
    }
}
