package com.gdg.Todak.member.controller;

import com.gdg.Todak.member.controller.request.CheckUserIdRequest;
import com.gdg.Todak.member.controller.request.EditMemberNicknameRequest;
import com.gdg.Todak.member.controller.request.LoginRequest;
import com.gdg.Todak.member.controller.request.SignupRequest;
import com.gdg.Todak.member.domain.AuthenticateUser;
import com.gdg.Todak.member.service.request.SignupServiceRequest;
import com.gdg.Todak.member.service.response.CheckUserIdServiceResponse;
import com.gdg.Todak.member.service.response.MeResponse;
import com.gdg.Todak.member.service.response.MemberResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class MemberControllerTest extends ControllerTestSupport {

    @DisplayName("회원가입을 한다.")
    @Test
    void signup() throws Exception {
        // given
        String userId = "testUserId";
        String password = "testPassword";

        SignupRequest request = SignupRequest.builder()
                .userId(userId)
                .password(password)
                .build();

        when(memberService.signup(any(SignupServiceRequest.class))).thenReturn(
                MemberResponse.of(userId));

        // when // then
        mockMvc.perform(
                        post("/api/v1/members/signup")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("OK"));
    }

    @DisplayName("회원가입 시 userId은 3글자 이상이어야 한다.")
    @Test
    void signupWithShortUserId() throws Exception {
        // given
        String userId = "us";
        String password = "testPassword";

        SignupRequest request = SignupRequest.builder()
                .userId(userId)
                .password(password)
                .build();

        // when // then
        mockMvc.perform(
                        post("/api/v1/members/signup")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
    }

    @DisplayName("회원가입 시 password는 8글자 이상이어야 한다.")
    @Test
    void signupWithShortPassword() throws Exception {
        // given
        String userId = "testUserId";
        String password = "passwor";

        SignupRequest request = SignupRequest.builder()
                .userId(userId)
                .password(password)
                .build();

        // when // then
        mockMvc.perform(
                        post("/api/v1/members/signup")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
    }

    @DisplayName("로그인을 한다.")
    @Test
    void login() throws Exception {
        // given
        String userId = "testUserId";
        String password = "testPassword";

        LoginRequest request = new LoginRequest(userId, password);

        // when // then
        mockMvc.perform(
                        post("/api/v1/members/login")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.status").value("OK"));
    }

    @DisplayName("유저 아이디 중복 체크를 한다.")
    @Test
    void checkUserId() throws Exception {
        // given
        String userId = "testUserId";
        CheckUserIdRequest request = new CheckUserIdRequest(userId);

        when(memberService.checkUserId(any())).thenReturn(
                new CheckUserIdServiceResponse(false));

        // when // then
        mockMvc.perform(
                        post("/api/v1/members/check-userId")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data.exists").value(false));
    }

    @DisplayName("마이페이지를 조회한다.")
    @Test
    void me() throws Exception {
        // given
        AuthenticateUser user = mock(AuthenticateUser.class);
        when(memberService.me(any(AuthenticateUser.class))).thenReturn(
                MeResponse.builder()
                        .userId("testUserId")
                        .nickname("testNickname")
                        .imageUrl("http://example.com/image.jpg")
                        .build());

        // when // then
        mockMvc.perform(
                        post("/api/v1/members/me")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data.userId").value("testUserId"))
                .andExpect(jsonPath("$.data.nickname").value("testNickname"));
    }

    @DisplayName("닉네임을 수정한다.")
    @Test
    void editMemberNickname() throws Exception {
        // given
        String nickname = "newNickname";
        EditMemberNicknameRequest request = new EditMemberNicknameRequest(nickname);
        AuthenticateUser user = mock(AuthenticateUser.class);

        when(memberService.editMemberNickname(any(AuthenticateUser.class), any())).thenReturn(
                MeResponse.builder()
                        .userId("testUserId")
                        .nickname(nickname)
                        .imageUrl("http://example.com/image.jpg")
                        .build());

        // when // then
        mockMvc.perform(
                        put("/api/v1/members/edit")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data.nickname").value(nickname));
    }

    @DisplayName("프로필 이미지를 수정한다.")
    @Test
    void editMemberProfileImage() throws Exception {
        // given
        MockMultipartFile profileImage = new MockMultipartFile(
                "profileImage",
                "profile.jpg",
                "image/jpeg",
                "image content".getBytes()
        );

        AuthenticateUser user = mock(AuthenticateUser.class);

        when(memberService.editMemberProfileImage(any(AuthenticateUser.class), any())).thenReturn(
                MeResponse.builder()
                        .userId("testUserId")
                        .nickname("testNickname")
                        .imageUrl("http://example.com/new-image.jpg")
                        .build());

        // when // then
        mockMvc.perform(
                        multipart("/api/v1/members/profile")
                                .file(profileImage)
                                .with(request -> {
                                    request.setMethod("PUT");
                                    return request;
                                })
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.status").value("OK"));
    }

    @DisplayName("프로필 이미지를 삭제한다.")
    @Test
    void deleteMemberProfileImage() throws Exception {
        // given
        AuthenticateUser user = mock(AuthenticateUser.class);

        when(memberService.deleteMemberProfileImage(any(AuthenticateUser.class))).thenReturn(
                String.valueOf(MeResponse.builder()
                        .userId("testUserId")
                        .nickname("testNickname")
                        .imageUrl("http://example.com/default.jpg")
                        .build()));

        // when // then
        mockMvc.perform(
                        delete("/api/v1/members/profile")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.status").value("OK"));
    }

    @DisplayName("AI 댓글 기능을 활성화한다.")
    @Test
    void enableAiComment() throws Exception {
        // given
        AuthenticateUser user = mock(AuthenticateUser.class);

        // when // then
        mockMvc.perform(
                        post("/api/v1/members/ai-comment/enable")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.status").value("OK"));
    }

    @DisplayName("AI 댓글 기능을 비활성화한다.")
    @Test
    void disableAiComment() throws Exception {
        // given
        AuthenticateUser user = mock(AuthenticateUser.class);

        // when // then
        mockMvc.perform(
                        post("/api/v1/members/ai-comment/disable")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.status").value("OK"));
    }
}
