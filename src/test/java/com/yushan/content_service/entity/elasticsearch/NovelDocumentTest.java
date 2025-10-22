package com.yushan.content_service.entity.elasticsearch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

/**
 * Unit tests for NovelDocument entity
 */
class NovelDocumentTest {

    private NovelDocument novelDocument;

    @BeforeEach
    void setUp() {
        novelDocument = new NovelDocument();
    }

    @Test
    void testDefaultConstructor() {
        NovelDocument document = new NovelDocument();
        assertNotNull(document);
        assertNull(document.getId());
        assertNull(document.getUuid());
        assertNull(document.getTitle());
        assertNull(document.getSynopsis());
        assertNull(document.getAuthorId());
        assertNull(document.getAuthorName());
        assertNull(document.getCategoryId());
        assertNull(document.getStatus());
        assertNull(document.getIsCompleted());
        assertNull(document.getChapterCnt());
        assertNull(document.getWordCnt());
        assertNull(document.getAvgRating());
        assertNull(document.getReviewCnt());
        assertNull(document.getViewCnt());
        assertNull(document.getVoteCnt());
        assertNull(document.getYuanCnt());
        assertNull(document.getCreateTime());
        assertNull(document.getUpdateTime());
        assertNull(document.getPublishTime());
        assertNull(document.getCoverImgUrl());
    }

    @Test
    void testParameterizedConstructor() {
        String id = "test-id";
        String uuid = "test-uuid";
        String title = "Test Novel";
        String synopsis = "Test synopsis";
        String authorId = "author-123";
        String authorName = "Test Author";
        Integer categoryId = 1;
        String status = "PUBLISHED";
        Boolean isCompleted = false;
        Integer chapterCnt = 10;
        Long wordCnt = 50000L;
        Double avgRating = 4.5;
        Integer reviewCnt = 25;
        Long viewCnt = 10000L;
        Integer voteCnt = 100;
        Double yuanCnt = 50.0;
        Date createTime = new Date();
        Date updateTime = new Date();
        Date publishTime = new Date();
        String coverImgUrl = "https://example.com/cover.jpg";

        NovelDocument document = new NovelDocument(
            id, uuid, title, synopsis, authorId, authorName, categoryId, status,
            isCompleted, chapterCnt, wordCnt, avgRating, reviewCnt, viewCnt,
            voteCnt, yuanCnt, createTime, updateTime, publishTime, coverImgUrl
        );

        assertEquals(id, document.getId());
        assertEquals(uuid, document.getUuid());
        assertEquals(title, document.getTitle());
        assertEquals(synopsis, document.getSynopsis());
        assertEquals(authorId, document.getAuthorId());
        assertEquals(authorName, document.getAuthorName());
        assertEquals(categoryId, document.getCategoryId());
        assertEquals(status, document.getStatus());
        assertEquals(isCompleted, document.getIsCompleted());
        assertEquals(chapterCnt, document.getChapterCnt());
        assertEquals(wordCnt, document.getWordCnt());
        assertEquals(avgRating, document.getAvgRating());
        assertEquals(reviewCnt, document.getReviewCnt());
        assertEquals(viewCnt, document.getViewCnt());
        assertEquals(voteCnt, document.getVoteCnt());
        assertEquals(yuanCnt, document.getYuanCnt());
        assertNotNull(document.getCreateTime());
        assertNotNull(document.getUpdateTime());
        assertNotNull(document.getPublishTime());
        assertEquals(coverImgUrl, document.getCoverImgUrl());
    }

    @Test
    void testParameterizedConstructorWithNullDates() {
        NovelDocument document = new NovelDocument(
            "id", "uuid", "title", "synopsis", "authorId", "authorName", 1, "PUBLISHED",
            false, 10, 50000L, 4.5, 25, 10000L, 100, 50.0, null, null, null, "cover.jpg"
        );

        assertNull(document.getCreateTime());
        assertNull(document.getUpdateTime());
        assertNull(document.getPublishTime());
    }

