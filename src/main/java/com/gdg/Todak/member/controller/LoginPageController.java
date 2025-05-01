package com.gdg.Todak.member.controller;

import com.gdg.Todak.member.controller.dto.LoginForm;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static com.gdg.Todak.member.util.SessionConst.ADMIN_LOGIN_MEMBER;

@RequiredArgsConstructor
@Controller
public class LoginPageController {

    private final MemberService memberService;

    @GetMapping("/login")
    public String loginPage(@ModelAttribute("loginForm") LoginForm form) {
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @Valid @ModelAttribute("loginForm") LoginForm form,
            BindingResult bindingResult,
            @RequestParam(defaultValue = "/admin") String redirectURL,
            HttpServletRequest request
    ) {
        if (bindingResult.hasErrors()) {
            return "/login";
        }

        Member member = memberService.adminLogin(form);
        if (member == null) {
            bindingResult.reject("loginFail", "관리자 로그인을 실패하였습니다.");
            return "/login";
        }

        HttpSession session = request.getSession();
        session.setAttribute(ADMIN_LOGIN_MEMBER, member);

        return "redirect:" + redirectURL;
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login";
    }
}
