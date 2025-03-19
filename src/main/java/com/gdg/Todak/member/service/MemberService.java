package com.gdg.Todak.member.service;

import com.gdg.Todak.member.domain.*;
import com.gdg.Todak.member.exception.UnauthorizedException;
import com.gdg.Todak.member.repository.MemberRepository;
import com.gdg.Todak.member.repository.MemberRoleRepository;
import com.gdg.Todak.member.service.request.CheckUsernameServiceRequest;
import com.gdg.Todak.member.service.request.LoginServiceRequest;
import com.gdg.Todak.member.service.request.LogoutServiceRequest;
import com.gdg.Todak.member.service.request.SignupServiceRequest;
import com.gdg.Todak.member.service.response.CheckUsernameServiceResponse;
import com.gdg.Todak.member.service.response.LogoutResponse;
import com.gdg.Todak.member.service.response.MeResponse;
import com.gdg.Todak.member.service.response.MemberResponse;
import com.gdg.Todak.member.util.JwtProvider;
import com.gdg.Todak.member.util.PasswordEncoder;
import lombok.RequiredArgsConstructor;
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

    public CheckUsernameServiceResponse checkUsername(CheckUsernameServiceRequest serviceRequest) {
        Optional<Member> findMember = memberRepository.findByUsername(serviceRequest.getUsername());
        return CheckUsernameServiceResponse.of(findMember.isPresent());
    }

    @Transactional
    public MemberResponse signup(SignupServiceRequest request) {

        String salt = PasswordEncoder.getSalt();

        String encodedPassword = PasswordEncoder.getEncodedPassword(salt, request.getPassword());

        Member member = memberRepository.save(
                Member.of(request.getUsername(), encodedPassword, null, salt));

        MemberRole role = MemberRole.of(Role.USER, member);
        member.addRole(role);

        memberRoleRepository.save(role);

        return MemberResponse.of(member.getUsername());
    }

    public Jwt login(LoginServiceRequest request) {

        Member member = findMember(request.getUsername());

        checkPassword(request, member);

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
        Member member = findMember(user.getUsername());
        return MeResponse.of(member);
    }

    private Member findMember(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("멤버가 존재하지 않습니다."));
    }

    private static void checkPassword(LoginServiceRequest request, Member member) {
        String encodedPassword = PasswordEncoder.getEncodedPassword(member.getSalt(),
                request.getPassword());
        if (!encodedPassword.equals(member.getPassword())) {
            throw new UnauthorizedException("비밀번호가 올바르지 않습니다.");
        }
    }

    private void saveRefreshToken(String refreshToken, Member member) {
        redisTemplate.opsForValue().set(refreshToken, member.getId(), 14, TimeUnit.DAYS);
    }
}
