package com.gdg.Todak.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdg.Todak.member.service.AuthService;
import com.gdg.Todak.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {
        AuthController.class,
        MemberController.class
})
@Import(TestWebConfig.class)
public abstract class ControllerTestSupport {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    MemberService memberService;

    @MockitoBean
    AuthService authService;
}

