package com.yushan.content_service.controller;

import com.yushan.content_service.dto.common.ApiResponse;
import com.yushan.content_service.dto.common.PageResponseDTO;
import com.yushan.content_service.dto.novel.NovelCreateRequestDTO;
import com.yushan.content_service.dto.novel.NovelDetailResponseDTO;
import com.yushan.content_service.dto.novel.NovelSearchRequestDTO;
import com.yushan.content_service.dto.novel.NovelUpdateRequestDTO;
import com.yushan.content_service.service.NovelService;
import com.yushan.content_service.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for novel management operations.
 * Provides CRUD endpoints for novels.
 */
@RestController
@RequestMapping("/api/v1/novels")
@CrossOrigin(origins = "*")
@Tag(name = "Novel Management", description = "APIs for managing novels")
public class NovelController {

    @Autowired
    private NovelService novelService;

    /**
     * Get current user from SecurityContext
     * @return CustomUserDetails or null if not authenticated
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
     * Create a new novel
     * POST /api/v1/novels
     */
    @PostMapping
    @PreAuthorize("isAuthorOrAdmin()")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "[AUTHOR/ADMIN] Create a new novel", description = "Creates a new novel with the provided details.")
    public ApiResponse<NovelDetailResponseDTO> createNovel(
            @Valid @RequestBody NovelCreateRequestDTO request) {
        
        // Get user information from JWT token
        CustomUserDetails userDetails = getCurrentUser();
        UUID userId = UUID.fromString(userDetails.getUserId());
        String authorName = userDetails.getEmail(); // Use email as author name for now
        
        NovelDetailResponseDTO novel = novelService.createNovel(userId, authorName, request);
        return ApiResponse.success("Novel created successfully", novel);
    }

    /**
     * Get novel by ID
     * GET /api/v1/novels/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "[PUBLIC] Get novel by ID", description = "Retrieves a novel by its ID.")
    public ApiResponse<NovelDetailResponseDTO> getNovel(@PathVariable Integer id) {
        NovelDetailResponseDTO novel = novelService.getNovel(id);
        return ApiResponse.success("Novel retrieved successfully", novel);
    }

    /**
     * Get novel by UUID
     * GET /api/v1/novels/uuid/{uuid}
     */
    @GetMapping("/uuid/{uuid}")
    @Operation(summary = "[PUBLIC] Get novel by UUID", description = "Retrieves a novel by its UUID.")
    public ApiResponse<NovelDetailResponseDTO> getNovelByUuid(@PathVariable UUID uuid) {
        NovelDetailResponseDTO novel = novelService.getNovelByUuid(uuid);
        return ApiResponse.success("Novel retrieved successfully", novel);
    }

    /**
     * Update novel
     * PUT /api/v1/novels/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("@novelGuard.canEdit(#id, authentication)")
    @Operation(summary = "[AUTHOR/ADMIN] Update novel", description = "Updates an existing novel.")
    public ApiResponse<NovelDetailResponseDTO> updateNovel(
            @PathVariable Integer id,
            @Valid @RequestBody NovelUpdateRequestDTO request,
            Authentication authentication) {
        
        // Check if user is trying to change status - only admin allowed
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) {
                throw new IllegalArgumentException("Only admin can change novel status directly");
            }
        }
        
        NovelDetailResponseDTO novel = novelService.updateNovel(id, request);
        return ApiResponse.success("Novel updated successfully", novel);
    }

    /**
     * Archive novel (soft delete)
     * DELETE /api/v1/novels/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @novelGuard.canEdit(#id, authentication)")
    @Operation(summary = "[AUTHOR/ADMIN] Archive novel", description = "Archives (soft delete) a novel.")
    public ApiResponse<NovelDetailResponseDTO> archiveNovel(
            @PathVariable Integer id) {
        
        NovelDetailResponseDTO novel = novelService.archiveNovel(id);
        return ApiResponse.success("Novel archived successfully", novel);
    }

    /**
     * Increment view count
     * POST /api/v1/novels/{id}/view
     */
    @PostMapping("/{id}/view")
    @Operation(summary = "[PUBLIC] Increment view count", description = "Increments the view count for a novel.")
    public ApiResponse<String> incrementViewCount(
            @PathVariable Integer id,
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
        
        novelService.incrementViewCount(id, userId, userAgent, ipAddress);
        return ApiResponse.success("View count incremented");
    }

    /**
     * Get novels with pagination and filtering
     * GET /api/v1/novels
     */
    @GetMapping
    @Operation(summary = "[PUBLIC] Get novels with pagination (excludes ARCHIVED)", description = "Retrieves a paginated list of novels with optional filtering. Excludes ARCHIVED novels from results.")
    public ApiResponse<PageResponseDTO<NovelDetailResponseDTO>> listNovels(
            @Parameter(description = "Page number (0-based)") @RequestParam(value = "page", defaultValue = "0") Integer page,
            @Parameter(description = "Page size") @RequestParam(value = "size", defaultValue = "10") Integer size,
            @Parameter(description = "Sort field") @RequestParam(value = "sort", defaultValue = "createTime") String sort,
            @Parameter(description = "Sort order (asc/desc)") @RequestParam(value = "order", defaultValue = "desc") String order,
            @Parameter(description = "Category ID filter") @RequestParam(value = "category", required = false) Integer categoryId,
            @Parameter(description = "Status filter") @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "Completion status filter") @RequestParam(value = "isCompleted", required = false) Boolean isCompleted,
            @Parameter(description = "Search term") @RequestParam(value = "search", required = false) String search,
            @Parameter(description = "Author name filter") @RequestParam(value = "authorName", required = false) String authorName,
            @Parameter(description = "Author ID filter") @RequestParam(value = "authorId", required = false) String authorId) {
        
        // Create request DTO from query parameters
        NovelSearchRequestDTO request = new NovelSearchRequestDTO(page, size, sort, order, 
                                                              categoryId, status, isCompleted, search, authorName, authorId);
        
        PageResponseDTO<NovelDetailResponseDTO> response = novelService.listNovelsWithPagination(request);
        return ApiResponse.success("Novels retrieved successfully", response);
    }

    /**
     * Get all novels for admin view (including ARCHIVED novels)
     * GET /api/v1/novels/admin/all
     */
    @GetMapping("/admin/all")
    @PreAuthorize("isAdmin()")
    @Operation(summary = "[ADMIN] Get all novels (includes ARCHIVED)", description = "Retrieves all novels including hidden/archived ones.")
    public ApiResponse<PageResponseDTO<NovelDetailResponseDTO>> getAllNovelsAdmin(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "sort", defaultValue = "createTime") String sort,
            @RequestParam(value = "order", defaultValue = "desc") String order,
            @RequestParam(value = "category", required = false) Integer categoryId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "isCompleted", required = false) Boolean isCompleted,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "authorName", required = false) String authorName,
            @RequestParam(value = "authorId", required = false) String authorId) {
        
        // Create request DTO from query parameters
        NovelSearchRequestDTO request = new NovelSearchRequestDTO(page, size, sort, order, 
                                                              categoryId, status, isCompleted, search, authorName, authorId);
        
        PageResponseDTO<NovelDetailResponseDTO> response = novelService.getAllNovelsAdmin(request);
        return ApiResponse.success("All novels retrieved successfully for admin", response);
    }

    /**
     * Submit novel for review (Author only)
     * POST /api/v1/novels/{id}/submit-review
     */
    @PostMapping("/{id}/submit-review")
    @PreAuthorize("isAuthor()")
    @Operation(summary = "[AUTHOR] Submit novel for review", description = "Submits a novel for admin review.")
    public ApiResponse<NovelDetailResponseDTO> submitForReview(
            @PathVariable Integer id) {
        
        CustomUserDetails userDetails = getCurrentUser();
        UUID userId = UUID.fromString(userDetails.getUserId());
        NovelDetailResponseDTO novel = novelService.submitForReview(id, userId);
        return ApiResponse.success("Novel submitted for review", novel);
    }

    /**
     * Approve novel for publishing (Admin only)
     * POST /api/v1/novels/{id}/approve
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("isAdmin()")
    @Operation(summary = "[ADMIN] Approve novel", description = "Approves a novel for publication.")
    public ApiResponse<NovelDetailResponseDTO> approveNovel(@PathVariable Integer id) {
        NovelDetailResponseDTO novel = novelService.approveNovel(id);
        return ApiResponse.success("Novel approved and published", novel);
    }

    /**
     * Reject novel (Admin only)
     * POST /api/v1/novels/{id}/reject
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("isAdmin()")
    @Operation(summary = "[ADMIN] Reject novel", description = "Rejects a novel submission.")
    public ApiResponse<NovelDetailResponseDTO> rejectNovel(@PathVariable Integer id) {
        NovelDetailResponseDTO novel = novelService.rejectNovel(id);
        return ApiResponse.success("Novel rejected and returned to draft", novel);
    }

    /**
     * Hide novel (Admin only)
     * POST /api/v1/novels/{id}/hide
     */
    @PostMapping("/{id}/hide")
    @PreAuthorize("hasRole('ADMIN') or @novelGuard.canHideOrUnhide(#id, authentication)")
    @Operation(summary = "[ADMIN/AUTHOR] Hide novel", description = "Hides a novel from public view.")
    public ApiResponse<NovelDetailResponseDTO> hideNovel(@PathVariable Integer id) {
        NovelDetailResponseDTO novel = novelService.hideNovel(id);
        return ApiResponse.success("Novel hidden", novel);
    }

    /**
     * Unhide novel (Admin only)
     * POST /api/v1/novels/{id}/unhide
     */
    @PostMapping("/{id}/unhide")
    @PreAuthorize("hasRole('ADMIN') or @novelGuard.canHideOrUnhide(#id, authentication)")
    @Operation(summary = "[ADMIN/AUTHOR] Unhide novel", description = "Makes a hidden novel visible again.")
    public ApiResponse<NovelDetailResponseDTO> unhideNovel(@PathVariable Integer id) {
        NovelDetailResponseDTO novel = novelService.unhideNovel(id);
        return ApiResponse.success("Novel unhidden and published", novel);
    }

    /**
     * Get novels under review (Admin only)
     * GET /api/v1/novels/admin/under-review
     */
    @GetMapping("/admin/under-review")
    @PreAuthorize("isAdmin()")
    @Operation(summary = "[ADMIN] Get novels under review", description = "Retrieves novels pending admin review.")
    public ApiResponse<PageResponseDTO<NovelDetailResponseDTO>> getNovelsUnderReview(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        
        PageResponseDTO<NovelDetailResponseDTO> novels = novelService.getNovelsUnderReview(page, size);
        return ApiResponse.success("Novels under review retrieved", novels);
    }

    /**
     * Get novels by author
     * GET /api/v1/novels/author/{authorId}
     */
    @GetMapping("/author/{authorId}")
    @Operation(summary = "[PUBLIC] Get novels by author", description = "Retrieves all novels by a specific author.")
    public ApiResponse<List<NovelDetailResponseDTO>> getNovelsByAuthor(@PathVariable UUID authorId) {
        List<NovelDetailResponseDTO> novels = novelService.getNovelsByAuthor(authorId);
        return ApiResponse.success("Novels retrieved successfully", novels);
    }

    /**
     * Get novels by category
     * GET /api/v1/novels/category/{categoryId}
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "[PUBLIC] Get novels by category", description = "Retrieves all novels in a specific category.")
    public ApiResponse<List<NovelDetailResponseDTO>> getNovelsByCategory(@PathVariable Integer categoryId) {
        List<NovelDetailResponseDTO> novels = novelService.getNovelsByCategory(categoryId);
        return ApiResponse.success("Novels retrieved successfully", novels);
    }

    /**
     * Get novel count with filtering
     * GET /api/v1/novels/count
     */
    @GetMapping("/count")
    @Operation(summary = "[PUBLIC] Get novel count (excludes ARCHIVED)", description = "Gets the total count of novels with optional filtering. Excludes ARCHIVED novels from count.")
    public ApiResponse<Long> getNovelCount(
            @RequestParam(value = "category", required = false) Integer categoryId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "isCompleted", required = false) Boolean isCompleted,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "authorId", required = false) String authorId) {
        
        NovelSearchRequestDTO request = new NovelSearchRequestDTO();
        request.setCategoryId(categoryId);
        request.setStatus(status);
        request.setIsCompleted(isCompleted);
        request.setSearch(search);
        request.setAuthorId(authorId);
        
        long count = novelService.getNovelCount(request);
        return ApiResponse.success("Novel count retrieved successfully", count);
    }

    /**
     * Helper method to get client IP address
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
}
