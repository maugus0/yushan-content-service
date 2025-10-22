package com.yushan.content_service.service;

import com.yushan.content_service.dao.NovelMapper;
import com.yushan.content_service.dto.novel.NovelCreateRequestDTO;
import com.yushan.content_service.dto.novel.NovelDetailResponseDTO;
import com.yushan.content_service.dto.novel.NovelSearchRequestDTO;
import com.yushan.content_service.dto.novel.NovelUpdateRequestDTO;
import com.yushan.content_service.dto.common.PageResponseDTO;
import com.yushan.content_service.entity.Novel;
import com.yushan.content_service.enums.NovelStatus;
import com.yushan.content_service.exception.ResourceNotFoundException;
import com.yushan.content_service.util.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NovelService with current method signatures and UUID authorId.
 */
public class NovelServiceTest {

    private NovelMapper novelMapper;
    private RedisUtil redisUtil;
    private KafkaEventProducerService kafkaEventProducerService;
    private CategoryService categoryService;
    private ElasticsearchAutoIndexService elasticsearchAutoIndexService;
    private NovelService novelService;

    @BeforeEach
    void setUp() {
        novelMapper = Mockito.mock(NovelMapper.class);
        redisUtil = Mockito.mock(RedisUtil.class);
        kafkaEventProducerService = Mockito.mock(KafkaEventProducerService.class);
        categoryService = Mockito.mock(CategoryService.class);
        elasticsearchAutoIndexService = Mockito.mock(ElasticsearchAutoIndexService.class);

        novelService = new NovelService();
        try {
            java.lang.reflect.Field f1 = NovelService.class.getDeclaredField("novelMapper");
            f1.setAccessible(true);
            f1.set(novelService, novelMapper);
            
            java.lang.reflect.Field f2 = NovelService.class.getDeclaredField("redisUtil");
            f2.setAccessible(true);
            f2.set(novelService, redisUtil);
            
            java.lang.reflect.Field f3 = NovelService.class.getDeclaredField("kafkaEventProducerService");
            f3.setAccessible(true);
            f3.set(novelService, kafkaEventProducerService);
            
            java.lang.reflect.Field f4 = NovelService.class.getDeclaredField("categoryService");
            f4.setAccessible(true);
            f4.set(novelService, categoryService);
            
            java.lang.reflect.Field f5 = NovelService.class.getDeclaredField("elasticsearchAutoIndexService");
            f5.setAccessible(true);
            f5.set(novelService, elasticsearchAutoIndexService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void createNovel_ShouldInsertWithDefaults_AndReturnDto() {
        // Arrange
        UUID authorId = UUID.randomUUID();
        String authorName = "test-author";
        NovelCreateRequestDTO request = new NovelCreateRequestDTO();
        request.setTitle("Test Novel");
        request.setCategoryId(1);
        request.setIsCompleted(false);

        Novel savedNovel = new Novel();
        savedNovel.setId(1);
        savedNovel.setUuid(UUID.randomUUID());
        savedNovel.setTitle("Test Novel");
        savedNovel.setAuthorId(authorId);
        savedNovel.setAuthorName(authorName);
        savedNovel.setCategoryId(1);
        savedNovel.setStatus(0); // DRAFT
        savedNovel.setIsCompleted(false);
        savedNovel.setCreateTime(new Date());
        savedNovel.setUpdateTime(new Date());

        when(novelMapper.insertSelective(any(Novel.class))).thenAnswer(invocation -> {
            Novel novel = invocation.getArgument(0);
            novel.setId(1); // Set ID after insert
            return 1;
        });
        when(novelMapper.selectByPrimaryKey(1)).thenReturn(savedNovel);
        
        // Mock CategoryService
        com.yushan.content_service.entity.Category mockCategory = new com.yushan.content_service.entity.Category();
        mockCategory.setId(1);
        mockCategory.setName("Test Category");
        mockCategory.setIsActive(true);
        when(categoryService.getCategoryById(1)).thenReturn(mockCategory);

        // Act
        NovelDetailResponseDTO result = novelService.createNovel(authorId, authorName, request);

        // Assert
        assertNotNull(result);
        assertEquals("Test Novel", result.getTitle());
        assertEquals(authorName, result.getAuthorUsername());
        assertEquals("DRAFT", result.getStatus());
        assertEquals(1, result.getCategoryId());
        assertFalse(result.getIsCompleted());

        verify(novelMapper).insertSelective(any(Novel.class));
        verify(redisUtil).cacheNovel(anyInt(), any(Novel.class));
        verify(kafkaEventProducerService).publishNovelCreatedEvent(any(Novel.class), any(UUID.class));
    }

    @Test
    void getNovel_WithValidId_ShouldReturnNovel() {
        // Arrange
        Integer novelId = 1;
        Novel novel = new Novel();
        novel.setId(novelId);
        novel.setTitle("Test Novel");
        novel.setStatus(0); // DRAFT

        when(novelMapper.selectByPrimaryKey(novelId)).thenReturn(novel);

        // Act
        NovelDetailResponseDTO result = novelService.getNovel(novelId);

        // Assert
        assertNotNull(result);
        assertEquals(novelId, result.getId());
        assertEquals("Test Novel", result.getTitle());
        assertEquals("DRAFT", result.getStatus());

        verify(novelMapper).selectByPrimaryKey(novelId);
    }

    @Test
    void getNovel_WithInvalidId_ShouldThrowException() {
        // Arrange
        Integer novelId = 999;
        when(novelMapper.selectByPrimaryKey(novelId)).thenReturn(null);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> novelService.getNovel(novelId));
        verify(novelMapper).selectByPrimaryKey(novelId);
    }

    @Test
    void updateNovel_WithValidData_ShouldUpdateAndReturnNovel() {
        // Arrange
        Integer novelId = 1;
        NovelUpdateRequestDTO request = new NovelUpdateRequestDTO();
        request.setTitle("Updated Novel");
        request.setCategoryId(2);
        request.setIsCompleted(true);

        Novel existingNovel = new Novel();
        existingNovel.setId(novelId);
        existingNovel.setTitle("Original Novel");
        existingNovel.setCategoryId(1);
        existingNovel.setIsCompleted(false);
        existingNovel.setStatus(NovelStatus.DRAFT.getValue()); // Set status to avoid NPE
        existingNovel.setAuthorId(UUID.randomUUID()); // Set authorId for Kafka event

        when(novelMapper.selectByPrimaryKey(novelId)).thenReturn(existingNovel);
        when(novelMapper.updateByPrimaryKeySelective(any(Novel.class))).thenReturn(1);
        
        // Mock CategoryService
        com.yushan.content_service.entity.Category mockCategory = new com.yushan.content_service.entity.Category();
        mockCategory.setId(2);
        mockCategory.setName("Updated Category");
        mockCategory.setIsActive(true);
        when(categoryService.getCategoryById(2)).thenReturn(mockCategory);

        // Act
        NovelDetailResponseDTO result = novelService.updateNovel(novelId, request);

        // Assert
        assertNotNull(result);
        assertEquals(novelId, result.getId());
        assertEquals("Updated Novel", result.getTitle());
        assertEquals(2, result.getCategoryId());
        assertTrue(result.getIsCompleted());

        verify(novelMapper).selectByPrimaryKey(novelId);
        verify(novelMapper).updateByPrimaryKeySelective(any(Novel.class));
        verify(kafkaEventProducerService).publishNovelUpdatedEvent(any(Novel.class), any(UUID.class), any(String[].class));
    }

    @Test
    void updateNovel_WithInvalidId_ShouldThrowException() {
        // Arrange
        Integer novelId = 999;
        NovelUpdateRequestDTO request = new NovelUpdateRequestDTO();
        request.setTitle("Updated Novel");

        when(novelMapper.selectByPrimaryKey(novelId)).thenReturn(null);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> novelService.updateNovel(novelId, request));
        verify(novelMapper).selectByPrimaryKey(novelId);
    }

    @Test
    void submitForReview_WithDraftNovel_ShouldUpdateStatus() {
        // Arrange
        Integer novelId = 1;
        UUID authorId = UUID.randomUUID();
        Novel novel = new Novel();
        novel.setId(novelId);
        novel.setAuthorId(authorId);
        novel.setStatus(0); // DRAFT

        when(novelMapper.selectByPrimaryKey(novelId)).thenReturn(novel);
        when(novelMapper.updateByPrimaryKeySelective(any(Novel.class))).thenReturn(1);
        doNothing().when(redisUtil).invalidateNovelCaches(novelId);
        doNothing().when(redisUtil).cacheNovel(eq(novelId), any(Novel.class));

        // Act
        NovelDetailResponseDTO result = novelService.submitForReview(novelId, authorId);

        // Assert
        assertNotNull(result);
        assertEquals("UNDER_REVIEW", result.getStatus());
        verify(novelMapper).selectByPrimaryKey(novelId);
        verify(novelMapper).updateByPrimaryKeySelective(any(Novel.class));
    }

    @Test
    void submitForReview_WithNonDraftNovel_ShouldThrowException() {
        // Arrange
        Integer novelId = 1;
        UUID authorId = UUID.randomUUID();
        Novel novel = new Novel();
        novel.setId(novelId);
        novel.setAuthorId(authorId);
        novel.setStatus(2); // PUBLISHED

        when(novelMapper.selectByPrimaryKey(novelId)).thenReturn(novel);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> novelService.submitForReview(novelId, authorId));
        verify(novelMapper).selectByPrimaryKey(novelId);
    }

