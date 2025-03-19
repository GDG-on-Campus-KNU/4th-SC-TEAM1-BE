package com.gdg.Todak.member.service.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CheckUsernameServiceResponse {

    private Boolean exists;

    @Builder
    public CheckUsernameServiceResponse(Boolean exists) {
        this.exists = exists;
    }

    public static CheckUsernameServiceResponse of(Boolean exists) {
        return CheckUsernameServiceResponse.builder()
                .exists(exists)
                .build();
    }
}
