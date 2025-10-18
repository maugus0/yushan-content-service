package com.yushan.content_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yushan.content_service.config.TestSecurityContextConfig;
import com.yushan.content_service.dto.novel.NovelCreateRequestDTO;
import com.yushan.content_service.dto.novel.NovelDetailResponseDTO;
import com.yushan.content_service.dto.novel.NovelUpdateRequestDTO;
import com.yushan.content_service.dto.common.PageResponseDTO;
import com.yushan.content_service.service.NovelService;
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
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Simple controller tests without Spring context.
 * Tests basic controller functionality with mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
public class NovelControllerTest {

    @Mock
    private NovelService novelService;

    @InjectMocks
    private NovelController novelController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(novelController).build();
        objectMapper = new ObjectMapper();
        TestSecurityContextConfig.setupMockUser();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        TestSecurityContextConfig.clearSecurityContext();
    }

    @Test
    void createNovel_WithValidData_ShouldReturnCreated() throws Exception {
        // Arrange
        NovelCreateRequestDTO request = new NovelCreateRequestDTO();
        request.setTitle("Test Novel");
        request.setCategoryId(1);
        request.setSynopsis("Test Synopsis");
        request.setCoverImgBase64("data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD");
        request.setIsCompleted(false);

        NovelDetailResponseDTO response = new NovelDetailResponseDTO();
        response.setId(1);
        response.setTitle("Test Novel");
        response.setStatus("DRAFT");

        when(novelService.createNovel(any(UUID.class), anyString(), any(NovelCreateRequestDTO.class)))
            .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/novels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("Test Novel"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));

        verify(novelService).createNovel(any(UUID.class), anyString(), any(NovelCreateRequestDTO.class));
    }

    @Test
    void createNovel_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange
        NovelCreateRequestDTO request = new NovelCreateRequestDTO();
        // Missing required fields

        // Act & Assert
        mockMvc.perform(post("/api/v1/novels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getNovels_ShouldReturnPaginatedResults() throws Exception {
        // Arrange
        NovelDetailResponseDTO novel1 = new NovelDetailResponseDTO();
        novel1.setId(1);
        novel1.setTitle("Novel 1");
        novel1.setStatus("PUBLISHED");

        NovelDetailResponseDTO novel2 = new NovelDetailResponseDTO();
        novel2.setId(2);
        novel2.setTitle("Novel 2");
        novel2.setStatus("PUBLISHED");

        PageResponseDTO<NovelDetailResponseDTO> pageResponse = new PageResponseDTO<>();
        pageResponse.setContent(Arrays.asList(novel1, novel2));
        pageResponse.setTotalElements(2L);
        pageResponse.setTotalPages(1);

        when(novelService.listNovelsWithPagination(any())).thenReturn(pageResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/novels")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[0].title").value("Novel 1"));
    }

    @Test
    void getNovelById_WithValidId_ShouldReturnNovel() throws Exception {
        // Arrange
        Integer novelId = 1;
        NovelDetailResponseDTO response = new NovelDetailResponseDTO();
        response.setId(novelId);
        response.setTitle("Test Novel");
        response.setStatus("PUBLISHED");

        when(novelService.getNovel(novelId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/novels/{id}", novelId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(novelId))
                .andExpect(jsonPath("$.data.title").value("Test Novel"));
    }

    @Test
    void updateNovel_WithValidData_ShouldReturnUpdatedNovel() throws Exception {
        // Arrange
        Integer novelId = 1;
        NovelUpdateRequestDTO request = new NovelUpdateRequestDTO();
        request.setTitle("Updated Novel");
        request.setSynopsis("Updated Synopsis");
        request.setIsCompleted(true);

        NovelDetailResponseDTO response = new NovelDetailResponseDTO();
        response.setId(novelId);
        response.setTitle("Updated Novel");
        response.setStatus("DRAFT");

        when(novelService.updateNovel(eq(novelId), any(NovelUpdateRequestDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/novels/{id}", novelId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("Updated Novel"));

        verify(novelService).updateNovel(eq(novelId), any(NovelUpdateRequestDTO.class));
    }

    @Test
    void submitForReview_WithValidNovel_ShouldUpdateStatus() throws Exception {
        // Arrange
        Integer novelId = 1;
        NovelDetailResponseDTO response = new NovelDetailResponseDTO();
        response.setId(novelId);
        response.setStatus("UNDER_REVIEW");

        when(novelService.submitForReview(eq(novelId), any(UUID.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/novels/{id}/submit-review", novelId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("UNDER_REVIEW"));

        verify(novelService).submitForReview(eq(novelId), any(UUID.class));
    }

    @Test
    void incrementViewCount_ShouldIncrementViewCount() throws Exception {
        // Arrange
        Integer novelId = 1;

        // Act & Assert
        mockMvc.perform(post("/api/v1/novels/{id}/view", novelId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(novelService).incrementViewCount(eq(novelId), any(UUID.class), isNull(), anyString());
    }
}