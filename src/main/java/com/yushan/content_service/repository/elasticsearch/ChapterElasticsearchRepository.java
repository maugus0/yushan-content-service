package com.yushan.content_service.repository.elasticsearch;

import com.yushan.content_service.entity.elasticsearch.ChapterDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Elasticsearch repository for chapters.
 * Provides search functionality for chapters using Elasticsearch.
 */
@Repository
public interface ChapterElasticsearchRepository extends ElasticsearchRepository<ChapterDocument, String> {

    /**
     * Search chapters by title or content
     */
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title^3\", \"content^1\"], \"type\": \"best_fields\", \"fuzziness\": \"AUTO\"}}")
    Page<ChapterDocument> searchByText(String query, Pageable pageable);

    /**
     * Search chapters by title
     */
    List<ChapterDocument> findByTitleContaining(String title);

    /**
     * Find chapters by novel ID
     */
    List<ChapterDocument> findByNovelId(Integer novelId);

    /**
     * Find chapters by novel ID with pagination
     */
    Page<ChapterDocument> findByNovelId(Integer novelId, Pageable pageable);

    /**
     * Find chapters by chapter number
     */
    List<ChapterDocument> findByChapterNumber(Integer chapterNumber);

    /**
     * Find chapters by novel ID and chapter number
     */
    ChapterDocument findByNovelIdAndChapterNumber(Integer novelId, Integer chapterNumber);

    /**
     * Find premium chapters
     */
    List<ChapterDocument> findByIsPremium(Boolean isPremium);

    /**
     * Find valid chapters
     */
    List<ChapterDocument> findByIsValid(Boolean isValid);

    /**
     * Find chapters by UUID
     */
    ChapterDocument findByUuid(String uuid);

    /**
     * Complex search with multiple criteria
     */
    @Query("{\"bool\": {\"must\": [{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title^3\", \"content^1\"], \"type\": \"best_fields\", \"fuzziness\": \"AUTO\"}}], \"filter\": [{\"term\": {\"novelId\": ?1}}, {\"term\": {\"isPremium\": ?2}}, {\"term\": {\"isValid\": ?3}}]}}")
    Page<ChapterDocument> searchWithFilters(String query, Integer novelId, Boolean isPremium, Boolean isValid, Pageable pageable);

    /**
     * Search for suggestions/autocomplete
     */
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title^2\"], \"type\": \"phrase_prefix\", \"max_expansions\": 10}}")
    List<ChapterDocument> searchSuggestions(String query, Pageable pageable);

    /**
     * Find popular chapters by view count
     */
    List<ChapterDocument> findByViewCntGreaterThanOrderByViewCntDesc(Long minViewCnt, Pageable pageable);

    /**
     * Find chapters by publish date range
     */
    List<ChapterDocument> findByPublishTimeBetween(java.util.Date startDate, java.util.Date endDate);

    /**
     * Find chapters by word count range
     */
    List<ChapterDocument> findByWordCntBetween(Integer minWordCnt, Integer maxWordCnt);

    /**
     * Find chapters by novel ID and premium status
     */
    List<ChapterDocument> findByNovelIdAndIsPremium(Integer novelId, Boolean isPremium);

    /**
     * Find chapters by novel ID and validity
     */
    List<ChapterDocument> findByNovelIdAndIsValid(Integer novelId, Boolean isValid);
}
