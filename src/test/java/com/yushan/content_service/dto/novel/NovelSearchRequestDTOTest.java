package com.yushan.content_service.dto.novel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;

/**
 * Unit tests for NovelSearchRequestDTO to improve code coverage.
 */
@DisplayName("NovelSearchRequestDTO Tests")
class NovelSearchRequestDTOTest {

    @Test
    @DisplayName("Default constructor should set default values")
    void testDefaultConstructor() {
        NovelSearchRequestDTO dto = new NovelSearchRequestDTO();
        
        assertEquals(0, dto.getPage());
        assertEquals(10, dto.getSize());
        assertEquals("createTime", dto.getSort());
        assertEquals("desc", dto.getOrder());
        assertNull(dto.getCategoryId());
        assertNull(dto.getStatus());
        assertNull(dto.getIsCompleted());
        assertNull(dto.getSearch());
        assertNull(dto.getAuthorName());
        assertNull(dto.getAuthorId());
    }

    @Test
    @DisplayName("Parameterized constructor should set all fields")
    void testParameterizedConstructor() {
        Integer page = 2;
        Integer size = 20;
        String sort = "title";
        String order = "asc";
        Integer categoryId = 1;
        String status = "PUBLISHED";
        Boolean isCompleted = true;
        String search = "fantasy";
        String authorName = "John Doe";
        String authorId = "author123";

        NovelSearchRequestDTO dto = new NovelSearchRequestDTO(
            page, size, sort, order, categoryId, status, isCompleted,
            search, authorName, authorId
        );

        assertEquals(page, dto.getPage());
        assertEquals(size, dto.getSize());
        assertEquals(sort, dto.getSort());
        assertEquals(order, dto.getOrder());
        assertEquals(categoryId, dto.getCategoryId());
        assertEquals(status, dto.getStatus());
        assertEquals(isCompleted, dto.getIsCompleted());
        assertEquals(search, dto.getSearch());
        assertEquals(authorName, dto.getAuthorName());
        assertEquals(authorId, dto.getAuthorId());
    }

    @Test
    @DisplayName("Parameterized constructor should handle null values with defaults")
    void testParameterizedConstructorWithNulls() {
        NovelSearchRequestDTO dto = new NovelSearchRequestDTO(
            null, null, null, null, null, null, null, null, null, null
        );

        assertEquals(0, dto.getPage());
        assertEquals(10, dto.getSize());
        assertEquals("createTime", dto.getSort());
        assertEquals("desc", dto.getOrder());
        assertNull(dto.getCategoryId());
        assertNull(dto.getStatus());
        assertNull(dto.getIsCompleted());
        assertNull(dto.getSearch());
        assertNull(dto.getAuthorName());
        assertNull(dto.getAuthorId());
    }

    @Test
    @DisplayName("Basic setters and getters should work correctly")
    void testBasicSettersAndGetters() {
        NovelSearchRequestDTO dto = new NovelSearchRequestDTO();

        // Test pagination
        dto.setPage(5);
        dto.setSize(25);
        assertEquals(5, dto.getPage());
        assertEquals(25, dto.getSize());

        // Test sorting
        dto.setSort("title");
        dto.setOrder("asc");
        assertEquals("title", dto.getSort());
        assertEquals("asc", dto.getOrder());

        // Test basic filters
        dto.setCategoryId(3);
        dto.setStatus("DRAFT");
        dto.setIsCompleted(false);
        dto.setSearch("romance");
        dto.setAuthorName("Jane Smith");
        dto.setAuthorId("author456");

        assertEquals(Integer.valueOf(3), dto.getCategoryId());
        assertEquals("DRAFT", dto.getStatus());
        assertEquals(false, dto.getIsCompleted());
        assertEquals("romance", dto.getSearch());
        assertEquals("Jane Smith", dto.getAuthorName());
        assertEquals("author456", dto.getAuthorId());
    }

