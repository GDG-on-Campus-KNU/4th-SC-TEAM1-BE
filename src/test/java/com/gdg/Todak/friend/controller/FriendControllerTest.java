package com.gdg.Todak.friend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdg.Todak.friend.FriendStatus;
import com.gdg.Todak.friend.dto.*;
import com.gdg.Todak.friend.service.FriendService;
import com.gdg.Todak.member.Interceptor.AdminLoginCheckInterceptor;
import com.gdg.Todak.member.Interceptor.LoginCheckInterceptor;
import com.gdg.Todak.member.domain.AuthenticateUser;
import com.gdg.Todak.member.domain.Role;
import com.gdg.Todak.member.repository.MemberRepository;
import com.gdg.Todak.member.resolver.LoginMemberArgumentResolver;
import com.gdg.Todak.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FriendController.class)
class FriendControllerTest {

    private final String token = "testToken";
    @MockitoBean
    MemberService memberService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private FriendService friendService;
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

        String userId = "testUser";
        AuthenticateUser authenticateUser = new AuthenticateUser(userId, Set.of(Role.USER));

        when(loginMemberArgumentResolver.supportsParameter(any())).thenReturn(true);
        when(loginMemberArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(authenticateUser);
    }

    @Test
    @DisplayName("친구 요청 보내기 테스트")
    void sendFriendRequestTest() throws Exception {
        // given
        FriendIdRequest request = new FriendIdRequest("friendId");
        doNothing().when(friendService).makeFriendRequest(anyString(), any(FriendIdRequest.class));

        // when & then
        mockMvc.perform(post("/api/v1/friends")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("친구 요청이 생성되었습니다."));
    }

    @Test
    @DisplayName("친구 목록 조회 테스트")
    void getAllFriendsTest() throws Exception {
        // given
        String friend1Id = "friend1";
        String friend2Id = "friend2";
        List<FriendResponse> responses = Arrays.asList(
                new FriendResponse(1L, friend1Id),
                new FriendResponse(2L, friend2Id)
        );
        when(friendService.getAllFriend(anyString())).thenReturn(responses);

        // when & then
        mockMvc.perform(get("/api/v1/friends")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].friendId").value(friend1Id))
                .andExpect(jsonPath("$.data[1].friendId").value(friend2Id));
    }

    @Test
    @DisplayName("대기중인 친구 요청 조회 테스트")
    void getAllPendingFriendRequestTest() throws Exception {
        // given
        String requester1Id = "requester1";
        String requester2Id = "requester2";
        List<FriendRequestResponse> responses = Arrays.asList(
                new FriendRequestResponse(1L, requester1Id, "profile1"),
                new FriendRequestResponse(2L, requester2Id, "profile2")
        );
        when(friendService.getAllFriendRequests(anyString())).thenReturn(responses);

        // when & then
        mockMvc.perform(get("/api/v1/friends/pending")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].requesterName").value(requester1Id))
                .andExpect(jsonPath("$.data[1].requesterName").value(requester2Id));
    }

    @Test
    @DisplayName("친구 요청 수락 테스트")
    void acceptFriendRequestTest() throws Exception {
        // given
        Long friendRequestId = 1L;
        doNothing().when(friendService).acceptFriendRequest(anyString(), anyLong());

        // when & then
        mockMvc.perform(put("/api/v1/friends/accept/" + friendRequestId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("친구 요청이 수락되었습니다."));
    }

    @Test
    @DisplayName("친구 요청 거절 테스트")
    void declineFriendRequestTest() throws Exception {
        // given
        Long friendRequestId = 1L;
        doNothing().when(friendService).declineFriendRequest(anyString(), anyLong());

        // when & then
        mockMvc.perform(put("/api/v1/friends/decline/" + friendRequestId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("친구 요청을 거절하였습니다."));
    }

    @Test
    @DisplayName("친구 삭제 테스트")
    void deleteFriendTest() throws Exception {
        // given
        Long friendRequestId = 1L;
        doNothing().when(friendService).deleteFriend(anyString(), anyLong());

        // when & then
        mockMvc.perform(delete("/api/v1/friends/" + friendRequestId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("친구/친구요청을 삭제하였습니다."));
    }

    @Test
    @DisplayName("친구요청수 및 친구수 확인 테스트")
    void getMyFriendCountTest() throws Exception {
        // given
        Long pendingCount = 5L;
        Long acceptedCount = 10L;
        List<FriendCountResponse> responses = Arrays.asList(
                new FriendCountResponse(FriendStatus.PENDING, true, false, pendingCount),
                new FriendCountResponse(FriendStatus.ACCEPTED, true, true, acceptedCount)
        );
        when(friendService.getMyFriendCountByStatus(anyString())).thenReturn(responses);

        // when & then
        mockMvc.perform(get("/api/v1/friends/count")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].friendStatus").value("PENDING"))
                .andExpect(jsonPath("$.data[0].count").value(pendingCount))
                .andExpect(jsonPath("$.data[1].friendStatus").value("ACCEPTED"))
                .andExpect(jsonPath("$.data[1].count").value(acceptedCount));
    }

    @Test
    @DisplayName("보낸 친구 요청들 확인 테스트")
    void getAllPendingAndDeclinedFriendRequestByRequesterTest() throws Exception {
        // given
        List<FriendRequestWithStatusResponse> responses = Arrays.asList(
                new FriendRequestWithStatusResponse(1L, "testUser", "friend1", FriendStatus.PENDING),
                new FriendRequestWithStatusResponse(2L, "testUser", "friend2", FriendStatus.DECLINED)
        );
        when(friendService.getAllPendingAndDeclinedFriendRequestByRequester(anyString())).thenReturn(responses);

        // when & then
        mockMvc.perform(get("/api/v1/friends/requester")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].requesterName").value("testUser"))
                .andExpect(jsonPath("$.data[0].accepterName").value("friend1"))
                .andExpect(jsonPath("$.data[0].friendStatus").value("PENDING"))
                .andExpect(jsonPath("$.data[1].requesterName").value("testUser"))
                .andExpect(jsonPath("$.data[1].accepterName").value("friend2"))
                .andExpect(jsonPath("$.data[1].friendStatus").value("DECLINED"));
    }

    @Test
    @DisplayName("받은 친구 요청들 확인 테스트")
    void getAllPendingAndDeclinedFriendRequestByAccepterTest() throws Exception {
        // given
        List<FriendRequestWithStatusResponse> responses = Arrays.asList(
                new FriendRequestWithStatusResponse(1L, "friend1", "testUser", FriendStatus.PENDING),
                new FriendRequestWithStatusResponse(2L, "friend2", "testUser", FriendStatus.DECLINED)
        );
        when(friendService.getAllPendingAndDeclinedFriendRequestByAccepter(anyString())).thenReturn(responses);

        // when & then
        mockMvc.perform(get("/api/v1/friends/accepter")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].requesterName").value("friend1"))
                .andExpect(jsonPath("$.data[0].accepterName").value("testUser"))
                .andExpect(jsonPath("$.data[0].friendStatus").value("PENDING"))
                .andExpect(jsonPath("$.data[1].requesterName").value("friend2"))
                .andExpect(jsonPath("$.data[1].accepterName").value("testUser"))
                .andExpect(jsonPath("$.data[1].friendStatus").value("DECLINED"));
    }
}
