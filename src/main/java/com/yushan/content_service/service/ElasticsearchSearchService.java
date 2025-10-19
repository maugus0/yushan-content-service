package com.yushan.content_service.service;

import com.yushan.content_service.dto.common.PageResponseDTO;
import com.yushan.content_service.dto.novel.NovelDetailResponseDTO;
import com.yushan.content_service.dto.novel.NovelSearchRequestDTO;
import com.yushan.content_service.dto.chapter.ChapterSummaryDTO;
import com.yushan.content_service.dto.chapter.ChapterSearchRequestDTO;
import com.yushan.content_service.entity.elasticsearch.NovelDocument;
import com.yushan.content_service.entity.elasticsearch.ChapterDocument;
import com.yushan.content_service.repository.elasticsearch.NovelElasticsearchRepository;
import com.yushan.content_service.repository.elasticsearch.ChapterElasticsearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for handling Elasticsearch-based search operations.
 * Provides advanced search functionality using Elasticsearch.
 */
@Service
@ConditionalOnProperty(name = "search.elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class ElasticsearchSearchService {

    @Autowired
    private NovelElasticsearchRepository novelElasticsearchRepository;

    @Autowired
    private ChapterElasticsearchRepository chapterElasticsearchRepository;

    /**
     * Search novels using Elasticsearch
     */
    public PageResponseDTO<NovelDetailResponseDTO> searchNovels(NovelSearchRequestDTO request) {
        // Create pageable for Elasticsearch
        Pageable pageable = createPageable(request);
        
        Page<NovelDocument> novelDocuments;
        
        if (StringUtils.hasText(request.getSearch())) {
            // Use Elasticsearch text search
            novelDocuments = novelElasticsearchRepository.searchByText(request.getSearch(), pageable);
        } else {
            // Use Elasticsearch filters
            novelDocuments = searchNovelsWithFilters(request, pageable);
        }
        
        // Convert Elasticsearch documents to DTOs
        List<NovelDetailResponseDTO> novelDTOs = novelDocuments.getContent().stream()
                .map(this::convertToNovelDTO)
                .collect(Collectors.toList());
        
        return new PageResponseDTO<>(
            novelDTOs, 
            novelDocuments.getTotalElements(), 
            request.getPage(), 
            request.getSize()
        );
    }

    /**
     * Search chapters using Elasticsearch
     */
    public PageResponseDTO<ChapterSummaryDTO> searchChapters(ChapterSearchRequestDTO request) {
        // Create pageable for Elasticsearch
        Pageable pageable = createPageableForChapters(request);
        
        Page<ChapterDocument> chapterDocuments;
        
        if (StringUtils.hasText(request.getTitleKeyword())) {
            // Use Elasticsearch text search
            chapterDocuments = chapterElasticsearchRepository.searchByText(request.getTitleKeyword(), pageable);
        } else {
            // Use Elasticsearch filters
            chapterDocuments = searchChaptersWithFilters(request, pageable);
        }
        
        // Convert Elasticsearch documents to DTOs
        List<ChapterSummaryDTO> chapterDTOs = chapterDocuments.getContent().stream()
                .map(this::convertToChapterDTO)
                .collect(Collectors.toList());
        
        return new PageResponseDTO<>(
            chapterDTOs, 
            chapterDocuments.getTotalElements(), 
            request.getPage() - 1, 
            request.getPageSize()
        );
    }

    /**
     * Get search suggestions using Elasticsearch
     */
    public List<String> getSearchSuggestions(String query, int limit) {
        if (!StringUtils.hasText(query) || query.length() < 2) {
            return Collections.emptyList();
        }

        Pageable pageable = PageRequest.of(0, limit);
        
        // Get suggestions from novels
        List<NovelDocument> novelSuggestions = novelElasticsearchRepository.searchSuggestions(query, pageable);
        
        // Get suggestions from chapters
        List<ChapterDocument> chapterSuggestions = chapterElasticsearchRepository.searchSuggestions(query, pageable);
        
        // Combine and deduplicate suggestions
        Set<String> suggestions = new HashSet<>();
        
        novelSuggestions.forEach(novel -> {
            if (novel.getTitle().toLowerCase().contains(query.toLowerCase())) {
                suggestions.add(novel.getTitle());
            }
            if (novel.getAuthorName().toLowerCase().contains(query.toLowerCase())) {
                suggestions.add(novel.getAuthorName());
            }
        });
        
        chapterSuggestions.forEach(chapter -> {
            if (chapter.getTitle().toLowerCase().contains(query.toLowerCase())) {
                suggestions.add(chapter.getTitle());
            }
        });
        
        return suggestions.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }


    // Private helper methods

    private Pageable createPageable(NovelSearchRequestDTO request) {
        Sort sort = createSort(request.getSort(), request.getOrder());
        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }

    private Pageable createPageableForChapters(ChapterSearchRequestDTO request) {
        Sort sort = createSortForChapters(request.getSortBy(), request.getSortOrder());
        return PageRequest.of(request.getPage() - 1, request.getPageSize(), sort);
    }

    private Sort createSort(String sortField, String order) {
        Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        switch (sortField) {
            case "relevance":
                return Sort.by(Sort.Direction.DESC, "_score");
            case "title":
                return Sort.by(direction, "title.keyword");
            case "viewCnt":
                return Sort.by(direction, "viewCnt");
            case "avgRating":
                return Sort.by(direction, "avgRating");
            case "createTime":
                return Sort.by(direction, "createTime");
            case "publishTime":
                return Sort.by(direction, "publishTime");
            case "popularity":
                return Sort.by(Sort.Direction.DESC, "viewCnt", "avgRating");
            default:
                return Sort.by(Sort.Direction.DESC, "createTime");
        }
    }

    private Sort createSortForChapters(String sortField, String order) {
        Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        switch (sortField) {
            case "relevance":
                return Sort.by(Sort.Direction.DESC, "_score");
            case "title":
                return Sort.by(direction, "title.keyword");
            case "chapterNumber":
                return Sort.by(direction, "chapterNumber");
            case "viewCnt":
                return Sort.by(direction, "viewCnt");
            case "publishTime":
                return Sort.by(direction, "publishTime");
            default:
                return Sort.by(Sort.Direction.ASC, "chapterNumber");
        }
    }

    private Page<NovelDocument> searchNovelsWithFilters(NovelSearchRequestDTO request, Pageable pageable) {
        // This is a simplified version - in production, you'd use more complex queries
        if (request.getCategoryId() != null) {
            return Page.empty();
        }
        if (request.getStatus() != null) {
            return Page.empty();
        }
        if (request.getIsCompleted() != null) {
            return Page.empty();
        }
        if (request.getAuthorId() != null) {
            return Page.empty();
        }
        
        // Return all novels if no filters
        return novelElasticsearchRepository.findAll(pageable);
    }

    private Page<ChapterDocument> searchChaptersWithFilters(ChapterSearchRequestDTO request, Pageable pageable) {
        if (request.getNovelId() != null) {
            return chapterElasticsearchRepository.findByNovelId(request.getNovelId(), pageable);
        }
        if (request.getIsPremium() != null) {
            return Page.empty();
        }
        if (request.getIsValid() != null) {
            return Page.empty();
        }
        
        // Return all chapters if no filters
        return chapterElasticsearchRepository.findAll(pageable);
    }

    private NovelDetailResponseDTO convertToNovelDTO(NovelDocument document) {
        // Convert Elasticsearch document to DTO
        // This is a simplified conversion - you might need to fetch additional data from database
        NovelDetailResponseDTO dto = new NovelDetailResponseDTO();
        dto.setId(Integer.valueOf(document.getId()));
        dto.setUuid(UUID.fromString(document.getUuid()));
        dto.setTitle(document.getTitle());
        dto.setSynopsis(document.getSynopsis());
        dto.setAuthorId(UUID.fromString(document.getAuthorId()));
        dto.setAuthorUsername(document.getAuthorName());
        dto.setCategoryId(document.getCategoryId());
        dto.setStatus(document.getStatus());
        dto.setIsCompleted(document.getIsCompleted());
        dto.setChapterCnt(document.getChapterCnt());
        dto.setWordCnt(document.getWordCnt());
        dto.setAvgRating(document.getAvgRating() != null ? document.getAvgRating().floatValue() : null);
        dto.setReviewCnt(document.getReviewCnt());
        dto.setViewCnt(document.getViewCnt());
        dto.setVoteCnt(document.getVoteCnt());
        dto.setYuanCnt(document.getYuanCnt() != null ? document.getYuanCnt().floatValue() : null);
        dto.setCreateTime(document.getCreateTime());
        dto.setUpdateTime(document.getUpdateTime());
        dto.setPublishTime(document.getPublishTime());
        dto.setCoverImgUrl(document.getCoverImgUrl());
        return dto;
    }

    private ChapterSummaryDTO convertToChapterDTO(ChapterDocument document) {
        // Convert Elasticsearch document to DTO
        ChapterSummaryDTO dto = new ChapterSummaryDTO();
        dto.setId(Integer.valueOf(document.getId()));
        dto.setUuid(UUID.fromString(document.getUuid()));
        dto.setNovelId(document.getNovelId());
        dto.setChapterNumber(document.getChapterNumber());
        dto.setTitle(document.getTitle());
        dto.setPreview(document.getContent() != null && document.getContent().length() > 200 
                     ? document.getContent().substring(0, 200) 
                     : document.getContent());
        dto.setWordCnt(document.getWordCnt());
        dto.setIsPremium(document.getIsPremium());
        dto.setYuanCost(document.getYuanCost() != null ? document.getYuanCost().floatValue() : null);
        dto.setViewCnt(document.getViewCnt());
        dto.setIsValid(document.getIsValid());
        dto.setCreateTime(document.getCreateTime());
        dto.setUpdateTime(document.getUpdateTime());
        dto.setPublishTime(document.getPublishTime());
        return dto;
    }

}
