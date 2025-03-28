package com.gdg.Todak.member.controller.request;

import com.gdg.Todak.member.service.request.EditMemberServiceRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EditMemberRequest {

    private String nickname;
    private String imageUrl;

    @Builder
    public EditMemberRequest(String userId, String nickname, String imageUrl) {
        this.nickname = nickname;
        this.imageUrl = imageUrl;
    }

    public EditMemberServiceRequest toServiceRequest() {
        return EditMemberServiceRequest.builder()
                .nickname(nickname)
                .imageUrl(imageUrl)
                .build();
    }
}
