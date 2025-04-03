package com.gdg.Todak.member.service;

import com.gdg.Todak.member.domain.*;
import com.gdg.Todak.member.exception.UnauthorizedException;
import com.gdg.Todak.member.repository.MemberRepository;
import com.gdg.Todak.member.repository.MemberRoleRepository;
import com.gdg.Todak.member.service.request.*;
import com.gdg.Todak.member.service.response.CheckUserIdServiceResponse;
import com.gdg.Todak.member.service.response.LogoutResponse;
import com.gdg.Todak.member.service.response.MeResponse;
import com.gdg.Todak.member.service.response.MemberResponse;
import com.gdg.Todak.member.util.JwtProvider;
import com.gdg.Todak.member.util.PasswordEncoder;
import com.gdg.Todak.point.service.PointCreateService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberRoleRepository memberRoleRepository;
    private final JwtProvider jwtProvider;
    private final RedisTemplate redisTemplate;
    private final PointCreateService pointCreateService;

    @Value("${DEFAULT_PROFILE_IMAGE_URL}")
    private String defaultProfileImageUrl;

    public CheckUserIdServiceResponse checkUserId(CheckUserIdServiceRequest serviceRequest) {
        Optional<Member> findMember = memberRepository.findByUserId(serviceRequest.getUserId());
        return CheckUserIdServiceResponse.of(findMember.isPresent());
    }

    @Transactional
    public MemberResponse signup(SignupServiceRequest request) {

        if (!request.getPassword().equals(request.getPasswordCheck())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String salt = PasswordEncoder.getSalt();

        String encodedPassword = PasswordEncoder.getEncodedPassword(salt, request.getPassword());

        Member member = memberRepository.save(
                Member.of(request.getUserId(), encodedPassword, request.getNickname(), defaultProfileImageUrl, salt));

        MemberRole role = MemberRole.of(Role.USER, member);
        member.addRole(role);

        memberRoleRepository.save(role);

        pointCreateService.createPoint(member);

        return MemberResponse.of(member.getUserId());
    }

    public Jwt login(LoginServiceRequest request) {

        Member member = findMember(request.getUserId());

        checkPassword(request.getPassword(), member);

        Set<Role> roles = member.getRoles();

        Map<String, Object> claims = jwtProvider.createClaims(member, roles);

        String accessToken = jwtProvider.createAccessToken(claims);
        String refreshToken = jwtProvider.createRefreshToken();

        saveRefreshToken(refreshToken, member);

        return Jwt.of(accessToken, refreshToken);
    }

    public LogoutResponse logout(AuthenticateUser user, LogoutServiceRequest serviceRequest) {
        if (user != null) {
            redisTemplate.delete(serviceRequest.getRefreshToken());
        }

        String message = "성공적으로 로그아웃 되었습니다.";

        return LogoutResponse.of(message);
    }

    public MeResponse me(AuthenticateUser user) {
        Member member = findMember(user.getUserId());
        return MeResponse.of(member);
    }

    @Transactional
    public MeResponse editMemberInfo(AuthenticateUser user, EditMemberServiceRequest serviceRequest) {
        Member member = findMember(user.getUserId());
        member.setUserId(serviceRequest.getUserId());
        member.setNickname(serviceRequest.getNickname());
        member.setImageUrl(serviceRequest.getImageUrl());
        return MeResponse.of(member);
    }

    @Transactional
    public String changePassword(AuthenticateUser user, ChangePasswordServiceRequest request) {
        Member member = findMember(user.getUserId());

        checkPassword(request.getOldPassword(), member);

        if (!request.getNewPassword().equals(request.getNewPasswordCheck())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String salt = PasswordEncoder.getSalt();

        String encodedPassword = PasswordEncoder.getEncodedPassword(salt, request.getNewPassword());

        member.setPassword(encodedPassword);

        return "비밀번호가 변경되었습니다.";
    }

    @Transactional
    public String deleteMember(AuthenticateUser user) {
        Member member = findMember(user.getUserId());
        memberRepository.delete(member);
        return "회원이 삭제되었습니다.";
    }

    private Member findMember(String userId) {
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new UnauthorizedException("멤버가 존재하지 않습니다."));
    }

    private static void checkPassword(String password, Member member) {
        String encodedPassword = PasswordEncoder.getEncodedPassword(member.getSalt(),
                password);
        if (!encodedPassword.equals(member.getPassword())) {
            throw new UnauthorizedException("비밀번호가 올바르지 않습니다.");
        }
    }

    private void saveRefreshToken(String refreshToken, Member member) {
        redisTemplate.opsForValue().set(refreshToken, member.getId().toString(), 14, TimeUnit.DAYS);
    }
}
