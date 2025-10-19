package com.yushan.content_service.repository.elasticsearch;

import com.yushan.content_service.entity.elasticsearch.NovelDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Elasticsearch repository for novels.
 * Provides search functionality for novels using Elasticsearch.
 */
@Repository
public interface NovelElasticsearchRepository extends ElasticsearchRepository<NovelDocument, String> {

    /**
     * Search novels by title, synopsis, or author name
     */
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title^3\", \"synopsis^2\", \"authorName^1\"], \"type\": \"best_fields\", \"fuzziness\": \"AUTO\"}}")
    Page<NovelDocument> searchByText(String query, Pageable pageable);

    /**
     * Search novels by title
     */
    List<NovelDocument> findByTitleContaining(String title);

    /**
     * Search novels by author name
     */
    List<NovelDocument> findByAuthorNameContaining(String authorName);

    /**
     * Find novels by category
     */
    List<NovelDocument> findByCategoryId(Integer categoryId);

    /**
     * Find novels by status
     */
    List<NovelDocument> findByStatus(String status);

    /**
     * Find completed novels
     */
    List<NovelDocument> findByIsCompleted(Boolean isCompleted);

    /**
     * Find novels by rating range
     */
    List<NovelDocument> findByAvgRatingBetween(Double minRating, Double maxRating);

    /**
     * Find novels by word count range
     */
    List<NovelDocument> findByWordCntBetween(Long minWordCnt, Long maxWordCnt);

    /**
     * Find novels by chapter count range
     */
    List<NovelDocument> findByChapterCntBetween(Integer minChapterCnt, Integer maxChapterCnt);

    /**
     * Find novels by author ID
     */
    List<NovelDocument> findByAuthorId(String authorId);

    /**
     * Find novels by UUID
     */
    NovelDocument findByUuid(String uuid);

    /**
     * Complex search with multiple criteria
     */
    @Query("{\"bool\": {\"must\": [{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title^3\", \"synopsis^2\", \"authorName^1\"], \"type\": \"best_fields\", \"fuzziness\": \"AUTO\"}}], \"filter\": [{\"term\": {\"status\": \"?1\"}}, {\"term\": {\"categoryId\": ?2}}, {\"range\": {\"avgRating\": {\"gte\": ?3}}}]}}")
    Page<NovelDocument> searchWithFilters(String query, String status, Integer categoryId, Double minRating, Pageable pageable);

    /**
     * Search for suggestions/autocomplete
     */
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title^2\", \"authorName^1\"], \"type\": \"phrase_prefix\", \"max_expansions\": 10}}")
    List<NovelDocument> searchSuggestions(String query, Pageable pageable);

    /**
     * Find popular novels by view count
     */
    List<NovelDocument> findByViewCntGreaterThanOrderByViewCntDesc(Long minViewCnt, Pageable pageable);

    /**
     * Find novels by publish date range
     */
    List<NovelDocument> findByPublishTimeBetween(java.util.Date startDate, java.util.Date endDate);

    /**
     * Find novels by create date range
     */
    List<NovelDocument> findByCreateTimeBetween(java.util.Date startDate, java.util.Date endDate);
}
