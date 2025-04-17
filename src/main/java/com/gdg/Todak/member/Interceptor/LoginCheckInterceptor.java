package com.gdg.Todak.member.Interceptor;

import com.gdg.Todak.member.exception.UnauthorizedException;
import com.gdg.Todak.member.util.JwtConstants;
import com.gdg.Todak.member.util.JwtProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.gdg.Todak.member.util.JwtConstants.*;

@RequiredArgsConstructor
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    private final JwtProvider jwtProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {

        String header = request.getHeader(AUTHORIZATION);
        if (isNotValidToken(header)) {
            throw new UnauthorizedException("토큰이 없거나, 헤더 형식에 맞지 않습니다.");
        }

        if (isNotValidClaim(header)) {
            throw new UnauthorizedException("유효하지 않은 토큰입니다.");
        }

        return true;
    }

    private boolean isNotValidToken(String header) {
        return header == null || !header.startsWith(BEARER);
    }

    private boolean isNotValidClaim(String header) {
        String token = header.substring(7);
        Claims claims = jwtProvider.getClaims(token);
        return claims == null;
    }
}
