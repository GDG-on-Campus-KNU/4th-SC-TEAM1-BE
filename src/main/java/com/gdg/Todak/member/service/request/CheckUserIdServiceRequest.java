package com.gdg.Todak.member.service.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CheckUserIdServiceRequest {

    private String userId;

    @Builder
    public CheckUserIdServiceRequest(String userId) {
        this.userId = userId;
    }
}
