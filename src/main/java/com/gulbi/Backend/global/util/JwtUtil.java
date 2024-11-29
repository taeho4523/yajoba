package com.gulbi.Backend.global.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expiration}")
    private long EXPIRATION_TIME;

    // JWT 생성 - 이메일을 subject로, ID는 claims에 추가
    public String generateToken(String email, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userId); // 사용자 ID를 claims에 추가

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email) // 이메일을 subject로 사용
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // 토큰 유효성 검사
    public boolean validateToken(String token, String email) {
        final String extractedEmail = extractEmail(token);
        return (extractedEmail.equals(email) && !isTokenExpired(token));
    }

    // 토큰에서 이메일(Subject)을 추출
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject(); // Subject는 이메일
    }

    // 토큰에서 사용자 ID를 추출
    public Long extractUserId(String token) {
        return extractAllClaims(token).get("id", Long.class); // Claim에서 ID 추출
    }

    // 만료 시간 추출
    private Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    // 토큰의 모든 클레임을 추출
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }
    public String extractJwt(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7); // "Bearer " 부분을 제외하고 토큰을 추출
        }
        return null; // 토큰이 없는 경우 null 리턴
    }

    // JWT에서 클레임을 추출
    public Claims parseClaims(String jwtToken) {
        return extractAllClaims(jwtToken); // JWT에서 클레임을 추출
    }

    // 토큰이 만료되었는지 확인
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
