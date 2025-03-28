package com.gdg.Todak.member.controller.request;

import com.gdg.Todak.member.service.request.CheckUsernameServiceRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CheckUsernameRequest {

    private String username;

    @Builder
    public CheckUsernameRequest(String username) {
        this.username = username;
    }

    public CheckUsernameServiceRequest toServiceRequest() {
        return CheckUsernameServiceRequest.builder()
                .username(username)
                .build();
    }
}
