package com.yushan.content_service.dto.novel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;
import java.util.UUID;

/**
 * Unit tests for NovelDetailResponseDTO to improve code coverage.
 */
@DisplayName("NovelDetailResponseDTO Tests")
class NovelDetailResponseDTOTest {

    @Test
    @DisplayName("Default constructor should create empty DTO")
    void testDefaultConstructor() {
        NovelDetailResponseDTO dto = new NovelDetailResponseDTO();
        
        assertNull(dto.getId());
        assertNull(dto.getUuid());
        assertNull(dto.getTitle());
        assertNull(dto.getAuthorId());
        assertNull(dto.getAuthorUsername());
        assertNull(dto.getCategoryId());
        assertNull(dto.getCategoryName());
        assertNull(dto.getSynopsis());
        assertNull(dto.getCoverImgUrl());
        assertNull(dto.getStatus());
        assertNull(dto.getIsCompleted());
        assertNull(dto.getChapterCnt());
        assertNull(dto.getWordCnt());
        assertNull(dto.getAvgRating());
        assertNull(dto.getReviewCnt());
        assertNull(dto.getViewCnt());
        assertNull(dto.getVoteCnt());
        assertNull(dto.getYuanCnt());
        assertNull(dto.getCreateTime());
        assertNull(dto.getUpdateTime());
        assertNull(dto.getPublishTime());
    }

    @Test
    @DisplayName("Parameterized constructor should set all fields")
    void testParameterizedConstructor() {
        Integer id = 1;
        UUID uuid = UUID.randomUUID();
        String title = "Test Novel";
        UUID authorId = UUID.randomUUID();
        String authorUsername = "testauthor";
        Integer categoryId = 1;
        String categoryName = "Fantasy";
        String synopsis = "Test Synopsis";
        String coverImgUrl = "http://example.com/cover.jpg";
        String status = "PUBLISHED";
        Boolean isCompleted = false;
        Integer chapterCnt = 10;
        Long wordCnt = 50000L;
        Float avgRating = 4.5f;
        Integer reviewCnt = 100;
        Long viewCnt = 1000L;
        Integer voteCnt = 50;
        Float yuanCnt = 10.5f;
        Date createTime = new Date();
        Date updateTime = new Date();
        Date publishTime = new Date();

        NovelDetailResponseDTO dto = new NovelDetailResponseDTO(
            id, uuid, title, authorId, authorUsername, categoryId, categoryName,
            synopsis, coverImgUrl, status, isCompleted, chapterCnt, wordCnt,
            avgRating, reviewCnt, viewCnt, voteCnt, yuanCnt,
            createTime, updateTime, publishTime
        );

        assertEquals(id, dto.getId());
        assertEquals(uuid, dto.getUuid());
        assertEquals(title, dto.getTitle());
        assertEquals(authorId, dto.getAuthorId());
        assertEquals(authorUsername, dto.getAuthorUsername());
        assertEquals(categoryId, dto.getCategoryId());
        assertEquals(categoryName, dto.getCategoryName());
        assertEquals(synopsis, dto.getSynopsis());
        assertEquals(coverImgUrl, dto.getCoverImgUrl());
        assertEquals(status, dto.getStatus());
        assertEquals(isCompleted, dto.getIsCompleted());
        assertEquals(chapterCnt, dto.getChapterCnt());
        assertEquals(wordCnt, dto.getWordCnt());
        assertEquals(avgRating, dto.getAvgRating());
        assertEquals(reviewCnt, dto.getReviewCnt());
        assertEquals(viewCnt, dto.getViewCnt());
        assertEquals(voteCnt, dto.getVoteCnt());
        assertEquals(yuanCnt, dto.getYuanCnt());
        assertNotNull(dto.getCreateTime());
        assertNotNull(dto.getUpdateTime());
        assertNotNull(dto.getPublishTime());
    }

    @Test
    @DisplayName("Constructor should handle null dates correctly")
    void testConstructorWithNullDates() {
        NovelDetailResponseDTO dto = new NovelDetailResponseDTO(
            1, UUID.randomUUID(), "Test", UUID.randomUUID(), "author",
            1, "Category", "Synopsis", "url", "status", false,
            10, 1000L, 4.0f, 10, 100L, 5, 1.0f,
            null, null, null
        );

        assertNull(dto.getCreateTime());
        assertNull(dto.getUpdateTime());
        assertNull(dto.getPublishTime());
    }

