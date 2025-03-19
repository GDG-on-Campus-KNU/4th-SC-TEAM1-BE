package com.gdg.Todak.member.service;

import com.gdg.Todak.member.domain.*;
import com.gdg.Todak.member.exception.UnauthorizedException;
import com.gdg.Todak.member.repository.MemberRepository;
import com.gdg.Todak.member.repository.MemberRoleRepository;
import com.gdg.Todak.member.service.request.CheckUsernameServiceRequest;
import com.gdg.Todak.member.service.request.LoginServiceRequest;
import com.gdg.Todak.member.service.request.SignupServiceRequest;
import com.gdg.Todak.member.service.response.CheckUsernameServiceResponse;
import com.gdg.Todak.member.service.response.MeResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class MemberServiceTest {

    public static final String USERNAME = "test_username";
    public static final String PASSWORD = "test_password";

    public String refreshToken;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberRoleRepository memberRoleRepository;

    @Autowired
    private RedisTemplate redisTemplate;

    @AfterEach
    void tearDown() {
        memberRoleRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        if (refreshToken != null) {
            redisTemplate.delete(refreshToken);
        }
    }

    @DisplayName("회원마다 랜덤 salt를 생성하여 salt와 함께 비밀번호를 인코딩하여 저장한다.")
    @Test
    void signupTest() {
        // given
        createMember(USERNAME, PASSWORD);

        // when
        Member findMember = memberRepository.findByUsername(USERNAME).get();

        // then
        assertThat(findMember.getUsername()).isEqualTo(USERNAME);
        assertThat(findMember.getPassword()).isNotEqualTo(PASSWORD);
        assertThat(findMember.getMemberRoles()).hasSize(1)
                .extracting(MemberRole::getRole)
                .containsExactly(Role.USER);
    }

    @DisplayName("존재하지 않는 username으로 로그인 시 예외가 발생한다.")
    @Test
    void nonexistentUsernameLoginTest() {
        // given
        createMember(USERNAME, PASSWORD);

        String nonexistentUsername = "nonexistentUsername";

        LoginServiceRequest request = LoginServiceRequest.builder()
                .username(nonexistentUsername)
                .password(PASSWORD)
                .build();

        // when // then
        assertThatThrownBy(() -> memberService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("멤버가 존재하지 않습니다.");
    }

    @DisplayName("존재하지 않는 password로 로그인 시 예외가 발생한다.")
    @Test
    void wrongPasswordLoginTest() {
        // given
        createMember(USERNAME, PASSWORD);

        String wrongPassword = "wrongPassword";

        LoginServiceRequest request = LoginServiceRequest.builder()
                .username(USERNAME)
                .password(wrongPassword)
                .build();

        // when // then
        assertThatThrownBy(() -> memberService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("비밀번호가 올바르지 않습니다.");
    }

    @DisplayName("올바른 username과 password로 로그인 시 accessToken과 refreshToken이 반환된다.")
    @Test
    void loginTest() {
        // given
        createMember(USERNAME, PASSWORD);

        LoginServiceRequest request = LoginServiceRequest.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .build();

        // when
        Jwt jwtToken = memberService.login(request);
        refreshToken = jwtToken.getRefreshToken();

        // then
        Long memberId = (Long) redisTemplate.opsForValue().get(refreshToken);
        assertThat(memberId).isNotNull();
    }

    @DisplayName("이미 존재하는 username이면 true를 반환한다.")
    @Test
    void existUsernameCheckTest() {
        // given
        createMember(USERNAME, PASSWORD);

        CheckUsernameServiceRequest request = CheckUsernameServiceRequest.builder()
                .username(USERNAME)
                .build();

        // when
        CheckUsernameServiceResponse result = memberService.checkUsername(request);

        // then
        assertThat(result.getExists()).isEqualTo(true);
    }

    @DisplayName("존재하지 않는 username이면 false를 반환한다.")
    @Test
    void nonexistentUsernameCheckTest() {
        // given
        createMember(USERNAME, PASSWORD);

        CheckUsernameServiceRequest request = CheckUsernameServiceRequest.builder()
                .username(USERNAME + "nonexistent")
                .build();

        // when
        CheckUsernameServiceResponse result = memberService.checkUsername(request);

        // then
        assertThat(result.getExists()).isEqualTo(false);
    }

    @DisplayName("마이페이지에서 username이 반환된다")
    @Test
    void meTest() {
        // given
        createMember(USERNAME, PASSWORD);

        Set<Role> roles = Set.of(Role.USER);

        AuthenticateUser user = AuthenticateUser.builder()
                .username(USERNAME)
                .roles(roles)
                .build();

        // when
        MeResponse me = memberService.me(user);

        // then
        assertThat(me.getUsername()).isEqualTo(USERNAME);
    }

    private void createMember(String username, String password) {
        SignupServiceRequest request = SignupServiceRequest.builder()
                .username(username)
                .password(password)
                .build();

        memberService.signup(request);
    }
}