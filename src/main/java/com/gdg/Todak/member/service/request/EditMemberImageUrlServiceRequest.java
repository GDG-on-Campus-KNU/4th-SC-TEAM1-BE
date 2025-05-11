package com.gdg.Todak.member.service.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EditMemberImageUrlServiceRequest {

    private String imageUrl;

    @Builder
    public EditMemberImageUrlServiceRequest(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
