package com.ktds.rcsp.message.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.util.ArrayList;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${JWT_SECRET}")
    private String jwtSecret;

    @PostConstruct
    public void init() {
        log.info("JWT Secret key length: {}", jwtSecret != null ? jwtSecret.length() : "null");
    }

    private final Key signingKey;

    public JwtAuthenticationFilter(@Value("${JWT_SECRET}") String secret) {
        // 안전한 키 생성
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        log.info("secret 키로 JWT 필터 초기화 ");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String jwt = getJwtFromRequest(request);
            log.info("Request URL: {}", request.getRequestURL());
            log.info("Authorization Header: {}", request.getHeader("Authorization"));
            log.info("JWT Secret: {}", jwtSecret); // 실제 시크릿 키 값 로깅
            log.info("JWT Token: {}", jwt);


            if (jwt != null) {
                log.info("JWT Token received: {}", jwt);

                if (validateToken(jwt)) {
                    Claims claims = getUserClaimsFromToken(jwt);
                    String userId = claims.getSubject();
                    String masterId = claims.get("masterId", String.class);

                    log.info("Token validated successfully. UserId: {}, MasterId: {}", userId, masterId);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(masterId, null, new ArrayList<>());

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("Authentication set in SecurityContext");
                } else {
                    log.warn("Token validation failed");
                }
            } else {
                log.warn("No JWT token found in request");
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private boolean validateToken(String token) {
        log.info("token {}", token);
        try {

            log.info("Signing Key Length: {}", signingKey.getEncoded().length);
            log.info("Signing Key Algorithm: {}", signingKey.getAlgorithm());

            Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("JWT validation error: {}", e.getMessage());
            return false;
        }
    }

    private Claims getUserClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}