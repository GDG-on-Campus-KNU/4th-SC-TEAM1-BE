package com.gdg.Todak.member.controller.request;

import com.gdg.Todak.member.service.request.EditMemberImageUrlServiceRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EditMemberImageUrlRequest {

    private String imageUrl;

    @Builder
    public EditMemberImageUrlRequest(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public EditMemberImageUrlServiceRequest toServiceRequest() {
        return EditMemberImageUrlServiceRequest.builder()
            .imageUrl(imageUrl)
            .build();
    }
}
