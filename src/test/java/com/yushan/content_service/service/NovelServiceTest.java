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
    private NovelService novelService;

    @BeforeEach
    void setUp() {
        novelMapper = Mockito.mock(NovelMapper.class);
        redisUtil = Mockito.mock(RedisUtil.class);
        kafkaEventProducerService = Mockito.mock(KafkaEventProducerService.class);

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
