package com.gdg.Todak.member.service.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CheckUsernameServiceRequest {

    private String username;

    @Builder
    public CheckUsernameServiceRequest(String username) {
        this.username = username;
    }
}
