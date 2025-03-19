package com.gdg.Todak.member.controller;

import com.gdg.Todak.common.domain.ApiResponse;
import com.gdg.Todak.member.controller.request.CheckUsernameRequest;
import com.gdg.Todak.member.controller.request.LoginRequest;
import com.gdg.Todak.member.controller.request.LogoutRequest;
import com.gdg.Todak.member.controller.request.SignupRequest;
import com.gdg.Todak.member.domain.AuthenticateUser;
import com.gdg.Todak.member.domain.Jwt;
import com.gdg.Todak.member.resolver.Login;
import com.gdg.Todak.member.service.MemberService;
import com.gdg.Todak.member.service.response.CheckUsernameServiceResponse;
import com.gdg.Todak.member.service.response.LogoutResponse;
import com.gdg.Todak.member.service.response.MeResponse;
import com.gdg.Todak.member.service.response.MemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/users")
@RestController
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/check-username")
    public ApiResponse<CheckUsernameServiceResponse> checkUsername(
            @RequestBody CheckUsernameRequest request) {
        return ApiResponse.ok(memberService.checkUsername(request.toServiceRequest()));
    }

    @PostMapping("/signup")
    public ApiResponse<MemberResponse> signup(@RequestBody @Validated SignupRequest request) {
        return ApiResponse.ok(memberService.signup(request.toServiceRequest()));
    }

    @PostMapping("/login")
    public ApiResponse<Jwt> login(@RequestBody LoginRequest request) {
        return ApiResponse.ok(memberService.login(request.toServiceRequest()));
    }

    @PostMapping("/logout")
    public ApiResponse<LogoutResponse> logout(@Login AuthenticateUser user,
                                              @RequestBody LogoutRequest request) {
        return ApiResponse.ok(memberService.logout(user, request.toServiceRequest()));
    }

    @PostMapping("/me")
    public ApiResponse<MeResponse> me(@Login AuthenticateUser user) {
        return ApiResponse.ok(memberService.me(user));
    }
}
