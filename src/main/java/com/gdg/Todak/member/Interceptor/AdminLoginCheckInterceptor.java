package com.gdg.Todak.member.Interceptor;

import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.domain.Role;
import com.gdg.Todak.member.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

import static com.gdg.Todak.member.util.SessionConst.ADMIN_LOGIN_MEMBER;

@RequiredArgsConstructor
@Component
public class AdminLoginCheckInterceptor implements HandlerInterceptor {

    private final MemberRepository memberRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String redirectURI = request.getRequestURI();

        HttpSession session = request.getSession();

        if (session == null) {
            response.sendRedirect("/login?redirectURL=" + redirectURI);
            return false;
        }

        String userId = (String) session.getAttribute(ADMIN_LOGIN_MEMBER);
        if (userId == null) {
            response.sendRedirect("/login?redirectURL=" + redirectURI);
            return false;
        }

        Optional<Member> findMember = memberRepository.findByUserId(userId);
        if (findMember.isEmpty()) {
            response.sendRedirect("/login?redirectURL=" + redirectURI);
            return false;
        }

        Member member = findMember.get();
        if (!member.getRoles().contains(Role.ADMIN)) {
            response.sendRedirect("/login?redirectURL=" + redirectURI);
            return false;
        }

        return true;
    }
}
