package com.gdg.Todak.tree.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdg.Todak.member.Interceptor.AdminLoginCheckInterceptor;
import com.gdg.Todak.member.Interceptor.LoginCheckInterceptor;
import com.gdg.Todak.member.domain.AuthenticateUser;
import com.gdg.Todak.member.domain.Role;
import com.gdg.Todak.member.resolver.LoginMemberArgumentResolver;
import com.gdg.Todak.tree.business.TreeService;
import com.gdg.Todak.tree.business.dto.GrowthButtonRequest;
import com.gdg.Todak.tree.business.dto.TreeInfoResponse;
import com.gdg.Todak.tree.domain.GrowthButton;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TreeController.class)
class TreeControllerTest {

    private final String token = "testToken";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TreeService treeService;

    @MockitoBean
    private LoginCheckInterceptor loginCheckInterceptor;

    @MockitoBean
    private AdminLoginCheckInterceptor adminLoginCheckInterceptor;

    @MockitoBean
    private LoginMemberArgumentResolver loginMemberArgumentResolver;

    private AuthenticateUser authenticateUser;

    @BeforeEach
    void setUp() throws Exception {
        authenticateUser = new AuthenticateUser("testUser", Set.of(Role.USER));
        when(loginCheckInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        when(loginMemberArgumentResolver.supportsParameter(any())).thenReturn(true);
        when(loginMemberArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(authenticateUser);
    }

    @Test
    @DisplayName("성장 버튼 구매 및 사용 API 테스트")
    void buyAndUseGrowthButtonTest() throws Exception {
        // given
        GrowthButtonRequest request = new GrowthButtonRequest(GrowthButton.WATER);
        given(treeService.earnExperience(anyString(), any(GrowthButton.class))).willReturn("정상적으로 경험치를 획득하였습니다.");

        // when & then
        mockMvc.perform(post("/api/v1/tree")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("정상적으로 경험치를 획득하였습니다."));

        verify(treeService).earnExperience("testUser", GrowthButton.WATER);
    }

    @Test
    @DisplayName("트리 정보 조회 API 테스트")
    void getTreeInfoTest() throws Exception {
        // given
        TreeInfoResponse response = TreeInfoResponse.create(3, 150);
        given(treeService.getTreeInfo(anyString())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/tree")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data.level").value(3))
                .andExpect(jsonPath("$.data.experience").value(150));

        verify(treeService).getTreeInfo("testUser");
    }

    @Test
    @DisplayName("유효하지 않은 성장 버튼 요청 시 실패 테스트")
    void buyAndUseInvalidGrowthButtonTest() throws Exception {
        // given
        String invalidRequest = "{\"growthButton\": \"INVALID_BUTTON\"}";

        // when & then
        mockMvc.perform(post("/api/v1/tree")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }
}