    @Test
    @DisplayName("Setters and getters should work correctly")
    void testSettersAndGetters() {
        NovelDetailResponseDTO dto = new NovelDetailResponseDTO();

        // Test basic fields
        Integer id = 123;
        dto.setId(id);
        assertEquals(id, dto.getId());

        UUID uuid = UUID.randomUUID();
        dto.setUuid(uuid);
        assertEquals(uuid, dto.getUuid());

        String title = "New Title";
        dto.setTitle(title);
        assertEquals(title, dto.getTitle());

        UUID authorId = UUID.randomUUID();
        dto.setAuthorId(authorId);
        assertEquals(authorId, dto.getAuthorId());

        String authorUsername = "newauthor";
        dto.setAuthorUsername(authorUsername);
        assertEquals(authorUsername, dto.getAuthorUsername());

        Integer categoryId = 5;
        dto.setCategoryId(categoryId);
        assertEquals(categoryId, dto.getCategoryId());

        String categoryName = "Romance";
        dto.setCategoryName(categoryName);
        assertEquals(categoryName, dto.getCategoryName());

        String synopsis = "New Synopsis";
        dto.setSynopsis(synopsis);
        assertEquals(synopsis, dto.getSynopsis());

        String coverImgUrl = "http://example.com/new-cover.jpg";
        dto.setCoverImgUrl(coverImgUrl);
        assertEquals(coverImgUrl, dto.getCoverImgUrl());

        String status = "DRAFT";
        dto.setStatus(status);
        assertEquals(status, dto.getStatus());

        Boolean isCompleted = true;
        dto.setIsCompleted(isCompleted);
        assertEquals(isCompleted, dto.getIsCompleted());
    }

    @Test
    @DisplayName("Statistics setters and getters should work correctly")
    void testStatisticsSettersAndGetters() {
        NovelDetailResponseDTO dto = new NovelDetailResponseDTO();

        Integer chapterCnt = 25;
        dto.setChapterCnt(chapterCnt);
        assertEquals(chapterCnt, dto.getChapterCnt());

        Long wordCnt = 100000L;
        dto.setWordCnt(wordCnt);
        assertEquals(wordCnt, dto.getWordCnt());

        Float avgRating = 4.8f;
        dto.setAvgRating(avgRating);
        assertEquals(avgRating, dto.getAvgRating());

        Integer reviewCnt = 200;
        dto.setReviewCnt(reviewCnt);
        assertEquals(reviewCnt, dto.getReviewCnt());

        Long viewCnt = 5000L;
        dto.setViewCnt(viewCnt);
        assertEquals(viewCnt, dto.getViewCnt());

        Integer voteCnt = 150;
        dto.setVoteCnt(voteCnt);
        assertEquals(voteCnt, dto.getVoteCnt());

        Float yuanCnt = 25.5f;
        dto.setYuanCnt(yuanCnt);
        assertEquals(yuanCnt, dto.getYuanCnt());
    }

    @Test
    @DisplayName("Date setters and getters should work correctly")
    void testDateSettersAndGetters() {
        NovelDetailResponseDTO dto = new NovelDetailResponseDTO();

        Date createTime = new Date();
        dto.setCreateTime(createTime);
        assertNotNull(dto.getCreateTime());
        assertEquals(createTime.getTime(), dto.getCreateTime().getTime());

        Date updateTime = new Date(System.currentTimeMillis() + 1000);
        dto.setUpdateTime(updateTime);
        assertNotNull(dto.getUpdateTime());
        assertEquals(updateTime.getTime(), dto.getUpdateTime().getTime());

        Date publishTime = new Date(System.currentTimeMillis() + 2000);
        dto.setPublishTime(publishTime);
        assertNotNull(dto.getPublishTime());
        assertEquals(publishTime.getTime(), dto.getPublishTime().getTime());
    }

    @Test
    @DisplayName("Date setters should handle null values correctly")
    void testDateSettersWithNullValues() {
        NovelDetailResponseDTO dto = new NovelDetailResponseDTO();

        dto.setCreateTime(null);
        assertNull(dto.getCreateTime());

        dto.setUpdateTime(null);
        assertNull(dto.getUpdateTime());

        dto.setPublishTime(null);
        assertNull(dto.getPublishTime());
    }

    @Test
    @DisplayName("Date getters should return defensive copies")
    void testDateGettersReturnDefensiveCopies() {
        NovelDetailResponseDTO dto = new NovelDetailResponseDTO();
        Date originalDate = new Date();
        dto.setCreateTime(originalDate);

        Date retrievedDate = dto.getCreateTime();
        assertNotSame(originalDate, retrievedDate);
        assertEquals(originalDate.getTime(), retrievedDate.getTime());

        // Modify the retrieved date
        retrievedDate.setTime(retrievedDate.getTime() + 1000);
        
        // Original should not be affected
        Date retrievedAgain = dto.getCreateTime();
        assertEquals(originalDate.getTime(), retrievedAgain.getTime());
    }

