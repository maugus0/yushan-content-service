package com.yushan.content_service.entity.elasticsearch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

/**
 * Unit tests for ChapterDocument entity
 */
class ChapterDocumentTest {

    private ChapterDocument chapterDocument;

    @BeforeEach
    void setUp() {
        chapterDocument = new ChapterDocument();
    }

    @Test
    void testDefaultConstructor() {
        ChapterDocument document = new ChapterDocument();
        assertNotNull(document);
        assertNull(document.getId());
        assertNull(document.getUuid());
        assertNull(document.getNovelId());
        assertNull(document.getChapterNumber());
        assertNull(document.getTitle());
        assertNull(document.getContent());
        assertNull(document.getWordCnt());
        assertNull(document.getIsPremium());
        assertNull(document.getYuanCost());
        assertNull(document.getViewCnt());
        assertNull(document.getIsValid());
        assertNull(document.getCreateTime());
        assertNull(document.getUpdateTime());
        assertNull(document.getPublishTime());
    }

    @Test
    void testParameterizedConstructor() {
        String id = "test-id";
        String uuid = "test-uuid";
        Integer novelId = 1;
        Integer chapterNumber = 1;
        String title = "Test Chapter";
        String content = "Test content";
        Integer wordCnt = 100;
        Boolean isPremium = true;
        Double yuanCost = 10.5;
        Long viewCnt = 1000L;
        Boolean isValid = true;
        Date createTime = new Date();
        Date updateTime = new Date();
        Date publishTime = new Date();

        ChapterDocument document = new ChapterDocument(
            id, uuid, novelId, chapterNumber, title, content, wordCnt,
            isPremium, yuanCost, viewCnt, isValid, createTime, updateTime, publishTime
        );

        assertEquals(id, document.getId());
        assertEquals(uuid, document.getUuid());
        assertEquals(novelId, document.getNovelId());
        assertEquals(chapterNumber, document.getChapterNumber());
        assertEquals(title, document.getTitle());
        assertEquals(content, document.getContent());
        assertEquals(wordCnt, document.getWordCnt());
        assertEquals(isPremium, document.getIsPremium());
        assertEquals(yuanCost, document.getYuanCost());
        assertEquals(viewCnt, document.getViewCnt());
        assertEquals(isValid, document.getIsValid());
        assertNotNull(document.getCreateTime());
        assertNotNull(document.getUpdateTime());
        assertNotNull(document.getPublishTime());
    }

    @Test
    void testParameterizedConstructorWithNullDates() {
        ChapterDocument document = new ChapterDocument(
            "id", "uuid", 1, 1, "title", "content", 100,
            true, 10.5, 1000L, true, null, null, null
        );

        assertNull(document.getCreateTime());
        assertNull(document.getUpdateTime());
        assertNull(document.getPublishTime());
    }

    @Test
    void testSettersAndGetters() {
        // Test all setters and getters
        chapterDocument.setId("test-id");
        assertEquals("test-id", chapterDocument.getId());

        chapterDocument.setUuid("test-uuid");
        assertEquals("test-uuid", chapterDocument.getUuid());

        chapterDocument.setNovelId(1);
        assertEquals(1, chapterDocument.getNovelId());

        chapterDocument.setChapterNumber(1);
        assertEquals(1, chapterDocument.getChapterNumber());

        chapterDocument.setTitle("Test Title");
        assertEquals("Test Title", chapterDocument.getTitle());

        chapterDocument.setContent("Test Content");
        assertEquals("Test Content", chapterDocument.getContent());

        chapterDocument.setWordCnt(100);
        assertEquals(100, chapterDocument.getWordCnt());

        chapterDocument.setIsPremium(true);
        assertTrue(chapterDocument.getIsPremium());

        chapterDocument.setYuanCost(10.5);
        assertEquals(10.5, chapterDocument.getYuanCost());

        chapterDocument.setViewCnt(1000L);
        assertEquals(1000L, chapterDocument.getViewCnt());

        chapterDocument.setIsValid(true);
        assertTrue(chapterDocument.getIsValid());
    }

