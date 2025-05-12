package com.gdg.Todak.diary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdg.Todak.diary.dto.CommentRequest;
import com.gdg.Todak.diary.dto.CommentResponse;
import com.gdg.Todak.diary.service.CommentService;
import com.gdg.Todak.member.Interceptor.AdminLoginCheckInterceptor;
import com.gdg.Todak.member.Interceptor.LoginCheckInterceptor;
import com.gdg.Todak.member.repository.MemberRepository;
import com.gdg.Todak.member.resolver.LoginMemberArgumentResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    private final String token = "testToken";

    @MockitoBean
    private CommentService commentService;

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
    void setUp() {
        when(loginCheckInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @DisplayName("댓글 작성 테스트")
    @Test
    void saveCommentTest() throws Exception {
        // given
        CommentRequest commentRequest = new CommentRequest("테스트 댓글");
        doNothing().when(commentService).saveComment(anyString(), any(), any(CommentRequest.class));

        // when
        mockMvc.perform(post("/api/v1/comments/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequest)))

                // then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("저장되었습니다."));
    }

    @DisplayName("댓글 조회 테스트")
    @Test
    void getCommentsTest() throws Exception {
        // given
        CommentResponse comment1 = new CommentResponse(1L, LocalDateTime.of(2025, 3, 31, 0, 0, 0), LocalDateTime.of(2025, 3, 31, 0, 0, 0), "testMember1", "testMember1", "test", true, false);
        CommentResponse comment2 = new CommentResponse(2L, LocalDateTime.of(2025, 3, 31, 0, 0, 0), LocalDateTime.of(2025, 3, 31, 0, 0, 0), "testMember2", "testMember2", "test", false, true);
        Page<CommentResponse> commentPage = new PageImpl<>(List.of(comment1, comment2), PageRequest.of(0, 10), 2);

        when(commentService.getComments(any(), anyLong(), any(Pageable.class)))
                .thenReturn(commentPage);

        // when
        mockMvc.perform(get("/api/v1/comments/1")
                        .header("Authorization", "Bearer " + token))

                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].userId").value("testMember1"))
                .andExpect(jsonPath("$.data.content[1].id").value(2))
                .andExpect(jsonPath("$.data.content[1].userId").value("testMember2"));
    }

    @DisplayName("댓글 수정 테스트")
    @Test
    void updateCommentTest() throws Exception {
        // given
        CommentRequest commentRequest = new CommentRequest("수정된 댓글");
        doNothing().when(commentService).updateComment(anyString(), any(), any(CommentRequest.class));

        // when
        mockMvc.perform(put("/api/v1/comments/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequest)))

                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("수정되었습니다."));
    }

    @Test
    @DisplayName("댓글 삭제 테스트")
    void deleteDiaryTest() throws Exception {
        // given
        doNothing().when(commentService).deleteComment(anyString(), anyLong());

        // when
        mockMvc.perform(delete("/api/v1/comments/1")
                        .header("Authorization", "Bearer " + token))

                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("삭제되었습니다."));
    }
}
