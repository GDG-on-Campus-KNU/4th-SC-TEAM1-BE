package com.gdg.Todak.member.util;

import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.domain.Role;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
class JwtProviderTest {

    @Autowired
    JwtProvider jwtProvider;

    @DisplayName("유효한 토큰을 파싱할 시 멤버 데이터를 반환한다.")
    @Test
    void ValidTokenClaimParsingTest() {
        // given
        Member member = Member.of("userId", "userPw", "user", "userImageUrl", "userSalt");
        Map<String, Object> claims = jwtProvider.createClaims(member, Set.of(Role.USER));
        String accessToken = jwtProvider.createAccessToken(claims);

        // when
        Claims result = jwtProvider.getClaims(accessToken);

        // then
        assertNotNull(result);
    }

    @DisplayName("유효하지 않은 토큰을 파싱할 시 null을 반환한다.")
    @Test
    void InValidTokenClaimParsingTest() {
        // given
        String accessToken = "invalidToken";

        // when
        Claims result = jwtProvider.getClaims(accessToken);

        // then
        assertNull(result);
    }

    @DisplayName("ExpireDate가 지난 유효하지 않은 토큰을 파싱할 시 null을 반환한다.")
    @Test
    void InValidTokenWithExpiredDateClaimParsingTest() {
        // given
        Member member = Member.of("userId", "userPw", "user", "userImageUrl", "userSalt");
        Map<String, Object> claims = jwtProvider.createClaims(member, Set.of(Role.USER));
        String accessToken = jwtProvider.createToken(claims, new Date(System.currentTimeMillis() - 1000));

        // when
        Claims result = jwtProvider.getClaims(accessToken);

        // then
        assertNull(result);
    }

}