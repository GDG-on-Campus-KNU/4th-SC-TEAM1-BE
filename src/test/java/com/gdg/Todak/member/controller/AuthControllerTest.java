package com.gdg.Todak.member.controller;

import com.gdg.Todak.member.controller.request.UpdateAccessTokenRequest;
import com.gdg.Todak.member.domain.Jwt;
import com.gdg.Todak.member.service.request.UpdateAccessTokenServiceRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends ControllerTestSupport {

    @DisplayName("유효한 리프레시 토큰으로 요청 시 새로운 액세스 토큰이 발급된다")
    @Test
    void updateRefreshTokenTest() throws Exception {
        // given
        String refreshToken = "refresh_token";

        UpdateAccessTokenRequest request = UpdateAccessTokenRequest.builder()
                .refreshToken(refreshToken)
                .build();

        Jwt jwt = Jwt.builder()
                .accessToken("new_access_token")
                .refreshToken(refreshToken)
                .build();

        // when
        when(authService.updateAccessToken(any(UpdateAccessTokenServiceRequest.class))).thenReturn(jwt);

        // then
        mockMvc.perform(
                        post("/api/auth/refresh")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("OK"));
    }
}