    @Test
    @DisplayName("Advanced filtering setters and getters should work correctly")
    void testAdvancedFilteringSettersAndGetters() {
        NovelSearchRequestDTO dto = new NovelSearchRequestDTO();

        // Test rating filters
        dto.setMinRating(3.5f);
        dto.setMaxRating(5.0f);
        assertEquals(Float.valueOf(3.5f), dto.getMinRating());
        assertEquals(Float.valueOf(5.0f), dto.getMaxRating());

        // Test word count filters
        dto.setMinWordCount(10000L);
        dto.setMaxWordCount(100000L);
        assertEquals(Long.valueOf(10000L), dto.getMinWordCount());
        assertEquals(Long.valueOf(100000L), dto.getMaxWordCount());

        // Test chapter count filters
        dto.setMinChapterCount(5);
        dto.setMaxChapterCount(50);
        assertEquals(Integer.valueOf(5), dto.getMinChapterCount());
        assertEquals(Integer.valueOf(50), dto.getMaxChapterCount());

        // Test tags and genres
        String[] tags = {"action", "adventure", "fantasy"};
        String[] genres = {"fiction", "novel"};
        dto.setTags(tags);
        dto.setGenres(genres);
        
        assertArrayEquals(tags, dto.getTags());
        assertArrayEquals(genres, dto.getGenres());

        // Test other filters
        dto.setLanguage("English");
        dto.setContentRating("PG-13");
        dto.setIsPremium(true);
        assertEquals("English", dto.getLanguage());
        assertEquals("PG-13", dto.getContentRating());
        assertEquals(true, dto.getIsPremium());

        // Test date filters
        Date publishedAfter = new Date();
        Date publishedBefore = new Date(System.currentTimeMillis() + 86400000);
        dto.setPublishedAfter(publishedAfter);
        dto.setPublishedBefore(publishedBefore);
        
        assertNotNull(dto.getPublishedAfter());
        assertNotNull(dto.getPublishedBefore());
        assertEquals(publishedAfter.getTime(), dto.getPublishedAfter().getTime());
        assertEquals(publishedBefore.getTime(), dto.getPublishedBefore().getTime());
    }

    @Test
    @DisplayName("Array setters should create defensive copies")
    void testArraySettersCreateDefensiveCopies() {
        NovelSearchRequestDTO dto = new NovelSearchRequestDTO();
        
        String[] originalTags = {"tag1", "tag2"};
        String[] originalGenres = {"genre1", "genre2"};
        
        dto.setTags(originalTags);
        dto.setGenres(originalGenres);
        
        String[] retrievedTags = dto.getTags();
        String[] retrievedGenres = dto.getGenres();
        
        // Modify original arrays
        originalTags[0] = "modified";
        originalGenres[0] = "modified";
        
        // Retrieved arrays should not be affected
        assertEquals("tag1", retrievedTags[0]);
        assertEquals("genre1", retrievedGenres[0]);
    }

    @Test
    @DisplayName("Array setters should handle null values")
    void testArraySettersWithNullValues() {
        NovelSearchRequestDTO dto = new NovelSearchRequestDTO();
        
        dto.setTags(null);
        dto.setGenres(null);
        
        assertNull(dto.getTags());
        assertNull(dto.getGenres());
    }

    @Test
    @DisplayName("Date setters should create defensive copies")
    void testDateSettersCreateDefensiveCopies() {
        NovelSearchRequestDTO dto = new NovelSearchRequestDTO();
        
        Date originalDate = new Date();
        dto.setPublishedAfter(originalDate);
        dto.setPublishedBefore(originalDate);
        
        Date retrievedAfter = dto.getPublishedAfter();
        Date retrievedBefore = dto.getPublishedBefore();
        
        // Modify original date
        originalDate.setTime(originalDate.getTime() + 1000);
        
        // Retrieved dates should not be affected
        assertNotEquals(originalDate.getTime(), retrievedAfter.getTime());
        assertNotEquals(originalDate.getTime(), retrievedBefore.getTime());
    }

    @Test
    @DisplayName("Date setters should handle null values")
    void testDateSettersWithNullValues() {
        NovelSearchRequestDTO dto = new NovelSearchRequestDTO();
        
        dto.setPublishedAfter(null);
        dto.setPublishedBefore(null);
        
        assertNull(dto.getPublishedAfter());
        assertNull(dto.getPublishedBefore());
    }

