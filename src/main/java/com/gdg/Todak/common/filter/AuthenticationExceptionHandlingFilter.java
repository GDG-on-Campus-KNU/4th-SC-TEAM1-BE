package com.gdg.Todak.common.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdg.Todak.common.domain.ApiResponse;
import jakarta.servlet.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthenticationExceptionHandlingFilter implements Filter {

    private final ObjectMapper objectMapper;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            response.setCharacterEncoding("UTF-8");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            try {
                objectMapper.writeValue(
                        response.getWriter(),
                        ApiResponse.of(HttpStatus.UNAUTHORIZED, e.getMessage())
                );
            } catch (IOException ex) {
                log.error("AuthenticationExceptionHandlingFilter error", ex);
            }
        }
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
