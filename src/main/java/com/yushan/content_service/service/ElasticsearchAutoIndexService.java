package com.yushan.content_service.service;

import com.yushan.content_service.entity.Novel;
import com.yushan.content_service.entity.Chapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Service for automatically indexing data to Elasticsearch.
 * Triggers indexing when novels or chapters are created, updated, or deleted.
 */
@Service
@ConditionalOnProperty(name = "search.elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class ElasticsearchAutoIndexService {

    @Autowired
    private ElasticsearchIndexService elasticsearchIndexService;

    /**
     * Index novel when created (only if published)
     */
    public void onNovelCreated(Novel novel) {
        // Only index if novel is published
        if (novel.getStatus() == 2) { // PUBLISHED status
            try {
                elasticsearchIndexService.indexNovel(novel.getId());
            } catch (Exception e) {
                // Log error but don't fail the operation
            }
        }
    }

    /**
     * Index novel when updated (only if published)
     */
    public void onNovelUpdated(Novel novel) {
        // Only index if novel is published
        if (novel.getStatus() == 2) { // PUBLISHED status
            try {
                elasticsearchIndexService.indexNovel(novel.getId());
            } catch (Exception e) {
                // Log error but don't fail the operation
            }
        } else {
            // If novel is not published, remove from Elasticsearch
            try {
                elasticsearchIndexService.removeNovel(novel.getId());
            } catch (Exception e) {
                // Log error but don't fail the operation
            }
        }
    }

    /**
     * Remove novel from Elasticsearch when deleted
     */
    public void onNovelDeleted(Integer novelId) {
        try {
            elasticsearchIndexService.removeNovel(novelId);
        } catch (Exception e) {
            // Log error but don't fail the operation
        }
    }

    /**
     * Index chapter when created (only if published)
     */
    public void onChapterCreated(Chapter chapter) {
        // Only index if chapter is published (is_valid = true and publish_time <= NOW())
        if (Boolean.TRUE.equals(chapter.getIsValid()) && 
            chapter.getPublishTime() != null && 
            chapter.getPublishTime().getTime() <= System.currentTimeMillis()) {
            try {
                elasticsearchIndexService.indexChapter(chapter.getId());
            } catch (Exception e) {
                // Log error but don't fail the operation
            }
        }
    }

    /**
     * Index chapter when updated (only if published)
     */
    public void onChapterUpdated(Chapter chapter) {
        // Only index if chapter is published (is_valid = true and publish_time <= NOW())
        if (Boolean.TRUE.equals(chapter.getIsValid()) && 
            chapter.getPublishTime() != null && 
            chapter.getPublishTime().getTime() <= System.currentTimeMillis()) {
            try {
                elasticsearchIndexService.indexChapter(chapter.getId());
            } catch (Exception e) {
                // Log error but don't fail the operation
            }
        } else {
            // If chapter is not published, remove from Elasticsearch
            try {
                elasticsearchIndexService.removeChapter(chapter.getId());
            } catch (Exception e) {
                // Log error but don't fail the operation
            }
        }
    }

    /**
     * Remove chapter from Elasticsearch when deleted
     */
    public void onChapterDeleted(Integer chapterId) {
        try {
            elasticsearchIndexService.removeChapter(chapterId);
        } catch (Exception e) {
            // Log error but don't fail the operation
        }
    }
}
