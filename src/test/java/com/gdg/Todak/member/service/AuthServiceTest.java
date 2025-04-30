package com.gdg.Todak.member.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdg.Todak.member.domain.AuthenticateUser;
import com.gdg.Todak.member.domain.Jwt;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.exception.UnauthorizedException;
import com.gdg.Todak.member.repository.MemberRepository;
import com.gdg.Todak.member.repository.MemberRoleRepository;
import com.gdg.Todak.member.service.request.LoginServiceRequest;
import com.gdg.Todak.member.service.request.SignupServiceRequest;
import com.gdg.Todak.member.service.request.UpdateAccessTokenServiceRequest;
import com.gdg.Todak.member.util.JwtProvider;
import com.gdg.Todak.point.service.PointService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.gdg.Todak.member.util.JwtConstants.AUTHENTICATE_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private ObjectMapper objectMapper;

    public String refreshToken;

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberRoleRepository memberRoleRepository;

    @Autowired
    private RedisTemplate redisTemplate;

    @MockitoBean
    private PointService pointService;

    @AfterEach
    void tearDown() {
        memberRoleRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        if (refreshToken != null) {
            redisTemplate.delete(refreshToken);
        }
    }

    @DisplayName("리프레시 토큰이 유효하면 새로운 액세스 토큰을 발급한다.")
    @Test
    void validRefreshTokenTest() throws JsonProcessingException {
        // given
        String userId = "test_userId";
        String password = "test_password";
        String passwordCheck = "test_password";

        doNothing().when(pointService).earnAttendancePointPerDay(any(Member.class));

        createMember(userId, password, passwordCheck);

        LoginServiceRequest loginRequest = LoginServiceRequest.builder()
                .userId(userId)
                .password(password)
                .build();

        LoginResponse jwt = memberService.login(loginRequest);
        refreshToken = jwt.getRefreshToken();

        // when
        UpdateAccessTokenServiceRequest updateAccessTokenServiceRequest = UpdateAccessTokenServiceRequest.builder()
                .refreshToken(jwt.getRefreshToken())
                .build();

        Jwt newJwt = authService.updateAccessToken(updateAccessTokenServiceRequest);

        Claims claims = jwtProvider.getClaims(newJwt.getAccessToken());
        String json = claims.get(AUTHENTICATE_USER).toString();
        AuthenticateUser authenticateUser = objectMapper.readValue(json, AuthenticateUser.class);

        // then
        assertThat(authenticateUser.getUserId()).isEqualTo(userId);
    }

    @DisplayName("리프레시 토큰이 유효하지 않으면 예외가 발생한다.")
    @Test
    void invalidRefreshTokenTest() {
        // given
        String invalidRefreshToken = "invalid_refresh_token";

        // when
        UpdateAccessTokenServiceRequest updateAccessTokenServiceRequest = UpdateAccessTokenServiceRequest.builder()
                .refreshToken(invalidRefreshToken)
                .build();

        // then
        assertThatThrownBy(() -> authService.updateAccessToken(updateAccessTokenServiceRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("리프레시 토큰이 만료되었습니다.");
    }

    private void createMember(String userId, String password, String passwordCheck) {
        SignupServiceRequest request = SignupServiceRequest.builder()
                .userId(userId)
                .password(password)
                .passwordCheck(passwordCheck)
                .build();

        memberService.signup(request);
    }

}
