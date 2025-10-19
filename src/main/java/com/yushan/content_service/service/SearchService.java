package com.yushan.content_service.service;

import com.yushan.content_service.dto.common.PageResponseDTO;
import com.yushan.content_service.dto.novel.NovelDetailResponseDTO;
import com.yushan.content_service.dto.novel.NovelSearchRequestDTO;
import com.yushan.content_service.dto.chapter.ChapterSummaryDTO;
import com.yushan.content_service.dto.chapter.ChapterSearchRequestDTO;
import com.yushan.content_service.dto.search.CombinedSearchResponseDTO;
import com.yushan.content_service.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Service for handling search operations.
 * Provides unified search functionality across novels and chapters.
 */
@Service
public class SearchService {

    @Autowired
    private NovelService novelService;

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired(required = false)
    private ElasticsearchSearchService elasticsearchSearchService;

    private static final String SEARCH_SUGGESTIONS_PREFIX = "search_suggestions:";

    /**
     * Combined search across novels and chapters
     */
    public CombinedSearchResponseDTO combinedSearch(String query, int page, int size, 
                                                   String type, Integer categoryId, 
                                                   String status, String authorId) {
        long startTime = System.currentTimeMillis();
        
        // Check cache first
        String cacheKey = generateCombinedSearchCacheKey(query, page, size, type, categoryId, status, authorId);
        CombinedSearchResponseDTO cachedResult = redisUtil.get(cacheKey, CombinedSearchResponseDTO.class);
        if (cachedResult != null) {
            return cachedResult;
        }

        PageResponseDTO<NovelDetailResponseDTO> novels = null;
        PageResponseDTO<ChapterSummaryDTO> chapters = null;
        long totalResults = 0;

        // Search novels if type is "all" or "novels"
        if ("all".equals(type) || "novels".equals(type)) {
            NovelSearchRequestDTO novelRequest = new NovelSearchRequestDTO(
                page, size, "relevance", "desc", categoryId, status, null, query, null, authorId
            );
            novels = novelService.listNovelsWithPagination(novelRequest);
            totalResults += novels.getTotalElements();
        }

        // Search chapters if type is "all" or "chapters"
        if ("all".equals(type) || "chapters".equals(type)) {
            ChapterSearchRequestDTO chapterRequest = new ChapterSearchRequestDTO(
                null, null, query, null, null, true, page + 1, size, "relevance", "desc"
            );
            chapters = chapterService.searchChapters(chapterRequest);
            totalResults += chapters.getTotalElements();
        }

        long searchTime = System.currentTimeMillis() - startTime;
        
        CombinedSearchResponseDTO result = new CombinedSearchResponseDTO(
            novels, chapters, totalResults, query, searchTime
        );

        // Cache the result
        redisUtil.set(cacheKey, result, 10 * 60); // 10 minutes cache


        return result;
    }

    /**
     * Search novels with advanced filtering
     * Uses Elasticsearch if available, falls back to MyBatis
     */
    public PageResponseDTO<NovelDetailResponseDTO> searchNovels(NovelSearchRequestDTO request) {
        // Check cache first
        String cacheKey = generateNovelSearchCacheKey(request);
        @SuppressWarnings("unchecked")
        PageResponseDTO<NovelDetailResponseDTO> cachedResult = redisUtil.get(cacheKey, PageResponseDTO.class);
        if (cachedResult != null) {
            return cachedResult;
        }

        PageResponseDTO<NovelDetailResponseDTO> result;
        
        try {
            // Try Elasticsearch first
            result = elasticsearchSearchService.searchNovels(request);
        } catch (Exception e) {
            // Fall back to MyBatis if Elasticsearch is not available
            result = novelService.listNovelsWithPagination(request);
        }

        // Cache the result
        redisUtil.set(cacheKey, result, 5 * 60); // 5 minutes cache


        return result;
    }

    /**
     * Search chapters with advanced filtering
     * Uses Elasticsearch if available, falls back to MyBatis
     */
    public PageResponseDTO<ChapterSummaryDTO> searchChapters(ChapterSearchRequestDTO request) {
        // Check cache first
        String cacheKey = generateChapterSearchCacheKey(request);
        @SuppressWarnings("unchecked")
        PageResponseDTO<ChapterSummaryDTO> cachedResult = redisUtil.get(cacheKey, PageResponseDTO.class);
        if (cachedResult != null) {
            return cachedResult;
        }

        PageResponseDTO<ChapterSummaryDTO> result;
        
        try {
            // Try Elasticsearch first
            result = elasticsearchSearchService.searchChapters(request);
        } catch (Exception e) {
            // Fall back to MyBatis if Elasticsearch is not available
            result = chapterService.searchChapters(request);
        }

        // Cache the result
        redisUtil.set(cacheKey, result, 5 * 60); // 5 minutes cache


        return result;
    }

    /**
     * Get search suggestions and autocomplete
     * Uses Elasticsearch if available, falls back to analytics service
     */
    public List<String> getSearchSuggestions(String query, int limit) {
        if (!StringUtils.hasText(query) || query.length() < 2) {
            return Collections.emptyList();
        }

        // Check cache first
        String cacheKey = SEARCH_SUGGESTIONS_PREFIX + query.toLowerCase() + ":" + limit;
        @SuppressWarnings("unchecked")
        List<String> cachedSuggestions = redisUtil.get(cacheKey, List.class);
        if (cachedSuggestions != null) {
            return cachedSuggestions;
        }

        List<String> suggestions;
        
        try {
            // Try Elasticsearch first
            suggestions = elasticsearchSearchService.getSearchSuggestions(query, limit);
        } catch (Exception e) {
            // Fall back to simple suggestions
            suggestions = getSimpleSearchSuggestions(query, limit);
        }

        // Cache suggestions
        redisUtil.set(cacheKey, suggestions, 30 * 60); // 30 minutes cache

        return suggestions;
    }

    // Private helper methods

    private List<String> getSimpleSearchSuggestions(String query, int limit) {
        // Simple fallback suggestions
        return Collections.emptyList();
    }


    private String generateCombinedSearchCacheKey(String query, int page, int size, String type, 
                                                Integer categoryId, String status, String authorId) {
        return String.format("combined_search:%s:%d:%d:%s:%s:%s:%s", 
                           query, page, size, type, categoryId, status, authorId);
    }

    private String generateNovelSearchCacheKey(NovelSearchRequestDTO request) {
        return String.format("novel_search:%s:%d:%d:%s:%s:%s:%s:%s", 
                           request.getSearch(), request.getPage(), request.getSize(),
                           request.getSort(), request.getOrder(), request.getCategoryId(),
                           request.getStatus(), request.getAuthorId());
    }

    private String generateChapterSearchCacheKey(ChapterSearchRequestDTO request) {
        return String.format("chapter_search:%s:%d:%d:%s:%s", 
                           request.getTitleKeyword(), request.getPage(), request.getPageSize(),
                           request.getNovelId(), request.getIsPremium());
    }
}
