package com.gdg.Todak.member.controller;

import com.gdg.Todak.member.controller.request.LoginRequest;
import com.gdg.Todak.member.controller.request.SignupRequest;
import com.gdg.Todak.member.service.request.SignupServiceRequest;
import com.gdg.Todak.member.service.response.MemberResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class MemberControllerTest extends ControllerTestSupport {

    @DisplayName("회원가입을 한다.")
    @Test
    void signup() throws Exception {
        // given
        String username = "testUsername";
        String password = "testPassword";

        SignupRequest request = SignupRequest.builder()
                .username(username)
                .password(password)
                .build();

        when(memberService.signup(any(SignupServiceRequest.class))).thenReturn(
                MemberResponse.of(username));

        // when // then
        mockMvc.perform(
                        post("/api/users/signup")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("OK"));
    }

    @DisplayName("회원가입 시 username은 3글자 이상이어야 한다.")
    @Test
    void signupWithShortUsername() throws Exception {
        // given
        String username = "us";
        String password = "testPassword";

        SignupRequest request = SignupRequest.builder()
                .username(username)
                .password(password)
                .build();

        // when // then
        mockMvc.perform(
                        post("/api/users/signup")
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
        String username = "testUsername";
        String password = "passwor";

        SignupRequest request = SignupRequest.builder()
                .username(username)
                .password(password)
                .build();

        // when // then
        mockMvc.perform(
                        post("/api/users/signup")
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
        String username = "testUsername";
        String password = "testPassword";

        LoginRequest request = new LoginRequest(username, password);

        // when // then
        mockMvc.perform(
                        post("/api/users/login")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.status").value("OK"));
    }

}