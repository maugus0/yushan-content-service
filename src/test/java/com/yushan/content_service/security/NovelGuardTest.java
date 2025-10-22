package com.yushan.content_service.security;

import com.yushan.content_service.dao.NovelMapper;
import com.yushan.content_service.entity.Novel;
import com.yushan.content_service.enums.NovelStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NovelGuard.
 */
@ExtendWith(MockitoExtension.class)
class NovelGuardTest {

    @Mock
    private NovelMapper novelMapper;

    @Mock
    private Authentication authentication;

    @Mock
    private CustomUserDetails customUserDetails;

    @InjectMocks
    private NovelGuard novelGuard;

    private Novel testNovel;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testNovel = new Novel();
        testNovel.setId(1);
        testNovel.setAuthorId(testUserId);
        testNovel.setStatus(NovelStatus.DRAFT.getValue());
    }

    @Test
    void testCanEditWithNullAuthentication() {
        // When
        boolean result = novelGuard.canEdit(1, null);

        // Then
        assertFalse(result);
    }

    @Test
    void testCanEditWithUnauthenticatedUser() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(false);

        // When
        boolean result = novelGuard.canEdit(1, authentication);

        // Then
        assertFalse(result);
    }

    @Test
    void testCanEditWithAdminRole() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(Arrays.asList());

        // When
        boolean result = novelGuard.canEdit(1, authentication);

        // Then
        assertFalse(result);
    }

    @Test
    void testCanEditWithNonCustomUserDetails() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(Arrays.asList());
        when(authentication.getPrincipal()).thenReturn("notCustomUserDetails");

        // When
        boolean result = novelGuard.canEdit(1, authentication);

        // Then
        assertFalse(result);
    }

    @Test
    void testCanEditWithNullUserId() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(Arrays.asList());
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(customUserDetails.getUserId()).thenReturn(null);

        // When
        boolean result = novelGuard.canEdit(1, authentication);

        // Then
        assertFalse(result);
    }

    @Test
    void testCanEditWithNovelNotFound() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(Arrays.asList());
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(customUserDetails.getUserId()).thenReturn(testUserId.toString());
        when(novelMapper.selectByPrimaryKey(1)).thenReturn(null);

        // When
        boolean result = novelGuard.canEdit(1, authentication);

        // Then
        assertFalse(result);
    }

    @Test
    void testCanEditWithNovelHavingNullAuthorId() {
        // Given
        testNovel.setAuthorId(null);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(Arrays.asList());
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(customUserDetails.getUserId()).thenReturn(testUserId.toString());
        when(novelMapper.selectByPrimaryKey(1)).thenReturn(testNovel);

        // When
        boolean result = novelGuard.canEdit(1, authentication);

        // Then
        assertFalse(result);
    }

    @Test
    void testCanEditWithDifferentAuthor() {
        // Given
        UUID differentUserId = UUID.randomUUID();
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(Arrays.asList());
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(customUserDetails.getUserId()).thenReturn(differentUserId.toString());
        when(novelMapper.selectByPrimaryKey(1)).thenReturn(testNovel);

        // When
        boolean result = novelGuard.canEdit(1, authentication);

        // Then
        assertFalse(result);
    }

    @Test
    void testCanEditWithDraftStatus() {
        // Given
        testNovel.setStatus(NovelStatus.DRAFT.getValue());
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(Arrays.asList());
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(customUserDetails.getUserId()).thenReturn(testUserId.toString());
        when(novelMapper.selectByPrimaryKey(1)).thenReturn(testNovel);

        // When
        boolean result = novelGuard.canEdit(1, authentication);

        // Then
        assertTrue(result);
    }

    @Test
    void testCanEditWithPublishedStatus() {
        // Given
        testNovel.setStatus(NovelStatus.PUBLISHED.getValue());
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(Arrays.asList());
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(customUserDetails.getUserId()).thenReturn(testUserId.toString());
        when(novelMapper.selectByPrimaryKey(1)).thenReturn(testNovel);

        // When
        boolean result = novelGuard.canEdit(1, authentication);

        // Then
        assertTrue(result);
    }

    @Test
    void testCanEditWithHiddenStatus() {
        // Given
        testNovel.setStatus(NovelStatus.HIDDEN.getValue());
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(Arrays.asList());
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(customUserDetails.getUserId()).thenReturn(testUserId.toString());
        when(novelMapper.selectByPrimaryKey(1)).thenReturn(testNovel);

        // When
        boolean result = novelGuard.canEdit(1, authentication);

        // Then
        assertTrue(result);
    }

    @Test
    void testCanEditWithUnderReviewStatus() {
        // Given
        testNovel.setStatus(NovelStatus.UNDER_REVIEW.getValue());
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(Arrays.asList());
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(customUserDetails.getUserId()).thenReturn(testUserId.toString());
        when(novelMapper.selectByPrimaryKey(1)).thenReturn(testNovel);

        // When
        boolean result = novelGuard.canEdit(1, authentication);

        // Then
        assertFalse(result);
    }

    @Test
    void testCanEditWithArchivedStatus() {
        // Given
        testNovel.setStatus(NovelStatus.ARCHIVED.getValue());
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(Arrays.asList());
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(customUserDetails.getUserId()).thenReturn(testUserId.toString());
        when(novelMapper.selectByPrimaryKey(1)).thenReturn(testNovel);

        // When
        boolean result = novelGuard.canEdit(1, authentication);

        // Then
        assertFalse(result);
    }

    @Test
    void testCanHideOrUnhideWithNullAuthentication() {
        // When
        boolean result = novelGuard.canHideOrUnhide(1, null);

        // Then
        assertFalse(result);
    }

    @Test
    void testCanHideOrUnhideWithUnauthenticatedUser() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(false);

        // When
        boolean result = novelGuard.canHideOrUnhide(1, authentication);

        // Then
        assertFalse(result);
    }

    @Test
    void testCanHideOrUnhideWithAdminRole() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(Arrays.asList());

        // When
        boolean result = novelGuard.canHideOrUnhide(1, authentication);

        // Then
        assertFalse(result);
    }

    @Test
    void testCanHideOrUnhideWithPublishedStatus() {
        // Given
        testNovel.setStatus(NovelStatus.PUBLISHED.getValue());
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(Arrays.asList());
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(customUserDetails.getUserId()).thenReturn(testUserId.toString());
        when(novelMapper.selectByPrimaryKey(1)).thenReturn(testNovel);

        // When
        boolean result = novelGuard.canHideOrUnhide(1, authentication);

        // Then
        assertTrue(result);
    }

    @Test
    void testCanHideOrUnhideWithHiddenStatus() {
        // Given
        testNovel.setStatus(NovelStatus.HIDDEN.getValue());
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(Arrays.asList());
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(customUserDetails.getUserId()).thenReturn(testUserId.toString());
        when(novelMapper.selectByPrimaryKey(1)).thenReturn(testNovel);

        // When
        boolean result = novelGuard.canHideOrUnhide(1, authentication);

        // Then
        assertTrue(result);
    }

    @Test
    void testCanHideOrUnhideWithDraftStatus() {
        // Given
        testNovel.setStatus(NovelStatus.DRAFT.getValue());
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(Arrays.asList());
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(customUserDetails.getUserId()).thenReturn(testUserId.toString());
        when(novelMapper.selectByPrimaryKey(1)).thenReturn(testNovel);

        // When
        boolean result = novelGuard.canHideOrUnhide(1, authentication);

        // Then
        assertFalse(result);
    }

    @Test
    void testCanHideOrUnhideWithDifferentAuthor() {
        // Given
        UUID differentUserId = UUID.randomUUID();
        testNovel.setStatus(NovelStatus.PUBLISHED.getValue());
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(Arrays.asList());
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(customUserDetails.getUserId()).thenReturn(differentUserId.toString());
        when(novelMapper.selectByPrimaryKey(1)).thenReturn(testNovel);

        // When
        boolean result = novelGuard.canHideOrUnhide(1, authentication);

        // Then
        assertFalse(result);
    }
}
