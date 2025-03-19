package com.gdg.Todak.member.controller;

import com.gdg.Todak.common.domain.ApiResponse;
import com.gdg.Todak.member.controller.request.UpdateAccessTokenRequest;
import com.gdg.Todak.member.domain.Jwt;
import com.gdg.Todak.member.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController {

    private final AuthService authService;

    @PostMapping("/refresh")
    public ApiResponse<Jwt> updateRefreshToken(@RequestBody UpdateAccessTokenRequest request) {
        return ApiResponse.ok(authService.updateAccessToken(request.toServiceRequest()));
    }

}
