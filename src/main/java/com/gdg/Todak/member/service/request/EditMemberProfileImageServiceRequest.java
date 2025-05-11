package com.gdg.Todak.member.service.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@NoArgsConstructor
public class EditMemberProfileImageServiceRequest {

    private MultipartFile image;

    @Builder
    public EditMemberProfileImageServiceRequest(MultipartFile image) {
        this.image = image;
    }
}
