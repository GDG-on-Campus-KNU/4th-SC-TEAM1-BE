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

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AuthService {

    private final JwtProvider jwtProvider;
    private final RedisTemplate redisTemplate;
    private final MemberRepository memberRepository;

    public Jwt updateAccessToken(UpdateAccessTokenServiceRequest request) {
        String refreshToken = request.getRefreshToken();

        Long memberId = getMemberId(refreshToken);

        if (memberId == null) {
            throw new UnauthorizedException("리프레시 토큰이 만료되었습니다.");
        }

        Member member = getMemberById(memberId);

        String accessToken = createNewAccessToken(member);

        return Jwt.of(accessToken, refreshToken);
    }

    private Long getMemberId(String refreshToken) {
        return Long.parseLong((String) redisTemplate.opsForValue().get(refreshToken));
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new UnauthorizedException("멤버가 존재하지 않습니다."));
    }

    private String createNewAccessToken(Member member) {
        Map<String, Object> claims = jwtProvider.createClaims(member, member.getRoles());

        String accessToken = jwtProvider.createAccessToken(claims);
        return accessToken;
    }
}
