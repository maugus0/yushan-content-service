package com.yushan.content_service.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Test Utility for generating test tokens
 * This is only for development/testing purposes
 */
@Component
public class JwtTestUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.issuer}")
    private String issuer;

    /**
     * Get the secret key for JWT signing
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate test JWT token for AUTHOR role
     */
    public String generateTestAuthorToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", "550e8400-e29b-41d4-a716-446655440001");
        claims.put("email", "author@test.com");
        claims.put("username", "test_author");
        claims.put("role", "AUTHOR");
        claims.put("status", 0); // NORMAL
        claims.put("tokenType", "access");

        return createToken(claims, "author@test.com", 3600000); // 1 hour
    }

    /**
     * Generate test JWT token for ADMIN role
     */
    public String generateTestAdminToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", "550e8400-e29b-41d4-a716-446655440002");
        claims.put("email", "admin@test.com");
        claims.put("username", "test_admin");
        claims.put("role", "ADMIN");
        claims.put("status", 0); // NORMAL
        claims.put("tokenType", "access");

        return createToken(claims, "admin@test.com", 3600000); // 1 hour
    }

    /**
     * Generate test JWT token for suspended user
     */
    public String generateTestSuspendedToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", "550e8400-e29b-41d4-a716-446655440003");
        claims.put("email", "suspended@test.com");
        claims.put("username", "test_suspended");
        claims.put("role", "AUTHOR");
        claims.put("status", 1); // SUSPENDED
        claims.put("tokenType", "access");

        return createToken(claims, "suspended@test.com", 3600000); // 1 hour
    }

    /**
     * Create JWT token with claims and expiration
     */
    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
}
