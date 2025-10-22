package com.yushan.content_service.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtUtil class
 */
@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    private static final String TEST_SECRET = "testSecretKeyForJwtUtilTestingPurposesOnly123456789";
    private static final String TEST_ISSUER = "test-issuer";
    private static final String TEST_USER_ID = "user-123";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_ROLE = "AUTHOR";
    private static final Integer TEST_STATUS = 0;
    private static final String TEST_TOKEN_TYPE = "access";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "issuer", TEST_ISSUER);
    }

    @Test
    void testGetSigningKey() {
        // This is a private method, but we can test it indirectly through other methods
        // The method should create a SecretKey from the secret string
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        assertNotNull(key);
        assertEquals("HmacSHA384", key.getAlgorithm());
    }

    @Test
    void testExtractUserId() {
        // Create a test token
        String token = createTestToken();
        
        String userId = jwtUtil.extractUserId(token);
        assertEquals(TEST_USER_ID, userId);
    }

    @Test
    void testExtractEmail() {
        String token = createTestToken();
        
        String email = jwtUtil.extractEmail(token);
        assertEquals(TEST_EMAIL, email);
    }

    @Test
    void testExtractRole() {
        String token = createTestToken();
        
        String role = jwtUtil.extractRole(token);
        assertEquals(TEST_ROLE, role);
    }

    @Test
    void testExtractStatus() {
        String token = createTestToken();
        
        Integer status = jwtUtil.extractStatus(token);
        assertEquals(TEST_STATUS, status);
    }

    @Test
    void testExtractUsername() {
        String token = createTestToken();
        
        String username = jwtUtil.extractUsername(token);
        assertEquals(TEST_USERNAME, username);
    }

    @Test
    void testExtractTokenType() {
        String token = createTestToken();
        
        String tokenType = jwtUtil.extractTokenType(token);
        assertEquals(TEST_TOKEN_TYPE, tokenType);
    }

    @Test
    void testExtractExpiration() {
        String token = createTestToken();
        
        Date expiration = jwtUtil.extractExpiration(token);
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void testExtractClaim() {
        String token = createTestToken();
        
        // Test extracting userId claim
        String userId = jwtUtil.extractClaim(token, claims -> claims.get("userId", String.class));
        assertEquals(TEST_USER_ID, userId);
        
        // Test extracting email claim
        String email = jwtUtil.extractClaim(token, claims -> claims.get("email", String.class));
        assertEquals(TEST_EMAIL, email);
    }

    @Test
    void testExtractAllClaims() {
        String token = createTestToken();
        
        Claims claims = jwtUtil.extractAllClaims(token);
        assertNotNull(claims);
        assertEquals(TEST_USER_ID, claims.get("userId"));
        assertEquals(TEST_EMAIL, claims.get("email"));
        assertEquals(TEST_USERNAME, claims.get("username"));
        assertEquals(TEST_ROLE, claims.get("role"));
        assertEquals(TEST_STATUS, claims.get("status"));
        assertEquals(TEST_TOKEN_TYPE, claims.get("tokenType"));
    }

    @Test
    void testIsTokenExpired() {
        // Test with valid token (not expired)
        String validToken = createTestToken();
        assertFalse(jwtUtil.isTokenExpired(validToken));
        
        // Test with expired token - expect exception to be thrown
        String expiredToken = createExpiredToken();
        assertThrows(Exception.class, () -> {
            jwtUtil.isTokenExpired(expiredToken);
        });
    }

    @Test
    void testValidateToken() {
        // Test with valid token
        String validToken = createTestToken();
        assertTrue(jwtUtil.validateToken(validToken));
        
        // Test with expired token
        String expiredToken = createExpiredToken();
        assertFalse(jwtUtil.validateToken(expiredToken));
        
        // Test with invalid token
        String invalidToken = "invalid.token.here";
        assertFalse(jwtUtil.validateToken(invalidToken));
    }

    @Test
    void testIsAccessToken() {
        String accessToken = createTestToken();
        assertTrue(jwtUtil.isAccessToken(accessToken));
        
        String refreshToken = createRefreshToken();
        assertFalse(jwtUtil.isAccessToken(refreshToken));
    }

    @Test
    void testIsRefreshToken() {
        String accessToken = createTestToken();
        assertFalse(jwtUtil.isRefreshToken(accessToken));
        
        String refreshToken = createRefreshToken();
        assertTrue(jwtUtil.isRefreshToken(refreshToken));
    }

    @Test
    void testExtractUserIdWithInvalidToken() {
        String invalidToken = "invalid.token.here";
        
        assertThrows(Exception.class, () -> {
            jwtUtil.extractUserId(invalidToken);
        });
    }

    @Test
    void testExtractEmailWithInvalidToken() {
        String invalidToken = "invalid.token.here";
        
        assertThrows(Exception.class, () -> {
            jwtUtil.extractEmail(invalidToken);
        });
    }

    @Test
    void testExtractRoleWithInvalidToken() {
        String invalidToken = "invalid.token.here";
        
        assertThrows(Exception.class, () -> {
            jwtUtil.extractRole(invalidToken);
        });
    }

    @Test
    void testExtractStatusWithInvalidToken() {
        String invalidToken = "invalid.token.here";
        
        assertThrows(Exception.class, () -> {
            jwtUtil.extractStatus(invalidToken);
        });
    }

    @Test
    void testExtractUsernameWithInvalidToken() {
        String invalidToken = "invalid.token.here";
        
        assertThrows(Exception.class, () -> {
            jwtUtil.extractUsername(invalidToken);
        });
    }

    @Test
    void testExtractTokenTypeWithInvalidToken() {
        String invalidToken = "invalid.token.here";
        
        assertThrows(Exception.class, () -> {
            jwtUtil.extractTokenType(invalidToken);
        });
    }

    @Test
    void testExtractExpirationWithInvalidToken() {
        String invalidToken = "invalid.token.here";
        
        assertThrows(Exception.class, () -> {
            jwtUtil.extractExpiration(invalidToken);
        });
    }

    @Test
    void testExtractClaimWithInvalidToken() {
        String invalidToken = "invalid.token.here";
        
        assertThrows(Exception.class, () -> {
            jwtUtil.extractClaim(invalidToken, Claims::getSubject);
        });
    }

    @Test
    void testExtractAllClaimsWithInvalidToken() {
        String invalidToken = "invalid.token.here";
        
        assertThrows(Exception.class, () -> {
            jwtUtil.extractAllClaims(invalidToken);
        });
    }

    @Test
    void testIsTokenExpiredWithInvalidToken() {
        String invalidToken = "invalid.token.here";
        
        assertThrows(Exception.class, () -> {
            jwtUtil.isTokenExpired(invalidToken);
        });
    }

    @Test
    void testValidateTokenWithNullToken() {
        assertFalse(jwtUtil.validateToken(null));
    }

    @Test
    void testValidateTokenWithEmptyToken() {
        assertFalse(jwtUtil.validateToken(""));
    }

    @Test
    void testIsAccessTokenWithNullToken() {
        assertThrows(Exception.class, () -> {
            jwtUtil.isAccessToken(null);
        });
    }

    @Test
    void testIsRefreshTokenWithNullToken() {
        assertThrows(Exception.class, () -> {
            jwtUtil.isRefreshToken(null);
        });
    }

    @Test
    void testExtractClaimWithNullToken() {
        assertThrows(Exception.class, () -> {
            jwtUtil.extractClaim(null, Claims::getSubject);
        });
    }

    @Test
    void testExtractClaimWithNullFunction() {
        String token = createTestToken();
        
        assertThrows(NullPointerException.class, () -> {
            jwtUtil.extractClaim(token, null);
        });
    }

    @Test
    void testTokenWithMissingClaims() {
        // Create token without some claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", TEST_USER_ID);
        // Missing email, username, role, status, tokenType
        
        String token = Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour
                .signWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
        
        // Should return null for missing claims
        assertNull(jwtUtil.extractEmail(token));
        assertNull(jwtUtil.extractUsername(token));
        assertNull(jwtUtil.extractRole(token));
        assertNull(jwtUtil.extractStatus(token));
        assertNull(jwtUtil.extractTokenType(token));
    }

    /**
     * Helper method to create a test token
     */
    private String createTestToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", TEST_USER_ID);
        claims.put("email", TEST_EMAIL);
        claims.put("username", TEST_USERNAME);
        claims.put("role", TEST_ROLE);
        claims.put("status", TEST_STATUS);
        claims.put("tokenType", TEST_TOKEN_TYPE);
        
        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour
                .signWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    /**
     * Helper method to create an expired token
     */
    private String createExpiredToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", TEST_USER_ID);
        claims.put("email", TEST_EMAIL);
        claims.put("username", TEST_USERNAME);
        claims.put("role", TEST_ROLE);
        claims.put("status", TEST_STATUS);
        claims.put("tokenType", TEST_TOKEN_TYPE);
        
        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date(System.currentTimeMillis() - 7200000)) // 2 hours ago
                .expiration(new Date(System.currentTimeMillis() - 3600000)) // 1 hour ago
                .signWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    /**
     * Helper method to create a refresh token
     */
    private String createRefreshToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", TEST_USER_ID);
        claims.put("email", TEST_EMAIL);
        claims.put("username", TEST_USERNAME);
        claims.put("role", TEST_ROLE);
        claims.put("status", TEST_STATUS);
        claims.put("tokenType", "refresh");
        
        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000)) // 24 hours
                .signWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
