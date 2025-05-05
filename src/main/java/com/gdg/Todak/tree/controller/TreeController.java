package com.gdg.Todak.tree.controller;

import com.gdg.Todak.common.domain.ApiResponse;
import com.gdg.Todak.member.domain.AuthenticateUser;
import com.gdg.Todak.member.resolver.Login;
import com.gdg.Todak.tree.business.TreeService;
import com.gdg.Todak.tree.business.dto.GrowthButtonRequest;
import com.gdg.Todak.tree.business.dto.TreeInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tree")
@Tag(name = "Tree", description = "Tree 관련 API")
public class TreeController {

    private final TreeService treeService;

    @Operation(summary = "트리 성장", description = "사용자가 포인트를 사용하여 트리 성장 버튼을 구매해 경험치를 획득합니다. 버튼은 WATER, SUN, NUTRIENT 3가지")
    @PostMapping
    public ApiResponse<Void> BuyAndUseGrowthButton(@Parameter(hidden = true) @Login AuthenticateUser authenticateUser,
                                                   @RequestBody GrowthButtonRequest growthButtonRequest) {
        String response = treeService.earnExperience(authenticateUser.getUserId(), growthButtonRequest.growthButton());
        return ApiResponse.ok(response);
    }

    @Operation(summary = "트리 정보 조회", description = "트리의 레벨, 경험치를 조회합니다.")
    @GetMapping
    public ApiResponse<TreeInfoResponse> getTreeInfo(@Parameter(hidden = true) @Login AuthenticateUser authenticateUser) {
        TreeInfoResponse treeInfoResponse = treeService.getTreeInfo(authenticateUser.getUserId());
        return ApiResponse.ok(treeInfoResponse);
    }
}