    @Test
    void testSettersAndGetters() {
        // Test all setters and getters
        novelDocument.setId("test-id");
        assertEquals("test-id", novelDocument.getId());

        novelDocument.setUuid("test-uuid");
        assertEquals("test-uuid", novelDocument.getUuid());

        novelDocument.setTitle("Test Title");
        assertEquals("Test Title", novelDocument.getTitle());

        novelDocument.setSynopsis("Test Synopsis");
        assertEquals("Test Synopsis", novelDocument.getSynopsis());

        novelDocument.setAuthorId("author-123");
        assertEquals("author-123", novelDocument.getAuthorId());

        novelDocument.setAuthorName("Test Author");
        assertEquals("Test Author", novelDocument.getAuthorName());

        novelDocument.setCategoryId(1);
        assertEquals(1, novelDocument.getCategoryId());

        novelDocument.setStatus("PUBLISHED");
        assertEquals("PUBLISHED", novelDocument.getStatus());

        novelDocument.setIsCompleted(true);
        assertTrue(novelDocument.getIsCompleted());

        novelDocument.setChapterCnt(10);
        assertEquals(10, novelDocument.getChapterCnt());

        novelDocument.setWordCnt(50000L);
        assertEquals(50000L, novelDocument.getWordCnt());

        novelDocument.setAvgRating(4.5);
        assertEquals(4.5, novelDocument.getAvgRating());

        novelDocument.setReviewCnt(25);
        assertEquals(25, novelDocument.getReviewCnt());

        novelDocument.setViewCnt(10000L);
        assertEquals(10000L, novelDocument.getViewCnt());

        novelDocument.setVoteCnt(100);
        assertEquals(100, novelDocument.getVoteCnt());

        novelDocument.setYuanCnt(50.0);
        assertEquals(50.0, novelDocument.getYuanCnt());

        novelDocument.setCoverImgUrl("https://example.com/cover.jpg");
        assertEquals("https://example.com/cover.jpg", novelDocument.getCoverImgUrl());
    }

    @Test
    void testDateHandling() {
        Date originalDate = new Date();
        
        // Test setCreateTime
        novelDocument.setCreateTime(originalDate);
        Date retrievedDate = novelDocument.getCreateTime();
        assertNotNull(retrievedDate);
        assertEquals(originalDate.getTime(), retrievedDate.getTime());
        assertNotSame(originalDate, retrievedDate); // Should be cloned

        // Test setUpdateTime
        novelDocument.setUpdateTime(originalDate);
        retrievedDate = novelDocument.getUpdateTime();
        assertNotNull(retrievedDate);
        assertEquals(originalDate.getTime(), retrievedDate.getTime());
        assertNotSame(originalDate, retrievedDate); // Should be cloned

        // Test setPublishTime
        novelDocument.setPublishTime(originalDate);
        retrievedDate = novelDocument.getPublishTime();
        assertNotNull(retrievedDate);
        assertEquals(originalDate.getTime(), retrievedDate.getTime());
        assertNotSame(originalDate, retrievedDate); // Should be cloned
    }

    @Test
    void testDateHandlingWithNull() {
        // Test setting null dates
        novelDocument.setCreateTime(null);
        assertNull(novelDocument.getCreateTime());

        novelDocument.setUpdateTime(null);
        assertNull(novelDocument.getUpdateTime());

        novelDocument.setPublishTime(null);
        assertNull(novelDocument.getPublishTime());
    }

    @Test
    void testBooleanFields() {
        // Test isCompleted field
        novelDocument.setIsCompleted(true);
        assertTrue(novelDocument.getIsCompleted());

        novelDocument.setIsCompleted(false);
        assertFalse(novelDocument.getIsCompleted());

        novelDocument.setIsCompleted(null);
        assertNull(novelDocument.getIsCompleted());
    }

