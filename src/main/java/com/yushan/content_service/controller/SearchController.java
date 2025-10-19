package com.yushan.content_service.controller;

import com.yushan.content_service.dto.common.ApiResponse;
import com.yushan.content_service.dto.common.PageResponseDTO;
import com.yushan.content_service.dto.novel.NovelDetailResponseDTO;
import com.yushan.content_service.dto.novel.NovelSearchRequestDTO;
import com.yushan.content_service.dto.chapter.ChapterSummaryDTO;
import com.yushan.content_service.dto.chapter.ChapterSearchRequestDTO;
import com.yushan.content_service.dto.search.CombinedSearchResponseDTO;
import com.yushan.content_service.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

/**
 * REST Controller for search operations.
 * Provides unified search endpoints for novels and chapters.
 */
@RestController
@RequestMapping("/api/v1/search")
@CrossOrigin(origins = "*")
@Tag(name = "Search", description = "APIs for searching novels and chapters")
public class SearchController {

    @Autowired
    private SearchService searchService;

    /**
     * Combined search endpoint - searches both novels and chapters
     * GET /api/v1/search
     */
    @GetMapping
    @Operation(summary = "[PUBLIC] Combined search", description = "Search across novels and chapters with unified results.")
    public ApiResponse<CombinedSearchResponseDTO> search(
            @Parameter(description = "Search query") @RequestParam String q,
            @Parameter(description = "Page number (0-based)") @RequestParam(value = "page", defaultValue = "0") @Min(0) Integer page,
            @Parameter(description = "Page size") @RequestParam(value = "size", defaultValue = "10") @Min(1) @Max(100) Integer size,
            @Parameter(description = "Search type") @RequestParam(value = "type", defaultValue = "all") String type,
            @Parameter(description = "Category ID filter") @RequestParam(value = "category", required = false) Integer categoryId,
            @Parameter(description = "Status filter") @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "Author ID filter") @RequestParam(value = "authorId", required = false) String authorId) {
        
        CombinedSearchResponseDTO result = searchService.combinedSearch(q, page, size, type, categoryId, status, authorId);
        return ApiResponse.success("Search completed successfully", result);
    }

    /**
     * Novel-specific search endpoint
     * GET /api/v1/search/novels
     */
    @GetMapping("/novels")
    @Operation(summary = "[PUBLIC] Search novels", description = "Search novels with advanced filtering and sorting options.")
    public ApiResponse<PageResponseDTO<NovelDetailResponseDTO>> searchNovels(
            @Parameter(description = "Search query") @RequestParam String q,
            @Parameter(description = "Page number (0-based)") @RequestParam(value = "page", defaultValue = "0") @Min(0) Integer page,
            @Parameter(description = "Page size") @RequestParam(value = "size", defaultValue = "10") @Min(1) @Max(100) Integer size,
            @Parameter(description = "Sort field") @RequestParam(value = "sort", defaultValue = "relevance") String sort,
            @Parameter(description = "Sort order (asc/desc)") @RequestParam(value = "order", defaultValue = "desc") String order,
            @Parameter(description = "Category ID filter") @RequestParam(value = "category", required = false) Integer categoryId,
            @Parameter(description = "Status filter") @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "Completion status filter") @RequestParam(value = "isCompleted", required = false) Boolean isCompleted,
            @Parameter(description = "Author ID filter") @RequestParam(value = "authorId", required = false) String authorId,
            @Parameter(description = "Min rating filter") @RequestParam(value = "minRating", required = false) Float minRating,
            @Parameter(description = "Min word count filter") @RequestParam(value = "minWordCount", required = false) Long minWordCount,
            @Parameter(description = "Max word count filter") @RequestParam(value = "maxWordCount", required = false) Long maxWordCount) {
        
        NovelSearchRequestDTO request = new NovelSearchRequestDTO(page, size, sort, order, 
                                                              categoryId, status, isCompleted, q, null, authorId);
        request.setMinRating(minRating);
        request.setMinWordCount(minWordCount);
        request.setMaxWordCount(maxWordCount);
        
        PageResponseDTO<NovelDetailResponseDTO> result = searchService.searchNovels(request);
        return ApiResponse.success("Novel search completed successfully", result);
    }

    /**
     * Chapter-specific search endpoint
     * GET /api/v1/search/chapters
     */
    @GetMapping("/chapters")
    @Operation(summary = "[PUBLIC] Search chapters", description = "Search chapters with advanced filtering options.")
    public ApiResponse<PageResponseDTO<ChapterSummaryDTO>> searchChapters(
            @Parameter(description = "Search query") @RequestParam String q,
            @Parameter(description = "Page number (0-based)") @RequestParam(value = "page", defaultValue = "0") @Min(0) Integer page,
            @Parameter(description = "Page size") @RequestParam(value = "size", defaultValue = "20") @Min(1) @Max(100) Integer size,
            @Parameter(description = "Novel ID filter") @RequestParam(value = "novelId", required = false) Integer novelId,
            @Parameter(description = "Premium content filter") @RequestParam(value = "isPremium", required = false) Boolean isPremium,
            @Parameter(description = "Published only filter") @RequestParam(value = "publishedOnly", defaultValue = "true") Boolean publishedOnly) {
        
        ChapterSearchRequestDTO request = new ChapterSearchRequestDTO(
                novelId, null, q, isPremium, null, publishedOnly,
                page + 1, size, "relevance", "desc"
        );
        
        PageResponseDTO<ChapterSummaryDTO> result = searchService.searchChapters(request);
        return ApiResponse.success("Chapter search completed successfully", result);
    }

    /**
     * Search suggestions and autocomplete
     * GET /api/v1/search/suggestions
     */
    @GetMapping("/suggestions")
    @Operation(summary = "[PUBLIC] Get search suggestions", description = "Get search suggestions and autocomplete results.")
    public ApiResponse<List<String>> getSearchSuggestions(
            @Parameter(description = "Search query") @RequestParam String q,
            @Parameter(description = "Number of suggestions") @RequestParam(value = "limit", defaultValue = "10") @Min(1) @Max(50) Integer limit) {
        
        List<String> suggestions = searchService.getSearchSuggestions(q, limit);
        return ApiResponse.success("Search suggestions retrieved successfully", suggestions);
    }
}