    @Test
    @DisplayName("Helper methods should work correctly")
    void testHelperMethods() {
        NovelSearchRequestDTO dto = new NovelSearchRequestDTO();

        // Test category filter
        assertFalse(dto.hasCategoryFilter());
        dto.setCategoryId(1);
        assertTrue(dto.hasCategoryFilter());
        dto.setCategoryId(0);
        assertFalse(dto.hasCategoryFilter());

        // Test status filter
        assertFalse(dto.hasStatusFilter());
        dto.setStatus("PUBLISHED");
        assertTrue(dto.hasStatusFilter());
        dto.setStatus("");
        assertFalse(dto.hasStatusFilter());
        dto.setStatus("   ");
        assertFalse(dto.hasStatusFilter());

        // Test isCompleted filter
        assertFalse(dto.hasIsCompletedFilter());
        dto.setIsCompleted(true);
        assertTrue(dto.hasIsCompletedFilter());
        dto.setIsCompleted(false);
        assertTrue(dto.hasIsCompletedFilter());

        // Test search filter
        assertFalse(dto.hasSearchFilter());
        dto.setSearch("test");
        assertTrue(dto.hasSearchFilter());
        dto.setSearch("");
        assertFalse(dto.hasSearchFilter());

        // Test author filter
        assertFalse(dto.hasAuthorFilter());
        dto.setAuthorName("John");
        assertTrue(dto.hasAuthorFilter());
        dto.setAuthorName("");
        assertFalse(dto.hasAuthorFilter());

        // Test author ID filter
        assertFalse(dto.hasAuthorIdFilter());
        dto.setAuthorId("123");
        assertTrue(dto.hasAuthorIdFilter());
        dto.setAuthorId("");
        assertFalse(dto.hasAuthorIdFilter());

        // Test order methods
        dto.setOrder("asc");
        assertTrue(dto.isAscending());
        assertFalse(dto.isDescending());

        dto.setOrder("desc");
        assertFalse(dto.isAscending());
        assertTrue(dto.isDescending());

        dto.setOrder("ASC");
        assertTrue(dto.isAscending());
        assertFalse(dto.isDescending());

        dto.setOrder("DESC");
        assertFalse(dto.isAscending());
        assertTrue(dto.isDescending());
    }

    @Test
    @DisplayName("Advanced filtering helper methods should work correctly")
    void testAdvancedFilteringHelperMethods() {
        NovelSearchRequestDTO dto = new NovelSearchRequestDTO();

        // Test rating filter
        assertFalse(dto.hasRatingFilter());
        dto.setMinRating(3.0f);
        assertTrue(dto.hasRatingFilter());
        dto.setMinRating(null);
        dto.setMaxRating(5.0f);
        assertTrue(dto.hasRatingFilter());

        // Test word count filter
        assertFalse(dto.hasWordCountFilter());
        dto.setMinWordCount(1000L);
        assertTrue(dto.hasWordCountFilter());
        dto.setMinWordCount(null);
        dto.setMaxWordCount(50000L);
        assertTrue(dto.hasWordCountFilter());

        // Test chapter count filter
        assertFalse(dto.hasChapterCountFilter());
        dto.setMinChapterCount(1);
        assertTrue(dto.hasChapterCountFilter());
        dto.setMinChapterCount(null);
        dto.setMaxChapterCount(100);
        assertTrue(dto.hasChapterCountFilter());

        // Test tags filter
        assertFalse(dto.hasTagsFilter());
        dto.setTags(new String[]{"tag1", "tag2"});
        assertTrue(dto.hasTagsFilter());
        dto.setTags(new String[]{});
        assertFalse(dto.hasTagsFilter());

        // Test genres filter
        assertFalse(dto.hasGenresFilter());
        dto.setGenres(new String[]{"genre1"});
        assertTrue(dto.hasGenresFilter());
        dto.setGenres(new String[]{});
        assertFalse(dto.hasGenresFilter());

        // Test language filter
        assertFalse(dto.hasLanguageFilter());
        dto.setLanguage("English");
        assertTrue(dto.hasLanguageFilter());
        dto.setLanguage("");
        assertFalse(dto.hasLanguageFilter());

        // Test content rating filter
        assertFalse(dto.hasContentRatingFilter());
        dto.setContentRating("PG");
        assertTrue(dto.hasContentRatingFilter());
        dto.setContentRating("");
        assertFalse(dto.hasContentRatingFilter());

        // Test premium filter
        assertFalse(dto.hasPremiumFilter());
        dto.setIsPremium(true);
        assertTrue(dto.hasPremiumFilter());
        dto.setIsPremium(false);
        assertTrue(dto.hasPremiumFilter());

        // Test date range filter
        assertFalse(dto.hasDateRangeFilter());
        dto.setPublishedAfter(new Date());
        assertTrue(dto.hasDateRangeFilter());
        dto.setPublishedAfter(null);
        dto.setPublishedBefore(new Date());
        assertTrue(dto.hasDateRangeFilter());
    }

