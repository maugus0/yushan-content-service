package com.yushan.content_service.dto.search;

import com.yushan.content_service.dto.common.PageResponseDTO;
import com.yushan.content_service.dto.novel.NovelDetailResponseDTO;
import com.yushan.content_service.dto.chapter.ChapterSummaryDTO;

/**
 * DTO for combined search response.
 * Contains search results for both novels and chapters.
 */
public class CombinedSearchResponseDTO {
    private PageResponseDTO<NovelDetailResponseDTO> novels;
    private PageResponseDTO<ChapterSummaryDTO> chapters;
    private long totalResults;
    private String searchQuery;
    private long searchTimeMs;

    // Constructors
    public CombinedSearchResponseDTO() {}

    public CombinedSearchResponseDTO(PageResponseDTO<NovelDetailResponseDTO> novels, 
                                   PageResponseDTO<ChapterSummaryDTO> chapters, 
                                   long totalResults, String searchQuery, long searchTimeMs) {
        this.novels = novels != null ? new PageResponseDTO<>(novels.getContent(), novels.getTotalElements(), novels.getCurrentPage(), novels.getSize()) : null;
        this.chapters = chapters != null ? new PageResponseDTO<>(chapters.getContent(), chapters.getTotalElements(), chapters.getCurrentPage(), chapters.getSize()) : null;
        this.totalResults = totalResults;
        this.searchQuery = searchQuery;
        this.searchTimeMs = searchTimeMs;
    }

    // Getters and setters
    public PageResponseDTO<NovelDetailResponseDTO> getNovels() { 
        return novels != null ? new PageResponseDTO<>(novels.getContent(), novels.getTotalElements(), novels.getCurrentPage(), novels.getSize()) : null;
    }
    
    public void setNovels(PageResponseDTO<NovelDetailResponseDTO> novels) { 
        this.novels = novels != null ? new PageResponseDTO<>(novels.getContent(), novels.getTotalElements(), novels.getCurrentPage(), novels.getSize()) : null;
    }
    
    public PageResponseDTO<ChapterSummaryDTO> getChapters() { 
        return chapters != null ? new PageResponseDTO<>(chapters.getContent(), chapters.getTotalElements(), chapters.getCurrentPage(), chapters.getSize()) : null;
    }
    
    public void setChapters(PageResponseDTO<ChapterSummaryDTO> chapters) { 
        this.chapters = chapters != null ? new PageResponseDTO<>(chapters.getContent(), chapters.getTotalElements(), chapters.getCurrentPage(), chapters.getSize()) : null;
    }
    
    public long getTotalResults() { 
        return totalResults; 
    }
    
    public void setTotalResults(long totalResults) { 
        this.totalResults = totalResults; 
    }
    
    public String getSearchQuery() { 
        return searchQuery; 
    }
    
    public void setSearchQuery(String searchQuery) { 
        this.searchQuery = searchQuery; 
    }
    
    public long getSearchTimeMs() { 
        return searchTimeMs; 
    }
    
    public void setSearchTimeMs(long searchTimeMs) { 
        this.searchTimeMs = searchTimeMs; 
    }
}
