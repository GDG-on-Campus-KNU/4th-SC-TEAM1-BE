package com.gdg.Todak.diary.controller;

import com.gdg.Todak.common.domain.ApiResponse;
import com.gdg.Todak.diary.dto.CommentRequest;
import com.gdg.Todak.diary.dto.CommentResponse;
import com.gdg.Todak.diary.service.CommentService;
import com.gdg.Todak.member.domain.AuthenticateUser;
import com.gdg.Todak.member.resolver.Login;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comments")
@Tag(name = "댓글", description = "댓글 관련 API")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/{diaryId}")
    @Operation(summary = "댓글 조회", description = "특정 게시글의 댓글 페이지네이션 조회, 본인 또는 친구의 일기의 댓글만 조회 가능")
    @Parameters({
            @Parameter(in = ParameterIn.QUERY, name = "page", description = "페이지 번호 (0부터 시작)", example = "0", schema = @Schema(type = "integer", defaultValue = "0")),
            @Parameter(in = ParameterIn.QUERY, name = "size", description = "페이지 크기", example = "10", schema = @Schema(type = "integer", defaultValue = "10")),
            @Parameter(in = ParameterIn.QUERY, name = "sort", description = "정렬 기준 (속성,오름차순|내림차순)", example = "createdAt,desc", schema = @Schema(type = "string"))
    })
    public ApiResponse<Page<CommentResponse>> getComments(@PathVariable("diaryId") Long diaryId,
                                                          @Parameter(hidden = true) @Login AuthenticateUser authenticateUser,
                                                          @Parameter(hidden = true) @PageableDefault Pageable pageable) {
        Page<CommentResponse> commentResponses = commentService.getComments(authenticateUser.getUserId(), diaryId, pageable);
        return ApiResponse.ok(commentResponses);
    }

    @PostMapping("/{diaryId}")
    @Operation(summary = "댓글 작성", description = "댓글 달기, 본인 또는 친구의 일기에만 작성 가능")
    public ApiResponse<Void> saveComment(@RequestBody CommentRequest commentRequest, @PathVariable("diaryId") Long diaryId,
                                         @Parameter(hidden = true) @Login AuthenticateUser authenticateUser) {
        commentService.saveComment(authenticateUser.getUserId(), diaryId, commentRequest);
        return ApiResponse.of(HttpStatus.OK, "저장되었습니다.");
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "댓글 수정", description = "댓글 수정하기")
    public ApiResponse<Void> updateComment(@RequestBody CommentRequest commentRequest, @PathVariable("commentId") Long commentId,
                                           @Parameter(hidden = true) @Login AuthenticateUser authenticateUser) {
        commentService.updateComment(authenticateUser.getUserId(), commentId, commentRequest);
        return ApiResponse.of(HttpStatus.OK, "수정되었습니다.");
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "댓글 삭제", description = "댓글 삭제하기")
    public ApiResponse<Void> deleteComment(@PathVariable("commentId") Long commentId,
                                           @Parameter(hidden = true) @Login AuthenticateUser authenticateUser) {
        commentService.deleteComment(authenticateUser.getUserId(), commentId);
        return ApiResponse.of(HttpStatus.OK, "삭제되었습니다.");
    }
}