    @Test
    void approveNovel_WithUnderReviewNovel_ShouldUpdateToPublished() {
        // Arrange
        Integer novelId = 1;
        Novel novel = new Novel();
        novel.setId(novelId);
        novel.setStatus(1); // UNDER_REVIEW

        when(novelMapper.selectByPrimaryKey(novelId)).thenReturn(novel);
        when(novelMapper.updateByPrimaryKeySelective(any(Novel.class))).thenReturn(1);
        doNothing().when(redisUtil).invalidateNovelCaches(novelId);
        doNothing().when(redisUtil).cacheNovel(eq(novelId), any(Novel.class));

        // Act
        NovelDetailResponseDTO result = novelService.approveNovel(novelId);

        // Assert
        assertNotNull(result);
        assertEquals("PUBLISHED", result.getStatus());
        verify(novelMapper).selectByPrimaryKey(novelId);
        verify(novelMapper).updateByPrimaryKeySelective(any(Novel.class));
    }

    @Test
    void hideNovel_WithPublishedNovel_ShouldUpdateToHidden() {
        // Arrange
        Integer novelId = 1;
        Novel novel = new Novel();
        novel.setId(novelId);
        novel.setStatus(2); // PUBLISHED

        when(novelMapper.selectByPrimaryKey(novelId)).thenReturn(novel);
        when(novelMapper.updateByPrimaryKeySelective(any(Novel.class))).thenReturn(1);
        doNothing().when(redisUtil).invalidateNovelCaches(novelId);
        doNothing().when(redisUtil).cacheNovel(eq(novelId), any(Novel.class));

        // Act
        NovelDetailResponseDTO result = novelService.hideNovel(novelId);

        // Assert
        assertNotNull(result);
        assertEquals("HIDDEN", result.getStatus());
        verify(novelMapper).selectByPrimaryKey(novelId);
        verify(novelMapper).updateByPrimaryKeySelective(any(Novel.class));
    }

