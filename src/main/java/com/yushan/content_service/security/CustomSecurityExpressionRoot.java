package com.yushan.content_service.security;

import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Custom Security Expression Root for method-level security.
 * Provides custom expressions that can be used with @PreAuthorize annotations.
 * These expressions work with user information from JWT tokens.
 */
public class CustomSecurityExpressionRoot extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {

    private Object filterObject;
    private Object returnObject;
    private AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

    public CustomSecurityExpressionRoot(Authentication authentication) {
        super(authentication);
        setTrustResolver(trustResolver);
    }

    /**
     * Check if user has any of the specified roles
     * @param roles Roles to check (e.g., "AUTHOR", "ADMIN")
     * @return true if user has any of the roles
     */
    public boolean hasAnyRoleCustom(String... roles) {
        CustomUserDetails user = getCurrentUser();
        if (user == null) {
            return false;
        }
        
        String userRole = user.getRole();
        for (String role : roles) {
            if (role.equals(userRole)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if user has the specified role
     * @param role Role to check (e.g., "AUTHOR")
     * @return true if user has the role
     */
    public boolean hasRoleCustom(String role) {
        CustomUserDetails user = getCurrentUser();
        if (user == null) {
            return false;
        }
        return role.equals(user.getRole());
    }

    /**
     * Check if user is authenticated (has valid token)
     * @return true if user is authenticated
     */
    public boolean isAuthenticatedCustom() {
        CustomUserDetails user = getCurrentUser();
        return user != null;
    }

    /**
     * Check if user is an author
     * @return true if user has AUTHOR role
     */
    public boolean isAuthor() {
        return hasRoleCustom("AUTHOR");
    }

    /**
     * Check if user is an admin
     * @return true if user has ADMIN role
     */
    public boolean isAdmin() {
        return hasRoleCustom("ADMIN");
    }

    /**
     * Check if user is author or admin
     * @return true if user has AUTHOR or ADMIN role
     */
    public boolean isAuthorOrAdmin() {
        return hasAnyRoleCustom("AUTHOR", "ADMIN");
    }

    /**
     * Get current user from User Service
     * @return UserValidationResponseDTO or null if not authenticated
     */
    private CustomUserDetails getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return null;
            }
            
            Object principal = auth.getPrincipal();
            if (principal instanceof CustomUserDetails) {
                return (CustomUserDetails) principal;
            }
            
            return null;
        } catch (Exception e) {
            // Log error but don't throw exception
            return null;
        }
    }


    // MethodSecurityExpressionOperations implementation
    @Override
    public void setFilterObject(Object filterObject) {
        this.filterObject = filterObject;
    }

    @Override
    public Object getFilterObject() {
        return filterObject;
    }

    @Override
    public void setReturnObject(Object returnObject) {
        this.returnObject = returnObject;
    }

    @Override
    public Object getReturnObject() {
        return returnObject;
    }

    @Override
    public Object getThis() {
        return this;
    }
}
