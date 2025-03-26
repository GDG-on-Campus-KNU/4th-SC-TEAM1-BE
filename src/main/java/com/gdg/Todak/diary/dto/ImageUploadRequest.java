package com.gdg.Todak.diary.dto;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

public record ImageUploadRequest(
        MultipartFile file,
        @NotBlank
        String storageUUID
) {
}
