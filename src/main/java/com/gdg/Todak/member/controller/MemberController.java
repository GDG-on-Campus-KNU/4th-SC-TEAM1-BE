package com.gdg.Todak.member.controller;

import com.gdg.Todak.common.domain.ApiResponse;
import com.gdg.Todak.member.controller.request.*;
import com.gdg.Todak.member.domain.AuthenticateUser;
import com.gdg.Todak.member.domain.Jwt;
import com.gdg.Todak.member.resolver.Login;
import com.gdg.Todak.member.service.LoginResponse;
import com.gdg.Todak.member.service.MemberService;
import com.gdg.Todak.member.service.response.CheckUserIdServiceResponse;
import com.gdg.Todak.member.service.response.LogoutResponse;
import com.gdg.Todak.member.service.response.MeResponse;
import com.gdg.Todak.member.service.response.MemberResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
@RestController
@Tag(name = "멤버", description = "멤버 관련 API")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/check-userId")
    @Operation(summary = "유저 아이디 중복 체크", description = "유저 아이디가 존재하는지 체크한다. 존재하면 True, 존재하지 않으면 False를 반환한다.")
    public ApiResponse<CheckUserIdServiceResponse> checkUserId(
            @RequestBody CheckUserIdRequest request) {
        return ApiResponse.ok(memberService.checkUserId(request.toServiceRequest()));
    }

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "유저 아이디, 비밀번호 그리고 닉네임으로 회원가입한다. 유저 아이디는 3글자 이상 20글자 이하, 비밀번호는 8글자 이상 16글자 이하이다.")
    public ApiResponse<MemberResponse> signup(@RequestBody @Validated SignupRequest request) {
        return ApiResponse.ok(memberService.signup(request.toServiceRequest()));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "유저 아이디와 비밀번호로 로그인한다. 성공시 accessToken과 refreshToken이 반환된다.")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.ok(memberService.login(request.toServiceRequest()));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "리프레시 토큰을 삭제하여 로그아웃한다.")
    public ApiResponse<LogoutResponse> logout(@Parameter(hidden = true) @Login AuthenticateUser user,
                                              @RequestBody LogoutRequest request) {
        return ApiResponse.ok(memberService.logout(user, request.toServiceRequest()));
    }

    @PostMapping("/me")
    @Operation(summary = "마이페이지", description = "로그인이 된 유저 아이디, 유저 닉네임 그리고 프로필 사진 url을 반환한다.")
    public ApiResponse<MeResponse> me(@Parameter(hidden = true) @Login AuthenticateUser user) {
        return ApiResponse.ok(memberService.me(user));
    }

    @PutMapping("/edit")
    @Operation(summary = "내 정보 수정", description = "유저 닉네임 혹은 프로필 사진 url을 수정한다.")
    public ApiResponse<MeResponse> editMember(@Parameter(hidden = true) @Login AuthenticateUser user, @RequestBody EditMemberRequest request) {
        return ApiResponse.ok(memberService.editMemberInfo(user, request.toServiceRequest()));
    }

    @PutMapping("/edit-password")
    @Operation(summary = "비밀번호 수정", description = "비밀번호를 수정한다.")
    public ApiResponse<String> changePassword(@Parameter(hidden = true) @Login AuthenticateUser user, @RequestBody ChangePasswordRequest request) {
        return ApiResponse.ok(memberService.changePassword(user, request.toServiceRequest()));
    }

    @DeleteMapping("/me")
    @Operation(summary = "계정 탈퇴", description = "회원 정보를 삭제하고 계정을 탈퇴한다.")
    public ApiResponse<Void> deleteMember(@Parameter(hidden = true) @Login AuthenticateUser user) {
        return ApiResponse.ok(memberService.deleteMember(user));
    }
}
