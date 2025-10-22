package com.yushan.content_service.service;

import com.yushan.content_service.dto.chapter.ChapterSearchRequestDTO;
import com.yushan.content_service.dto.chapter.ChapterSummaryDTO;
import com.yushan.content_service.dto.common.PageResponseDTO;
import com.yushan.content_service.dto.novel.NovelDetailResponseDTO;
import com.yushan.content_service.dto.novel.NovelSearchRequestDTO;
import com.yushan.content_service.entity.elasticsearch.ChapterDocument;
import com.yushan.content_service.entity.elasticsearch.NovelDocument;
import com.yushan.content_service.repository.elasticsearch.ChapterElasticsearchRepository;
import com.yushan.content_service.repository.elasticsearch.NovelElasticsearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ElasticsearchSearchServiceTest {

    @Mock
    private NovelElasticsearchRepository novelElasticsearchRepository;

    @Mock
    private ChapterElasticsearchRepository chapterElasticsearchRepository;

    @InjectMocks
    private ElasticsearchSearchService elasticsearchSearchService;

    private NovelDocument testNovelDocument;
    private ChapterDocument testChapterDocument;
    private NovelSearchRequestDTO novelSearchRequest;
    private ChapterSearchRequestDTO chapterSearchRequest;

    @BeforeEach
    void setUp() {
        // Setup test novel document
        testNovelDocument = new NovelDocument();
        testNovelDocument.setId("1");
        testNovelDocument.setUuid("550e8400-e29b-41d4-a716-446655440000");
        testNovelDocument.setTitle("Test Novel");
        testNovelDocument.setSynopsis("Test Synopsis");
        testNovelDocument.setAuthorId("550e8400-e29b-41d4-a716-446655440001");
        testNovelDocument.setAuthorName("Test Author");
        testNovelDocument.setCategoryId(1);
        testNovelDocument.setStatus("PUBLISHED");
        testNovelDocument.setIsCompleted(false);
        testNovelDocument.setChapterCnt(10);
        testNovelDocument.setWordCnt(50000L);
        testNovelDocument.setAvgRating(4.5);
        testNovelDocument.setReviewCnt(100);
        testNovelDocument.setViewCnt(1000L);
        testNovelDocument.setVoteCnt(50);
        testNovelDocument.setYuanCnt(10.0);
        testNovelDocument.setCreateTime(new Date());
        testNovelDocument.setUpdateTime(new Date());
        testNovelDocument.setPublishTime(new Date());
        testNovelDocument.setCoverImgUrl("http://example.com/cover.jpg");

        // Setup test chapter document
        testChapterDocument = new ChapterDocument();
        testChapterDocument.setId("1");
        testChapterDocument.setUuid("550e8400-e29b-41d4-a716-446655440002");
        testChapterDocument.setNovelId(1);
        testChapterDocument.setChapterNumber(1);
        testChapterDocument.setTitle("Test Chapter");
        testChapterDocument.setContent("This is test content for the chapter");
        testChapterDocument.setWordCnt(1000);
        testChapterDocument.setIsPremium(false);
        testChapterDocument.setYuanCost(0.0);
        testChapterDocument.setViewCnt(100L);
        testChapterDocument.setIsValid(true);
        testChapterDocument.setCreateTime(new Date());
        testChapterDocument.setUpdateTime(new Date());
        testChapterDocument.setPublishTime(new Date());

        // Setup novel search request
        novelSearchRequest = new NovelSearchRequestDTO();
        novelSearchRequest.setPage(0);
        novelSearchRequest.setSize(10);
        novelSearchRequest.setSort("title");
        novelSearchRequest.setOrder("asc");

        // Setup chapter search request
        chapterSearchRequest = new ChapterSearchRequestDTO();
        chapterSearchRequest.setPage(1);
        chapterSearchRequest.setPageSize(10);
        chapterSearchRequest.setSortBy("title");
        chapterSearchRequest.setSortOrder("asc");
    }

    @Test
    void searchNovels_WithSearchText_ShouldReturnNovels() {
        // Arrange
        novelSearchRequest.setSearch("test");
        Page<NovelDocument> mockPage = new PageImpl<>(Arrays.asList(testNovelDocument));
        when(novelElasticsearchRepository.searchByText(eq("test"), any(Pageable.class))).thenReturn(mockPage);

        // Act
        PageResponseDTO<NovelDetailResponseDTO> result = elasticsearchSearchService.searchNovels(novelSearchRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Novel");
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getCurrentPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);
        
        verify(novelElasticsearchRepository).searchByText(eq("test"), any(Pageable.class));
    }

    @Test
    void searchNovels_WithoutSearchText_ShouldUseFilters() {
        // Arrange
        novelSearchRequest.setSearch(null);
        Page<NovelDocument> mockPage = new PageImpl<>(Arrays.asList(testNovelDocument));
        when(novelElasticsearchRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

        // Act
        PageResponseDTO<NovelDetailResponseDTO> result = elasticsearchSearchService.searchNovels(novelSearchRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Novel");
        
        verify(novelElasticsearchRepository).findAll(any(Pageable.class));
    }

    @Test
    void searchNovels_WithEmptySearchText_ShouldUseFilters() {
        // Arrange
        novelSearchRequest.setSearch("");
        Page<NovelDocument> mockPage = new PageImpl<>(Arrays.asList(testNovelDocument));
        when(novelElasticsearchRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

        // Act
        PageResponseDTO<NovelDetailResponseDTO> result = elasticsearchSearchService.searchNovels(novelSearchRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        
        verify(novelElasticsearchRepository).findAll(any(Pageable.class));
    }

    @Test
    void searchNovels_WithCategoryFilter_ShouldReturnEmptyPage() {
        // Arrange
        novelSearchRequest.setCategoryId(1);

        // Act
        PageResponseDTO<NovelDetailResponseDTO> result = elasticsearchSearchService.searchNovels(novelSearchRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void searchNovels_WithStatusFilter_ShouldReturnEmptyPage() {
        // Arrange
        novelSearchRequest.setStatus("DRAFT");

        // Act
        PageResponseDTO<NovelDetailResponseDTO> result = elasticsearchSearchService.searchNovels(novelSearchRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void searchNovels_WithIsCompletedFilter_ShouldReturnEmptyPage() {
        // Arrange
        novelSearchRequest.setIsCompleted(true);

        // Act
        PageResponseDTO<NovelDetailResponseDTO> result = elasticsearchSearchService.searchNovels(novelSearchRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void searchNovels_WithAuthorIdFilter_ShouldReturnEmptyPage() {
        // Arrange
        novelSearchRequest.setAuthorId("550e8400-e29b-41d4-a716-446655440001");
        // Act
        PageResponseDTO<NovelDetailResponseDTO> result = elasticsearchSearchService.searchNovels(novelSearchRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void searchChapters_WithTitleKeyword_ShouldReturnChapters() {
        // Arrange
        chapterSearchRequest.setTitleKeyword("test");
        Page<ChapterDocument> mockPage = new PageImpl<>(Arrays.asList(testChapterDocument));
        when(chapterElasticsearchRepository.searchByText(eq("test"), any(Pageable.class))).thenReturn(mockPage);

        // Act
        PageResponseDTO<ChapterSummaryDTO> result = elasticsearchSearchService.searchChapters(chapterSearchRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Chapter");
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getCurrentPage()).isEqualTo(0); // page is decremented by 1
        assertThat(result.getSize()).isEqualTo(10);
        
        verify(chapterElasticsearchRepository).searchByText(eq("test"), any(Pageable.class));
    }

    @Test
    void searchChapters_WithoutTitleKeyword_ShouldUseFilters() {
        // Arrange
        chapterSearchRequest.setTitleKeyword(null);
        Page<ChapterDocument> mockPage = new PageImpl<>(Arrays.asList(testChapterDocument));
        when(chapterElasticsearchRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

        // Act
        PageResponseDTO<ChapterSummaryDTO> result = elasticsearchSearchService.searchChapters(chapterSearchRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Chapter");
        
        verify(chapterElasticsearchRepository).findAll(any(Pageable.class));
    }

    @Test
    void searchChapters_WithNovelIdFilter_ShouldReturnChaptersForNovel() {
        // Arrange
        chapterSearchRequest.setNovelId(1);
        Page<ChapterDocument> mockPage = new PageImpl<>(Arrays.asList(testChapterDocument));
        when(chapterElasticsearchRepository.findByNovelId(eq(1), any(Pageable.class))).thenReturn(mockPage);

        // Act
        PageResponseDTO<ChapterSummaryDTO> result = elasticsearchSearchService.searchChapters(chapterSearchRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNovelId()).isEqualTo(1);
        
        verify(chapterElasticsearchRepository).findByNovelId(eq(1), any(Pageable.class));
    }

    @Test
    void searchChapters_WithIsPremiumFilter_ShouldReturnEmptyPage() {
        // Arrange
        chapterSearchRequest.setIsPremium(true);

        // Act
        PageResponseDTO<ChapterSummaryDTO> result = elasticsearchSearchService.searchChapters(chapterSearchRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void searchChapters_WithIsValidFilter_ShouldReturnEmptyPage() {
        // Arrange
        chapterSearchRequest.setIsValid(false);

        // Act
        PageResponseDTO<ChapterSummaryDTO> result = elasticsearchSearchService.searchChapters(chapterSearchRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void getSearchSuggestions_WithValidQuery_ShouldReturnSuggestions() {
        // Arrange
        String query = "test";
        int limit = 5;
        List<NovelDocument> novelSuggestions = Arrays.asList(testNovelDocument);
        List<ChapterDocument> chapterSuggestions = Arrays.asList(testChapterDocument);
        
        when(novelElasticsearchRepository.searchSuggestions(eq(query), any(Pageable.class))).thenReturn(novelSuggestions);
        when(chapterElasticsearchRepository.searchSuggestions(eq(query), any(Pageable.class))).thenReturn(chapterSuggestions);

        // Act
        List<String> result = elasticsearchSearchService.getSearchSuggestions(query, limit);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).contains("Test Novel");
        assertThat(result).contains("Test Author");
        assertThat(result).contains("Test Chapter");
        
        verify(novelElasticsearchRepository).searchSuggestions(eq(query), any(Pageable.class));
        verify(chapterElasticsearchRepository).searchSuggestions(eq(query), any(Pageable.class));
    }

    @Test
    void getSearchSuggestions_WithEmptyQuery_ShouldReturnEmptyList() {
        // Arrange
        String query = "";
        int limit = 5;

        // Act
        List<String> result = elasticsearchSearchService.getSearchSuggestions(query, limit);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        
        verify(novelElasticsearchRepository, never()).searchSuggestions(anyString(), any(Pageable.class));
        verify(chapterElasticsearchRepository, never()).searchSuggestions(anyString(), any(Pageable.class));
    }

    @Test
    void getSearchSuggestions_WithNullQuery_ShouldReturnEmptyList() {
        // Arrange
        String query = null;
        int limit = 5;

        // Act
        List<String> result = elasticsearchSearchService.getSearchSuggestions(query, limit);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        
        verify(novelElasticsearchRepository, never()).searchSuggestions(anyString(), any(Pageable.class));
        verify(chapterElasticsearchRepository, never()).searchSuggestions(anyString(), any(Pageable.class));
    }

    @Test
    void getSearchSuggestions_WithShortQuery_ShouldReturnEmptyList() {
        // Arrange
        String query = "a";
        int limit = 5;

        // Act
        List<String> result = elasticsearchSearchService.getSearchSuggestions(query, limit);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        
        verify(novelElasticsearchRepository, never()).searchSuggestions(anyString(), any(Pageable.class));
        verify(chapterElasticsearchRepository, never()).searchSuggestions(anyString(), any(Pageable.class));
    }

    @Test
    void getSearchSuggestions_WithLimit_ShouldRespectLimit() {
        // Arrange
        String query = "test";
        int limit = 2;
        List<NovelDocument> novelSuggestions = Arrays.asList(testNovelDocument);
        List<ChapterDocument> chapterSuggestions = Arrays.asList(testChapterDocument);
        
        when(novelElasticsearchRepository.searchSuggestions(eq(query), any(Pageable.class))).thenReturn(novelSuggestions);
        when(chapterElasticsearchRepository.searchSuggestions(eq(query), any(Pageable.class))).thenReturn(chapterSuggestions);

        // Act
        List<String> result = elasticsearchSearchService.getSearchSuggestions(query, limit);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSizeLessThanOrEqualTo(limit);
    }

    @Test
    void getSearchSuggestions_WithNoMatchingSuggestions_ShouldReturnEmptyList() {
        // Arrange
        String query = "nonexistent";
        int limit = 5;
        
        NovelDocument novelDoc = new NovelDocument();
        novelDoc.setTitle("Different Title");
        novelDoc.setAuthorName("Different Author");
        
        ChapterDocument chapterDoc = new ChapterDocument();
        chapterDoc.setTitle("Different Chapter");
        
        when(novelElasticsearchRepository.searchSuggestions(eq(query), any(Pageable.class))).thenReturn(Arrays.asList(novelDoc));
        when(chapterElasticsearchRepository.searchSuggestions(eq(query), any(Pageable.class))).thenReturn(Arrays.asList(chapterDoc));

        // Act
        List<String> result = elasticsearchSearchService.getSearchSuggestions(query, limit);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }
}
