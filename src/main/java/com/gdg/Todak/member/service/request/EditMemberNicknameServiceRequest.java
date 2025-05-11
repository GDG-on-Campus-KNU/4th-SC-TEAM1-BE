package com.gdg.Todak.member.service.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EditMemberNicknameServiceRequest {

    private String nickname;

    @Builder
    public EditMemberNicknameServiceRequest(String nickname) {
        this.nickname = nickname;
    }
}
