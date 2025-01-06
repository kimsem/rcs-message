package com.ktds.rcsp.auth.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final Key key;
    private final long tokenValidityInMilliseconds;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.expiration}") long tokenValidityInMilliseconds) {
        // Keys.secretKeyFor()를 사용하여 충분히 강한 키 생성
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        this.tokenValidityInMilliseconds = tokenValidityInMilliseconds;
        log.debug("JWT Token Provider initialized with validity: {} ms", tokenValidityInMilliseconds);
    }

    public String createToken(String masterId, String userId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + tokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(userId)
                .claim("masterId", masterId)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT token validation error: {}", e.getMessage());
            return false;
        }
    }
}