    @Test
    void listNovelsWithPagination_ShouldReturnPaginatedResults() {
        // Arrange
        NovelSearchRequestDTO request = new NovelSearchRequestDTO();
        request.setPage(0);
        request.setSize(10);

        List<Novel> novels = Arrays.asList(
            createTestNovel(1, "Novel 1"),
            createTestNovel(2, "Novel 2")
        );

        when(novelMapper.selectNovelsWithPagination(any())).thenReturn(novels);
        when(novelMapper.countNovels(any())).thenReturn(2L);
        
        // Mock CategoryService for batch loading
        when(categoryService.getCategoryMapByIds(any())).thenReturn(java.util.Map.of(1, "Test Category"));

        // Act
        PageResponseDTO<NovelDetailResponseDTO> result = novelService.listNovelsWithPagination(request);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(2, result.getContent().size());

        verify(novelMapper).selectNovelsWithPagination(any());
        verify(novelMapper).countNovels(any());
    }

    @Test
    void incrementViewCount_ShouldIncrementViewCount() {
        // Arrange
        Integer novelId = 1;
        UUID userId = UUID.randomUUID();
        String userAgent = "Test-Agent";
        String ipAddress = "192.168.1.1";
        Novel novel = createTestNovel(novelId, "Test Novel");
        
        when(novelMapper.selectByPrimaryKey(novelId)).thenReturn(novel);
        when(novelMapper.incrementViewCount(novelId)).thenReturn(1);

        // Act
        novelService.incrementViewCount(novelId, userId, userAgent, ipAddress);

        // Assert
        verify(novelMapper, times(2)).selectByPrimaryKey(novelId); // Called twice: once to check existence, once to get updated data
        verify(novelMapper).incrementViewCount(novelId);
        verify(redisUtil).incrementCachedViewCount(novelId);
        verify(redisUtil).cacheNovel(eq(novelId), any(Novel.class));
        verify(kafkaEventProducerService).publishNovelViewEvent(any(Novel.class), eq(userId), eq(userAgent), eq(ipAddress), isNull());
    }