    @Test
    void testNumericFields() {
        // Test Integer fields
        novelDocument.setCategoryId(123);
        assertEquals(123, novelDocument.getCategoryId());

        novelDocument.setChapterCnt(456);
        assertEquals(456, novelDocument.getChapterCnt());

        novelDocument.setReviewCnt(789);
        assertEquals(789, novelDocument.getReviewCnt());

        novelDocument.setVoteCnt(999);
        assertEquals(999, novelDocument.getVoteCnt());

        // Test Long fields
        novelDocument.setWordCnt(50000L);
        assertEquals(50000L, novelDocument.getWordCnt());

        novelDocument.setViewCnt(10000L);
        assertEquals(10000L, novelDocument.getViewCnt());

        // Test Double fields
        novelDocument.setAvgRating(4.5);
        assertEquals(4.5, novelDocument.getAvgRating());

        novelDocument.setYuanCnt(50.0);
        assertEquals(50.0, novelDocument.getYuanCnt());
    }

    @Test
    void testStringFields() {
        // Test all string fields
        novelDocument.setId("novel-123");
        assertEquals("novel-123", novelDocument.getId());

        novelDocument.setUuid("uuid-456");
        assertEquals("uuid-456", novelDocument.getUuid());

        novelDocument.setTitle("Amazing Novel");
        assertEquals("Amazing Novel", novelDocument.getTitle());

        novelDocument.setSynopsis("This is an amazing novel synopsis...");
        assertEquals("This is an amazing novel synopsis...", novelDocument.getSynopsis());

        novelDocument.setAuthorId("author-789");
        assertEquals("author-789", novelDocument.getAuthorId());

        novelDocument.setAuthorName("John Doe");
        assertEquals("John Doe", novelDocument.getAuthorName());

        novelDocument.setStatus("DRAFT");
        assertEquals("DRAFT", novelDocument.getStatus());

        novelDocument.setCoverImgUrl("https://example.com/cover.jpg");
        assertEquals("https://example.com/cover.jpg", novelDocument.getCoverImgUrl());
    }

    @Test
    void testEdgeCases() {
        // Test with empty strings
        novelDocument.setTitle("");
        assertEquals("", novelDocument.getTitle());

        novelDocument.setSynopsis("");
        assertEquals("", novelDocument.getSynopsis());

        novelDocument.setAuthorName("");
        assertEquals("", novelDocument.getAuthorName());

        novelDocument.setStatus("");
        assertEquals("", novelDocument.getStatus());

        novelDocument.setCoverImgUrl("");
        assertEquals("", novelDocument.getCoverImgUrl());

        // Test with zero values
        novelDocument.setCategoryId(0);
        assertEquals(0, novelDocument.getCategoryId());

        novelDocument.setChapterCnt(0);
        assertEquals(0, novelDocument.getChapterCnt());

        novelDocument.setWordCnt(0L);
        assertEquals(0L, novelDocument.getWordCnt());

        novelDocument.setAvgRating(0.0);
        assertEquals(0.0, novelDocument.getAvgRating());

        novelDocument.setReviewCnt(0);
        assertEquals(0, novelDocument.getReviewCnt());

        novelDocument.setViewCnt(0L);
        assertEquals(0L, novelDocument.getViewCnt());

        novelDocument.setVoteCnt(0);
        assertEquals(0, novelDocument.getVoteCnt());

        novelDocument.setYuanCnt(0.0);
        assertEquals(0.0, novelDocument.getYuanCnt());
    }

    @Test
    void testStatusValues() {
        // Test different status values
        novelDocument.setStatus("DRAFT");
        assertEquals("DRAFT", novelDocument.getStatus());

        novelDocument.setStatus("UNDER_REVIEW");
        assertEquals("UNDER_REVIEW", novelDocument.getStatus());

        novelDocument.setStatus("PUBLISHED");
        assertEquals("PUBLISHED", novelDocument.getStatus());

        novelDocument.setStatus("HIDDEN");
        assertEquals("HIDDEN", novelDocument.getStatus());

        novelDocument.setStatus("ARCHIVED");
        assertEquals("ARCHIVED", novelDocument.getStatus());
    }
}
