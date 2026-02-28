package com.example.elderly.util;

import com.example.elderly.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private final JwtProperties properties;
    private final Key key;

    public JwtUtil(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Map<String, Object> claims) {
        Instant now = Instant.now();
        Instant expireAt = now.plusSeconds(properties.getExpireHours() * 3600);
        return Jwts.builder()
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt))
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
