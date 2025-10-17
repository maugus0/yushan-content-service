package com.yushan.content_service.config;

import com.yushan.content_service.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

/**
 * Test configuration for mocking SecurityContext.
 * Provides mock user details for controller tests.
 */
public class TestSecurityContextConfig {

    public static void setupMockUser() {
        CustomUserDetails userDetails = new CustomUserDetails(
            UUID.fromString("550e8400-e29b-41d4-a716-446655440001").toString(),
            "test@example.com",
            "AUTHOR",
            0
        );

        Authentication authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities()
        );

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    public static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}
