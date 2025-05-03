package com.gdg.Todak.point.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdg.Todak.member.Interceptor.AdminLoginCheckInterceptor;
import com.gdg.Todak.member.Interceptor.LoginCheckInterceptor;
import com.gdg.Todak.member.domain.AuthenticateUser;
import com.gdg.Todak.member.domain.Role;
import com.gdg.Todak.member.repository.MemberRepository;
import com.gdg.Todak.member.resolver.LoginMemberArgumentResolver;
import com.gdg.Todak.point.PointStatus;
import com.gdg.Todak.point.PointType;
import com.gdg.Todak.point.dto.PointLogResponse;
import com.gdg.Todak.point.dto.PointResponse;
import com.gdg.Todak.point.service.PointLogService;
import com.gdg.Todak.point.service.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PointController.class)
class PointControllerTest {

    private final String token = "testToken";

    @MockitoBean
    private PointService pointService;

    @MockitoBean
    private PointLogService pointLogService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoginCheckInterceptor loginCheckInterceptor;

    @MockitoBean
    private LoginMemberArgumentResolver loginMemberArgumentResolver;

    @MockitoBean
    private AdminLoginCheckInterceptor adminLoginCheckInterceptor;

    @MockitoBean
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() throws Exception {
        when(loginCheckInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    @DisplayName("포인트 로그 조회 테스트")
    void getPointLogTest() throws Exception {
        // given
        PointLogResponse pointLogResponse1 = new PointLogResponse(PointType.ATTENDANCE_DAY_1, PointStatus.EARNED, LocalDateTime.now(), 10);
        PointLogResponse pointLogResponse2 = new PointLogResponse(PointType.DIARY, PointStatus.EARNED, LocalDateTime.now(), 15);

        Page<PointLogResponse> pointLogPage = new PageImpl<>(List.of(pointLogResponse1, pointLogResponse2), PageRequest.of(0, 10), 2);

        when(pointLogService.getPointLogList(anyString(), any(Pageable.class)))
                .thenReturn(pointLogPage);

        // when
        mockMvc.perform(get("/api/v1/points/log")
                        .header("Authorization", "Bearer " + token))

                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("OK"));
    }

    @Test
    @DisplayName("포인트 조회 테스트")
    void getMemberPointTest() throws Exception {
        // given
        String userId = "testUser";
        AuthenticateUser authenticateUser = new AuthenticateUser(userId, Set.of(Role.USER));

        when(loginMemberArgumentResolver.supportsParameter(any())).thenReturn(true);
        when(loginMemberArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(authenticateUser);

        PointResponse pointResponse = new PointResponse(100);
        when(pointService.getPoint(anyString())).thenReturn(pointResponse);

        // when
        mockMvc.perform(get("/api/v1/points")
                        .header("Authorization", "Bearer " + token))

                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.point").value(100));
    }
}