    @Test
    @DisplayName("toString should return formatted string")
    void testToString() {
        NovelDetailResponseDTO dto = new NovelDetailResponseDTO();
        dto.setId(1);
        dto.setUuid(UUID.randomUUID());
        dto.setTitle("Test Novel");
        dto.setAuthorId(UUID.randomUUID());
        dto.setAuthorUsername("testauthor");
        dto.setCategoryId(1);
        dto.setCategoryName("Fantasy");
        dto.setStatus("PUBLISHED");
        dto.setIsCompleted(false);
        dto.setChapterCnt(10);
        dto.setWordCnt(50000L);
        dto.setViewCnt(1000L);
        dto.setCreateTime(new Date());

        String result = dto.toString();
        
        assertTrue(result.contains("NovelDetailResponseDTO"));
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("title='Test Novel'"));
        assertTrue(result.contains("authorUsername='testauthor'"));
        assertTrue(result.contains("categoryId=1"));
        assertTrue(result.contains("categoryName='Fantasy'"));
        assertTrue(result.contains("status='PUBLISHED'"));
        assertTrue(result.contains("isCompleted=false"));
        assertTrue(result.contains("chapterCnt=10"));
        assertTrue(result.contains("wordCnt=50000"));
        assertTrue(result.contains("viewCnt=1000"));
    }

    @Test
    @DisplayName("Should handle null values in setters")
    void testNullValuesInSetters() {
        NovelDetailResponseDTO dto = new NovelDetailResponseDTO();

        dto.setId(null);
        dto.setUuid(null);
        dto.setTitle(null);
        dto.setAuthorId(null);
        dto.setAuthorUsername(null);
        dto.setCategoryId(null);
        dto.setCategoryName(null);
        dto.setSynopsis(null);
        dto.setCoverImgUrl(null);
        dto.setStatus(null);
        dto.setIsCompleted(null);
        dto.setChapterCnt(null);
        dto.setWordCnt(null);
        dto.setAvgRating(null);
        dto.setReviewCnt(null);
        dto.setViewCnt(null);
        dto.setVoteCnt(null);
        dto.setYuanCnt(null);

        assertNull(dto.getId());
        assertNull(dto.getUuid());
        assertNull(dto.getTitle());
        assertNull(dto.getAuthorId());
        assertNull(dto.getAuthorUsername());
        assertNull(dto.getCategoryId());
        assertNull(dto.getCategoryName());
        assertNull(dto.getSynopsis());
        assertNull(dto.getCoverImgUrl());
        assertNull(dto.getStatus());
        assertNull(dto.getIsCompleted());
        assertNull(dto.getChapterCnt());
        assertNull(dto.getWordCnt());
        assertNull(dto.getAvgRating());
        assertNull(dto.getReviewCnt());
        assertNull(dto.getViewCnt());
        assertNull(dto.getVoteCnt());
        assertNull(dto.getYuanCnt());
    }

    @Test
    @DisplayName("Should handle edge case values")
    void testEdgeCaseValues() {
        NovelDetailResponseDTO dto = new NovelDetailResponseDTO();

        // Test negative values
        dto.setId(-1);
        dto.setCategoryId(-1);
        dto.setChapterCnt(-1);
        dto.setWordCnt(-1L);
        dto.setAvgRating(-1.0f);
        dto.setReviewCnt(-1);
        dto.setViewCnt(-1L);
        dto.setVoteCnt(-1);
        dto.setYuanCnt(-1.0f);

        assertEquals(Integer.valueOf(-1), dto.getId());
        assertEquals(Integer.valueOf(-1), dto.getCategoryId());
        assertEquals(Integer.valueOf(-1), dto.getChapterCnt());
        assertEquals(Long.valueOf(-1), dto.getWordCnt());
        assertEquals(Float.valueOf(-1.0f), dto.getAvgRating());
        assertEquals(Integer.valueOf(-1), dto.getReviewCnt());
        assertEquals(Long.valueOf(-1), dto.getViewCnt());
        assertEquals(Integer.valueOf(-1), dto.getVoteCnt());
        assertEquals(Float.valueOf(-1.0f), dto.getYuanCnt());

        // Test zero values
        dto.setId(0);
        dto.setCategoryId(0);
        dto.setChapterCnt(0);
        dto.setWordCnt(0L);
        dto.setAvgRating(0.0f);
        dto.setReviewCnt(0);
        dto.setViewCnt(0L);
        dto.setVoteCnt(0);
        dto.setYuanCnt(0.0f);

        assertEquals(Integer.valueOf(0), dto.getId());
        assertEquals(Integer.valueOf(0), dto.getCategoryId());
        assertEquals(Integer.valueOf(0), dto.getChapterCnt());
        assertEquals(Long.valueOf(0), dto.getWordCnt());
        assertEquals(Float.valueOf(0.0f), dto.getAvgRating());
        assertEquals(Integer.valueOf(0), dto.getReviewCnt());
        assertEquals(Long.valueOf(0), dto.getViewCnt());
        assertEquals(Integer.valueOf(0), dto.getVoteCnt());
        assertEquals(Float.valueOf(0.0f), dto.getYuanCnt());
    }
}
