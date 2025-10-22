package com.yushan.content_service.dto.search;

import com.yushan.content_service.dto.common.PageResponseDTO;
import com.yushan.content_service.dto.novel.NovelDetailResponseDTO;
import com.yushan.content_service.dto.chapter.ChapterSummaryDTO;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CombinedSearchResponseDTO.
 */
class CombinedSearchResponseDTOTest {

    @Test
    void testDefaultConstructor() {
        // When
        CombinedSearchResponseDTO response = new CombinedSearchResponseDTO();

        // Then
        assertNull(response.getNovels());
        assertNull(response.getChapters());
        assertEquals(0, response.getTotalResults());
        assertNull(response.getSearchQuery());
        assertEquals(0, response.getSearchTimeMs());
    }

    @Test
    void testParameterizedConstructor() {
        // Given
        List<NovelDetailResponseDTO> novelList = Arrays.asList(new NovelDetailResponseDTO());
        PageResponseDTO<NovelDetailResponseDTO> novels = new PageResponseDTO<>(novelList, 1L, 0, 10);
        
        List<ChapterSummaryDTO> chapterList = Arrays.asList(new ChapterSummaryDTO());
        PageResponseDTO<ChapterSummaryDTO> chapters = new PageResponseDTO<>(chapterList, 1L, 0, 10);
        
        long totalResults = 2L;
        String searchQuery = "test query";
        long searchTimeMs = 150L;

        // When
        CombinedSearchResponseDTO response = new CombinedSearchResponseDTO(
            novels, chapters, totalResults, searchQuery, searchTimeMs);

        // Then
        assertNotNull(response.getNovels());
        assertNotNull(response.getChapters());
        assertEquals(totalResults, response.getTotalResults());
        assertEquals(searchQuery, response.getSearchQuery());
        assertEquals(searchTimeMs, response.getSearchTimeMs());
    }

    @Test
    void testParameterizedConstructorWithNullValues() {
        // When
        CombinedSearchResponseDTO response = new CombinedSearchResponseDTO(
            null, null, 0L, null, 0L);

        // Then
        assertNull(response.getNovels());
        assertNull(response.getChapters());
        assertEquals(0, response.getTotalResults());
        assertNull(response.getSearchQuery());
        assertEquals(0, response.getSearchTimeMs());
    }

    @Test
    void testSetAndGetNovels() {
        // Given
        CombinedSearchResponseDTO response = new CombinedSearchResponseDTO();
        List<NovelDetailResponseDTO> novelList = Arrays.asList(new NovelDetailResponseDTO());
        PageResponseDTO<NovelDetailResponseDTO> novels = new PageResponseDTO<>(novelList, 1L, 0, 10);

        // When
        response.setNovels(novels);

        // Then
        assertNotNull(response.getNovels());
        assertEquals(novels.getContent(), response.getNovels().getContent());
        assertEquals(novels.getTotalElements(), response.getNovels().getTotalElements());
    }

    @Test
    void testSetAndGetNovelsWithNull() {
        // Given
        CombinedSearchResponseDTO response = new CombinedSearchResponseDTO();

        // When
        response.setNovels(null);

        // Then
        assertNull(response.getNovels());
    }

    @Test
    void testSetAndGetChapters() {
        // Given
        CombinedSearchResponseDTO response = new CombinedSearchResponseDTO();
        List<ChapterSummaryDTO> chapterList = Arrays.asList(new ChapterSummaryDTO());
        PageResponseDTO<ChapterSummaryDTO> chapters = new PageResponseDTO<>(chapterList, 1L, 0, 10);

        // When
        response.setChapters(chapters);

        // Then
        assertNotNull(response.getChapters());
        assertEquals(chapters.getContent(), response.getChapters().getContent());
        assertEquals(chapters.getTotalElements(), response.getChapters().getTotalElements());
    }

    @Test
    void testSetAndGetChaptersWithNull() {
        // Given
        CombinedSearchResponseDTO response = new CombinedSearchResponseDTO();

        // When
        response.setChapters(null);

        // Then
        assertNull(response.getChapters());
    }

    @Test
    void testSetAndGetTotalResults() {
        // Given
        CombinedSearchResponseDTO response = new CombinedSearchResponseDTO();
        long totalResults = 100L;

        // When
        response.setTotalResults(totalResults);

        // Then
        assertEquals(totalResults, response.getTotalResults());
    }

    @Test
    void testSetAndGetSearchQuery() {
        // Given
        CombinedSearchResponseDTO response = new CombinedSearchResponseDTO();
        String searchQuery = "fantasy novel";

        // When
        response.setSearchQuery(searchQuery);

        // Then
        assertEquals(searchQuery, response.getSearchQuery());
    }

    @Test
    void testSetAndGetSearchQueryWithNull() {
        // Given
        CombinedSearchResponseDTO response = new CombinedSearchResponseDTO();

        // When
        response.setSearchQuery(null);

        // Then
        assertNull(response.getSearchQuery());
    }

    @Test
    void testSetAndGetSearchTimeMs() {
        // Given
        CombinedSearchResponseDTO response = new CombinedSearchResponseDTO();
        long searchTimeMs = 250L;

        // When
        response.setSearchTimeMs(searchTimeMs);

        // Then
        assertEquals(searchTimeMs, response.getSearchTimeMs());
    }

    @Test
    void testNegativeValues() {
        // Given
        CombinedSearchResponseDTO response = new CombinedSearchResponseDTO();

        // When
        response.setTotalResults(-1L);
        response.setSearchTimeMs(-100L);

        // Then
        assertEquals(-1L, response.getTotalResults());
        assertEquals(-100L, response.getSearchTimeMs());
    }

    @Test
    void testLargeValues() {
        // Given
        CombinedSearchResponseDTO response = new CombinedSearchResponseDTO();
        long largeTotalResults = Long.MAX_VALUE;
        long largeSearchTimeMs = Long.MAX_VALUE;

        // When
        response.setTotalResults(largeTotalResults);
        response.setSearchTimeMs(largeSearchTimeMs);

        // Then
        assertEquals(largeTotalResults, response.getTotalResults());
        assertEquals(largeSearchTimeMs, response.getSearchTimeMs());
    }

    @Test
    void testEmptyStringSearchQuery() {
        // Given
        CombinedSearchResponseDTO response = new CombinedSearchResponseDTO();

        // When
        response.setSearchQuery("");

        // Then
        assertEquals("", response.getSearchQuery());
    }
}
