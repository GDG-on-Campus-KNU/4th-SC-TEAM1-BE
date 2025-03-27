package com.gdg.Todak.diary.controller;

import com.gdg.Todak.common.domain.ApiResponse;
import com.gdg.Todak.diary.dto.ImageDeleteRequest;
import com.gdg.Todak.diary.dto.ImageUploadRequest;
import com.gdg.Todak.diary.dto.UUIDResponse;
import com.gdg.Todak.diary.dto.UrlResponse;
import com.gdg.Todak.diary.service.ImageService;
import com.gdg.Todak.member.domain.AuthenticateUser;
import com.gdg.Todak.member.resolver.Login;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Tag(name = "이미지 관리", description = "이미지 관련 API")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping(value = "/api/v1/images/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "이미지 업로드(png, jpeg 그리고 10MB 까지 업로드 가능)", description = "로그인 필요, 파일 필수, storageUUID 필수")
    public ApiResponse<UrlResponse> getImageUrl(@ModelAttribute @Valid ImageUploadRequest request,
                                                @Parameter(hidden = true) @Login AuthenticateUser authenticateUser) {
        UrlResponse urlResponse = imageService.uploadImage(request.file(), request.storageUUID(), authenticateUser.getUsername());
        return ApiResponse.ok(urlResponse);
    }

    @PostMapping("/api/v1/images/delete")
    @Operation(summary = "이미지 제거(업로드 API를 통해 받는 url을 body에)", description = "로그인 필요, url 필수")
    public ApiResponse<Void> deleteImage(@RequestBody @Valid ImageDeleteRequest request,
                                         @Parameter(hidden = true) @Login AuthenticateUser authenticateUser) {
        imageService.deleteImage(request.url(), authenticateUser.getUsername());
        return ApiResponse.of(HttpStatus.OK, "제거가 완료되었습니다.");
    }

    @GetMapping("/api/v1/make/uuid")
    @Operation(summary = "storage uuid로 사용할 랜덤 uuid 생성 API", description = "uuid 생성 방식에 맞추어 랜덤값을 생성한다")
    public ApiResponse<UUIDResponse> makeStorageUUID() {
        String randomUUID = UUID.randomUUID().toString();
        return ApiResponse.ok(new UUIDResponse(randomUUID));
    }
}
