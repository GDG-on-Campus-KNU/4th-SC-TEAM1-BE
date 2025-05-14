package com.gdg.Todak.member.service;

import com.gdg.Todak.member.domain.Jwt;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.exception.UnauthorizedException;
import com.gdg.Todak.member.repository.MemberRepository;
import com.gdg.Todak.member.service.request.UpdateAccessTokenServiceRequest;
import com.gdg.Todak.member.util.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AuthService {

    private final JwtProvider jwtProvider;
    private final RedisTemplate redisTemplate;
    private final MemberRepository memberRepository;

    public Jwt updateAccessToken(UpdateAccessTokenServiceRequest request) {
        String accessToken = request.getAccessToken();

        Member member = getMember(accessToken);

        String memberId = member.getId().toString();
        String refreshToken = (String) redisTemplate.opsForValue().get(memberId);

        if (refreshToken == null) {
            throw new UnauthorizedException("리프레시 토큰이 만료되었습니다.");
        }

        if (!refreshToken.equals(request.getRefreshToken())) {
            throw new UnauthorizedException("유효하지 않은 리프레시 토큰입니다.");
        }

        String newAccessToken = createNewAccessToken(member);
        String newRefreshToken = jwtProvider.createRefreshToken();

        saveRefreshToken(newRefreshToken, member);

        return Jwt.of(newAccessToken, newRefreshToken);
    }

    private Member getMember(String accessToken) {
        String userId = jwtProvider.getUserIdForReissue(accessToken)
            .orElseThrow(() -> new UnauthorizedException("존재하지 않는 유저정보 입니다."));
        Member member = memberRepository.findByUserId(userId)
            .orElseThrow(() -> new UnauthorizedException("존재하지 않는 유저정보 입니다."));
        return member;
    }

    private String createNewAccessToken(Member member) {
        Map<String, Object> claims = jwtProvider.createClaims(member, member.getRoles());

        String accessToken = jwtProvider.createAccessToken(claims);
        return accessToken;
    }

    private void saveRefreshToken(String refreshToken, Member member) {
        String memberId = member.getId().toString();
        redisTemplate.opsForValue().set(memberId, refreshToken, 14, TimeUnit.DAYS);
    }
}
