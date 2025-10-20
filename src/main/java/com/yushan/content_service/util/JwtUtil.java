package com.yushan.content_service.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

/**
 * JWT Utility class for token validation and parsing
 * 
 * This class provides methods to:
 * - Extract information from JWT tokens
 * - Validate tokens
 * - Check token expiration
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.issuer}")
    private String issuer;

    /**
     * Get the secret key for JWT validation
     * 
     * @return SecretKey object for JWT operations
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extract user ID from JWT token
     * 
     * @param token JWT token
     * @return User ID from token claims
     */
    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    /**
     * Extract email from JWT token
     * 
     * @param token JWT token
     * @return Email from token claims
     */
    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    /**
     * Extract role from JWT token
     * 
     * @param token JWT token
     * @return Role from token claims
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Extract user status from JWT token
     * 
     * @param token JWT token
     * @return User status from token claims
     */
    public Integer extractStatus(String token) {
        return extractClaim(token, claims -> claims.get("status", Integer.class));
    }

    /**
     * Extract username from JWT token
     * 
     * @param token JWT token
     * @return Username from token claims
     */
    public String extractUsername(String token) {
        return extractClaim(token, claims -> claims.get("username", String.class));
    }


    /**
     * Extract token type from JWT token
     * 
     * @param token JWT token
     * @return Token type (access/refresh) from token claims
     */
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("tokenType", String.class));
    }

    /**
     * Extract expiration date from JWT token
     * 
     * @param token JWT token
     * @return Expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract specific claim from JWT token
     * 
     * @param token JWT token
     * @param claimsResolver Function to extract specific claim
     * @return Extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from JWT token
     * 
     * @param token JWT token
     * @return Claims object containing all token claims
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Check if token is expired
     * 
     * @param token JWT token
     * @return true if token is expired, false otherwise
     */
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validate token (check expiration and signature)
     * 
     * @param token JWT token
     * @return true if token is valid, false otherwise
     */
    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if token is access token
     * 
     * @param token JWT token
     * @return true if token is access token, false otherwise
     */
    public Boolean isAccessToken(String token) {
        String tokenType = extractTokenType(token);
        return "access".equals(tokenType);
    }

    /**
     * Check if token is refresh token
     * 
     * @param token JWT token
     * @return true if token is refresh token, false otherwise
     */
    public Boolean isRefreshToken(String token) {
        String tokenType = extractTokenType(token);
        return "refresh".equals(tokenType);
    }
}
