package com.gdg.Todak.member.service.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberResponse {

    private String userId;

    @Builder
    public MemberResponse(String userId) {
        this.userId = userId;
    }

    public static MemberResponse of(String userId) {
        return MemberResponse.builder()
                .userId(userId)
                .build();
    }
}
