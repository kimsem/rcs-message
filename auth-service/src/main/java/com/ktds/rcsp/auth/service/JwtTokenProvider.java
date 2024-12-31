package com.ktds.rcsp.auth.service;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {
    private final String secret;

    public JwtTokenProvider(@Value("${JWT_SECRET}") String secret) {
        this.secret = secret;
    }

    public String createToken(String masterId, String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 1800000); // 30분

        log.info("Creating token with secret: {}", secret);
        log.info("Signing Key Length: {}", getSigningKey().getEncoded().length);

        return Jwts.builder()
                .setSubject(userId)
                .claim("masterId", masterId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("JWT token validation error: {}", e.getMessage());
            return false;
        }
    }
}