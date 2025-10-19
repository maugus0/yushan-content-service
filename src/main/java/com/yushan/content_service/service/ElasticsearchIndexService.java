package com.yushan.content_service.service;

import com.yushan.content_service.entity.Novel;
import com.yushan.content_service.entity.Chapter;
import com.yushan.content_service.entity.elasticsearch.NovelDocument;
import com.yushan.content_service.entity.elasticsearch.ChapterDocument;
import com.yushan.content_service.dto.novel.NovelSearchRequestDTO;
import com.yushan.content_service.repository.elasticsearch.NovelElasticsearchRepository;
import com.yushan.content_service.repository.elasticsearch.ChapterElasticsearchRepository;
import com.yushan.content_service.dao.NovelMapper;
import com.yushan.content_service.dao.ChapterMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for indexing data to Elasticsearch.
 * Handles synchronization between database and Elasticsearch.
 */
@Service
@ConditionalOnProperty(name = "search.elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class ElasticsearchIndexService {

    @Autowired
    private NovelElasticsearchRepository novelElasticsearchRepository;

    @Autowired
    private ChapterElasticsearchRepository chapterElasticsearchRepository;

    @Autowired
    private NovelMapper novelMapper;

    @Autowired
    private ChapterMapper chapterMapper;

    /**
     * Index all published novels to Elasticsearch
     */
    @Transactional(readOnly = true)
    public void indexAllNovels() {
        // Get only published novels from database
        NovelSearchRequestDTO request = new NovelSearchRequestDTO();
        request.setPage(0);
        request.setSize(10000); // Large number to get all
        request.setStatus("PUBLISHED"); // Only published novels
        List<Novel> novels = novelMapper.selectNovelsWithPagination(request);
        
        // Convert to Elasticsearch documents
        List<NovelDocument> documents = novels.stream()
                .map(this::convertToNovelDocument)
                .collect(Collectors.toList());
        
        // Save to Elasticsearch
        novelElasticsearchRepository.saveAll(documents);
    }

    /**
     * Index all published chapters to Elasticsearch
     */
    @Transactional(readOnly = true)
    public void indexAllChapters() {
        // Get all published chapters from database
        List<Chapter> chapters = chapterMapper.selectPublishedChapters();
        
        // Convert to Elasticsearch documents
        List<ChapterDocument> documents = chapters.stream()
                .map(this::convertToChapterDocument)
                .collect(Collectors.toList());
        
        // Save to Elasticsearch
        chapterElasticsearchRepository.saveAll(documents);
    }

    /**
     * Index a specific novel to Elasticsearch
     */
    public void indexNovel(Integer novelId) {
        Novel novel = novelMapper.selectByPrimaryKey(novelId);
        if (novel != null) {
            NovelDocument document = convertToNovelDocument(novel);
            novelElasticsearchRepository.save(document);
        }
    }

    /**
     * Index a specific chapter to Elasticsearch
     */
    public void indexChapter(Integer chapterId) {
        Chapter chapter = chapterMapper.selectByPrimaryKey(chapterId);
        if (chapter != null) {
            ChapterDocument document = convertToChapterDocument(chapter);
            chapterElasticsearchRepository.save(document);
        }
    }

    /**
     * Remove a novel from Elasticsearch
     */
    public void removeNovel(Integer novelId) {
        novelElasticsearchRepository.deleteById(novelId.toString());
    }

    /**
     * Remove a chapter from Elasticsearch
     */
    public void removeChapter(Integer chapterId) {
        chapterElasticsearchRepository.deleteById(chapterId.toString());
    }

    /**
     * Clear all data from Elasticsearch
     */
    public void clearAllData() {
        novelElasticsearchRepository.deleteAll();
        chapterElasticsearchRepository.deleteAll();
    }

    /**
     * Reindex all data (clear + index)
     */
    public void reindexAllData() {
        clearAllData();
        indexAllNovels();
        indexAllChapters();
    }

    /**
     * Convert Novel entity to NovelDocument
     */
    private NovelDocument convertToNovelDocument(Novel novel) {
        return new NovelDocument(
            novel.getId().toString(),
            novel.getUuid().toString(),
            novel.getTitle(),
            novel.getSynopsis(),
            novel.getAuthorId().toString(),
            novel.getAuthorName(),
            novel.getCategoryId(),
            novel.getStatus().toString(),
            novel.getIsCompleted(),
            novel.getChapterCnt(),
            novel.getWordCnt(),
            novel.getAvgRating() != null ? novel.getAvgRating().doubleValue() : null,
            novel.getReviewCnt(),
            novel.getViewCnt(),
            novel.getVoteCnt(),
            novel.getYuanCnt() != null ? novel.getYuanCnt().doubleValue() : null,
            novel.getCreateTime(),
            novel.getUpdateTime(),
            novel.getPublishTime(),
            novel.getCoverImgUrl()
        );
    }

    /**
     * Convert Chapter entity to ChapterDocument
     */
    private ChapterDocument convertToChapterDocument(Chapter chapter) {
        return new ChapterDocument(
            chapter.getId().toString(),
            chapter.getUuid().toString(),
            chapter.getNovelId(),
            chapter.getChapterNumber(),
            chapter.getTitle(),
            chapter.getContent(),
            chapter.getWordCnt(),
            chapter.getIsPremium(),
            chapter.getYuanCost() != null ? chapter.getYuanCost().doubleValue() : null,
            chapter.getViewCnt(),
            chapter.getIsValid(),
            chapter.getCreateTime(),
            chapter.getUpdateTime(),
            chapter.getPublishTime()
        );
    }
}
