package com.yushan.content_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yushan.content_service.config.TestSecurityContextConfig;
import com.yushan.content_service.dto.chapter.*;
import com.yushan.content_service.dto.common.PageResponseDTO;
import com.yushan.content_service.service.ChapterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Simple unit tests for ChapterController with mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
public class ChapterControllerTest {

    @Mock
    private ChapterService chapterService;

    @InjectMocks
    private ChapterController chapterController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(chapterController).build();
        objectMapper = new ObjectMapper();
        TestSecurityContextConfig.setupMockUser();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        TestSecurityContextConfig.clearSecurityContext();
    }

    @Test
    void testGetChapterByUuid_Success() throws Exception {
        // Given
        UUID chapterUuid = UUID.randomUUID();
        ChapterDetailResponseDTO responseDTO = new ChapterDetailResponseDTO();
        responseDTO.setId(1);
        responseDTO.setUuid(chapterUuid);
        responseDTO.setNovelId(1);
        responseDTO.setChapterNumber(1);
        responseDTO.setTitle("Test Chapter");
        responseDTO.setContent("Test content");
        responseDTO.setPreview("Test content");
        responseDTO.setWordCnt(10);
        responseDTO.setIsPremium(false);
        responseDTO.setYuanCost(0.0f);
        responseDTO.setViewCnt(0L);
        responseDTO.setIsValid(true);
        responseDTO.setCreateTime(new Date());
        responseDTO.setUpdateTime(new Date());
        responseDTO.setPublishTime(new Date());

        when(chapterService.getChapterByUuid(chapterUuid)).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(get("/api/v1/chapters/{uuid}", chapterUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Chapter retrieved successfully"))
                .andExpect(jsonPath("$.data.title").value("Test Chapter"))
                .andExpect(jsonPath("$.data.content").value("Test content"))
                .andExpect(jsonPath("$.data.wordCnt").value(10));

        verify(chapterService).getChapterByUuid(chapterUuid);
    }

    @Test
    void testGetChaptersByNovelId_Success() throws Exception {
        // Given
        Integer novelId = 1;
        int page = 1;
        int pageSize = 5;
        boolean publishedOnly = true;

        ChapterSummaryDTO chapter1 = new ChapterSummaryDTO();
        chapter1.setId(1);
        chapter1.setUuid(UUID.randomUUID());
        chapter1.setNovelId(1);
        chapter1.setChapterNumber(1);
        chapter1.setTitle("Chapter 1");
        chapter1.setPreview("Content 1");
        chapter1.setWordCnt(10);
        chapter1.setIsPremium(false);
        chapter1.setYuanCost(0.0f);
        chapter1.setViewCnt(100L);
        chapter1.setIsValid(true);
        chapter1.setCreateTime(new Date());
        chapter1.setUpdateTime(new Date());
        chapter1.setPublishTime(new Date());

        ChapterSummaryDTO chapter2 = new ChapterSummaryDTO();
        chapter2.setId(2);
        chapter2.setUuid(UUID.randomUUID());
        chapter2.setNovelId(1);
        chapter2.setChapterNumber(2);
        chapter2.setTitle("Chapter 2");
        chapter2.setPreview("Content 2");
        chapter2.setWordCnt(15);
        chapter2.setIsPremium(false);
        chapter2.setYuanCost(0.0f);
        chapter2.setViewCnt(200L);
        chapter2.setIsValid(true);
        chapter2.setCreateTime(new Date());
        chapter2.setUpdateTime(new Date());
        chapter2.setPublishTime(new Date());

        PageResponseDTO<ChapterSummaryDTO> pageResponse = new PageResponseDTO<>(
                Arrays.asList(chapter1, chapter2), 2L, 0, 5
        );

        when(chapterService.getChaptersByNovelId(novelId, page, pageSize, publishedOnly))
                .thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/chapters/novel/{novelId}", novelId)
                .param("page", String.valueOf(page))
                .param("pageSize", String.valueOf(pageSize))
                .param("publishedOnly", String.valueOf(publishedOnly)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Chapters retrieved successfully"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.currentPage").value(0))
                .andExpect(jsonPath("$.data.size").value(5));

        verify(chapterService).getChaptersByNovelId(novelId, page, pageSize, publishedOnly);
    }

    @Test
    void testGetChapterStatistics_Success() throws Exception {
        // Given
        Integer novelId = 1;
        ChapterStatisticsResponseDTO responseDTO = new ChapterStatisticsResponseDTO();
        responseDTO.setTotalChapters(2L);
        responseDTO.setTotalWordCount(25L);
        responseDTO.setTotalViewCount(300L);
        responseDTO.setTotalRevenue(5.0f);
        responseDTO.setFreeChapters(1L);
        responseDTO.setPremiumChapters(1L);

        when(chapterService.getChapterStatistics(novelId)).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(get("/api/v1/chapters/novel/{novelId}/statistics", novelId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Statistics retrieved successfully"))
                .andExpect(jsonPath("$.data.totalChapters").value(2))
                .andExpect(jsonPath("$.data.totalWordCount").value(25))
                .andExpect(jsonPath("$.data.totalViewCount").value(300))
                .andExpect(jsonPath("$.data.totalRevenue").value(5.0))
                .andExpect(jsonPath("$.data.freeChapters").value(1))
                .andExpect(jsonPath("$.data.premiumChapters").value(1));

        verify(chapterService).getChapterStatistics(novelId);
    }

    @Test
    void testCreateChapter_Success() throws Exception {
        // Given
        ChapterCreateRequestDTO requestDTO = new ChapterCreateRequestDTO();
        requestDTO.setNovelId(1);
        requestDTO.setChapterNumber(1);
        requestDTO.setTitle("New Chapter");
        requestDTO.setContent("New chapter content");
        requestDTO.setWordCnt(100);
        requestDTO.setIsPremium(false);
        requestDTO.setYuanCost(0.0f);

        ChapterDetailResponseDTO responseDTO = new ChapterDetailResponseDTO();
        responseDTO.setId(1);
        responseDTO.setUuid(UUID.randomUUID());
        responseDTO.setNovelId(1);
        responseDTO.setChapterNumber(1);
        responseDTO.setTitle("New Chapter");
        responseDTO.setContent("New chapter content");
        responseDTO.setPreview("New chapter content");
        responseDTO.setWordCnt(100);
        responseDTO.setIsPremium(false);
        responseDTO.setYuanCost(0.0f);
        responseDTO.setViewCnt(0L);
        responseDTO.setIsValid(true);
        responseDTO.setCreateTime(new Date());
        responseDTO.setUpdateTime(new Date());
        responseDTO.setPublishTime(new Date());

        lenient().doReturn(responseDTO).when(chapterService).createChapter(any(), any());

        // When & Then
        mockMvc.perform(post("/api/v1/chapters")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Chapter created successfully"))
                .andExpect(jsonPath("$.data.title").value("New Chapter"))
                .andExpect(jsonPath("$.data.content").value("New chapter content"))
                .andExpect(jsonPath("$.data.wordCnt").value(100));

        verify(chapterService).createChapter(isNull(), any(ChapterCreateRequestDTO.class));
    }

    @Test
    void testUpdateChapter_Success() throws Exception {
        // Given
        UUID chapterUuid = UUID.randomUUID();
        ChapterUpdateRequestDTO requestDTO = new ChapterUpdateRequestDTO();
        requestDTO.setUuid(chapterUuid);
        requestDTO.setTitle("Updated Chapter");
        requestDTO.setContent("Updated chapter content");
        requestDTO.setWordCnt(150);

        ChapterDetailResponseDTO responseDTO = new ChapterDetailResponseDTO();
        responseDTO.setId(1);
        responseDTO.setUuid(chapterUuid);
        responseDTO.setNovelId(1);
        responseDTO.setChapterNumber(1);
        responseDTO.setTitle("Updated Chapter");
        responseDTO.setContent("Updated chapter content");
        responseDTO.setPreview("Updated chapter content");
        responseDTO.setWordCnt(150);
        responseDTO.setIsPremium(false);
        responseDTO.setYuanCost(0.0f);
        responseDTO.setViewCnt(0L);
        responseDTO.setIsValid(true);
        responseDTO.setCreateTime(new Date());
        responseDTO.setUpdateTime(new Date());
        responseDTO.setPublishTime(new Date());

        lenient().doReturn(responseDTO).when(chapterService).updateChapter(any(), any());

        // When & Then
        mockMvc.perform(put("/api/v1/chapters")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Chapter updated successfully"))
                .andExpect(jsonPath("$.data.title").value("Updated Chapter"))
                .andExpect(jsonPath("$.data.content").value("Updated chapter content"))
                .andExpect(jsonPath("$.data.wordCnt").value(150));

        verify(chapterService).updateChapter(isNull(), any(ChapterUpdateRequestDTO.class));
    }

    @Test
    void testDeleteChapter_Success() throws Exception {
        // Given
        UUID chapterUuid = UUID.randomUUID();

        lenient().doNothing().when(chapterService).deleteChapter(any(), any());

        // When & Then
        mockMvc.perform(delete("/api/v1/chapters/{uuid}", chapterUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Chapter deleted successfully"));

        verify(chapterService).deleteChapter(isNull(), any(UUID.class));
    }

    @Test
    void testGetChapterByNovelIdAndNumber_Success() throws Exception {
        // Given
        Integer novelId = 1;
        Integer chapterNumber = 1;

        ChapterDetailResponseDTO responseDTO = new ChapterDetailResponseDTO();
        responseDTO.setId(1);
        responseDTO.setUuid(UUID.randomUUID());
        responseDTO.setNovelId(1);
        responseDTO.setChapterNumber(1);
        responseDTO.setTitle("Chapter 1");
        responseDTO.setContent("Chapter 1 content");
        responseDTO.setPreview("Chapter 1 content");
        responseDTO.setWordCnt(100);
        responseDTO.setIsPremium(false);
        responseDTO.setYuanCost(0.0f);
        responseDTO.setViewCnt(0L);
        responseDTO.setIsValid(true);
        responseDTO.setCreateTime(new Date());
        responseDTO.setUpdateTime(new Date());
        responseDTO.setPublishTime(new Date());

        when(chapterService.getChapterByNovelIdAndNumber(novelId, chapterNumber)).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(get("/api/v1/chapters/novel/{novelId}/number/{chapterNumber}", novelId, chapterNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Chapter retrieved successfully"))
                .andExpect(jsonPath("$.data.title").value("Chapter 1"))
                .andExpect(jsonPath("$.data.content").value("Chapter 1 content"))
                .andExpect(jsonPath("$.data.wordCnt").value(100));

        verify(chapterService).getChapterByNovelIdAndNumber(novelId, chapterNumber);
    }

    @Test
    void testSearchChapters_Success() throws Exception {
        // Given
        ChapterSummaryDTO chapter1 = new ChapterSummaryDTO();
        chapter1.setId(1);
        chapter1.setUuid(UUID.randomUUID());
        chapter1.setNovelId(1);
        chapter1.setChapterNumber(1);
        chapter1.setTitle("Chapter 1");
        chapter1.setPreview("Content 1");
        chapter1.setWordCnt(10);
        chapter1.setIsPremium(false);
        chapter1.setYuanCost(0.0f);
        chapter1.setViewCnt(100L);
        chapter1.setIsValid(true);
        chapter1.setCreateTime(new Date());
        chapter1.setUpdateTime(new Date());
        chapter1.setPublishTime(new Date());

        ChapterSummaryDTO chapter2 = new ChapterSummaryDTO();
        chapter2.setId(2);
        chapter2.setUuid(UUID.randomUUID());
        chapter2.setNovelId(1);
        chapter2.setChapterNumber(2);
        chapter2.setTitle("Chapter 2");
        chapter2.setPreview("Content 2");
        chapter2.setWordCnt(15);
        chapter2.setIsPremium(false);
        chapter2.setYuanCost(0.0f);
        chapter2.setViewCnt(200L);
        chapter2.setIsValid(true);
        chapter2.setCreateTime(new Date());
        chapter2.setUpdateTime(new Date());
        chapter2.setPublishTime(new Date());

        PageResponseDTO<ChapterSummaryDTO> pageResponse = new PageResponseDTO<>(
                Arrays.asList(chapter1, chapter2), 2L, 0, 5
        );

        when(chapterService.searchChapters(any(ChapterSearchRequestDTO.class)))
                .thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/chapters/search")
                .param("novelId", "1")
                .param("titleKeyword", "test")
                .param("page", "1")
                .param("pageSize", "5")
                .param("sortBy", "chapterNumber")
                .param("sortOrder", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.currentPage").value(0))
                .andExpect(jsonPath("$.data.size").value(5));

        verify(chapterService).searchChapters(any(ChapterSearchRequestDTO.class));
    }

    @Test
    void testIncrementViewCount_Success() throws Exception {
        // Given
        UUID chapterUuid = UUID.randomUUID();

        lenient().doNothing().when(chapterService).incrementViewCount(any(), any(), any(), any(), any());

        // When & Then
        mockMvc.perform(post("/api/v1/chapters/{uuid}/view", chapterUuid)
                .header("User-Agent", "Test Agent")
                .header("X-Forwarded-For", "192.168.1.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("View count incremented"));

        verify(chapterService).incrementViewCount(any(UUID.class), any(UUID.class), any(String.class), any(String.class), isNull());
    }

    @Test
    void testGetNextChapterUuid_Success() throws Exception {
        // Given
        UUID chapterUuid = UUID.randomUUID();
        UUID nextChapterUuid = UUID.randomUUID();

        when(chapterService.getNextChapterUuid(chapterUuid)).thenReturn(nextChapterUuid);

        // When & Then
        mockMvc.perform(get("/api/v1/chapters/{uuid}/next", chapterUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Next chapter retrieved"))
                .andExpect(jsonPath("$.data").value(nextChapterUuid.toString()));

        verify(chapterService).getNextChapterUuid(chapterUuid);
    }

    @Test
    void testGetPreviousChapterUuid_Success() throws Exception {
        // Given
        UUID chapterUuid = UUID.randomUUID();
        UUID previousChapterUuid = UUID.randomUUID();

        when(chapterService.getPreviousChapterUuid(chapterUuid)).thenReturn(previousChapterUuid);

        // When & Then
        mockMvc.perform(get("/api/v1/chapters/{uuid}/previous", chapterUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Previous chapter retrieved"))
                .andExpect(jsonPath("$.data").value(previousChapterUuid.toString()));

        verify(chapterService).getPreviousChapterUuid(chapterUuid);
    }

    @Test
    void testGetNextAvailableChapterNumber_Success() throws Exception {
        // Given
        Integer novelId = 1;
        Integer nextChapterNumber = 3;

        when(chapterService.getNextAvailableChapterNumber(novelId)).thenReturn(nextChapterNumber);

        // When & Then
        mockMvc.perform(get("/api/v1/chapters/novel/{novelId}/next-number", novelId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Next chapter number retrieved"))
                .andExpect(jsonPath("$.data").value(3));

        verify(chapterService).getNextAvailableChapterNumber(novelId);
    }

    @Test
    void testBatchCreateChapters_Success() throws Exception {
        // Given
        ChapterBatchCreateRequestDTO requestDTO = new ChapterBatchCreateRequestDTO();
        requestDTO.setNovelId(1);
        
        List<ChapterBatchCreateRequestDTO.ChapterData> chapters = new ArrayList<>();
        ChapterBatchCreateRequestDTO.ChapterData chapter1 = new ChapterBatchCreateRequestDTO.ChapterData();
        chapter1.setChapterNumber(1);
        chapter1.setTitle("Chapter 1");
        chapter1.setContent("Content 1");
        chapter1.setWordCnt(100);
        chapter1.setIsPremium(false);
        chapter1.setYuanCost(0.0f);
        
        ChapterBatchCreateRequestDTO.ChapterData chapter2 = new ChapterBatchCreateRequestDTO.ChapterData();
        chapter2.setChapterNumber(2);
        chapter2.setTitle("Chapter 2");
        chapter2.setContent("Content 2");
        chapter2.setWordCnt(150);
        chapter2.setIsPremium(false);
        chapter2.setYuanCost(0.0f);
        
        chapters.add(chapter1);
        chapters.add(chapter2);
        requestDTO.setChapters(chapters);

        lenient().doNothing().when(chapterService).batchCreateChapters(any(), any());

        // When & Then
        mockMvc.perform(post("/api/v1/chapters/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Chapters created successfully"));

        verify(chapterService).batchCreateChapters(isNull(), any(ChapterBatchCreateRequestDTO.class));
    }

    @Test
    void testPublishChapter_Success() throws Exception {
        // Given
        ChapterPublishRequestDTO requestDTO = new ChapterPublishRequestDTO();
        requestDTO.setUuid(UUID.randomUUID());
        requestDTO.setIsValid(true);

        lenient().doNothing().when(chapterService).publishChapter(any(), any());

        // When & Then
        mockMvc.perform(patch("/api/v1/chapters/publish")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Chapter publish status updated successfully"));

        verify(chapterService).publishChapter(isNull(), any(ChapterPublishRequestDTO.class));
    }

    @Test
    void testBatchPublishChapters_Success() throws Exception {
        // Given
        Integer novelId = 1;
        Boolean isValid = true;

        lenient().doNothing().when(chapterService).batchPublishChapters(any(), any(), any());

        // When & Then
        mockMvc.perform(patch("/api/v1/chapters/novel/{novelId}/publish", novelId)
                .param("isValid", isValid.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Chapters publish status updated successfully"));

        verify(chapterService).batchPublishChapters(isNull(), eq(novelId), eq(isValid));
    }

    @Test
    void testDeleteChaptersByNovelId_Success() throws Exception {
        // Given
        Integer novelId = 1;

        lenient().doNothing().when(chapterService).deleteChaptersByNovelId(any(), any());

        // When & Then
        mockMvc.perform(delete("/api/v1/chapters/novel/{novelId}", novelId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("All chapters deleted successfully"));

        verify(chapterService).deleteChaptersByNovelId(isNull(), eq(novelId));
    }

    @Test
    void testAdminDeleteChapter_Success() throws Exception {
        // Given
        UUID chapterUuid = UUID.randomUUID();

        lenient().doNothing().when(chapterService).adminDeleteChapter(any());

        // When & Then
        mockMvc.perform(delete("/api/v1/chapters/admin/{uuid}", chapterUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Chapter deleted successfully by admin"));

        verify(chapterService).adminDeleteChapter(eq(chapterUuid));
    }

    @Test
    void testAdminDeleteChaptersByNovelId_Success() throws Exception {
        // Given
        Integer novelId = 1;

        lenient().doNothing().when(chapterService).adminDeleteChaptersByNovelId(any());

        // When & Then
        mockMvc.perform(delete("/api/v1/chapters/admin/novel/{novelId}", novelId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("All chapters deleted successfully by admin"));

        verify(chapterService).adminDeleteChaptersByNovelId(eq(novelId));
    }

    @Test
    void testChapterExists_Success() throws Exception {
        // Given
        Integer novelId = 1;
        Integer chapterNumber = 1;
        boolean exists = true;

        when(chapterService.chapterExists(novelId, chapterNumber)).thenReturn(exists);

        // When & Then
        mockMvc.perform(get("/api/v1/chapters/exists")
                .param("novelId", novelId.toString())
                .param("chapterNumber", chapterNumber.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Chapter existence checked"))
                .andExpect(jsonPath("$.data").value(true));

        verify(chapterService).chapterExists(novelId, chapterNumber);
    }

    @Test
    void testGetChaptersByIds_Success() throws Exception {
        // Given
        List<Integer> chapterIds = Arrays.asList(1, 2);
        
        ChapterDetailResponseDTO chapter1 = new ChapterDetailResponseDTO();
        chapter1.setId(1);
        chapter1.setUuid(UUID.randomUUID());
        chapter1.setNovelId(1);
        chapter1.setChapterNumber(1);
        chapter1.setTitle("Chapter 1");
        chapter1.setContent("Content 1");
        chapter1.setPreview("Content 1");
        chapter1.setWordCnt(100);
        chapter1.setIsPremium(false);
        chapter1.setYuanCost(0.0f);
        chapter1.setViewCnt(0L);
        chapter1.setIsValid(true);
        chapter1.setCreateTime(new Date());
        chapter1.setUpdateTime(new Date());
        chapter1.setPublishTime(new Date());

        ChapterDetailResponseDTO chapter2 = new ChapterDetailResponseDTO();
        chapter2.setId(2);
        chapter2.setUuid(UUID.randomUUID());
        chapter2.setNovelId(1);
        chapter2.setChapterNumber(2);
        chapter2.setTitle("Chapter 2");
        chapter2.setContent("Content 2");
        chapter2.setPreview("Content 2");
        chapter2.setWordCnt(150);
        chapter2.setIsPremium(false);
        chapter2.setYuanCost(0.0f);
        chapter2.setViewCnt(0L);
        chapter2.setIsValid(true);
        chapter2.setCreateTime(new Date());
        chapter2.setUpdateTime(new Date());
        chapter2.setPublishTime(new Date());

        when(chapterService.getChaptersByIds(chapterIds)).thenReturn(Arrays.asList(chapter1, chapter2));

        // When & Then
        mockMvc.perform(post("/api/v1/chapters/batch/get")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(chapterIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Chapters retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("Chapter 1"))
                .andExpect(jsonPath("$.data[1].title").value("Chapter 2"));

        verify(chapterService).getChaptersByIds(chapterIds);
    }

    @Test
    void testGetChaptersByIds_WithEmptyIds_ShouldReturnEmptyList() throws Exception {
        // Given
        List<Integer> chapterIds = new ArrayList<>();

        // When & Then
        mockMvc.perform(post("/api/v1/chapters/batch/get")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(chapterIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Chapters retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(chapterService, never()).getChaptersByIds(any());
    }
}