package com.gdg.Todak.member.service;

import com.gdg.Todak.member.domain.*;
import com.gdg.Todak.member.exception.UnauthorizedException;
import com.gdg.Todak.member.repository.MemberRepository;
import com.gdg.Todak.member.repository.MemberRoleRepository;
import com.gdg.Todak.member.service.request.*;
import com.gdg.Todak.member.service.response.CheckUserIdServiceResponse;
import com.gdg.Todak.member.service.response.LoginResponse;
import com.gdg.Todak.member.service.response.MeResponse;
import com.gdg.Todak.point.repository.PointRepository;
import com.gdg.Todak.point.service.PointService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
class MemberServiceTest {

    public static final String USERNAME = "test_userId";
    public static final String PASSWORD = "test_password";
    public static final String PASSWORD_CHECK = "test_password";

    public String refreshToken;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberRoleRepository memberRoleRepository;
    @Autowired
    private PointRepository pointRepository;
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

    @BeforeEach
    void setUp() {
        createMember(USERNAME, PASSWORD, PASSWORD_CHECK);
    }

    @DisplayName("회원마다 랜덤 salt를 생성하여 salt와 함께 비밀번호를 인코딩하여 저장한다.")
    @Test
    void signupTest() {
        // given // when
        Member findMember = memberRepository.findByUserId(USERNAME).get();

        // then
        assertThat(findMember.getUserId()).isEqualTo(USERNAME);
        assertThat(findMember.getPassword()).isNotEqualTo(PASSWORD);
        assertThat(findMember.getMemberRoles()).hasSize(1)
                .extracting(MemberRole::getRole)
                .containsExactly(Role.USER);
    }

    @DisplayName("존재하지 않는 userId으로 로그인 시 예외가 발생한다.")
    @Test
    void nonexistentUserIdLoginTest() {
        // given
        String nonexistentUserId = "nonexistentUserId";

        LoginServiceRequest request = LoginServiceRequest.builder()
                .userId(nonexistentUserId)
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
        String wrongPassword = "wrongPassword";

        LoginServiceRequest request = LoginServiceRequest.builder()
                .userId(USERNAME)
                .password(wrongPassword)
                .build();

        // when // then
        assertThatThrownBy(() -> memberService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("비밀번호가 올바르지 않습니다.");
    }

    @DisplayName("올바른 userId과 password로 로그인 시 accessToken과 refreshToken이 반환된다.")
    @Test
    void loginTest() {
        // given
        LoginServiceRequest request = LoginServiceRequest.builder()
                .userId(USERNAME)
                .password(PASSWORD)
                .build();
        doNothing().when(pointService).earnAttendancePointPerDay(any(Member.class));

        // when
        LoginResponse loginResponse = memberService.login(request);
        refreshToken = loginResponse.getRefreshToken();

        // then
        Long memberId = Long.valueOf((String) redisTemplate.opsForValue().get(refreshToken));
        assertThat(memberId).isNotNull();
    }

    @DisplayName("이미 존재하는 userId이면 true를 반환한다.")
    @Test
    void existUserIdCheckTest() {
        // given
        CheckUserIdServiceRequest request = CheckUserIdServiceRequest.builder()
                .userId(USERNAME)
                .build();

        // when
        CheckUserIdServiceResponse result = memberService.checkUserId(request);

        // then
        assertThat(result.getExists()).isEqualTo(true);
    }

    @DisplayName("존재하지 않는 userId이면 false를 반환한다.")
    @Test
    void nonexistentUserIdCheckTest() {
        // given
        CheckUserIdServiceRequest request = CheckUserIdServiceRequest.builder()
                .userId(USERNAME + "nonexistent")
                .build();

        // when
        CheckUserIdServiceResponse result = memberService.checkUserId(request);

        // then
        assertThat(result.getExists()).isEqualTo(false);
    }

    @DisplayName("마이페이지에서 userId이 반환된다")
    @Test
    void meTest() {
        // given
        Set<Role> roles = Set.of(Role.USER);

        AuthenticateUser user = AuthenticateUser.builder()
                .userId(USERNAME)
                .roles(roles)
                .build();

        // when
        MeResponse me = memberService.me(user);

        // then
        assertThat(me.getUserId()).isEqualTo(USERNAME);
    }

    @DisplayName("유저 정보(닉네임)를 수정한다.")
    @Test
    void editMemberNicknameTest() {
        // given
        String changedNickname = "changedNickname";

        Set<Role> roles = Set.of(Role.USER);
        AuthenticateUser user = AuthenticateUser.builder()
                .userId(USERNAME)
                .roles(roles)
                .build();

        EditMemberNicknameServiceRequest request = EditMemberNicknameServiceRequest.builder()
                .nickname(changedNickname)
                .build();

        // when
        MeResponse meResponse = memberService.editMemberNickname(user, request);

        // then
        assertThat(meResponse.getNickname()).isEqualTo(changedNickname);
    }

    @DisplayName("비밀번호 수정 시 새로운 비밀번호와 수정된 비밀번호 확인이 같아야 한다.")
    @Test
    void changePasswordNonMatchingTest() {
        // given
        Set<Role> roles = Set.of(Role.USER);
        AuthenticateUser user = AuthenticateUser.builder()
                .userId(USERNAME)
                .roles(roles)
                .build();

        String changedPassword = "changedPassword";
        String changePasswordCheck = "changePasswordCheck";

        ChangePasswordServiceRequest request = ChangePasswordServiceRequest.builder()
                .oldPassword(PASSWORD)
                .newPassword(changedPassword)
                .newPasswordCheck(changePasswordCheck)
                .build();


        // when // then
        assertThatThrownBy(() -> memberService.changePassword(user, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.");
    }

    @DisplayName("회원 탈퇴시 회원 정보가 삭제된다.")
    @Test
    void deleteMemberTest() {
        // given
        Set<Role> roles = Set.of(Role.USER);
        AuthenticateUser user = AuthenticateUser.builder()
                .userId(USERNAME)
                .roles(roles)
                .build();

        // when
        memberService.deleteMember(user);

        // then
        Optional<Member> findMember = memberRepository.findByUserId(USERNAME);
        assertThat(findMember.isEmpty()).isTrue();
    }

    @DisplayName("AI 댓글 기능이 활성화 된다.")
    @Test
    void enableAiCommentTest() {
        // given
        Set<Role> roles = Set.of(Role.USER);

        AuthenticateUser user = AuthenticateUser.builder()
            .userId(USERNAME)
            .roles(roles)
            .build();

        // when
        String result = memberService.enableAiComment(user);
        Member findMember = memberRepository.findByUserId(USERNAME).get();

        // then
        assertThat(result).isEqualTo("AI 댓글 기능이 활성화되었습니다.");
        assertThat(findMember.isAiCommentEnabled()).isEqualTo(true);
    }

    @DisplayName("AI 댓글 기능이 비활성화 된다.")
    @Test
    void disableAiCommentTest() {
        // given
        Set<Role> roles = Set.of(Role.USER);

        AuthenticateUser user = AuthenticateUser.builder()
            .userId(USERNAME)
            .roles(roles)
            .build();

        // when
        String result = memberService.disableAiComment(user);
        Member findMember = memberRepository.findByUserId(USERNAME).get();

        // then
        assertThat(result).isEqualTo("AI 댓글 기능이 비활성화되었습니다.");
        assertThat(findMember.isAiCommentEnabled()).isEqualTo(false);
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
