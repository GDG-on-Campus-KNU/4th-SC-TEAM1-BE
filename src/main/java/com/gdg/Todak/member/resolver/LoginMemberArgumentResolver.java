package com.gdg.Todak.member.resolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdg.Todak.member.domain.AuthenticateUser;
import com.gdg.Todak.member.util.JwtProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static com.gdg.Todak.member.util.JwtConstants.*;

@RequiredArgsConstructor
@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasLoginAnnotation = parameter.hasParameterAnnotation(Login.class);

        boolean hasAuthenticateUserType = AuthenticateUser.class.isAssignableFrom(
                parameter.getParameterType());

        return hasLoginAnnotation && hasAuthenticateUserType;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

        String header = request.getHeader(AUTHORIZATION);
        if (isNotValidToken(header)) {
            return null;
        }

        Claims claims = getClaims(header);
        if (claims == null) {
            return null;
        }

        AuthenticateUser authenticateUser = getAuthenticateUser(claims);

        return authenticateUser;
    }

    private static boolean isNotValidToken(String header) {
        return header == null || !header.startsWith(BEARER);
    }

    private Claims getClaims(String header) {
        String token = header.substring(7);
        Claims claims = jwtProvider.getClaims(token);
        return claims;
    }

    private AuthenticateUser getAuthenticateUser(Claims claims) throws JsonProcessingException {
        String json = claims.get(AUTHENTICATE_USER).toString();
        AuthenticateUser authenticateUser = objectMapper.readValue(json, AuthenticateUser.class);
        return authenticateUser;
    }
}
