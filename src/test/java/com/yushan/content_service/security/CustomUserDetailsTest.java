package com.yushan.content_service.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CustomUserDetails.
 */
class CustomUserDetailsTest {

    @Test
    void testConstructorWithThreeParameters() {
        // Given
        String userId = "user123";
        String email = "test@example.com";
        String role = "AUTHOR";

        // When
        CustomUserDetails userDetails = new CustomUserDetails(userId, email, role);

        // Then
        assertEquals(userId, userDetails.getUserId());
        assertEquals(email, userDetails.getEmail());
        assertEquals(role, userDetails.getRole());
        assertEquals(email, userDetails.getDisplayUsername()); // Returns email when username is null
        assertEquals(0, userDetails.getStatus());
        assertTrue(userDetails.isEnabled());
    }

    @Test
    void testConstructorWithFourParameters() {
        // Given
        String userId = "user123";
        String email = "test@example.com";
        String role = "ADMIN";
        Integer status = 1;

        // When
        CustomUserDetails userDetails = new CustomUserDetails(userId, email, role, status);

        // Then
        assertEquals(userId, userDetails.getUserId());
        assertEquals(email, userDetails.getEmail());
        assertEquals(role, userDetails.getRole());
        assertEquals(email, userDetails.getDisplayUsername()); // Returns email when username is null
        assertEquals(status, userDetails.getStatus());
        assertFalse(userDetails.isEnabled());
    }

    @Test
    void testConstructorWithFiveParameters() {
        // Given
        String userId = "user123";
        String email = "test@example.com";
        String username = "testuser";
        String role = "USER";
        Integer status = 0;

        // When
        CustomUserDetails userDetails = new CustomUserDetails(userId, email, username, role, status);

        // Then
        assertEquals(userId, userDetails.getUserId());
        assertEquals(email, userDetails.getEmail());
        assertEquals(username, userDetails.getDisplayUsername());
        assertEquals(role, userDetails.getRole());
        assertEquals(status, userDetails.getStatus());
        assertTrue(userDetails.isEnabled());
    }

    @Test
    void testGetAuthorities() {
        // Given
        CustomUserDetails userDetails = new CustomUserDetails("user123", "test@example.com", "AUTHOR");

        // When
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        // Then
        assertNotNull(authorities);
        assertEquals(2, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_AUTHOR")));
    }

    @Test
    void testGetAuthoritiesWithNullRole() {
        // Given
        CustomUserDetails userDetails = new CustomUserDetails("user123", "test@example.com", null);

        // When
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        // Then
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void testGetPassword() {
        // Given
        CustomUserDetails userDetails = new CustomUserDetails("user123", "test@example.com", "AUTHOR");

        // When & Then
        assertNull(userDetails.getPassword());
    }

    @Test
    void testGetUsername() {
        // Given
        String email = "test@example.com";
        CustomUserDetails userDetails = new CustomUserDetails("user123", email, "AUTHOR");

        // When & Then
        assertEquals(email, userDetails.getUsername());
    }

    @Test
    void testIsAccountNonExpired() {
        // Given
        CustomUserDetails userDetails = new CustomUserDetails("user123", "test@example.com", "AUTHOR");

        // When & Then
        assertTrue(userDetails.isAccountNonExpired());
    }

    @Test
    void testIsAccountNonLocked() {
        // Given
        CustomUserDetails userDetails = new CustomUserDetails("user123", "test@example.com", "AUTHOR");

        // When & Then
        assertTrue(userDetails.isAccountNonLocked());
    }

    @Test
    void testIsCredentialsNonExpired() {
        // Given
        CustomUserDetails userDetails = new CustomUserDetails("user123", "test@example.com", "AUTHOR");

        // When & Then
        assertTrue(userDetails.isCredentialsNonExpired());
    }

    @Test
    void testIsEnabledWithNormalStatus() {
        // Given
        CustomUserDetails userDetails = new CustomUserDetails("user123", "test@example.com", "AUTHOR", 0);

        // When & Then
        assertTrue(userDetails.isEnabled());
    }

    @Test
    void testIsEnabledWithSuspendedStatus() {
        // Given
        CustomUserDetails userDetails = new CustomUserDetails("user123", "test@example.com", "AUTHOR", 1);

        // When & Then
        assertFalse(userDetails.isEnabled());
    }

    @Test
    void testIsEnabledWithNullStatus() {
        // Given
        CustomUserDetails userDetails = new CustomUserDetails("user123", "test@example.com", "AUTHOR", null);

        // When & Then
        assertFalse(userDetails.isEnabled());
    }

    @Test
    void testGetDisplayUsernameWithUsername() {
        // Given
        String username = "testuser";
        CustomUserDetails userDetails = new CustomUserDetails("user123", "test@example.com", username, "AUTHOR", 0);

        // When & Then
        assertEquals(username, userDetails.getDisplayUsername());
    }

    @Test
    void testGetDisplayUsernameWithNullUsername() {
        // Given
        String email = "test@example.com";
        CustomUserDetails userDetails = new CustomUserDetails("user123", email, null, "AUTHOR", 0);

        // When & Then
        assertEquals(email, userDetails.getDisplayUsername());
    }

    @Test
    void testIsAuthor() {
        // Given
        CustomUserDetails authorDetails = new CustomUserDetails("user123", "test@example.com", "AUTHOR");
        CustomUserDetails adminDetails = new CustomUserDetails("user456", "admin@example.com", "ADMIN");
        CustomUserDetails userDetails = new CustomUserDetails("user789", "user@example.com", "USER");

        // When & Then
        assertTrue(authorDetails.isAuthor());
        assertFalse(adminDetails.isAuthor());
        assertFalse(userDetails.isAuthor());
    }

    @Test
    void testIsAdmin() {
        // Given
        CustomUserDetails authorDetails = new CustomUserDetails("user123", "test@example.com", "AUTHOR");
        CustomUserDetails adminDetails = new CustomUserDetails("user456", "admin@example.com", "ADMIN");
        CustomUserDetails userDetails = new CustomUserDetails("user789", "user@example.com", "USER");

        // When & Then
        assertFalse(authorDetails.isAdmin());
        assertTrue(adminDetails.isAdmin());
        assertFalse(userDetails.isAdmin());
    }

    @Test
    void testSerialVersionUID() {
        // Given
        CustomUserDetails userDetails = new CustomUserDetails("user123", "test@example.com", "AUTHOR");

        // When & Then
        // Test that the class can be instantiated (serialVersionUID is private)
        assertNotNull(userDetails);
        assertTrue(userDetails instanceof CustomUserDetails);
    }
}
