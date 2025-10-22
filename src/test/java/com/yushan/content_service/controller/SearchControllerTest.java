package com.yushan.content_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yushan.content_service.dto.chapter.ChapterSearchRequestDTO;
import com.yushan.content_service.dto.chapter.ChapterSummaryDTO;
import com.yushan.content_service.dto.common.PageResponseDTO;
import com.yushan.content_service.dto.novel.NovelDetailResponseDTO;
import com.yushan.content_service.dto.novel.NovelSearchRequestDTO;
import com.yushan.content_service.dto.search.CombinedSearchResponseDTO;
import com.yushan.content_service.service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    @Mock
    private SearchService searchService;

    @InjectMocks
    private SearchController searchController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(searchController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testSearch_CombinedSearch_Success() throws Exception {
        // Given
        String query = "fantasy";
        Integer page = 0;
        Integer size = 10;
        String type = "all";
        Integer categoryId = 1;
        String status = "PUBLISHED";
        String authorId = "author123";

        CombinedSearchResponseDTO mockResponse = new CombinedSearchResponseDTO();
        PageResponseDTO<NovelDetailResponseDTO> emptyNovels = new PageResponseDTO<>();
        emptyNovels.setContent(Arrays.asList());
        emptyNovels.setTotalElements(0L);
        emptyNovels.setCurrentPage(0);
        emptyNovels.setSize(10);
        
        PageResponseDTO<ChapterSummaryDTO> emptyChapters = new PageResponseDTO<>();
        emptyChapters.setContent(Arrays.asList());
        emptyChapters.setTotalElements(0L);
        emptyChapters.setCurrentPage(0);
        emptyChapters.setSize(10);
        
        mockResponse.setNovels(emptyNovels);
        mockResponse.setChapters(emptyChapters);
        mockResponse.setTotalResults(0L);

        when(searchService.combinedSearch(query, page, size, type, categoryId, status, authorId))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/search")
                .param("q", query)
                .param("page", page.toString())
                .param("size", size.toString())
                .param("type", type)
                .param("category", categoryId.toString())
                .param("status", status)
                .param("authorId", authorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Search completed successfully"))
                .andExpect(jsonPath("$.data").exists());

        verify(searchService).combinedSearch(query, page, size, type, categoryId, status, authorId);
    }

    @Test
    void testSearch_WithMinimalParams_Success() throws Exception {
        // Given
        String query = "test";

        CombinedSearchResponseDTO mockResponse = new CombinedSearchResponseDTO();
        PageResponseDTO<NovelDetailResponseDTO> emptyNovels = new PageResponseDTO<>();
        emptyNovels.setContent(Arrays.asList());
        emptyNovels.setTotalElements(0L);
        emptyNovels.setCurrentPage(0);
        emptyNovels.setSize(10);
        
        PageResponseDTO<ChapterSummaryDTO> emptyChapters = new PageResponseDTO<>();
        emptyChapters.setContent(Arrays.asList());
        emptyChapters.setTotalElements(0L);
        emptyChapters.setCurrentPage(0);
        emptyChapters.setSize(10);
        
        mockResponse.setNovels(emptyNovels);
        mockResponse.setChapters(emptyChapters);
        mockResponse.setTotalResults(0L);

        when(searchService.combinedSearch(eq(query), eq(0), eq(10), eq("all"), isNull(), isNull(), isNull()))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/search")
                .param("q", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Search completed successfully"));

        verify(searchService).combinedSearch(eq(query), eq(0), eq(10), eq("all"), isNull(), isNull(), isNull());
    }

    @Test
    void testSearchNovels_Success() throws Exception {
        // Given
        String query = "adventure";
        Integer page = 0;
        Integer size = 10;
        String sort = "title";
        String order = "asc";
        Integer categoryId = 2;
        String status = "PUBLISHED";
        Boolean isCompleted = true;
        String authorId = "author456";
        Float minRating = 4.0f;
        Long minWordCount = 1000L;
        Long maxWordCount = 10000L;

        NovelDetailResponseDTO novel = new NovelDetailResponseDTO();
        novel.setId(1);
        novel.setUuid(UUID.randomUUID());
        novel.setTitle("Adventure Novel");
        novel.setAuthorUsername("Test Author");
        novel.setCategoryId(2);
        novel.setSynopsis("An adventure story");
        novel.setCoverImgUrl("cover.jpg");
        novel.setStatus("PUBLISHED");
        novel.setIsCompleted(true);
        novel.setChapterCnt(10);
        novel.setWordCnt(5000L);
        novel.setAvgRating(4.5f);
        novel.setReviewCnt(25);
        novel.setViewCnt(1000L);
        novel.setVoteCnt(50);
        novel.setYuanCnt(0.0f);
        novel.setCreateTime(new Date());
        novel.setUpdateTime(new Date());
        novel.setPublishTime(new Date());

        PageResponseDTO<NovelDetailResponseDTO> mockResponse = new PageResponseDTO<>();
        mockResponse.setContent(Arrays.asList(novel));
        mockResponse.setTotalElements(1L);
        mockResponse.setTotalPages(1);
        mockResponse.setSize(10);
        mockResponse.setCurrentPage(0);

        when(searchService.searchNovels(any(NovelSearchRequestDTO.class))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/search/novels")
                .param("q", query)
                .param("page", page.toString())
                .param("size", size.toString())
                .param("sort", sort)
                .param("order", order)
                .param("category", categoryId.toString())
                .param("status", status)
                .param("isCompleted", isCompleted.toString())
                .param("authorId", authorId)
                .param("minRating", minRating.toString())
                .param("minWordCount", minWordCount.toString())
                .param("maxWordCount", maxWordCount.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Novel search completed successfully"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].title").value("Adventure Novel"));

        verify(searchService).searchNovels(any(NovelSearchRequestDTO.class));
    }

    @Test
    void testSearchNovels_WithMinimalParams_Success() throws Exception {
        // Given
        String query = "test";

        PageResponseDTO<NovelDetailResponseDTO> mockResponse = new PageResponseDTO<>();
        mockResponse.setContent(Arrays.asList());
        mockResponse.setTotalElements(0L);
        mockResponse.setTotalPages(0);
        mockResponse.setSize(10);
        mockResponse.setCurrentPage(0);

        when(searchService.searchNovels(any(NovelSearchRequestDTO.class))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/search/novels")
                .param("q", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Novel search completed successfully"));

        verify(searchService).searchNovels(any(NovelSearchRequestDTO.class));
    }

    @Test
    void testSearchChapters_Success() throws Exception {
        // Given
        String query = "magic";
        Integer page = 0;
        Integer size = 20;
        Integer novelId = 1;
        Boolean isPremium = false;
        Boolean publishedOnly = true;

        ChapterSummaryDTO chapter = new ChapterSummaryDTO();
        chapter.setId(1);
        chapter.setUuid(UUID.randomUUID());
        chapter.setNovelId(1);
        chapter.setChapterNumber(1);
        chapter.setTitle("Magic Chapter");
        chapter.setPreview("A chapter about magic");
        chapter.setWordCnt(500);
        chapter.setIsPremium(false);
        chapter.setYuanCost(0.0f);
        chapter.setViewCnt(100L);
        chapter.setIsValid(true);
        chapter.setCreateTime(new Date());
        chapter.setUpdateTime(new Date());
        chapter.setPublishTime(new Date());

        PageResponseDTO<ChapterSummaryDTO> mockResponse = new PageResponseDTO<>();
        mockResponse.setContent(Arrays.asList(chapter));
        mockResponse.setTotalElements(1L);
        mockResponse.setTotalPages(1);
        mockResponse.setSize(20);
        mockResponse.setCurrentPage(0);

        when(searchService.searchChapters(any(ChapterSearchRequestDTO.class))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/search/chapters")
                .param("q", query)
                .param("page", page.toString())
                .param("size", size.toString())
                .param("novelId", novelId.toString())
                .param("isPremium", isPremium.toString())
                .param("publishedOnly", publishedOnly.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Chapter search completed successfully"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].title").value("Magic Chapter"));

        verify(searchService).searchChapters(any(ChapterSearchRequestDTO.class));
    }

    @Test
    void testSearchChapters_WithMinimalParams_Success() throws Exception {
        // Given
        String query = "test";

        PageResponseDTO<ChapterSummaryDTO> mockResponse = new PageResponseDTO<>();
        mockResponse.setContent(Arrays.asList());
        mockResponse.setTotalElements(0L);
        mockResponse.setTotalPages(0);
        mockResponse.setSize(20);
        mockResponse.setCurrentPage(0);

        when(searchService.searchChapters(any(ChapterSearchRequestDTO.class))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/search/chapters")
                .param("q", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Chapter search completed successfully"));

        verify(searchService).searchChapters(any(ChapterSearchRequestDTO.class));
    }

    @Test
    void testGetSearchSuggestions_Success() throws Exception {
        // Given
        String query = "fan";
        Integer limit = 10;

        List<String> suggestions = Arrays.asList("fantasy", "fanfiction", "fantastic");

        when(searchService.getSearchSuggestions(query, limit)).thenReturn(suggestions);

        // When & Then
        mockMvc.perform(get("/api/v1/search/suggestions")
                .param("q", query)
                .param("limit", limit.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Search suggestions retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0]").value("fantasy"));

        verify(searchService).getSearchSuggestions(query, limit);
    }

    @Test
    void testGetSearchSuggestions_WithMinimalParams_Success() throws Exception {
        // Given
        String query = "test";

        List<String> suggestions = Arrays.asList("test", "testing", "tested");

        when(searchService.getSearchSuggestions(eq(query), eq(10))).thenReturn(suggestions);

        // When & Then
        mockMvc.perform(get("/api/v1/search/suggestions")
                .param("q", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Search suggestions retrieved successfully"));

        verify(searchService).getSearchSuggestions(eq(query), eq(10));
    }

    @Test
    void testSearch_WithEmptyQuery_Success() throws Exception {
        // Given
        String query = "";

        CombinedSearchResponseDTO mockResponse = new CombinedSearchResponseDTO();
        PageResponseDTO<NovelDetailResponseDTO> emptyNovels = new PageResponseDTO<>();
        emptyNovels.setContent(Arrays.asList());
        emptyNovels.setTotalElements(0L);
        emptyNovels.setCurrentPage(0);
        emptyNovels.setSize(10);
        
        PageResponseDTO<ChapterSummaryDTO> emptyChapters = new PageResponseDTO<>();
        emptyChapters.setContent(Arrays.asList());
        emptyChapters.setTotalElements(0L);
        emptyChapters.setCurrentPage(0);
        emptyChapters.setSize(10);
        
        mockResponse.setNovels(emptyNovels);
        mockResponse.setChapters(emptyChapters);
        mockResponse.setTotalResults(0L);

        when(searchService.combinedSearch(eq(query), eq(0), eq(10), eq("all"), isNull(), isNull(), isNull()))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/search")
                .param("q", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Search completed successfully"));

        verify(searchService).combinedSearch(eq(query), eq(0), eq(10), eq("all"), isNull(), isNull(), isNull());
    }

    @Test
    void testSearchNovels_WithAllFilters_Success() throws Exception {
        // Given
        String query = "comprehensive search";
        Integer page = 1;
        Integer size = 5;
        String sort = "rating";
        String order = "desc";
        Integer categoryId = 3;
        String status = "COMPLETED";
        Boolean isCompleted = true;
        String authorId = "author789";
        Float minRating = 3.5f;
        Long minWordCount = 2000L;
        Long maxWordCount = 50000L;

        PageResponseDTO<NovelDetailResponseDTO> mockResponse = new PageResponseDTO<>();
        mockResponse.setContent(Arrays.asList());
        mockResponse.setTotalElements(0L);
        mockResponse.setTotalPages(0);
        mockResponse.setSize(5);
        mockResponse.setCurrentPage(1);

        when(searchService.searchNovels(any(NovelSearchRequestDTO.class))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/search/novels")
                .param("q", query)
                .param("page", page.toString())
                .param("size", size.toString())
                .param("sort", sort)
                .param("order", order)
                .param("category", categoryId.toString())
                .param("status", status)
                .param("isCompleted", isCompleted.toString())
                .param("authorId", authorId)
                .param("minRating", minRating.toString())
                .param("minWordCount", minWordCount.toString())
                .param("maxWordCount", maxWordCount.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Novel search completed successfully"));

        verify(searchService).searchNovels(any(NovelSearchRequestDTO.class));
    }
}
