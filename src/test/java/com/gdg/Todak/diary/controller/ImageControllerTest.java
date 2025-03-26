package com.gdg.Todak.diary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdg.Todak.diary.dto.ImageDeleteRequest;
import com.gdg.Todak.diary.dto.UrlResponse;
import com.gdg.Todak.diary.service.ImageService;
import com.gdg.Todak.member.Interceptor.LoginCheckInterceptor;
import com.gdg.Todak.member.domain.AuthenticateUser;
import com.gdg.Todak.member.domain.Role;
import com.gdg.Todak.member.resolver.LoginMemberArgumentResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ImageController.class)
class ImageControllerTest {

    private final String token = "testToken";
    private final String imageUrl = "https://example.com/test-image.jpg";

    @MockitoBean
    private ImageService imageService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoginCheckInterceptor loginCheckInterceptor;

    @MockitoBean
    private LoginMemberArgumentResolver loginMemberArgumentResolver;

    @BeforeEach
    void setUp() throws Exception {
        when(loginCheckInterceptor.preHandle(any(), any(), any())).thenReturn(true);

        String username = "testUser";
        AuthenticateUser authenticateUser = new AuthenticateUser(username, Set.of(Role.USER));

        when(loginMemberArgumentResolver.supportsParameter(any())).thenReturn(true);
        when(loginMemberArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(authenticateUser);
    }

    @Test
    @DisplayName("이미지 업로드 테스트")
    void uploadImageTest() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "image-content".getBytes());

        String storageUUID = "testStorageUUID";
        UrlResponse urlResponse = new UrlResponse(imageUrl);

        when(imageService.uploadImage(any(), anyString(), anyString())).thenReturn(urlResponse);

        // when
        mockMvc.perform(multipart("/api/v1/images/upload")
                        .file(file)
                        .param("storageUUID", storageUUID)
                        .header("Authorization", "Bearer " + token))

                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.url").value(imageUrl));
    }

    @Test
    @DisplayName("이미지 삭제 테스트")
    void deleteImageTest() throws Exception {
        // given
        ImageDeleteRequest request = new ImageDeleteRequest(imageUrl);
        doNothing().when(imageService).deleteImage(anyString(), anyString());

        // when
        mockMvc.perform(post("/api/v1/images/delete")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("제거가 완료되었습니다."));
    }

    @Test
    @DisplayName("UUID 생성 테스트")
    void generateUUIDTest() throws Exception {
        // when
        mockMvc.perform(get("/api/v1/make/uuid"))

                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.uuid").isNotEmpty());
    }
}
