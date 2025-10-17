package com.yushan.content_service.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Custom UserDetails implementation for JWT authentication
 * 
 * This class wraps JWT claims and implements UserDetails interface
 * for Spring Security integration
 */
public class CustomUserDetails implements UserDetails {
    private static final long serialVersionUID = 1L;

    private final String userId;
    private final String email;
    private final String role;
    private final Integer status;

    public CustomUserDetails(String userId, String email, String role) {
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.status = 0; // Default to NORMAL status
    }

    public CustomUserDetails(String userId, String email, String role, Integer status) {
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.status = status;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Add basic user authority
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        
        // Add role-based authority
        if (role != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }
        
        return authorities;
    }

    @Override
    public String getPassword() {
        return null; // JWT doesn't store password
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // JWT handles expiration
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // JWT handles account status
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // JWT handles credential expiration
    }

    @Override
    public boolean isEnabled() {
        // Check if user has normal status (0 = NORMAL)
        return status != null && status == 0;
    }

    /**
     * Get user ID
     * 
     * @return User ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Get user email
     * 
     * @return User email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Get user role
     * 
     * @return User role
     */
    public String getRole() {
        return role;
    }

    /**
     * Check if user is author
     * 
     * @return true if user is author, false otherwise
     */
    public boolean isAuthor() {
        return "AUTHOR".equals(role);
    }

    /**
     * Check if user is admin
     * 
     * @return true if user is admin, false otherwise
     */
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    /**
     * Get user status
     * 
     * @return User status (0 = NORMAL, 1 = SUSPENDED, etc.)
     */
    public Integer getStatus() {
        return status;
    }

}
