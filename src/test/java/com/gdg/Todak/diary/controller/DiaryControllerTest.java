package com.gdg.Todak.diary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdg.Todak.diary.Emotion;
import com.gdg.Todak.diary.dto.DiaryDetailResponse;
import com.gdg.Todak.diary.dto.DiaryRequest;
import com.gdg.Todak.diary.dto.DiarySummaryResponse;
import com.gdg.Todak.diary.dto.DiaryUpdateRequest;
import com.gdg.Todak.diary.service.DiaryService;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DiaryController.class)
class DiaryControllerTest {

    private final String token = "testToken";
    private final String storageUUID = "testUUID";

    @MockitoBean
    private DiaryService diaryService;

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

        String userId = "testUser";
        AuthenticateUser authenticateUser = new AuthenticateUser(userId, Set.of(Role.USER));

        when(loginMemberArgumentResolver.supportsParameter(any())).thenReturn(true);
        when(loginMemberArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(authenticateUser);
    }

    @Test
    @DisplayName("일기 작성 테스트")
    void writeDiaryTest() throws Exception {
        // given
        DiaryRequest request = new DiaryRequest("오늘은 좋은 하루였다.", Emotion.HAPPY, storageUUID);
        doNothing().when(diaryService).writeDiary(anyString(), any(DiaryRequest.class));

        // when
        mockMvc.perform(post("/api/v1/diary")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("작성되었습니다."));
    }

    @Test
    @DisplayName("본인의 년/월에 해당하는 모든 일기 요약 조회 테스트")
    void getOwnAllDiaryTest() throws Exception {
        // given
        List<DiarySummaryResponse> responses = Arrays.asList(
                new DiarySummaryResponse(1L, LocalDate.of(2025, 3, 1), Emotion.HAPPY),
                new DiarySummaryResponse(2L, LocalDate.of(2025, 3, 2), Emotion.SAD)
        );
        when(diaryService.getMySummaryByYearAndMonth(anyString(), any())).thenReturn(responses);

        // when
        mockMvc.perform(get("/api/v1/diary/me/2025/3")
                        .header("Authorization", "Bearer " + token))

                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].diaryId").value(1))
                .andExpect(jsonPath("$.data[0].createdAt").value("2025-03-01"))
                .andExpect(jsonPath("$.data[0].emotion").value("HAPPY"))
                .andExpect(jsonPath("$.data[1].diaryId").value(2))
                .andExpect(jsonPath("$.data[1].createdAt").value("2025-03-02"))
                .andExpect(jsonPath("$.data[1].emotion").value("SAD"));
    }

    @Test
    @DisplayName("친구의 년/월에 해당하는 모든 일기 요약 조회 테스트")
    void getAllDiaryByFriendTest() throws Exception {
        // given
        List<DiarySummaryResponse> responses = Arrays.asList(
                new DiarySummaryResponse(1L, LocalDate.of(2025, 3, 1), Emotion.HAPPY),
                new DiarySummaryResponse(2L, LocalDate.of(2025, 3, 2), Emotion.SAD)
        );
        when(diaryService.getFriendSummaryByYearAndMonth(anyString(), anyString(), any())).thenReturn(responses);

        // when
        mockMvc.perform(get("/api/v1/diary/friend/friendId/2025/3")
                        .header("Authorization", "Bearer " + token))

                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].diaryId").value(1))
                .andExpect(jsonPath("$.data[0].createdAt").value("2025-03-01"))
                .andExpect(jsonPath("$.data[0].emotion").value("HAPPY"))
                .andExpect(jsonPath("$.data[1].diaryId").value(2))
                .andExpect(jsonPath("$.data[1].createdAt").value("2025-03-02"))
                .andExpect(jsonPath("$.data[1].emotion").value("SAD"));
    }

    @Test
    @DisplayName("일기 상세보기 테스트")
    void getDiaryTest() throws Exception {
        // given
        DiaryDetailResponse response = new DiaryDetailResponse(1L, LocalDateTime.of(2025, 3, 1, 12, 12, 0, 0), "오늘은 기쁜 날이다", Emotion.HAPPY, storageUUID, true);
        when(diaryService.readDiary(anyString(), anyLong())).thenReturn(response);

        // when
        mockMvc.perform(get("/api/v1/diary/1")
                        .header("Authorization", "Bearer " + token))

                // then
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.diaryId").value(1))
                .andExpect(jsonPath("$.data.createdAt").value("2025-03-01T12:12:00"))
                .andExpect(jsonPath("$.data.content").value("오늘은 기쁜 날이다"))
                .andExpect(jsonPath("$.data.emotion").value("HAPPY"))
                .andExpect(jsonPath("$.data.isWriter").value(true));
    }

    @Test
    @DisplayName("일기 수정 테스트")
    void updateDiaryTest() throws Exception {
        // given
        DiaryUpdateRequest request = new DiaryUpdateRequest("오늘은 슬펐다.", Emotion.SAD);
        doNothing().when(diaryService).updateDiary(anyString(), anyLong(), any(DiaryUpdateRequest.class));

        // when
        mockMvc.perform(put("/api/v1/diary/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("수정되었습니다."));
    }

    @Test
    @DisplayName("일기 삭제 테스트")
    void deleteDiaryTest() throws Exception {
        // given
        doNothing().when(diaryService).deleteDiary(anyString(), anyLong());

        // when
        mockMvc.perform(delete("/api/v1/diary/1")
                        .header("Authorization", "Bearer " + token))

                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("삭제되었습니다."));
    }
}
