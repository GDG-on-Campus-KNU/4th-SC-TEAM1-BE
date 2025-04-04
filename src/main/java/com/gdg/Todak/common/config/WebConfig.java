package com.gdg.Todak.common.config;

import com.gdg.Todak.common.filter.AuthenticationExceptionHandlingFilter;
import com.gdg.Todak.member.Interceptor.LoginCheckInterceptor;
import com.gdg.Todak.member.resolver.LoginMemberArgumentResolver;
import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.path}")
    String uploadPath;
    @Value("${image.url}")
    String imageUrl;
    @Value("${file.url}")
    String fileUrl;

    private final LoginCheckInterceptor loginCheckInterceptor;
    private final LoginMemberArgumentResolver loginMemberArgumentResolver;

    private final AuthenticationExceptionHandlingFilter authenticationExceptionHandlingFilter;

    List<String> whiteList = List.of(
            "/api/v1/members/check-userId",
            "/api/v1/members/signup",
            "/api/v1/members/login",
            "/api/v1/members/logout",
            "/api/v1/members/edit",
            "/api/v1/members/edit-password",

            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/backend/**"
    );

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("caught by interceptor");
        registry.addInterceptor(loginCheckInterceptor)
                .order(1)
                .addPathPatterns("/**")
                .excludePathPatterns(whiteList);
    }

    @Bean
    public FilterRegistrationBean uriFilterRegistrationBean() {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(authenticationExceptionHandlingFilter);
        registrationBean.setOrder(1);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginMemberArgumentResolver);
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.addExposedHeader("Authorization");
        config.setMaxAge(3600L);
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(imageUrl + "**")
                .addResourceLocations("file://" + uploadPath);

        registry.addResourceHandler(fileUrl + "**")
                .addResourceLocations("file://" + uploadPath);
    }
}