    @Test
    void getNovelByUuid_WithValidUuid_ShouldReturnNovel() {
        // Arrange
        UUID novelUuid = UUID.randomUUID();
        Novel novel = new Novel();
        novel.setId(1);
        novel.setUuid(novelUuid);
        novel.setTitle("Test Novel");
        novel.setStatus(0); // DRAFT

        when(novelMapper.selectByUuid(novelUuid)).thenReturn(novel);

        // Act
        NovelDetailResponseDTO result = novelService.getNovelByUuid(novelUuid);

        // Assert
        assertNotNull(result);
        assertEquals(novelUuid, result.getUuid());
        assertEquals("Test Novel", result.getTitle());
        assertEquals("DRAFT", result.getStatus());

        verify(novelMapper).selectByUuid(novelUuid);
    }

    @Test
    void getNovelByUuid_WithInvalidUuid_ShouldThrowException() {
        // Arrange
        UUID novelUuid = UUID.randomUUID();
        when(novelMapper.selectByUuid(novelUuid)).thenReturn(null);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> novelService.getNovelByUuid(novelUuid));
        verify(novelMapper).selectByUuid(novelUuid);
    }

    @Test
    void archiveNovel_WithValidId_ShouldArchiveNovel() {
        // Arrange
        Integer novelId = 1;
        Novel novel = new Novel();
        novel.setId(novelId);
        novel.setTitle("Test Novel");
        novel.setStatus(0); // DRAFT

        when(novelMapper.selectByPrimaryKey(novelId)).thenReturn(novel);
        when(novelMapper.updateByPrimaryKeySelective(any(Novel.class))).thenReturn(1);
        doNothing().when(redisUtil).invalidateNovelCaches(novelId);

        // Act
        NovelDetailResponseDTO result = novelService.archiveNovel(novelId);

        // Assert
        assertNotNull(result);
        assertEquals("ARCHIVED", result.getStatus());
        verify(novelMapper).selectByPrimaryKey(novelId);
        verify(novelMapper).updateByPrimaryKeySelective(any(Novel.class));
        verify(redisUtil).invalidateNovelCaches(novelId);
    }

    @Test
    void rejectNovel_WithUnderReviewNovel_ShouldUpdateToDraft() {
        // Arrange
        Integer novelId = 1;
        Novel novel = new Novel();
        novel.setId(novelId);
        novel.setStatus(1); // UNDER_REVIEW

        when(novelMapper.selectByPrimaryKey(novelId)).thenReturn(novel);
        when(novelMapper.updateByPrimaryKeySelective(any(Novel.class))).thenReturn(1);
        doNothing().when(redisUtil).invalidateNovelCaches(novelId);
        doNothing().when(redisUtil).cacheNovel(eq(novelId), any(Novel.class));

        // Act
        NovelDetailResponseDTO result = novelService.rejectNovel(novelId);

        // Assert
        assertNotNull(result);
        assertEquals("DRAFT", result.getStatus());
        verify(novelMapper).selectByPrimaryKey(novelId);
        verify(novelMapper).updateByPrimaryKeySelective(any(Novel.class));
    }

    @Test
    void unhideNovel_WithHiddenNovel_ShouldUpdateToPublished() {
        // Arrange
        Integer novelId = 1;
        Novel novel = new Novel();
        novel.setId(novelId);
        novel.setStatus(3); // HIDDEN

        when(novelMapper.selectByPrimaryKey(novelId)).thenReturn(novel);
        when(novelMapper.updateByPrimaryKeySelective(any(Novel.class))).thenReturn(1);
        doNothing().when(redisUtil).invalidateNovelCaches(novelId);
        doNothing().when(redisUtil).cacheNovel(eq(novelId), any(Novel.class));

        // Act
        NovelDetailResponseDTO result = novelService.unhideNovel(novelId);

        // Assert
        assertNotNull(result);
        assertEquals("PUBLISHED", result.getStatus());
        verify(novelMapper).selectByPrimaryKey(novelId);
        verify(novelMapper).updateByPrimaryKeySelective(any(Novel.class));
    }

    @Test
    void getNovelsByAuthor_ShouldReturnAuthorNovels() {
        // Arrange
        UUID authorId = UUID.randomUUID();
        List<Novel> novels = Arrays.asList(
            createTestNovel(1, "Novel 1"),
            createTestNovel(2, "Novel 2")
        );

        when(novelMapper.selectNovelsWithPagination(any())).thenReturn(novels);
        when(categoryService.getCategoryMapByIds(any())).thenReturn(java.util.Map.of(1, "Test Category"));

        // Act
        List<NovelDetailResponseDTO> result = novelService.getNovelsByAuthor(authorId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(novelMapper).selectNovelsWithPagination(any());
    }

    @Test
    void getNovelsByCategory_ShouldReturnCategoryNovels() {
        // Arrange
        Integer categoryId = 1;
        List<Novel> novels = Arrays.asList(
            createTestNovel(1, "Novel 1"),
            createTestNovel(2, "Novel 2")
        );

        when(novelMapper.selectNovelsWithPagination(any())).thenReturn(novels);
        when(categoryService.getCategoryMapByIds(any())).thenReturn(java.util.Map.of(1, "Test Category"));

        // Act
        List<NovelDetailResponseDTO> result = novelService.getNovelsByCategory(categoryId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(novelMapper).selectNovelsWithPagination(any());
    }

    @Test
    void getNovelCount_ShouldReturnCount() {
        // Arrange
        NovelSearchRequestDTO request = new NovelSearchRequestDTO();
        request.setCategoryId(1);
        when(novelMapper.countNovels(request)).thenReturn(5L);

        // Act
        long result = novelService.getNovelCount(request);

        // Assert
        assertEquals(5L, result);
        verify(novelMapper).countNovels(request);
    }

    @Test
    void getNovelVoteCount_ShouldReturnVoteCount() {
        // Arrange
        Integer novelId = 1;
        Novel novel = createTestNovel(novelId, "Test Novel");
        novel.setVoteCnt(10);

        when(novelMapper.selectByPrimaryKey(novelId)).thenReturn(novel);

        // Act
        Integer result = novelService.getNovelVoteCount(novelId);

        // Assert
        assertEquals(10, result);
        verify(novelMapper).selectByPrimaryKey(novelId);
    }

    @Test
    void incrementVoteCount_ShouldIncrementVoteCount() {
        // Arrange
        Integer novelId = 1;
        Novel novel = createTestNovel(novelId, "Test Novel");
        novel.setVoteCnt(5);

        when(novelMapper.selectByPrimaryKey(novelId)).thenReturn(novel);
        when(novelMapper.incrementVoteCount(novelId)).thenReturn(1);

        // Act
        novelService.incrementVoteCount(novelId);

        // Assert
        verify(novelMapper).incrementVoteCount(novelId);
        verify(redisUtil).cacheNovel(eq(novelId), any(Novel.class));
    }

    @Test
    void updateNovelRatingAndCount_ShouldUpdateRatingAndCount() {
        // Arrange
        Integer novelId = 1;
        Float avgRating = 4.5f;
        Integer reviewCount = 10;
        Novel novel = createTestNovel(novelId, "Test Novel");

        when(novelMapper.selectByPrimaryKey(novelId)).thenReturn(novel);
        when(novelMapper.updateByPrimaryKeySelective(any(Novel.class))).thenReturn(1);

        // Act
        novelService.updateNovelRatingAndCount(novelId, avgRating, reviewCount);

        // Assert
        verify(novelMapper).selectByPrimaryKey(novelId);
        verify(novelMapper).updateByPrimaryKeySelective(any(Novel.class));
        verify(redisUtil).cacheNovel(eq(novelId), any(Novel.class));
    }

    @Test
    void getNovelsByIds_ShouldReturnNovelsByIds() {
        // Arrange
        List<Integer> novelIds = Arrays.asList(1, 2);
        List<Novel> novels = Arrays.asList(
            createTestNovel(1, "Novel 1"),
            createTestNovel(2, "Novel 2")
        );

        when(novelMapper.selectByIds(novelIds)).thenReturn(novels);

        // Act
        List<NovelDetailResponseDTO> result = novelService.getNovelsByIds(novelIds);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(novelMapper).selectByIds(novelIds);
    }

    @Test
    void getNovelsByIds_WithEmptyList_ShouldReturnEmptyList() {
        // Arrange
        List<Integer> novelIds = new java.util.ArrayList<>();

        // Act
        List<NovelDetailResponseDTO> result = novelService.getNovelsByIds(novelIds);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(novelMapper, never()).selectByIds(any());
    }

    @Test
    void unarchiveNovel_WithArchivedNovel_ShouldUpdateToDraft() {
        // Arrange
        Integer novelId = 1;
        Novel novel = new Novel();
        novel.setId(novelId);
        novel.setStatus(4); // ARCHIVED

        when(novelMapper.selectByPrimaryKey(novelId)).thenReturn(novel);
        when(novelMapper.updateByPrimaryKeySelective(any(Novel.class))).thenReturn(1);
        doNothing().when(redisUtil).invalidateNovelCaches(novelId);

        // Act
        NovelDetailResponseDTO result = novelService.unarchiveNovel(novelId);

        // Assert
        assertNotNull(result);
        assertEquals("DRAFT", result.getStatus());
        verify(novelMapper).selectByPrimaryKey(novelId);
        verify(novelMapper).updateByPrimaryKeySelective(any(Novel.class));
        verify(redisUtil).invalidateNovelCaches(novelId);
    }

    @Test
    void unarchiveNovel_WithNonArchivedNovel_ShouldThrowException() {
        // Arrange
        Integer novelId = 1;
        Novel novel = new Novel();
        novel.setId(novelId);
        novel.setStatus(0); // DRAFT

        when(novelMapper.selectByPrimaryKey(novelId)).thenReturn(novel);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> novelService.unarchiveNovel(novelId));
        verify(novelMapper).selectByPrimaryKey(novelId);
    }

    @Test
    void getNovel_WithNullId_ShouldThrowException() {
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> novelService.getNovel(null));
    }

    private Novel createTestNovel(Integer id, String title) {
        Novel novel = new Novel();
        novel.setId(id);
        novel.setUuid(UUID.randomUUID());
        novel.setTitle(title);
        novel.setAuthorId(UUID.randomUUID());
        novel.setAuthorName("test-author");
        novel.setCategoryId(1);
        novel.setStatus(0); // DRAFT
        novel.setIsCompleted(false);
        novel.setCreateTime(new Date());
        novel.setUpdateTime(new Date());
        return novel;
    }
}
