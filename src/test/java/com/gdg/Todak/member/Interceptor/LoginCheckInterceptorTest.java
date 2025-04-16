package com.gdg.Todak.member.Interceptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdg.Todak.member.domain.AuthenticateUser;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.domain.MemberRole;
import com.gdg.Todak.member.domain.Role;
import com.gdg.Todak.member.repository.MemberRepository;
import com.gdg.Todak.member.util.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginCheckInterceptorTest {

    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;

    @Mock
    JwtProvider jwtProvider;

    @InjectMocks
    LoginCheckInterceptor loginCheckInterceptor;

    @DisplayName("LoginCheckInterceptor에 의해 요청이 검사된다.")
    @Test
    void LoginCheckInterceptorTest() throws JsonProcessingException {
        // given
        String accessToken = "Bearer abc";

        String userId = "userId";
        Set<Role> roles = Set.of(Role.USER);

        Member member = Member.builder()
                .userId(userId)
                .password("password")
                .build();

        member.addRole(new MemberRole(member, Role.USER));

        AuthenticateUser user = AuthenticateUser.builder()
                .userId(userId)
                .roles(roles)
                .build();

        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("authenticateUser", user);

        Claims mockClaims = Jwts.claims(claimsMap);

        when(request.getHeader("Authorization")).thenReturn(accessToken);

        when(jwtProvider.getClaims(any(String.class))).thenReturn(mockClaims);

        // when
        boolean result = loginCheckInterceptor.preHandle(request, response, null);

        // then
        assertThat(result).isTrue();
    }

}