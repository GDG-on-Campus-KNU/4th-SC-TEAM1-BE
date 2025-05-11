package com.gdg.Todak.member.controller.request;

import com.gdg.Todak.member.service.request.EditMemberNicknameServiceRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EditMemberNicknameRequest {

    private String nickname;

    @Builder
    public EditMemberNicknameRequest(String nickname) {
        this.nickname = nickname;
    }

    public EditMemberNicknameServiceRequest toServiceRequest() {
        return EditMemberNicknameServiceRequest.builder()
            .nickname(nickname)
            .build();
    }
}
