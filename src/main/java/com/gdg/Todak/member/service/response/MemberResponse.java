package com.gdg.Todak.member.service.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberResponse {

    private String username;

    @Builder
    public MemberResponse(String username) {
        this.username = username;
    }

    public static MemberResponse of(String username) {
        return MemberResponse.builder()
                .username(username)
                .build();
    }
}
