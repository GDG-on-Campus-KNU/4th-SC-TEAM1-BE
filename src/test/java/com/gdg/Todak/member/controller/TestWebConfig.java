package com.gdg.Todak.member.controller;

import com.gdg.Todak.member.Interceptor.LoginCheckInterceptor;
import com.gdg.Todak.member.resolver.LoginMemberArgumentResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.ArgumentMatchers.any;

@TestConfiguration
public class TestWebConfig {

    @Bean
    public LoginCheckInterceptor loginCheckInterceptor() {
        LoginCheckInterceptor mockInterceptor = Mockito.mock(LoginCheckInterceptor.class);
        Mockito.when(mockInterceptor.preHandle(
                any(HttpServletRequest.class),
                any(HttpServletResponse.class),
                any(Object.class)
        )).thenReturn(true);
        return mockInterceptor;
    }

    @Bean
    public LoginMemberArgumentResolver loginMemberArgumentResolver() {
        return Mockito.mock(LoginMemberArgumentResolver.class);
    }

}
