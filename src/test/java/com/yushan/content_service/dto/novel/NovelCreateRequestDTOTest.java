package com.yushan.content_service.dto.novel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NovelCreateRequestDTO to improve code coverage.
 */
@DisplayName("NovelCreateRequestDTO Tests")
class NovelCreateRequestDTOTest {

    @Test
    @DisplayName("Default constructor should create empty DTO")
    void testDefaultConstructor() {
        NovelCreateRequestDTO dto = new NovelCreateRequestDTO();
        
        assertNull(dto.getTitle());
        assertNull(dto.getSynopsis());
        assertNull(dto.getCategoryId());
        assertNull(dto.getCoverImgBase64());
        assertNull(dto.getIsCompleted());
    }

    @Test
    @DisplayName("Parameterized constructor should set all fields")
    void testParameterizedConstructor() {
        String title = "Test Novel";
        String synopsis = "Test Synopsis";
        Integer categoryId = 1;
        String coverImgBase64 = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAYEBQYFBAYGBQYHBwYIChAKCgkJChQODwwQFxQYGBcUFhYaHSUfGhsjHBYWICwgIyYnKSopGR8tMC0oMCUoKSj/2wBDAQcHBwoIChMKChMoGhYaKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCj/wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAv/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/8QAFQEBAQAAAAAAAAAAAAAAAAAAAAX/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIRAxEAPwCdABmX/9k=";
        Boolean isCompleted = false;

        NovelCreateRequestDTO dto = new NovelCreateRequestDTO(title, synopsis, categoryId, coverImgBase64, isCompleted);

        assertEquals(title, dto.getTitle());
        assertEquals(synopsis, dto.getSynopsis());
        assertEquals(categoryId, dto.getCategoryId());
        assertEquals(coverImgBase64, dto.getCoverImgBase64());
        assertEquals(isCompleted, dto.getIsCompleted());
    }

    @Test
    @DisplayName("Setters and getters should work correctly")
    void testSettersAndGetters() {
        NovelCreateRequestDTO dto = new NovelCreateRequestDTO();

        // Test title
        String title = "New Title";
        dto.setTitle(title);
        assertEquals(title, dto.getTitle());

        // Test synopsis
        String synopsis = "New Synopsis";
        dto.setSynopsis(synopsis);
        assertEquals(synopsis, dto.getSynopsis());

        // Test categoryId
        Integer categoryId = 5;
        dto.setCategoryId(categoryId);
        assertEquals(categoryId, dto.getCategoryId());

        // Test coverImgBase64
        String coverImg = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
        dto.setCoverImgBase64(coverImg);
        assertEquals(coverImg, dto.getCoverImgBase64());

        // Test isCompleted
        Boolean isCompleted = true;
        dto.setIsCompleted(isCompleted);
        assertEquals(isCompleted, dto.getIsCompleted());
    }

    @Test
    @DisplayName("toString should return formatted string")
    void testToString() {
        NovelCreateRequestDTO dto = new NovelCreateRequestDTO();
        dto.setTitle("Test Title");
        dto.setSynopsis("Test Synopsis");
        dto.setCategoryId(1);
        dto.setCoverImgBase64("data:image/jpeg;base64,test");
        dto.setIsCompleted(false);

        String result = dto.toString();
        
        assertTrue(result.contains("NovelCreateRequestDTO"));
        assertTrue(result.contains("title='Test Title'"));
        assertTrue(result.contains("synopsis='Test Synopsis'"));
        assertTrue(result.contains("categoryId=1"));
        assertTrue(result.contains("[BASE64_DATA]"));
        assertTrue(result.contains("isCompleted=false"));
    }

    @Test
    @DisplayName("toString should handle null values correctly")
    void testToStringWithNullValues() {
        NovelCreateRequestDTO dto = new NovelCreateRequestDTO();
        dto.setTitle(null);
        dto.setCoverImgBase64(null);

        String result = dto.toString();
        
        assertTrue(result.contains("title='null'"));
        assertTrue(result.contains("coverImgBase64='null'"));
    }

    @Test
    @DisplayName("Should handle null values in setters")
    void testNullValuesInSetters() {
        NovelCreateRequestDTO dto = new NovelCreateRequestDTO();

        dto.setTitle(null);
        dto.setSynopsis(null);
        dto.setCategoryId(null);
        dto.setCoverImgBase64(null);
        dto.setIsCompleted(null);

        assertNull(dto.getTitle());
        assertNull(dto.getSynopsis());
        assertNull(dto.getCategoryId());
        assertNull(dto.getCoverImgBase64());
        assertNull(dto.getIsCompleted());
    }

    @Test
    @DisplayName("Should handle empty strings")
    void testEmptyStrings() {
        NovelCreateRequestDTO dto = new NovelCreateRequestDTO();

        dto.setTitle("");
        dto.setSynopsis("");
        dto.setCoverImgBase64("");

        assertEquals("", dto.getTitle());
        assertEquals("", dto.getSynopsis());
        assertEquals("", dto.getCoverImgBase64());
    }

    @Test
    @DisplayName("Should handle edge case values")
    void testEdgeCaseValues() {
        NovelCreateRequestDTO dto = new NovelCreateRequestDTO();

        // Test maximum length strings
        String longTitle = "A".repeat(255);
        String longSynopsis = "B".repeat(4000);
        
        dto.setTitle(longTitle);
        dto.setSynopsis(longSynopsis);
        
        assertEquals(longTitle, dto.getTitle());
        assertEquals(longSynopsis, dto.getSynopsis());

        // Test negative category ID
        dto.setCategoryId(-1);
        assertEquals(Integer.valueOf(-1), dto.getCategoryId());

        // Test zero category ID
        dto.setCategoryId(0);
        assertEquals(Integer.valueOf(0), dto.getCategoryId());
    }
}
