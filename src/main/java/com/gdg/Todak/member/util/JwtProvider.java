package com.gdg.Todak.member.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdg.Todak.member.domain.AuthenticateUser;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.domain.Role;
import com.gdg.Todak.member.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.Key;
import java.util.*;

import static com.gdg.Todak.member.util.JwtConstants.AUTHENTICATE_USER;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final ObjectMapper objectMapper;

    @Value("${SECRET_KEY}")
    private String secretKey;

    private byte[] secretKeyBytes;
    private Key key;

    @PostConstruct
    public void init() {
        secretKeyBytes = secretKey.getBytes();
        key = Keys.hmacShaKeyFor(secretKeyBytes);
    }

    public String createToken(Map<String, Object> claims, Date expireDate) {
        return Jwts.builder()
            .claims(claims)
            .expiration(expireDate)
            .signWith(key)
            .compact();
    }

    public Claims getClaims(String token) {
        try {
            return Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    public Optional<String> getUserIdForReissue(String token) {
        Claims claims = getClaimsForReissue(token);
        return Optional.of(getUserIdByClaims(claims));
    }

    private String getUserIdByClaims(Claims claims) {
        String json = claims.get(AUTHENTICATE_USER, String.class);
        try {
            AuthenticateUser user = objectMapper.readValue(json, AuthenticateUser.class);
            return user.getUserId();
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private Claims getClaimsForReissue(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (ExpiredJwtException e) {
            claims = e.getClaims();
        }
        return claims;
    }

    public String createRefreshToken() {
        return createToken(new HashMap<>(), getExpireDateRefreshToken());
    }

    public Map<String, Object> createClaims(Member member, Set<Role> roles) {
        Map<String, Object> claims = new HashMap<>();
        AuthenticateUser authenticateUser = new AuthenticateUser(member.getUserId(), roles);

        try {
            String authenticateUserJson = objectMapper.writeValueAsString(authenticateUser);
            claims.put(AUTHENTICATE_USER, authenticateUserJson);
        } catch (IOException e) {
            throw new UnauthorizedException(e.getMessage());
        }
        return claims;
    }

    public String createAccessToken(Map<String, Object> claims) {
        return createToken(claims, getExpireDateAccessToken());
    }

    public Date getExpireDateAccessToken() {
        long expireTimeMils = 1000 * 60 * 30;
        return new Date(System.currentTimeMillis() + expireTimeMils);
    }

    public Date getExpireDateRefreshToken() {
        long expireTimeMils = 1000L * 60 * 60 * 24 * 14;
        return new Date(System.currentTimeMillis() + expireTimeMils);
    }
}
