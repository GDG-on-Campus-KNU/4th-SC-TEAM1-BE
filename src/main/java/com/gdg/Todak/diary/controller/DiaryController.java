package com.gdg.Todak.diary.controller;

import com.gdg.Todak.common.domain.ApiResponse;
import com.gdg.Todak.diary.dto.DiaryDetailResponse;
import com.gdg.Todak.diary.dto.DiaryRequest;
import com.gdg.Todak.diary.dto.DiarySearchRequest;
import com.gdg.Todak.diary.dto.DiarySummaryResponse;
import com.gdg.Todak.diary.service.DiaryService;
import com.gdg.Todak.member.domain.AuthenticateUser;
import com.gdg.Todak.member.resolver.Login;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/diary")
@Tag(name = "일기", description = "일기 관련 API")
public class DiaryController {

    private final DiaryService diaryService;

    @PostMapping
    @Operation(summary = "일기 작성 / 감정 등록", description = "일기를 작성한다. 감정만 등록하기도 가능. 감정은 HAPPY, SAD, ANGRY, EXCITED, NEUTRAL")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> writeDiary(@Parameter(hidden = true) @Login AuthenticateUser authenticateUser, @RequestBody DiaryRequest diaryRequest) {
        diaryService.writeDiary(authenticateUser.getUsername(), diaryRequest);
        return ApiResponse.of(HttpStatus.CREATED, "작성되었습니다.");
    }

    @GetMapping("/me/{year}/{month}")
    @Operation(summary = "본인의 년/월에 해당하는 모든 일기 불러오기", description = "본인이 작성한 일기 중 year, month에 해당하는 모든 일기를 불러온다.")
    public ApiResponse<List<DiarySummaryResponse>> getMyAllDiary(@Parameter(hidden = true) @Login AuthenticateUser authenticateUser, @PathVariable("year") int year, @PathVariable("month") int month) {
        DiarySearchRequest diarySearchRequest = new DiarySearchRequest(year, month);
        List<DiarySummaryResponse> diaryResponses = diaryService.getMySummaryByYearAndMonth(authenticateUser.getUsername(), diarySearchRequest);
        return ApiResponse.ok(diaryResponses);
    }

    @GetMapping("/friend/{friendName}/{year}/{month}")
    @Operation(summary = "친구의 년/월에 해당하는 모든 일기 불러오기", description = "친구가 작성한 일기 중 year, month에 해당하는 모든 일기를 불러온다.")
    public ApiResponse<List<DiarySummaryResponse>> getAllDiaryByFriend(@Parameter(hidden = true) @Login AuthenticateUser authenticateUser, @PathVariable("friendName") String friendName, @PathVariable("year") int year, @PathVariable("month") int month) {
        DiarySearchRequest diarySearchRequest = new DiarySearchRequest(year, month);
        List<DiarySummaryResponse> diaryResponses = diaryService.getFriendSummaryByYearAndMonth(authenticateUser.getUsername(), friendName, diarySearchRequest);
        return ApiResponse.ok(diaryResponses);
    }

    @GetMapping("/{diaryId}")
    @Operation(summary = "일기 상세보기", description = "본인 또는 친구의 diaryId에 해당하는 일기를 확인한다.")
    public ApiResponse<DiaryDetailResponse> getDiary(@Parameter(hidden = true) @Login AuthenticateUser authenticateUser, @PathVariable("diaryId") Long diaryId) {
        DiaryDetailResponse diaryDetailResponse = diaryService.readDiary(authenticateUser.getUsername(), diaryId);
        return ApiResponse.ok(diaryDetailResponse);
    }

    @PutMapping("/{diaryId}")
    @Operation(summary = "일기 수정하기", description = "diaryId에 해당하는 일기를 수정한다.")
    public ApiResponse<Void> updateDiary(@Parameter(hidden = true) @Login AuthenticateUser authenticateUser, @PathVariable("diaryId") Long diaryId, @RequestBody DiaryRequest diaryRequest) {
        diaryService.updateDiary(authenticateUser.getUsername(), diaryId, diaryRequest);
        return ApiResponse.of(HttpStatus.OK, "수정되었습니다.");
    }

    @DeleteMapping("{diaryId}")
    @Operation(summary = "일기 삭제하기", description = "diaryId에 해당하는 일기를 삭제한다.")
    public ApiResponse<Void> deleteDiary(@Parameter(hidden = true) @Login AuthenticateUser authenticateUser, @PathVariable("diaryId") Long diaryId) {
        diaryService.deleteDiary(authenticateUser.getUsername(), diaryId);
        return ApiResponse.of(HttpStatus.OK, "삭제되었습니다.");
    }
}
