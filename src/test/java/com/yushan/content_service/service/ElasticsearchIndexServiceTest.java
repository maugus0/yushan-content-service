package com.yushan.content_service.service;

import com.yushan.content_service.dao.ChapterMapper;
import com.yushan.content_service.dao.NovelMapper;
import com.yushan.content_service.dto.novel.NovelSearchRequestDTO;
import com.yushan.content_service.entity.Chapter;
import com.yushan.content_service.entity.Novel;
import com.yushan.content_service.entity.elasticsearch.ChapterDocument;
import com.yushan.content_service.entity.elasticsearch.NovelDocument;
import com.yushan.content_service.repository.elasticsearch.ChapterElasticsearchRepository;
import com.yushan.content_service.repository.elasticsearch.NovelElasticsearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElasticsearchIndexServiceTest {

    @InjectMocks
    private ElasticsearchIndexService elasticsearchIndexService;

    @Mock
    private NovelElasticsearchRepository novelElasticsearchRepository;

    @Mock
    private ChapterElasticsearchRepository chapterElasticsearchRepository;

    @Mock
    private NovelMapper novelMapper;

    @Mock
    private ChapterMapper chapterMapper;

    private Novel testNovel;
    private Chapter testChapter;

    @BeforeEach
    void setUp() {

        testNovel = new Novel();
        testNovel.setId(1);
        testNovel.setUuid(UUID.randomUUID());
        testNovel.setTitle("Test Novel");
        testNovel.setSynopsis("Test Synopsis");
        testNovel.setAuthorId(UUID.randomUUID());
        testNovel.setAuthorName("Test Author");
        testNovel.setCategoryId(1);
        testNovel.setStatus(1); // PUBLISHED
        testNovel.setIsCompleted(false);
        testNovel.setChapterCnt(10);
        testNovel.setWordCnt(50000L);
        testNovel.setAvgRating(4.5f);
        testNovel.setReviewCnt(100);
        testNovel.setViewCnt(1000L);
        testNovel.setVoteCnt(50);
        testNovel.setYuanCnt(10.0f);
        testNovel.setCreateTime(new Date());
        testNovel.setUpdateTime(new Date());
        testNovel.setPublishTime(new Date());
        testNovel.setCoverImgUrl("http://example.com/cover.jpg");

        testChapter = new Chapter();
        testChapter.setId(1);
        testChapter.setUuid(UUID.randomUUID());
        testChapter.setNovelId(1);
        testChapter.setChapterNumber(1);
        testChapter.setTitle("Test Chapter");
        testChapter.setContent("Test content");
        testChapter.setWordCnt(1000);
        testChapter.setIsPremium(false);
        testChapter.setYuanCost(0.0f);
        testChapter.setViewCnt(500L);
        testChapter.setIsValid(true);
        testChapter.setCreateTime(new Date());
        testChapter.setUpdateTime(new Date());
        testChapter.setPublishTime(new Date());
    }

    @Test
    void indexAllNovels_ShouldIndexAllPublishedNovels() {
        // Arrange
        List<Novel> novels = Arrays.asList(testNovel);
        when(novelMapper.selectNovelsWithPagination(any(NovelSearchRequestDTO.class))).thenReturn(novels);

        // Act
        elasticsearchIndexService.indexAllNovels();

        // Assert
        verify(novelMapper).selectNovelsWithPagination(any(NovelSearchRequestDTO.class));
        verify(novelElasticsearchRepository).saveAll(anyList());
    }

    @Test
    void indexAllChapters_ShouldIndexAllPublishedChapters() {
        // Arrange
        List<Chapter> chapters = Arrays.asList(testChapter);
        when(chapterMapper.selectPublishedChapters()).thenReturn(chapters);

        // Act
        elasticsearchIndexService.indexAllChapters();

        // Assert
        verify(chapterMapper).selectPublishedChapters();
        verify(chapterElasticsearchRepository).saveAll(anyList());
    }

    @Test
    void indexNovel_WithValidNovelId_ShouldIndexNovel() {
        // Arrange
        Integer novelId = 1;
        when(novelMapper.selectByPrimaryKey(novelId)).thenReturn(testNovel);

        // Act
        elasticsearchIndexService.indexNovel(novelId);

        // Assert
        verify(novelMapper).selectByPrimaryKey(novelId);
        verify(novelElasticsearchRepository).save(any(NovelDocument.class));
    }

    @Test
    void indexNovel_WithInvalidNovelId_ShouldNotIndex() {
        // Arrange
        Integer novelId = 999;
        when(novelMapper.selectByPrimaryKey(novelId)).thenReturn(null);

        // Act
        elasticsearchIndexService.indexNovel(novelId);

        // Assert
        verify(novelMapper).selectByPrimaryKey(novelId);
        verify(novelElasticsearchRepository, never()).save(any(NovelDocument.class));
    }

    @Test
    void indexChapter_WithValidChapterId_ShouldIndexChapter() {
        // Arrange
        Integer chapterId = 1;
        when(chapterMapper.selectByPrimaryKey(chapterId)).thenReturn(testChapter);

        // Act
        elasticsearchIndexService.indexChapter(chapterId);

        // Assert
        verify(chapterMapper).selectByPrimaryKey(chapterId);
        verify(chapterElasticsearchRepository).save(any(ChapterDocument.class));
    }

    @Test
    void indexChapter_WithInvalidChapterId_ShouldNotIndex() {
        // Arrange
        Integer chapterId = 999;
        when(chapterMapper.selectByPrimaryKey(chapterId)).thenReturn(null);

        // Act
        elasticsearchIndexService.indexChapter(chapterId);

        // Assert
        verify(chapterMapper).selectByPrimaryKey(chapterId);
        verify(chapterElasticsearchRepository, never()).save(any(ChapterDocument.class));
    }

    @Test
    void removeNovel_ShouldDeleteNovelFromElasticsearch() {
        // Arrange
        Integer novelId = 1;

        // Act
        elasticsearchIndexService.removeNovel(novelId);

        // Assert
        verify(novelElasticsearchRepository).deleteById(eq("1"));
    }

    @Test
    void removeChapter_ShouldDeleteChapterFromElasticsearch() {
        // Arrange
        Integer chapterId = 1;

        // Act
        elasticsearchIndexService.removeChapter(chapterId);

        // Assert
        verify(chapterElasticsearchRepository).deleteById(eq("1"));
    }

    @Test
    void clearAllData_ShouldClearAllDataFromElasticsearch() {
        // Act
        elasticsearchIndexService.clearAllData();

        // Assert
        verify(novelElasticsearchRepository).deleteAll();
        verify(chapterElasticsearchRepository).deleteAll();
    }

    @Test
    void reindexAllData_ShouldClearAndReindexAllData() {
        // Arrange
        List<Novel> novels = Arrays.asList(testNovel);
        List<Chapter> chapters = Arrays.asList(testChapter);
        when(novelMapper.selectNovelsWithPagination(any(NovelSearchRequestDTO.class))).thenReturn(novels);
        when(chapterMapper.selectPublishedChapters()).thenReturn(chapters);

        // Act
        elasticsearchIndexService.reindexAllData();

        // Assert
        verify(novelElasticsearchRepository).deleteAll();
        verify(chapterElasticsearchRepository).deleteAll();
        verify(novelMapper).selectNovelsWithPagination(any(NovelSearchRequestDTO.class));
        verify(chapterMapper).selectPublishedChapters();
        verify(novelElasticsearchRepository).saveAll(anyList());
        verify(chapterElasticsearchRepository).saveAll(anyList());
    }
}
