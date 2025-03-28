package com.gdg.Todak.member.resolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdg.Todak.member.domain.AuthenticateUser;
import com.gdg.Todak.member.domain.Role;
import com.gdg.Todak.member.util.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginMemberArgumentResolverTest {

    @Mock
    MethodParameter parameter;

    @Mock
    NativeWebRequest webRequest;

    @Mock
    HttpServletRequest request;

    @Mock
    JwtProvider jwtProvider;

    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    LoginMemberArgumentResolver resolver;

    @DisplayName("ArgumentResolver에 의해 헤더의 accessToken이 AuthenticatedUser로 resolve 된다.")
    @Test
    void LoginMemberArgumentResolverTest() throws Exception {
        // given
        String userId = "userId";

        String accessToken = "Bearer abc";

        AuthenticateUser user = AuthenticateUser.builder()
                .userId(userId)
                .roles(Set.of(Role.USER))
                .build();

        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("authenticateUser", user);

        Claims mockClaims = Jwts.claims(claimsMap);

        when(webRequest.getNativeRequest()).thenReturn(request);
        when(request.getHeader("Authorization")).thenReturn(accessToken);

        when(jwtProvider.getClaims(any(String.class))).thenReturn(mockClaims);

        when(objectMapper.readValue(any(String.class), eq(AuthenticateUser.class))).thenReturn(user);

        // when
        AuthenticateUser resolvedUser = (AuthenticateUser) resolver.resolveArgument(parameter, null,
                webRequest, null);

        // then
        assertThat(resolvedUser.getUserId()).isEqualTo(userId);
    }

}