    @Test
    @DisplayName("Should handle edge case values")
    void testEdgeCaseValues() {
        NovelSearchRequestDTO dto = new NovelSearchRequestDTO();

        // Test negative values
        dto.setPage(-1);
        dto.setSize(-5);
        dto.setCategoryId(-1);
        dto.setMinRating(-1.0f);
        dto.setMaxRating(-1.0f);
        dto.setMinWordCount(-1L);
        dto.setMaxWordCount(-1L);
        dto.setMinChapterCount(-1);
        dto.setMaxChapterCount(-1);

        assertEquals(-1, dto.getPage());
        assertEquals(-5, dto.getSize());
        assertEquals(Integer.valueOf(-1), dto.getCategoryId());
        assertEquals(Float.valueOf(-1.0f), dto.getMinRating());
        assertEquals(Float.valueOf(-1.0f), dto.getMaxRating());
        assertEquals(Long.valueOf(-1L), dto.getMinWordCount());
        assertEquals(Long.valueOf(-1L), dto.getMaxWordCount());
        assertEquals(Integer.valueOf(-1), dto.getMinChapterCount());
        assertEquals(Integer.valueOf(-1), dto.getMaxChapterCount());

        // Test zero values
        dto.setPage(0);
        dto.setSize(0);
        dto.setCategoryId(0);
        dto.setMinRating(0.0f);
        dto.setMaxRating(0.0f);
        dto.setMinWordCount(0L);
        dto.setMaxWordCount(0L);
        dto.setMinChapterCount(0);
        dto.setMaxChapterCount(0);

        assertEquals(0, dto.getPage());
        assertEquals(0, dto.getSize());
        assertEquals(Integer.valueOf(0), dto.getCategoryId());
        assertEquals(Float.valueOf(0.0f), dto.getMinRating());
        assertEquals(Float.valueOf(0.0f), dto.getMaxRating());
        assertEquals(Long.valueOf(0L), dto.getMinWordCount());
        assertEquals(Long.valueOf(0L), dto.getMaxWordCount());
        assertEquals(Integer.valueOf(0), dto.getMinChapterCount());
        assertEquals(Integer.valueOf(0), dto.getMaxChapterCount());

        // Test maximum values
        dto.setPage(Integer.MAX_VALUE);
        dto.setSize(Integer.MAX_VALUE);
        dto.setCategoryId(Integer.MAX_VALUE);
        dto.setMinRating(Float.MAX_VALUE);
        dto.setMaxRating(Float.MAX_VALUE);
        dto.setMinWordCount(Long.MAX_VALUE);
        dto.setMaxWordCount(Long.MAX_VALUE);
        dto.setMinChapterCount(Integer.MAX_VALUE);
        dto.setMaxChapterCount(Integer.MAX_VALUE);

        assertEquals(Integer.MAX_VALUE, dto.getPage());
        assertEquals(Integer.MAX_VALUE, dto.getSize());
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), dto.getCategoryId());
        assertEquals(Float.valueOf(Float.MAX_VALUE), dto.getMinRating());
        assertEquals(Float.valueOf(Float.MAX_VALUE), dto.getMaxRating());
        assertEquals(Long.valueOf(Long.MAX_VALUE), dto.getMinWordCount());
        assertEquals(Long.valueOf(Long.MAX_VALUE), dto.getMaxWordCount());
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), dto.getMinChapterCount());
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), dto.getMaxChapterCount());
    }

    @Test
    @DisplayName("Should handle null values in setters")
    void testNullValuesInSetters() {
        NovelSearchRequestDTO dto = new NovelSearchRequestDTO();

        dto.setPage(null);
        dto.setSize(null);
        dto.setSort(null);
        dto.setOrder(null);
        dto.setCategoryId(null);
        dto.setStatus(null);
        dto.setIsCompleted(null);
        dto.setSearch(null);
        dto.setAuthorName(null);
        dto.setAuthorId(null);
        dto.setMinRating(null);
        dto.setMaxRating(null);
        dto.setMinWordCount(null);
        dto.setMaxWordCount(null);
        dto.setMinChapterCount(null);
        dto.setMaxChapterCount(null);
        dto.setLanguage(null);
        dto.setContentRating(null);
        dto.setIsPremium(null);

        assertNull(dto.getPage());
        assertNull(dto.getSize());
        assertNull(dto.getSort());
        assertNull(dto.getOrder());
        assertNull(dto.getCategoryId());
        assertNull(dto.getStatus());
        assertNull(dto.getIsCompleted());
        assertNull(dto.getSearch());
        assertNull(dto.getAuthorName());
        assertNull(dto.getAuthorId());
        assertNull(dto.getMinRating());
        assertNull(dto.getMaxRating());
        assertNull(dto.getMinWordCount());
        assertNull(dto.getMaxWordCount());
        assertNull(dto.getMinChapterCount());
        assertNull(dto.getMaxChapterCount());
        assertNull(dto.getLanguage());
        assertNull(dto.getContentRating());
        assertNull(dto.getIsPremium());
    }
}
