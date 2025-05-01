package com.gdg.Todak.member.Interceptor;

import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.domain.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.gdg.Todak.member.util.SessionConst.ADMIN_LOGIN_MEMBER;

@Component
public class AdminLoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String redirectURI = request.getRequestURI();

        HttpSession session = request.getSession();

        if (session == null) {
            response.sendRedirect("/login?redirectURL=" + redirectURI);
            return false;
        }

        Member member = (Member) session.getAttribute(ADMIN_LOGIN_MEMBER);
        if (member == null) {
            response.sendRedirect("/login?redirectURL=" + redirectURI);
            return false;
        }

        if (!member.getRoles().contains(Role.ADMIN)) {
            response.sendRedirect("/login?redirectURL=" + redirectURI);
            return false;
        }

        return true;
    }
}
