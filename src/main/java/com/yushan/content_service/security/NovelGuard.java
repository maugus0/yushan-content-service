package com.yushan.content_service.security;

import com.yushan.content_service.dao.NovelMapper;
import com.yushan.content_service.entity.Novel;
import com.yushan.content_service.enums.NovelStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Security guard for novel operations.
 * Provides authorization logic for novel-related actions.
 */
@Component
public class NovelGuard {

    @Autowired
    private NovelMapper novelMapper;

    /**
     * Check if user can edit a novel
     * @param novelId Novel ID
     * @param authentication Current authentication
     * @return true if user can edit the novel
     */
    public boolean canEdit(Integer novelId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // Admin can always edit
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));
        if (isAdmin) return true;

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails)) {
            return false;
        }
        String userIdStr = ((CustomUserDetails) principal).getUserId();
        if (userIdStr == null) return false;
        UUID userId = UUID.fromString(userIdStr);

        Novel novel = novelMapper.selectByPrimaryKey(novelId);
        if (novel == null || novel.getAuthorId() == null) {
            return false;
        }
        
        // Check if user is the author
        if (!userId.equals(novel.getAuthorId())) {
            return false;
        }
        
        // Only allow editing if novel is in DRAFT, PUBLISHED, or HIDDEN status
        int status = novel.getStatus();
        return status == NovelStatus.DRAFT.getValue() || 
               status == NovelStatus.PUBLISHED.getValue() || 
               status == NovelStatus.HIDDEN.getValue();
    }

    /**
     * Check if user can hide or unhide a novel
     * @param novelId Novel ID
     * @param authentication Current authentication
     * @return true if user can hide/unhide the novel
     */
    public boolean canHideOrUnhide(Integer novelId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // Admin can always hide/unhide
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));
        if (isAdmin) return true;

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails)) {
            return false;
        }
        String userIdStr = ((CustomUserDetails) principal).getUserId();
        if (userIdStr == null) return false;
        UUID userId = UUID.fromString(userIdStr);

        Novel novel = novelMapper.selectByPrimaryKey(novelId);
        if (novel == null || novel.getAuthorId() == null) {
            return false;
        }
        
        // Check if user is the author
        if (!userId.equals(novel.getAuthorId())) {
            return false;
        }
        
        // Allow hide/unhide for PUBLISHED or HIDDEN novels
        int status = novel.getStatus();
        return status == NovelStatus.PUBLISHED.getValue() || 
               status == NovelStatus.HIDDEN.getValue();
    }
}
