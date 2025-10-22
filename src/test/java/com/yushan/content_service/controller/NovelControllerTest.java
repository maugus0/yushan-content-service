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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    @Test
    void getNovelByUuid_WithValidUuid_ShouldReturnNovel() throws Exception {
        // Arrange
        UUID novelUuid = UUID.randomUUID();
        NovelDetailResponseDTO response = new NovelDetailResponseDTO();
        response.setId(1);
        response.setUuid(novelUuid);
        response.setTitle("Test Novel");
        response.setStatus("PUBLISHED");

        when(novelService.getNovelByUuid(novelUuid)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/novels/uuid/{uuid}", novelUuid)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.uuid").value(novelUuid.toString()))
                .andExpect(jsonPath("$.data.title").value("Test Novel"));

        verify(novelService).getNovelByUuid(novelUuid);
    }

    @Test
    void archiveNovel_WithValidId_ShouldReturnArchivedNovel() throws Exception {
        // Arrange
        Integer novelId = 1;
        NovelDetailResponseDTO response = new NovelDetailResponseDTO();
        response.setId(novelId);
        response.setTitle("Test Novel");
        response.setStatus("ARCHIVED");

        when(novelService.archiveNovel(novelId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/novels/{id}", novelId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("ARCHIVED"));

        verify(novelService).archiveNovel(novelId);
    }

    @Test
    void unarchiveNovel_WithValidId_ShouldReturnUnarchivedNovel() throws Exception {
        // Arrange
        Integer novelId = 1;
        NovelDetailResponseDTO response = new NovelDetailResponseDTO();
        response.setId(novelId);
        response.setTitle("Test Novel");
        response.setStatus("DRAFT");

        when(novelService.unarchiveNovel(novelId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/novels/{id}/unarchive", novelId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));

        verify(novelService).unarchiveNovel(novelId);
    }

    @Test
    void getAllNovelsAdmin_ShouldReturnAllNovels() throws Exception {
        // Arrange
        NovelDetailResponseDTO novel1 = new NovelDetailResponseDTO();
        novel1.setId(1);
        novel1.setTitle("Novel 1");
        novel1.setStatus("PUBLISHED");

        NovelDetailResponseDTO novel2 = new NovelDetailResponseDTO();
        novel2.setId(2);
        novel2.setTitle("Novel 2");
        novel2.setStatus("ARCHIVED");

        PageResponseDTO<NovelDetailResponseDTO> pageResponse = new PageResponseDTO<>();
        pageResponse.setContent(Arrays.asList(novel1, novel2));
        pageResponse.setTotalElements(2L);
        pageResponse.setTotalPages(1);

        when(novelService.getAllNovelsAdmin(any())).thenReturn(pageResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/novels/admin/all")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[0].title").value("Novel 1"))
                .andExpect(jsonPath("$.data.content[1].title").value("Novel 2"));

        verify(novelService).getAllNovelsAdmin(any());
    }

    @Test
    void approveNovel_WithValidId_ShouldReturnApprovedNovel() throws Exception {
        // Arrange
        Integer novelId = 1;
        NovelDetailResponseDTO response = new NovelDetailResponseDTO();
        response.setId(novelId);
        response.setTitle("Test Novel");
        response.setStatus("PUBLISHED");

        when(novelService.approveNovel(novelId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/novels/{id}/approve", novelId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));

        verify(novelService).approveNovel(novelId);
    }

    @Test
    void rejectNovel_WithValidId_ShouldReturnRejectedNovel() throws Exception {
        // Arrange
        Integer novelId = 1;
        NovelDetailResponseDTO response = new NovelDetailResponseDTO();
        response.setId(novelId);
        response.setTitle("Test Novel");
        response.setStatus("DRAFT");

        when(novelService.rejectNovel(novelId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/novels/{id}/reject", novelId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));

        verify(novelService).rejectNovel(novelId);
    }

    @Test
    void hideNovel_WithValidId_ShouldReturnHiddenNovel() throws Exception {
        // Arrange
        Integer novelId = 1;
        NovelDetailResponseDTO response = new NovelDetailResponseDTO();
        response.setId(novelId);
        response.setTitle("Test Novel");
        response.setStatus("HIDDEN");

        when(novelService.hideNovel(novelId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/novels/{id}/hide", novelId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("HIDDEN"));

        verify(novelService).hideNovel(novelId);
    }

    @Test
    void unhideNovel_WithValidId_ShouldReturnUnhiddenNovel() throws Exception {
        // Arrange
        Integer novelId = 1;
        NovelDetailResponseDTO response = new NovelDetailResponseDTO();
        response.setId(novelId);
        response.setTitle("Test Novel");
        response.setStatus("PUBLISHED");

        when(novelService.unhideNovel(novelId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/novels/{id}/unhide", novelId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));

        verify(novelService).unhideNovel(novelId);
    }

    @Test
    void getNovelsUnderReview_ShouldReturnUnderReviewNovels() throws Exception {
        // Arrange
        NovelDetailResponseDTO novel1 = new NovelDetailResponseDTO();
        novel1.setId(1);
        novel1.setTitle("Novel 1");
        novel1.setStatus("UNDER_REVIEW");

        PageResponseDTO<NovelDetailResponseDTO> pageResponse = new PageResponseDTO<>();
        pageResponse.setContent(Arrays.asList(novel1));
        pageResponse.setTotalElements(1L);
        pageResponse.setTotalPages(1);

        when(novelService.getNovelsUnderReview(0, 10)).thenReturn(pageResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/novels/admin/under-review")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].status").value("UNDER_REVIEW"));

        verify(novelService).getNovelsUnderReview(0, 10);
    }

    @Test
    void getNovelsByAuthor_WithValidAuthorId_ShouldReturnNovels() throws Exception {
        // Arrange
        UUID authorId = UUID.randomUUID();
        NovelDetailResponseDTO novel1 = new NovelDetailResponseDTO();
        novel1.setId(1);
        novel1.setTitle("Novel 1");
        novel1.setAuthorId(authorId);

        NovelDetailResponseDTO novel2 = new NovelDetailResponseDTO();
        novel2.setId(2);
        novel2.setTitle("Novel 2");
        novel2.setAuthorId(authorId);

        when(novelService.getNovelsByAuthor(authorId)).thenReturn(Arrays.asList(novel1, novel2));

        // Act & Assert
        mockMvc.perform(get("/api/v1/novels/author/{authorId}", authorId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("Novel 1"))
                .andExpect(jsonPath("$.data[1].title").value("Novel 2"));

        verify(novelService).getNovelsByAuthor(authorId);
    }

    @Test
    void getNovelsByCategory_WithValidCategoryId_ShouldReturnNovels() throws Exception {
        // Arrange
        Integer categoryId = 1;
        NovelDetailResponseDTO novel1 = new NovelDetailResponseDTO();
        novel1.setId(1);
        novel1.setTitle("Novel 1");
        novel1.setCategoryId(categoryId);

        when(novelService.getNovelsByCategory(categoryId)).thenReturn(Arrays.asList(novel1));

        // Act & Assert
        mockMvc.perform(get("/api/v1/novels/category/{categoryId}", categoryId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Novel 1"));

        verify(novelService).getNovelsByCategory(categoryId);
    }

    @Test
    void getNovelCount_WithFilters_ShouldReturnCount() throws Exception {
        // Arrange
        long count = 5L;
        when(novelService.getNovelCount(any())).thenReturn(count);

        // Act & Assert
        mockMvc.perform(get("/api/v1/novels/count")
                .param("category", "1")
                .param("status", "PUBLISHED")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(5));

        verify(novelService).getNovelCount(any());
    }

    @Test
    void getNovelsByIds_WithValidIds_ShouldReturnNovels() throws Exception {
        // Arrange
        List<Integer> novelIds = Arrays.asList(1, 2);
        NovelDetailResponseDTO novel1 = new NovelDetailResponseDTO();
        novel1.setId(1);
        novel1.setTitle("Novel 1");

        NovelDetailResponseDTO novel2 = new NovelDetailResponseDTO();
        novel2.setId(2);
        novel2.setTitle("Novel 2");

        when(novelService.getNovelsByIds(novelIds)).thenReturn(Arrays.asList(novel1, novel2));

        // Act & Assert
        mockMvc.perform(post("/api/v1/novels/batch/get")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(novelIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("Novel 1"))
                .andExpect(jsonPath("$.data[1].title").value("Novel 2"));

        verify(novelService).getNovelsByIds(novelIds);
    }

    @Test
    void getNovelsByIds_WithEmptyIds_ShouldReturnEmptyList() throws Exception {
        // Arrange
        List<Integer> novelIds = new ArrayList<>();

        // Act & Assert
        mockMvc.perform(post("/api/v1/novels/batch/get")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(novelIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(novelService, never()).getNovelsByIds(any());
    }

    @Test
    void getNovelVoteCount_WithValidId_ShouldReturnVoteCount() throws Exception {
        // Arrange
        Integer novelId = 1;
        Integer voteCount = 10;
        when(novelService.getNovelVoteCount(novelId)).thenReturn(voteCount);

        // Act & Assert
        mockMvc.perform(get("/api/v1/novels/{id}/vote-count", novelId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(10));

        verify(novelService).getNovelVoteCount(novelId);
    }

    @Test
    void incrementVoteCount_WithValidId_ShouldIncrementVote() throws Exception {
        // Arrange
        Integer novelId = 1;

        // Act & Assert
        mockMvc.perform(post("/api/v1/novels/{id}/vote", novelId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(novelService).incrementVoteCount(novelId);
    }

    @Test
    void updateNovelRatingAndCount_WithValidData_ShouldUpdateRating() throws Exception {
        // Arrange
        Integer novelId = 1;
        Float avgRating = 4.5f;
        Integer reviewCount = 10;

        // Act & Assert
        mockMvc.perform(put("/api/v1/novels/{id}/rating", novelId)
                .param("avgRating", avgRating.toString())
                .param("reviewCount", reviewCount.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(novelService).updateNovelRatingAndCount(novelId, avgRating, reviewCount);
    }
}