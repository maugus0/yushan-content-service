package com.yushan.content_service.service;

import com.yushan.content_service.entity.Novel;
import com.yushan.content_service.entity.Chapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * Unit tests for ElasticsearchAutoIndexService.
 */
@ExtendWith(MockitoExtension.class)
class ElasticsearchAutoIndexServiceTest {

    @Mock
    private ElasticsearchIndexService elasticsearchIndexService;

    @InjectMocks
    private ElasticsearchAutoIndexService elasticsearchAutoIndexService;

    private Novel testNovel;
    private Chapter testChapter;

    @BeforeEach
    void setUp() {
        testNovel = new Novel();
        testNovel.setId(1);
        testNovel.setStatus(2); // PUBLISHED status

        testChapter = new Chapter();
        testChapter.setId(1);
        testChapter.setNovelId(1);
        testChapter.setIsValid(true);
        testChapter.setPublishTime(new java.util.Date(System.currentTimeMillis() - 1000)); // Published in the past
    }

    @Test
    void onNovelCreated_WithPublishedNovel_ShouldIndexNovel() {
        // Given
        testNovel.setStatus(2); // PUBLISHED

        // When
        elasticsearchAutoIndexService.onNovelCreated(testNovel);

        // Then
        verify(elasticsearchIndexService).indexNovel(1);
    }

    @Test
    void onNovelCreated_WithDraftNovel_ShouldNotIndexNovel() {
        // Given
        testNovel.setStatus(0); // DRAFT

        // When
        elasticsearchAutoIndexService.onNovelCreated(testNovel);

        // Then
        verify(elasticsearchIndexService, never()).indexNovel(anyInt());
    }

    @Test
    void onNovelUpdated_WithPublishedNovel_ShouldIndexNovel() {
        // Given
        testNovel.setStatus(2); // PUBLISHED

        // When
        elasticsearchAutoIndexService.onNovelUpdated(testNovel);

        // Then
        verify(elasticsearchIndexService).indexNovel(1);
    }

    @Test
    void onNovelUpdated_WithDraftNovel_ShouldRemoveNovel() {
        // Given
        testNovel.setStatus(0); // DRAFT

        // When
        elasticsearchAutoIndexService.onNovelUpdated(testNovel);

        // Then
        verify(elasticsearchIndexService).removeNovel(1);
        verify(elasticsearchIndexService, never()).indexNovel(anyInt());
    }

    @Test
    void onNovelDeleted_ShouldRemoveNovel() {
        // When
        elasticsearchAutoIndexService.onNovelDeleted(1);

        // Then
        verify(elasticsearchIndexService).removeNovel(1);
    }

    @Test
    void onChapterCreated_WithValidChapter_ShouldIndexChapter() {
        // Given
        testChapter.setIsValid(true);

        // When
        elasticsearchAutoIndexService.onChapterCreated(testChapter);

        // Then
        verify(elasticsearchIndexService).indexChapter(1);
    }

    @Test
    void onChapterCreated_WithInvalidChapter_ShouldNotIndexChapter() {
        // Given
        testChapter.setIsValid(false);

        // When
        elasticsearchAutoIndexService.onChapterCreated(testChapter);

        // Then
        verify(elasticsearchIndexService, never()).indexChapter(anyInt());
    }

    @Test
    void onChapterCreated_WithFuturePublishTime_ShouldNotIndexChapter() {
        // Given
        testChapter.setIsValid(true);
        testChapter.setPublishTime(new java.util.Date(System.currentTimeMillis() + 10000)); // Future publish time

        // When
        elasticsearchAutoIndexService.onChapterCreated(testChapter);

        // Then
        verify(elasticsearchIndexService, never()).indexChapter(anyInt());
    }

    @Test
    void onChapterCreated_WithNullPublishTime_ShouldNotIndexChapter() {
        // Given
        testChapter.setIsValid(true);
        testChapter.setPublishTime(null);

        // When
        elasticsearchAutoIndexService.onChapterCreated(testChapter);

        // Then
        verify(elasticsearchIndexService, never()).indexChapter(anyInt());
    }

    @Test
    void onChapterUpdated_WithValidChapter_ShouldIndexChapter() {
        // Given
        testChapter.setIsValid(true);

        // When
        elasticsearchAutoIndexService.onChapterUpdated(testChapter);

        // Then
        verify(elasticsearchIndexService).indexChapter(1);
    }

    @Test
    void onChapterUpdated_WithInvalidChapter_ShouldRemoveChapter() {
        // Given
        testChapter.setIsValid(false);

        // When
        elasticsearchAutoIndexService.onChapterUpdated(testChapter);

        // Then
        verify(elasticsearchIndexService).removeChapter(1);
        verify(elasticsearchIndexService, never()).indexChapter(anyInt());
    }

    @Test
    void onChapterUpdated_WithFuturePublishTime_ShouldRemoveChapter() {
        // Given
        testChapter.setIsValid(true);
        testChapter.setPublishTime(new java.util.Date(System.currentTimeMillis() + 10000)); // Future publish time

        // When
        elasticsearchAutoIndexService.onChapterUpdated(testChapter);

        // Then
        verify(elasticsearchIndexService).removeChapter(1);
        verify(elasticsearchIndexService, never()).indexChapter(anyInt());
    }

    @Test
    void onChapterUpdated_WithNullPublishTime_ShouldRemoveChapter() {
        // Given
        testChapter.setIsValid(true);
        testChapter.setPublishTime(null);

        // When
        elasticsearchAutoIndexService.onChapterUpdated(testChapter);

        // Then
        verify(elasticsearchIndexService).removeChapter(1);
        verify(elasticsearchIndexService, never()).indexChapter(anyInt());
    }

    @Test
    void onChapterDeleted_ShouldRemoveChapter() {
        // When
        elasticsearchAutoIndexService.onChapterDeleted(1);

        // Then
        verify(elasticsearchIndexService).removeChapter(1);
    }
}
