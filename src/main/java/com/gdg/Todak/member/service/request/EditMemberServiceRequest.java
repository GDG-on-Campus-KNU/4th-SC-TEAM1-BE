package com.gdg.Todak.member.service.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EditMemberServiceRequest {

    private String userId;
    private String nickname;
    private String imageUrl;

    @Builder
    public EditMemberServiceRequest(String userId, String nickname, String imageUrl) {
        this.userId = userId;
        this.nickname = nickname;
        this.imageUrl = imageUrl;
    }
}
