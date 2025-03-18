package com.gdg.Todak.diary.controller;

import com.gdg.Todak.common.domain.ApiResponse;
import com.gdg.Todak.diary.dto.DiaryRequest;
import com.gdg.Todak.diary.dto.DiaryResponse;
import com.gdg.Todak.diary.dto.EmotionRequest;
import com.gdg.Todak.diary.service.DiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/diary")
@Tag(name = "일기", description = "일기 관련 API")
public class DiaryController {

    private final DiaryService diaryService;

    @PostMapping
    @Operation(summary = "일기 작성 / 감정 등록", description = "일기를 작성한다. 감정만 등록하기도 가능. 감정은 HAPPY, SAD, ANGRY, EXCITED, NEUTRAL")
    public ApiResponse<Void> writeDiary(@RequestBody DiaryRequest diaryRequest) {
        diaryService.writeDiary(diaryRequest);
        return ApiResponse.of(HttpStatus.CREATED, "작성되었습니다.");
    }

    @GetMapping("/all")
    @Parameters({
            @Parameter(in = ParameterIn.QUERY, name = "page", description = "페이지 번호 (0부터 시작)", example = "0", schema = @Schema(type = "integer", defaultValue = "0")),
            @Parameter(in = ParameterIn.QUERY, name = "size", description = "페이지 크기", example = "10", schema = @Schema(type = "integer", defaultValue = "10")),
            @Parameter(in = ParameterIn.QUERY, name = "sort", description = "정렬 기준 (속성,오름차순|내림차순)", example = "createdAt,desc", schema = @Schema(type = "string"))
    })
    @Operation(summary = "모든 일기 불러오기", description = "모든 일기를 불러온다 (페이징 처리)")
    public ApiResponse<Page<DiaryResponse>> getAllDiary(@Parameter(hidden = true)
                                                        @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
                                                        Pageable pageable) {
        Page<DiaryResponse> diaryResponses = diaryService.readAllDiary(pageable);
        return ApiResponse.ok(diaryResponses);
    }

    @GetMapping("/{diaryId}")
    @Operation(summary = "일기 상세보기", description = "diaryId에 해당하는 일기를 확인한다")
    public ApiResponse<DiaryResponse> getDiary(@PathVariable("diaryId") Long diaryId) {
        DiaryResponse diaryResponse = diaryService.readDiary(diaryId);
        return ApiResponse.ok(diaryResponse);
    }

    @PutMapping("/{diaryId}")
    @Operation(summary = "일기 수정하기", description = "diaryId에 해당하는 일기를 수정한다")
    public ApiResponse<Void> updateDiary(@PathVariable("diaryId") Long diaryId, @RequestBody DiaryRequest diaryRequest) {
        diaryService.updateDiary(diaryId, diaryRequest);
        return ApiResponse.of(HttpStatus.OK, "수정되었습니다.");
    }

    @PutMapping("/emotion/{diaryId}")
    @Operation(summary = "감정 수정하기", description = "diaryId에 해당하는 일기의 감정을 수정한다. 감정은 HAPPY, SAD, ANGRY, EXCITED, NEUTRAL")
    public ApiResponse<Void> updateEmotion(@PathVariable("diaryId") Long diaryId, @RequestBody EmotionRequest emotionRequest) {
        diaryService.updateDiaryEmotion(diaryId, emotionRequest);
        return ApiResponse.of(HttpStatus.OK, "수정되었습니다.");
    }

    @DeleteMapping("{diaryId}")
    @Operation(summary = "일기 삭제하기", description = "diaryId에 해당하는 일기를 삭제한다")
    public ApiResponse<Void> deteleDiary(@PathVariable("diaryId") Long diaryId) {
        diaryService.deleteDiary(diaryId);
        return ApiResponse.of(HttpStatus.OK, "삭제되었습니다.");
    }
}
