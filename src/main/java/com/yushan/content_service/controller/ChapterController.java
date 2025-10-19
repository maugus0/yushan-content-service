package com.yushan.content_service.controller;

import com.yushan.content_service.dto.chapter.*;
import com.yushan.content_service.dto.common.ApiResponse;
import com.yushan.content_service.dto.common.PageResponseDTO;
import com.yushan.content_service.service.ChapterService;
import com.yushan.content_service.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for chapter management operations.
 * Provides CRUD endpoints for chapters.
 */
@RestController
@RequestMapping("/api/v1/chapters")
@CrossOrigin(origins = "*")
@Tag(name = "Chapter Management", description = "APIs for managing novel chapters")
public class ChapterController {

    @Autowired
    private ChapterService chapterService;

    /**
     * Create a new chapter
     * Author only - must be the author of the novel
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "[AUTHOR/ADMIN] Create a new chapter", description = "Creates a new chapter with the provided details.")
    public ApiResponse<ChapterDetailResponseDTO> createChapter(
            @Valid @RequestBody ChapterCreateRequestDTO requestDTO,
            Authentication authentication) {
        UUID userId = extractUserId(authentication);
        ChapterDetailResponseDTO chapter = chapterService.createChapter(userId, requestDTO);
        return ApiResponse.success("Chapter created successfully", chapter);
    }

    /**
     * Batch create chapters
     * Author only - must be the author of the novel
     */
    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "[AUTHOR/ADMIN] Batch create chapters", description = "Creates multiple chapters at once.")
    public ApiResponse<String> batchCreateChapters(
            @Valid @RequestBody ChapterBatchCreateRequestDTO requestDTO,
            Authentication authentication) {
        UUID userId = extractUserId(authentication);
        chapterService.batchCreateChapters(userId, requestDTO);
        return ApiResponse.success("Chapters created successfully");
    }

    /**
     * Get chapter by UUID (public endpoint)
     * Returns full chapter content
     */
    @GetMapping("/{uuid}")
    @Operation(summary = "[PUBLIC] Get chapter by UUID", description = "Retrieves chapter details by UUID.")
    public ApiResponse<ChapterDetailResponseDTO> getChapterByUuid(@PathVariable UUID uuid) {
        ChapterDetailResponseDTO chapter = chapterService.getChapterByUuid(uuid);
        return ApiResponse.success("Chapter retrieved successfully", chapter);
    }

    /**
     * Get chapter by novel ID and chapter number (public endpoint)
     * Alternative way to access chapters using chapter number
     */
    @GetMapping("/novel/{novelId}/number/{chapterNumber}")
    @Operation(summary = "[PUBLIC] Get chapter by novel ID and chapter number", description = "Retrieves chapter by novel ID and chapter number.")
    public ApiResponse<ChapterDetailResponseDTO> getChapterByNovelIdAndNumber(
            @Parameter(description = "Novel ID") @PathVariable Integer novelId,
            @Parameter(description = "Chapter number") @PathVariable Integer chapterNumber) {
        ChapterDetailResponseDTO chapter = chapterService.getChapterByNovelIdAndNumber(novelId, chapterNumber);
        return ApiResponse.success("Chapter retrieved successfully", chapter);
    }

    /**
     * Get all chapters for a novel with pagination (public endpoint)
     * Use publishedOnly=true for public view, false for author dashboard
     */
    @GetMapping("/novel/{novelId}")
    @Operation(summary = "[PUBLIC] Get chapters by novel ID", description = "Retrieves chapters for a specific novel.")
    public ApiResponse<PageResponseDTO<ChapterSummaryDTO>> getChaptersByNovelId(
            @Parameter(description = "Novel ID") @PathVariable Integer novelId,
            @Parameter(description = "Page number") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(description = "Page size") @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
            @Parameter(description = "Published only") @RequestParam(value = "publishedOnly", defaultValue = "true") Boolean publishedOnly) {
        PageResponseDTO<ChapterSummaryDTO> chapters = chapterService.getChaptersByNovelId(novelId, page, pageSize, publishedOnly);
        return ApiResponse.success("Chapters retrieved successfully", chapters);
    }

    /**
     * Get chapter statistics for a novel
     * Author only - for dashboard analytics
     */
    @GetMapping("/novel/{novelId}/statistics")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    @Operation(summary = "[AUTHOR/ADMIN] Get chapter statistics", description = "Retrieves statistics for chapters of a novel.")
    public ApiResponse<ChapterStatisticsResponseDTO> getChapterStatistics(
            @Parameter(description = "Novel ID") @PathVariable Integer novelId,
            Authentication authentication) {
        ChapterStatisticsResponseDTO statistics = chapterService.getChapterStatistics(novelId);
        return ApiResponse.success("Statistics retrieved successfully", statistics);
    }

    /**
     * Update chapter
     * Author only - must be the author of the novel
     */
    @PutMapping
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    @Operation(summary = "[AUTHOR/ADMIN] Update chapter", description = "Updates an existing chapter.")
    public ApiResponse<ChapterDetailResponseDTO> updateChapter(
            @Valid @RequestBody ChapterUpdateRequestDTO requestDTO,
            Authentication authentication) {
        UUID userId = extractUserId(authentication);
        ChapterDetailResponseDTO chapter = chapterService.updateChapter(userId, requestDTO);
        return ApiResponse.success("Chapter updated successfully", chapter);
    }

    /**
     * Publish/unpublish a chapter or schedule it
     * Author only - must be the author of the novel
     */
    @PatchMapping("/publish")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    @Operation(summary = "[AUTHOR/ADMIN] Publish/unpublish chapter", description = "Publishes or unpublishes a chapter.")
    public ApiResponse<String> publishChapter(
            @Valid @RequestBody ChapterPublishRequestDTO requestDTO,
            Authentication authentication) {
        UUID userId = extractUserId(authentication);
        chapterService.publishChapter(userId, requestDTO);
        return ApiResponse.success("Chapter publish status updated successfully");
    }

    /**
     * Batch publish/unpublish all chapters of a novel
     * Author only - must be the author of the novel
     */
    @PatchMapping("/novel/{novelId}/publish")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    @Operation(summary = "[AUTHOR/ADMIN] Batch publish chapters", description = "Publishes or unpublishes all chapters of a novel.")
    public ApiResponse<String> batchPublishChapters(
            @Parameter(description = "Novel ID") @PathVariable Integer novelId,
            @Parameter(description = "Publish status") @RequestParam Boolean isValid,
            Authentication authentication) {
        UUID userId = extractUserId(authentication);
        chapterService.batchPublishChapters(userId, novelId, isValid);
        return ApiResponse.success("Chapters publish status updated successfully");
    }

    /**
     * Increment view count for a chapter (public endpoint)
     * Called when a user reads a chapter
     */
    @PostMapping("/{uuid}/view")
    @Operation(summary = "[PUBLIC] Increment view count", description = "Increments the view count for a chapter.")
    public ApiResponse<String> incrementViewCount(
            @PathVariable UUID uuid,
            HttpServletRequest request) {
        
        // Extract user information from request
        UUID userId = null;
        try {
            CustomUserDetails userDetails = getCurrentUser();
            userId = UUID.fromString(userDetails.getUserId());
        } catch (Exception e) {
            // User not authenticated, use anonymous
            userId = UUID.randomUUID(); // Generate anonymous user ID
        }
        
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = getClientIpAddress(request);
        String referrer = request.getHeader("Referer");
        
        chapterService.incrementViewCount(uuid, userId, userAgent, ipAddress, referrer);
        return ApiResponse.success("View count incremented");
    }
    
    /**
     * Search chapters with dynamic criteria (public endpoint)
     */
    @GetMapping("/search")
    @Operation(summary = "[PUBLIC] Search chapters", description = "Search chapters with dynamic criteria including filtering, sorting, and pagination.")
    public ApiResponse<PageResponseDTO<ChapterSummaryDTO>> searchChapters(
            @RequestParam(required = false) Integer novelId,
            @RequestParam(required = false) Integer chapterNumber,
            @RequestParam(required = false) String titleKeyword,
            @RequestParam(required = false) Boolean isPremium,
            @RequestParam(required = false) Boolean isValid,
            @RequestParam(required = false) Boolean publishedOnly,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer pageSize,
            @RequestParam(defaultValue = "chapterNumber") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder) {
        
        ChapterSearchRequestDTO searchRequest = new ChapterSearchRequestDTO(
                novelId, chapterNumber, titleKeyword, isPremium, isValid, publishedOnly,
                page, pageSize, sortBy, sortOrder
        );
        
        PageResponseDTO<ChapterSummaryDTO> result = chapterService.searchChapters(searchRequest);
        return ApiResponse.success(result);
    }

    /**
     * Delete a chapter (soft delete)
     * Author only - must be the author of the novel
     */
    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    @Operation(summary = "[AUTHOR/ADMIN] Delete chapter", description = "Soft deletes a chapter.")
    public ApiResponse<String> deleteChapter(
            @Parameter(description = "Chapter UUID") @PathVariable UUID uuid,
            Authentication authentication) {
        UUID userId = extractUserId(authentication);
        chapterService.deleteChapter(userId, uuid);
        return ApiResponse.success("Chapter deleted successfully");
    }

    /**
     * Delete all chapters of a novel (soft delete)
     * Author only - must be the author of the novel
     * Use with caution!
     */
    @DeleteMapping("/novel/{novelId}")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    @Operation(summary = "[AUTHOR/ADMIN] Delete all chapters", description = "Soft deletes all chapters of a novel.")
    public ApiResponse<String> deleteChaptersByNovelId(
            @Parameter(description = "Novel ID") @PathVariable Integer novelId,
            Authentication authentication) {
        UUID userId = extractUserId(authentication);
        chapterService.deleteChaptersByNovelId(userId, novelId);
        return ApiResponse.success("All chapters deleted successfully");
    }

    /**
     * Admin-only: Force delete a chapter (soft delete)
     * Bypasses author ownership check
     */
    @DeleteMapping("/admin/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Force delete chapter", description = "Admin-only: Force delete a chapter bypassing author ownership check.")
    public ApiResponse<String> adminDeleteChapter(@PathVariable UUID uuid) {
        chapterService.adminDeleteChapter(uuid);
        return ApiResponse.success("Chapter deleted successfully by admin");
    }

    /**
     * Admin-only: Force delete all chapters of a novel (soft delete)
     * Bypasses author ownership check
     * Use with caution!
     */
    @DeleteMapping("/admin/novel/{novelId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Force delete all chapters", description = "Admin-only: Force delete all chapters of a novel bypassing author ownership check.")
    public ApiResponse<String> adminDeleteChaptersByNovelId(@PathVariable Integer novelId) {
        chapterService.adminDeleteChaptersByNovelId(novelId);
        return ApiResponse.success("All chapters deleted successfully by admin");
    }

    /**
     * Get next chapter UUID for navigation (public endpoint)
     * Used for "Next Chapter" button
     */
    @GetMapping("/{uuid}/next")
    @Operation(summary = "[PUBLIC] Get next chapter", description = "Retrieves the next chapter UUID for navigation.")
    public ApiResponse<UUID> getNextChapter(@PathVariable UUID uuid) {
        UUID nextUuid = chapterService.getNextChapterUuid(uuid);
        return ApiResponse.success("Next chapter retrieved", nextUuid);
    }

    /**
     * Get previous chapter UUID for navigation (public endpoint)
     * Used for "Previous Chapter" button
     */
    @GetMapping("/{uuid}/previous")
    @Operation(summary = "[PUBLIC] Get previous chapter", description = "Retrieves the previous chapter UUID for navigation.")
    public ApiResponse<UUID> getPreviousChapter(@PathVariable UUID uuid) {
        UUID prevUuid = chapterService.getPreviousChapterUuid(uuid);
        return ApiResponse.success("Previous chapter retrieved", prevUuid);
    }

    /**
     * Check if chapter exists (public endpoint)
     * Useful for validation before creating chapters
     */
    @GetMapping("/exists")
    @Operation(summary = "[PUBLIC] Check chapter existence", description = "Checks if a chapter exists for validation.")
    public ApiResponse<Boolean> chapterExists(
            @RequestParam Integer novelId,
            @RequestParam Integer chapterNumber) {
        boolean exists = chapterService.chapterExists(novelId, chapterNumber);
        return ApiResponse.success("Chapter existence checked", exists);
    }

    /**
     * Get next available chapter number for a novel
     * Author only - useful when creating new chapters
     */
    @GetMapping("/novel/{novelId}/next-number")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    @Operation(summary = "[AUTHOR/ADMIN] Get next chapter number", description = "Gets the next available chapter number for a novel.")
    public ApiResponse<Integer> getNextAvailableChapterNumber(
            @PathVariable Integer novelId,
            Authentication authentication) {
        Integer nextNumber = chapterService.getNextAvailableChapterNumber(novelId);
        return ApiResponse.success("Next chapter number retrieved", nextNumber);
    }

    /**
     * Batch get chapters by IDs
     * POST /api/v1/chapters/batch/get
     */
    @PostMapping("/batch/get")
    @Operation(summary = "[PUBLIC] Batch get chapters by IDs", description = "Retrieves multiple chapters by their IDs in a single request.")
    public ApiResponse<List<ChapterDetailResponseDTO>> getChaptersByIds(
            @RequestBody List<Integer> chapterIds) {
        // Handle empty or null IDs
        if (chapterIds == null || chapterIds.isEmpty()) {
            return ApiResponse.success("Chapters retrieved successfully", new ArrayList<>());
        }
            
        List<ChapterDetailResponseDTO> chapters = chapterService.getChaptersByIds(chapterIds);
        return ApiResponse.success("Chapters retrieved successfully", chapters);
    }

    /**
     * Get current authenticated user details
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
            return null;
        }
    }

    /**
     * Get client IP address from request headers
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Helper method to extract user ID from authentication
     */
    private UUID extractUserId(Authentication authentication) {
        Object principal = authentication != null ? authentication.getPrincipal() : null;
        if (principal instanceof CustomUserDetails) {
            CustomUserDetails cud = (CustomUserDetails) principal;
            return cud.getUserId() != null ? UUID.fromString(cud.getUserId()) : null;
        }
        return null;
    }
}