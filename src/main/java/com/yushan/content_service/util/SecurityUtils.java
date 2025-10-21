package com.yushan.content_service.util;

import com.yushan.content_service.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public class SecurityUtils {

    /**
     * Get current user ID from SecurityContext
     * @return User ID as UUID, null if not authenticated
     */
    public static UUID getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return null;
            }
            
            Object principal = auth.getPrincipal();
            if (principal instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) principal;
                return UUID.fromString(userDetails.getUserId());
            }
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