    @Test
    void testDateHandling() {
        Date originalDate = new Date();
        
        // Test setCreateTime
        chapterDocument.setCreateTime(originalDate);
        Date retrievedDate = chapterDocument.getCreateTime();
        assertNotNull(retrievedDate);
        assertEquals(originalDate.getTime(), retrievedDate.getTime());
        assertNotSame(originalDate, retrievedDate); // Should be cloned

        // Test setUpdateTime
        chapterDocument.setUpdateTime(originalDate);
        retrievedDate = chapterDocument.getUpdateTime();
        assertNotNull(retrievedDate);
        assertEquals(originalDate.getTime(), retrievedDate.getTime());
        assertNotSame(originalDate, retrievedDate); // Should be cloned

        // Test setPublishTime
        chapterDocument.setPublishTime(originalDate);
        retrievedDate = chapterDocument.getPublishTime();
        assertNotNull(retrievedDate);
        assertEquals(originalDate.getTime(), retrievedDate.getTime());
        assertNotSame(originalDate, retrievedDate); // Should be cloned
    }

    @Test
    void testDateHandlingWithNull() {
        // Test setting null dates
        chapterDocument.setCreateTime(null);
        assertNull(chapterDocument.getCreateTime());

        chapterDocument.setUpdateTime(null);
        assertNull(chapterDocument.getUpdateTime());

        chapterDocument.setPublishTime(null);
        assertNull(chapterDocument.getPublishTime());
    }

    @Test
    void testBooleanFields() {
        // Test isPremium field
        chapterDocument.setIsPremium(true);
        assertTrue(chapterDocument.getIsPremium());

        chapterDocument.setIsPremium(false);
        assertFalse(chapterDocument.getIsPremium());

        chapterDocument.setIsPremium(null);
        assertNull(chapterDocument.getIsPremium());

        // Test isValid field
        chapterDocument.setIsValid(true);
        assertTrue(chapterDocument.getIsValid());

        chapterDocument.setIsValid(false);
        assertFalse(chapterDocument.getIsValid());

        chapterDocument.setIsValid(null);
        assertNull(chapterDocument.getIsValid());
    }

    @Test
    void testNumericFields() {
        // Test Integer fields
        chapterDocument.setNovelId(123);
        assertEquals(123, chapterDocument.getNovelId());

        chapterDocument.setChapterNumber(456);
        assertEquals(456, chapterDocument.getChapterNumber());

        chapterDocument.setWordCnt(789);
        assertEquals(789, chapterDocument.getWordCnt());

        // Test Long field
        chapterDocument.setViewCnt(999L);
        assertEquals(999L, chapterDocument.getViewCnt());

        // Test Double field
        chapterDocument.setYuanCost(15.75);
        assertEquals(15.75, chapterDocument.getYuanCost());
    }

    @Test
    void testStringFields() {
        // Test all string fields
        chapterDocument.setId("chapter-123");
        assertEquals("chapter-123", chapterDocument.getId());

        chapterDocument.setUuid("uuid-456");
        assertEquals("uuid-456", chapterDocument.getUuid());

        chapterDocument.setTitle("Amazing Chapter");
        assertEquals("Amazing Chapter", chapterDocument.getTitle());

        chapterDocument.setContent("This is the chapter content with lots of text...");
        assertEquals("This is the chapter content with lots of text...", chapterDocument.getContent());
    }

    @Test
    void testEdgeCases() {
        // Test with empty strings
        chapterDocument.setTitle("");
        assertEquals("", chapterDocument.getTitle());

        chapterDocument.setContent("");
        assertEquals("", chapterDocument.getContent());

        // Test with zero values
        chapterDocument.setNovelId(0);
        assertEquals(0, chapterDocument.getNovelId());

        chapterDocument.setChapterNumber(0);
        assertEquals(0, chapterDocument.getChapterNumber());

        chapterDocument.setWordCnt(0);
        assertEquals(0, chapterDocument.getWordCnt());

        chapterDocument.setViewCnt(0L);
        assertEquals(0L, chapterDocument.getViewCnt());

        chapterDocument.setYuanCost(0.0);
        assertEquals(0.0, chapterDocument.getYuanCost());
    }
}
