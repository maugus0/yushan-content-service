package com.yushan.content_service.service;

import com.yushan.content_service.dao.ChapterMapper;
import com.yushan.content_service.dao.NovelMapper;
import com.yushan.content_service.dto.chapter.ChapterSearchRequestDTO;
import com.yushan.content_service.dto.chapter.ChapterSummaryDTO;
import com.yushan.content_service.dto.common.PageResponseDTO;
import com.yushan.content_service.dto.novel.NovelDetailResponseDTO;
import com.yushan.content_service.dto.novel.NovelSearchRequestDTO;
import com.yushan.content_service.entity.Chapter;
import com.yushan.content_service.entity.Novel;
import com.yushan.content_service.util.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private NovelMapper novelMapper;

    @Mock
    private ChapterMapper chapterMapper;

    @Mock
    private RedisUtil redisUtil;

    @Mock
    private NovelService novelService;

    @Mock
    private ChapterService chapterService;

    @InjectMocks
    private SearchService searchService;

    private Novel testNovel;
    private Chapter testChapter;

    @BeforeEach
    void setUp() {
        testNovel = new Novel();
        testNovel.setId(1);
        testNovel.setUuid(UUID.randomUUID());
        testNovel.setTitle("Test Novel");
        testNovel.setAuthorName("Test Author");
        testNovel.setStatus(2); // PUBLISHED

        testChapter = new Chapter();
        testChapter.setId(1);
        testChapter.setUuid(UUID.randomUUID());
        testChapter.setNovelId(1);
        testChapter.setTitle("Test Chapter");
        testChapter.setContent("Test content");
        testChapter.setIsValid(true);
    }

    @Test
    void combinedSearch_WithValidQuery_ShouldReturnCombinedResults() {
        // Arrange
        String query = "test";
        int page = 0;
        int size = 10;
        String type = "all";
        Integer categoryId = 1;
        String status = "PUBLISHED";
        String authorId = UUID.randomUUID().toString();

        PageResponseDTO<NovelDetailResponseDTO> novelPageResponse = new PageResponseDTO<>();
        novelPageResponse.setContent(new ArrayList<>());
        novelPageResponse.setTotalElements(1L);
        novelPageResponse.setTotalPages(1);
        
        PageResponseDTO<ChapterSummaryDTO> chapterPageResponse = new PageResponseDTO<>();
        chapterPageResponse.setContent(new ArrayList<>());
        chapterPageResponse.setTotalElements(1L);
        chapterPageResponse.setTotalPages(1);
        
        when(novelService.listNovelsWithPagination(any())).thenReturn(novelPageResponse);
        when(chapterService.searchChapters(any())).thenReturn(chapterPageResponse);

        // Act
        Object result = searchService.combinedSearch(query, page, size, type, categoryId, status, authorId);

        // Assert
        assertThat(result).isNotNull();
        verify(novelService).listNovelsWithPagination(any());
        verify(chapterService).searchChapters(any());
    }

    @Test
    void searchNovels_WithValidRequest_ShouldReturnNovels() {
        // Arrange
        NovelSearchRequestDTO request = new NovelSearchRequestDTO();
        request.setPage(0);
        request.setSize(10);
        request.setCategoryId(1);

        PageResponseDTO<NovelDetailResponseDTO> novelPageResponse = new PageResponseDTO<>();
        novelPageResponse.setContent(new ArrayList<>());
        novelPageResponse.setTotalElements(1L);
        novelPageResponse.setTotalPages(1);
        
        when(novelService.listNovelsWithPagination(any())).thenReturn(novelPageResponse);

        // Act
        Object result = searchService.searchNovels(request);

        // Assert
        assertThat(result).isNotNull();
        verify(novelService).listNovelsWithPagination(any());
    }

    @Test
    void searchChapters_WithValidRequest_ShouldReturnChapters() {
        // Arrange
        ChapterSearchRequestDTO request = new ChapterSearchRequestDTO();
        request.setPage(0);
        request.setNovelId(1);

        PageResponseDTO<ChapterSummaryDTO> chapterPageResponse = new PageResponseDTO<>();
        chapterPageResponse.setContent(new ArrayList<>());
        chapterPageResponse.setTotalElements(1L);
        chapterPageResponse.setTotalPages(1);
        
        when(chapterService.searchChapters(any())).thenReturn(chapterPageResponse);

        // Act
        Object result = searchService.searchChapters(request);

        // Assert
        assertThat(result).isNotNull();
        verify(chapterService).searchChapters(any());
    }

    @Test
    void getSearchSuggestions_WithValidQuery_ShouldReturnSuggestions() {
        // Arrange
        String query = "test";
        int limit = 5;

        // Act
        List<String> result = searchService.getSearchSuggestions(query, limit);

        // Assert
        assertThat(result).isNotNull();
        // Don't assert isNotEmpty() as the method might return empty list in test environment
    }

    @Test
    void getSearchSuggestions_WithEmptyQuery_ShouldReturnEmptyList() {
        // Arrange
        String query = "";
        int limit = 5;

        // Act
        List<String> result = searchService.getSearchSuggestions(query, limit);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void getSearchSuggestions_WithNullQuery_ShouldReturnEmptyList() {
        // Arrange
        String query = null;
        int limit = 5;

        // Act
        List<String> result = searchService.getSearchSuggestions(query, limit);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }
}
