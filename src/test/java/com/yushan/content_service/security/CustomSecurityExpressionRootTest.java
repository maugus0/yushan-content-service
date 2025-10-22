package com.yushan.content_service.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomSecurityExpressionRoot.
 */
@ExtendWith(MockitoExtension.class)
class CustomSecurityExpressionRootTest {

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private CustomUserDetails customUserDetails;

    private CustomSecurityExpressionRoot securityExpressionRoot;

    @BeforeEach
    void setUp() {
        securityExpressionRoot = new CustomSecurityExpressionRoot(authentication);
    }

    @Test
    void testConstructor() {
        // Given & When
        CustomSecurityExpressionRoot root = new CustomSecurityExpressionRoot(authentication);

        // Then
        assertNotNull(root);
        assertEquals(authentication, root.getAuthentication());
    }

    @Test
    void testHasAnyRoleCustomWithValidRole() {
        // Given
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(customUserDetails.getRole()).thenReturn("AUTHOR");

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);

            // When
            boolean result = securityExpressionRoot.hasAnyRoleCustom("AUTHOR", "ADMIN");

            // Then
            assertTrue(result);
        }
    }

    @Test
    void testHasAnyRoleCustomWithInvalidRole() {
        // Given
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(customUserDetails.getRole()).thenReturn("USER");

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);

            // When
            boolean result = securityExpressionRoot.hasAnyRoleCustom("AUTHOR", "ADMIN");

            // Then
            assertFalse(result);
        }
    }

    @Test
    void testHasAnyRoleCustomWithNullUser() {
        // Given
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);

            // When
            boolean result = securityExpressionRoot.hasAnyRoleCustom("AUTHOR", "ADMIN");

            // Then
            assertFalse(result);
        }
    }

    @Test
    void testHasRoleCustomWithValidRole() {
        // Given
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(customUserDetails.getRole()).thenReturn("AUTHOR");

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);

            // When
            boolean result = securityExpressionRoot.hasRoleCustom("AUTHOR");

            // Then
            assertTrue(result);
        }
    }

    @Test
    void testHasRoleCustomWithInvalidRole() {
        // Given
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(customUserDetails.getRole()).thenReturn("USER");

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);

            // When
            boolean result = securityExpressionRoot.hasRoleCustom("AUTHOR");

            // Then
            assertFalse(result);
        }
    }

    @Test
    void testHasRoleCustomWithNullUser() {
        // Given
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);

            // When
            boolean result = securityExpressionRoot.hasRoleCustom("AUTHOR");

            // Then
            assertFalse(result);
        }
    }

    @Test
    void testIsAuthenticatedCustomWithValidUser() {
        // Given
        when(authentication.getPrincipal()).thenReturn(customUserDetails);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);

            // When
            boolean result = securityExpressionRoot.isAuthenticatedCustom();

            // Then
            assertTrue(result);
        }
    }

    @Test
    void testIsAuthenticatedCustomWithNullUser() {
        // Given
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);

            // When
            boolean result = securityExpressionRoot.isAuthenticatedCustom();

            // Then
            assertFalse(result);
        }
    }

    @Test
    void testIsAuthorWithAuthorRole() {
        // Given
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(customUserDetails.getRole()).thenReturn("AUTHOR");

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);

            // When
            boolean result = securityExpressionRoot.isAuthor();

            // Then
            assertTrue(result);
        }
    }

    @Test
    void testIsAuthorWithNonAuthorRole() {
        // Given
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(customUserDetails.getRole()).thenReturn("ADMIN");

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);

            // When
            boolean result = securityExpressionRoot.isAuthor();

            // Then
            assertFalse(result);
        }
    }

    @Test
    void testIsAdminWithAdminRole() {
        // Given
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(customUserDetails.getRole()).thenReturn("ADMIN");

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);

            // When
            boolean result = securityExpressionRoot.isAdmin();

            // Then
            assertTrue(result);
        }
    }

    @Test
    void testIsAdminWithNonAdminRole() {
        // Given
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(customUserDetails.getRole()).thenReturn("AUTHOR");

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);

            // When
            boolean result = securityExpressionRoot.isAdmin();

            // Then
            assertFalse(result);
        }
    }

    @Test
    void testIsAuthorOrAdminWithAuthorRole() {
        // Given
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(customUserDetails.getRole()).thenReturn("AUTHOR");

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);

            // When
            boolean result = securityExpressionRoot.isAuthorOrAdmin();

            // Then
            assertTrue(result);
        }
    }

    @Test
    void testIsAuthorOrAdminWithAdminRole() {
        // Given
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(customUserDetails.getRole()).thenReturn("ADMIN");

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);

            // When
            boolean result = securityExpressionRoot.isAuthorOrAdmin();

            // Then
            assertTrue(result);
        }
    }

    @Test
    void testIsAuthorOrAdminWithUserRole() {
        // Given
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(customUserDetails.getRole()).thenReturn("USER");

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);

            // When
            boolean result = securityExpressionRoot.isAuthorOrAdmin();

            // Then
            assertFalse(result);
        }
    }

    @Test
    void testSetAndGetFilterObject() {
        // Given
        Object filterObject = "testFilter";

        // When
        securityExpressionRoot.setFilterObject(filterObject);
        Object result = securityExpressionRoot.getFilterObject();

        // Then
        assertEquals(filterObject, result);
    }

    @Test
    void testSetAndGetReturnObject() {
        // Given
        Object returnObject = "testReturn";

        // When
        securityExpressionRoot.setReturnObject(returnObject);
        Object result = securityExpressionRoot.getReturnObject();

        // Then
        assertEquals(returnObject, result);
    }

    @Test
    void testGetThis() {
        // When
        Object result = securityExpressionRoot.getThis();

        // Then
        assertEquals(securityExpressionRoot, result);
    }

    @Test
    void testGetCurrentUserWithException() {
        // Given
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenThrow(new RuntimeException("Test exception"));

            // When
            boolean result = securityExpressionRoot.isAuthenticatedCustom();

            // Then
            assertFalse(result);
        }
    }

    @Test
    void testGetCurrentUserWithNonCustomUserDetails() {
        // Given
        Object nonCustomUserDetails = "notCustomUserDetails";

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn(nonCustomUserDetails);

            // When
            boolean result = securityExpressionRoot.isAuthenticatedCustom();

            // Then
            assertFalse(result);
        }
    }
}
