package com.gdg.Todak.member.controller.request;

import com.gdg.Todak.member.service.request.EditMemberProfileImageServiceRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@NoArgsConstructor
public class EditMemberProfileImageRequest {

    private MultipartFile image;

    @Builder
    public EditMemberProfileImageRequest(MultipartFile image) {
        this.image = image;
    }

    public EditMemberProfileImageServiceRequest toServiceRequest() {
        return EditMemberProfileImageServiceRequest.builder()
            .image(image)
            .build();
    }
}
