package com.gdg.Todak.point.controller;

import com.gdg.Todak.common.domain.ApiResponse;
import com.gdg.Todak.member.domain.AuthenticateUser;
import com.gdg.Todak.member.resolver.Login;
import com.gdg.Todak.point.dto.PointLogResponse;
import com.gdg.Todak.point.dto.PointResponse;
import com.gdg.Todak.point.service.PointLogService;
import com.gdg.Todak.point.service.PointService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "포인트 조회", description = "포인트 조회 관련 API")
@RequestMapping("/api/v1/points")
public class PointController {

    private final PointLogService pointLogService;
    private final PointService pointService;

    @Operation(summary = "포인트 로그 조회", description = "로그인한 사용자의 포인트 로그를 조회하여 리스트형태로 받습니다.")
    @GetMapping("/log")
    @Parameters({
            @Parameter(in = ParameterIn.QUERY, name = "page", description = "페이지 번호 (0부터 시작)", example = "0", schema = @Schema(type = "integer", defaultValue = "0")),
            @Parameter(in = ParameterIn.QUERY, name = "size", description = "페이지 크기", example = "10", schema = @Schema(type = "integer", defaultValue = "10")),
            @Parameter(in = ParameterIn.QUERY, name = "sort", description = "정렬 기준 (속성,오름차순|내림차순)", example = "createdAt,desc", schema = @Schema(type = "string"))
    })
    public ApiResponse<Page<PointLogResponse>> getPointLog(@Parameter(hidden = true) @Login AuthenticateUser authenticateUser,
                                                           @Parameter(hidden = true) @PageableDefault Pageable pageable) {
        Page<PointLogResponse> pointLogResponses = pointLogService.getPointLogList(authenticateUser.getUserId(), pageable);
        return ApiResponse.ok(pointLogResponses);
    }

    @Operation(summary = "포인트 조회", description = "로그인한 사용자의 포인트를 조회합니다.")
    @GetMapping
    public ApiResponse<PointResponse> getMemberPoint(@Parameter(hidden = true) @Login AuthenticateUser authenticateUser) {
        PointResponse pointResponse = pointService.getPoint(authenticateUser.getUserId());
        return ApiResponse.ok(pointResponse);
    }
}
