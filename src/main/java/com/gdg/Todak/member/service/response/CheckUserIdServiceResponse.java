package com.gdg.Todak.member.service.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CheckUserIdServiceResponse {

    private Boolean exists;

    @Builder
    public CheckUserIdServiceResponse(Boolean exists) {
        this.exists = exists;
    }

    public static CheckUserIdServiceResponse of(Boolean exists) {
        return CheckUserIdServiceResponse.builder()
                .exists(exists)
                .build();
    }
}
