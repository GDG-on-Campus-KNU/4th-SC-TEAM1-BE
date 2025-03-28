package com.gdg.Todak.member.Interceptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdg.Todak.member.domain.AuthenticateUser;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.domain.Role;
import com.gdg.Todak.member.exception.UnauthorizedException;
import com.gdg.Todak.member.repository.MemberRepository;
import com.gdg.Todak.member.util.JwtProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    public static final String BEARER = "Bearer ";

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;
    private final MemberRepository memberRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {

        String header = request.getHeader("Authorization");

        if (isNotValidToken(header)) {
            throw new UnauthorizedException("토큰이 없거나, 헤더 형식에 맞지 않습니다.");
        }

        String token = getToken(header);
        Claims claims = jwtProvider.getClaims(token);
        if (claims == null) {
            throw new UnauthorizedException("유효하지 않은 토큰입니다.");
        }

        AuthenticateUser authenticateUser = getAuthenticateUser(claims);

        String userId = authenticateUser.getUserId();
        Set<Role> roles = authenticateUser.getRoles();

        Optional<Member> findMember = memberRepository.findByUserId(userId);
        if (findMember.isEmpty()) {
            throw new UnauthorizedException("해당 사용자가 존재하지 않습니다.");
        }

        if (invalidMemberRoles(findMember, roles)) {
            throw new UnauthorizedException("사용자의 권한이 올바르지 않습니다.");
        }

        return true;
    }

    private static boolean isNotValidToken(String header) {
        return header == null || !header.startsWith(BEARER);
    }

    private static String getToken(String header) {
        return header.substring(7);
    }

    private static boolean invalidMemberRoles(Optional<Member> findMember, Set<Role> roles) {
        return !findMember.get().getRoles().containsAll(roles);
    }

    private AuthenticateUser getAuthenticateUser(Claims claims) {
        try {
            String json = claims.get(jwtProvider.AUTHENTICATE_USER).toString();
            return objectMapper.readValue(json, AuthenticateUser.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("토큰에서 사용자 정보를 변환할 수 없습니다.", e);
        }
    }
}
