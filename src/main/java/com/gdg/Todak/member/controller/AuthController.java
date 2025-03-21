package com.gdg.Todak.member.controller;

import com.gdg.Todak.common.domain.ApiResponse;
import com.gdg.Todak.member.controller.request.UpdateAccessTokenRequest;
import com.gdg.Todak.member.domain.Jwt;
import com.gdg.Todak.member.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@RestController
@Tag(name = "인증", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/refresh")
    @Operation(summary = "액세스 토큰 갱신", description = "리프레시 토큰이 유효하다면 액세스 토큰을 갱신한다.")
    public ApiResponse<Jwt> updateAccessTokenToken(@RequestBody UpdateAccessTokenRequest request) {
        return ApiResponse.ok(authService.updateAccessToken(request.toServiceRequest()));
    }

}
