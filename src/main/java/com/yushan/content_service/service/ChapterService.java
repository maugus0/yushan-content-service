package com.yushan.content_service.service;

import com.yushan.content_service.dao.ChapterMapper;
import com.yushan.content_service.dto.chapter.*;
import com.yushan.content_service.dto.common.PageResponseDTO;
import com.yushan.content_service.entity.Chapter;
import com.yushan.content_service.entity.Novel;
import com.yushan.content_service.enums.NovelStatus;
import com.yushan.content_service.exception.ResourceNotFoundException;
import com.yushan.content_service.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChapterService {

    @Autowired
    private ChapterMapper chapterMapper;

    @Autowired
    private NovelService novelService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private KafkaEventProducerService kafkaEventProducerService;

    @Autowired(required = false)
    private ElasticsearchAutoIndexService elasticsearchAutoIndexService;


    @Transactional
    public ChapterDetailResponseDTO createChapter(UUID userId, ChapterCreateRequestDTO req) {
        // Validate novel exists and user is the author
        Novel novel = novelService.getNovelEntity(req.getNovelId());
        if (novel == null || novel.getStatus().equals(NovelStatus.ARCHIVED.getValue())) {
            throw new ResourceNotFoundException("novel not found");
        }

        if (!novel.getAuthorId().equals(userId)) {
            throw new IllegalArgumentException("only the author can create chapters");
        }

        // Validate chapter number doesn't exist
        if (chapterMapper.existsByNovelIdAndChapterNumber(req.getNovelId(), req.getChapterNumber())) {
            throw new IllegalArgumentException("chapter number already exists for this novel");
        }

        // Calculate word count if not provided
        Integer wordCnt = req.getWordCnt();
        if (wordCnt == null && req.getContent() != null && !req.getContent().trim().isEmpty()) {
            wordCnt = req.getContent().trim().length();
        }

        Date now = new Date();

        // Create chapter entity
        Chapter chapter = new Chapter();
        chapter.setUuid(UUID.randomUUID());
        chapter.setNovelId(req.getNovelId());
        chapter.setChapterNumber(req.getChapterNumber());
        chapter.setTitle(req.getTitle());
        chapter.setContent(req.getContent());
        chapter.setWordCnt(wordCnt);
        chapter.setIsPremium(req.getIsPremium() != null ? req.getIsPremium() : false);
        chapter.setYuanCost(req.getYuanCost() != null ? req.getYuanCost() : 0.0f);
        chapter.setViewCnt(0L);
        chapter.setIsValid(req.getIsValid() != null ? req.getIsValid() : true);
        chapter.setCreateTime(now);
        chapter.setUpdateTime(now);
        chapter.setPublishTime(req.getPublishTime() != null ? req.getPublishTime() : now);

        chapterMapper.insertSelective(chapter);

        // Update novel's chapter count and word count
        updateNovelStatistics(req.getNovelId());

        // Invalidate chapter caches for this novel
        redisUtil.invalidateChapterCaches(req.getNovelId());

        // Publish chapter created event
        try {
            kafkaEventProducerService.publishChapterCreatedEvent(chapter, novel, userId);
        } catch (Exception e) {
            // Log error but don't fail the transaction
            System.err.println("Failed to publish chapter created event: " + e.getMessage());
        }

        // Auto-index to Elasticsearch
        if (elasticsearchAutoIndexService != null) {
            elasticsearchAutoIndexService.onChapterCreated(chapter);
        }

        return getChapterByUuid(chapter.getUuid());
    }

    @Transactional
    public void batchCreateChapters(UUID userId, ChapterBatchCreateRequestDTO req) {
        // Validate novel exists and user is the author
        Novel novel = novelService.getNovelEntity(req.getNovelId());
        if (novel == null || novel.getStatus().equals(NovelStatus.ARCHIVED.getValue())) {
            throw new ResourceNotFoundException("novel not found");
        }

        if (!novel.getAuthorId().equals(userId)) {
            throw new IllegalArgumentException("only the author can create chapters");
        }

        List<Chapter> chapters = new ArrayList<>();
        Date now = new Date();

        for (ChapterBatchCreateRequestDTO.ChapterData data : req.getChapters()) {
            // Validate chapter number doesn't exist
            if (chapterMapper.existsByNovelIdAndChapterNumber(req.getNovelId(), data.getChapterNumber())) {
                throw new IllegalArgumentException("chapter number " + data.getChapterNumber() + " already exists");
            }

            // Calculate word count if not provided
            Integer wordCnt = data.getWordCnt();
            if (wordCnt == null && data.getContent() != null && !data.getContent().trim().isEmpty()) {
                wordCnt = data.getContent().trim().length();
            }

            Chapter chapter = new Chapter();
            chapter.setUuid(UUID.randomUUID());
            chapter.setNovelId(req.getNovelId());
            chapter.setChapterNumber(data.getChapterNumber());
            chapter.setTitle(data.getTitle());
            chapter.setContent(data.getContent());
            chapter.setWordCnt(wordCnt);
            chapter.setIsPremium(data.getIsPremium() != null ? data.getIsPremium() : false);
            chapter.setYuanCost(data.getYuanCost() != null ? data.getYuanCost() : 0.0f);
            chapter.setViewCnt(0L);
            chapter.setIsValid(data.getIsValid() != null ? data.getIsValid() : true);
            chapter.setCreateTime(now);
            chapter.setUpdateTime(now);
            chapter.setPublishTime(data.getPublishTime() != null ? data.getPublishTime() : now);

            chapters.add(chapter);
        }

        chapterMapper.batchInsert(chapters);

        // Update novel's chapter count and word count
        updateNovelStatistics(req.getNovelId());

        // Invalidate chapter caches for this novel
        redisUtil.invalidateChapterCaches(req.getNovelId());
    }

    public ChapterDetailResponseDTO getChapterByUuid(UUID uuid) {
        // Try to get from cache first
        ChapterDetailResponseDTO cachedResponse = redisUtil.getCachedChapter(uuid, ChapterDetailResponseDTO.class);
        if (cachedResponse != null) {
            return cachedResponse;
        }

        // Cache miss - get from database
        Chapter chapter = chapterMapper.selectByUuid(uuid);
        if (chapter == null) {
            throw new ResourceNotFoundException("chapter not found");
        }

        // Check if chapter is valid
        if (Boolean.FALSE.equals(chapter.getIsValid())) {
            throw new ResourceNotFoundException("chapter not found");
        }

        ChapterDetailResponseDTO response = toDetailResponse(chapter);

        // Get navigation links
        Chapter nextChapter = chapterMapper.selectNextChapter(chapter.getNovelId(), chapter.getChapterNumber());
        Chapter prevChapter = chapterMapper.selectPreviousChapter(chapter.getNovelId(), chapter.getChapterNumber());

        if (nextChapter != null) {
            response.setNextChapterUuid(nextChapter.getUuid());
        }
        if (prevChapter != null) {
            response.setPreviousChapterUuid(prevChapter.getUuid());
        }

        // Cache the response
        redisUtil.cacheChapter(uuid, response);

        return response;
    }

    public ChapterDetailResponseDTO getChapterByNovelIdAndNumber(Integer novelId, Integer chapterNumber) {
        // Try to get from cache first
        ChapterDetailResponseDTO cachedResponse = redisUtil.getCachedChapterByNovelAndNumber(novelId, chapterNumber, ChapterDetailResponseDTO.class);
        if (cachedResponse != null) {
            return cachedResponse;
        }

        // Cache miss - get from database
        Chapter chapter = chapterMapper.selectByNovelIdAndChapterNumber(novelId, chapterNumber);
        if (chapter == null || Boolean.FALSE.equals(chapter.getIsValid())) {
            throw new ResourceNotFoundException("chapter not found");
        }
        
        ChapterDetailResponseDTO response = getChapterByUuid(chapter.getUuid());
        
        // Cache the response by novel ID and chapter number
        redisUtil.cacheChapterByNovelAndNumber(novelId, chapterNumber, response);
        
        return response;
    }

    public List<ChapterDetailResponseDTO> getChaptersByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Remove duplicates and limit to reasonable size
        List<Integer> uniqueIds = ids.stream()
            .distinct()
            .limit(100) // Limit to 100 chapters per request
            .collect(Collectors.toList());
        
        // Get chapters from database
        List<Chapter> chapters = chapterMapper.selectByIds(uniqueIds);
        
        // Convert to response DTOs
        return chapters.stream()
            .map(this::toDetailResponse)
            .collect(Collectors.toList());
    }

    public PageResponseDTO<ChapterSummaryDTO> getChaptersByNovelId(Integer novelId, Integer page, Integer pageSize, Boolean publishedOnly) {
        // Validate novel exists
        Novel novel = novelService.getNovelEntity(novelId);
        if (novel == null || novel.getStatus().equals(NovelStatus.ARCHIVED.getValue())) {
            throw new ResourceNotFoundException("novel not found");
        }

        // Validate and set defaults
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize <= 0) {
            pageSize = 20;
        }
        if (pageSize > 100) {
            pageSize = 100;
        }

        // Create cache key
        String cacheKey = String.format("page_%d_size_%d_published_%s", page, pageSize, publishedOnly);
        
        // Try to get from cache first
        PageResponseDTO<ChapterSummaryDTO> cachedResponse = redisUtil.getCachedChapterListTyped(novelId, cacheKey);
        if (cachedResponse != null) {
            return cachedResponse;
        }

        // Cache miss - get from database
        int offset = (page - 1) * pageSize;

        List<Chapter> chapters;
        long totalCount;

        if (Boolean.TRUE.equals(publishedOnly)) {
            chapters = chapterMapper.selectPublishedByNovelIdWithPagination(novelId, offset, pageSize);
            totalCount = chapterMapper.countPublishedByNovelId(novelId);
        } else {
            chapters = chapterMapper.selectByNovelIdWithPagination(novelId, offset, pageSize);
            totalCount = chapterMapper.countByNovelId(novelId);
        }

        List<ChapterSummaryDTO> summaries = chapters.stream()
                .map(this::toSummary)
                .collect(Collectors.toList());

        // Create response using PageResponseDTO
        PageResponseDTO<ChapterSummaryDTO> response = PageResponseDTO.of(summaries, totalCount, page - 1, pageSize);
        
        // Cache the response
        redisUtil.cacheChapterList(novelId, cacheKey, response);

        return response;
    }

    public ChapterStatisticsResponseDTO getChapterStatistics(Integer novelId) {
        // Validate novel exists
        Novel novel = novelService.getNovelEntity(novelId);
        if (novel == null || novel.getStatus().equals(NovelStatus.ARCHIVED.getValue())) {
            throw new ResourceNotFoundException("novel not found");
        }

        // Try to get from cache first
        ChapterStatisticsResponseDTO cachedResponse = redisUtil.getCachedChapterStatistics(novelId, ChapterStatisticsResponseDTO.class);
        if (cachedResponse != null) {
            return cachedResponse;
        }

        // Cache miss - calculate statistics
        long totalChapters = chapterMapper.countByNovelId(novelId);
        long publishedChapters = chapterMapper.countPublishedByNovelId(novelId);

        List<Chapter> drafts = chapterMapper.selectDraftsByNovelId(novelId);
        List<Chapter> scheduled = chapterMapper.selectScheduledByNovelId(novelId);

        List<Chapter> allChapters = chapterMapper.selectByNovelId(novelId);

        long premiumChapters = allChapters.stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsPremium()))
                .count();
        long freeChapters = totalChapters - premiumChapters;

        long totalWordCount = chapterMapper.sumWordCountByNovelId(novelId);
        long totalViewCount = allChapters.stream()
                .mapToLong(Chapter::getViewCnt)
                .sum();

        float totalRevenue = allChapters.stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsPremium()))
                .map(c -> c.getYuanCost() * c.getViewCnt())
                .reduce(0f, Float::sum);

        Integer maxChapterNumber = chapterMapper.selectMaxChapterNumberByNovelId(novelId);

        ChapterStatisticsResponseDTO response = new ChapterStatisticsResponseDTO(
                novelId,
                totalChapters,
                publishedChapters,
                (long) drafts.size(),
                (long) scheduled.size(),
                premiumChapters,
                freeChapters,
                totalWordCount,
                totalViewCount,
                totalRevenue,
                maxChapterNumber
        );

        // Get latest chapter
        if (!allChapters.isEmpty()) {
            Chapter latest = allChapters.stream()
                    .max(Comparator.comparing(Chapter::getPublishTime))
                    .orElse(null);
            if (latest != null) {
                response.setLatestChapter(new ChapterStatisticsResponseDTO.ChapterSummary(
                        latest.getChapterNumber(),
                        latest.getTitle(),
                        latest.getViewCnt()
                ));
            }
        }

        // Get most viewed chapter
        if (!allChapters.isEmpty()) {
            Chapter mostViewed = allChapters.stream()
                    .max(Comparator.comparing(Chapter::getViewCnt))
                    .orElse(null);
            if (mostViewed != null) {
                response.setMostViewedChapter(new ChapterStatisticsResponseDTO.ChapterSummary(
                        mostViewed.getChapterNumber(),
                        mostViewed.getTitle(),
                        mostViewed.getViewCnt()
                ));
            }
        }

        // Cache the response
        redisUtil.cacheChapterStatistics(novelId, response);

        return response;
    }

    @Transactional
    public ChapterDetailResponseDTO updateChapter(UUID userId, ChapterUpdateRequestDTO req) {
        Chapter existing = chapterMapper.selectByUuid(req.getUuid());
        if (existing == null || Boolean.FALSE.equals(existing.getIsValid())) {
            throw new ResourceNotFoundException("chapter not found");
        }

        // Validate user is the author
        Novel novel = novelService.getNovelEntity(existing.getNovelId());
        if (novel == null || !novel.getAuthorId().equals(userId)) {
            throw new IllegalArgumentException("only the author can update chapters");
        }

        boolean hasChanges = false;

        // Update only provided fields
        if (req.getTitle() != null && !req.getTitle().trim().isEmpty()) {
            if (!req.getTitle().equals(existing.getTitle())) {
                existing.setTitle(req.getTitle());
                hasChanges = true;
            }
        }

        if (req.getContent() != null && !req.getContent().trim().isEmpty()) {
            if (!req.getContent().equals(existing.getContent())) {
                existing.setContent(req.getContent());
                hasChanges = true;

                // Recalculate word count if content changed
                if (req.getWordCnt() == null) {
                    existing.setWordCnt(req.getContent().trim().length());
                }
            }
        }

        if (req.getWordCnt() != null && !req.getWordCnt().equals(existing.getWordCnt())) {
            existing.setWordCnt(req.getWordCnt());
            hasChanges = true;
        }

        if (req.getIsPremium() != null && !req.getIsPremium().equals(existing.getIsPremium())) {
            existing.setIsPremium(req.getIsPremium());
            hasChanges = true;
        }

        if (req.getYuanCost() != null && !req.getYuanCost().equals(existing.getYuanCost())) {
            existing.setYuanCost(req.getYuanCost());
            hasChanges = true;
        }

        if (req.getIsValid() != null && !req.getIsValid().equals(existing.getIsValid())) {
            existing.setIsValid(req.getIsValid());
            hasChanges = true;
        }

        if (req.getPublishTime() != null && !req.getPublishTime().equals(existing.getPublishTime())) {
            existing.setPublishTime(req.getPublishTime());
            hasChanges = true;
        }

        if (hasChanges) {
            existing.setUpdateTime(new Date());
            chapterMapper.updateByPrimaryKeySelective(existing);

            // Update novel statistics if word count changed
            if (req.getWordCnt() != null || req.getContent() != null) {
                updateNovelStatistics(existing.getNovelId());
            }

            // Invalidate chapter caches
            redisUtil.deleteChapterCache(req.getUuid());
            redisUtil.deleteChapterCacheByNovelAndNumber(existing.getNovelId(), existing.getChapterNumber());
            redisUtil.invalidateChapterCaches(existing.getNovelId());

            // Publish chapter updated event
            try {
                kafkaEventProducerService.publishChapterUpdatedEvent(existing, novel, userId);
            } catch (Exception e) {
                // Log error but don't fail the transaction
                System.err.println("Failed to publish chapter updated event: " + e.getMessage());
            }

            // Auto-index to Elasticsearch
            if (elasticsearchAutoIndexService != null) {
                elasticsearchAutoIndexService.onChapterUpdated(existing);
            }
        }

        return getChapterByUuid(req.getUuid());
    }

    @Transactional
    public void publishChapter(UUID userId, ChapterPublishRequestDTO req) {
        Chapter chapter = chapterMapper.selectByUuid(req.getUuid());
        if (chapter == null || Boolean.FALSE.equals(chapter.getIsValid())) {
            throw new ResourceNotFoundException("chapter not found");
        }

        // Validate user is the author
        Novel novel = novelService.getNovelEntity(chapter.getNovelId());
        if (novel == null || !novel.getAuthorId().equals(userId)) {
            throw new IllegalArgumentException("only the author can publish chapters");
        }

        chapter.setIsValid(req.getIsValid());
        if (req.getPublishTime() != null) {
            chapter.setPublishTime(req.getPublishTime());
        }
        chapter.setUpdateTime(new Date());

        chapterMapper.updateByPrimaryKeySelective(chapter);

        // Update novel statistics
        updateNovelStatistics(chapter.getNovelId());

        // Invalidate chapter caches
        redisUtil.deleteChapterCache(req.getUuid());
        redisUtil.deleteChapterCacheByNovelAndNumber(chapter.getNovelId(), chapter.getChapterNumber());
        redisUtil.invalidateChapterCaches(chapter.getNovelId());

        // Publish chapter published event
        try {
            kafkaEventProducerService.publishChapterPublishedEvent(chapter, novel, userId);
        } catch (Exception e) {
            // Log error but don't fail the transaction
            System.err.println("Failed to publish chapter published event: " + e.getMessage());
        }
    }

    @Transactional
    public void batchPublishChapters(UUID userId, Integer novelId, Boolean isValid) {
        // Validate novel exists and user is the author
        Novel novel = novelService.getNovelEntity(novelId);
        if (novel == null || novel.getStatus().equals(NovelStatus.ARCHIVED.getValue())) {
            throw new ResourceNotFoundException("novel not found");
        }

        if (!novel.getAuthorId().equals(userId)) {
            throw new IllegalArgumentException("only the author can publish chapters");
        }

        List<Chapter> chapters = chapterMapper.selectByNovelId(novelId);
        List<Integer> ids = chapters.stream()
                .map(Chapter::getId)
                .collect(Collectors.toList());

        if (!ids.isEmpty()) {
            chapterMapper.updatePublishStatusByIds(ids, isValid);
            
            // Update novel statistics
            updateNovelStatistics(novelId);
            
            // Invalidate chapter caches for this novel
            redisUtil.invalidateChapterCaches(novelId);
        }
    }

    @Transactional
    public void incrementViewCount(UUID uuid, UUID userId, String userAgent, String ipAddress, String referrer) {
        Chapter chapter = chapterMapper.selectByUuid(uuid);
        if (chapter == null || Boolean.FALSE.equals(chapter.getIsValid())) {
            throw new ResourceNotFoundException("chapter not found");
        }
        
        // Increment view count
        chapterMapper.incrementViewCount(chapter.getId());
        
        // Publish chapter view event
        try {
            Novel novel = novelService.getNovelEntity(chapter.getNovelId());
            if (novel != null) {
                kafkaEventProducerService.publishChapterViewEvent(chapter, novel, userId, userAgent, ipAddress, referrer);
            }
        } catch (Exception e) {
            // Log error but don't fail the transaction
            System.err.println("Failed to publish chapter view event: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteChapter(UUID userId, UUID uuid) {
        Chapter chapter = chapterMapper.selectByUuid(uuid);
        if (chapter == null) {
            throw new ResourceNotFoundException("chapter not found");
        }

        // Validate user is the author
        Novel novel = novelService.getNovelEntity(chapter.getNovelId());
        if (novel == null || !novel.getAuthorId().equals(userId)) {
            throw new IllegalArgumentException("only the author can delete chapters");
        }

        chapterMapper.softDeleteByUuid(uuid);

        // Update novel statistics
        updateNovelStatistics(chapter.getNovelId());

        // Invalidate chapter caches
        redisUtil.deleteChapterCache(uuid);
        redisUtil.deleteChapterCacheByNovelAndNumber(chapter.getNovelId(), chapter.getChapterNumber());
        redisUtil.invalidateChapterCaches(chapter.getNovelId());

        // Auto-remove from Elasticsearch
        if (elasticsearchAutoIndexService != null) {
            elasticsearchAutoIndexService.onChapterDeleted(chapter.getId());
        }
    }

    @Transactional
    public void deleteChaptersByNovelId(UUID userId, Integer novelId) {
        // Validate novel exists and user is the author
        Novel novel = novelService.getNovelEntity(novelId);
        if (novel == null || novel.getStatus().equals(NovelStatus.ARCHIVED.getValue())) {
            throw new ResourceNotFoundException("novel not found");
        }

        if (!novel.getAuthorId().equals(userId)) {
            throw new IllegalArgumentException("only the author can delete chapters");
        }

        List<Chapter> chapters = chapterMapper.selectByNovelId(novelId);
        for (Chapter chapter : chapters) {
            chapterMapper.softDeleteByPrimaryKey(chapter.getId());
            // Auto-remove from Elasticsearch
            if (elasticsearchAutoIndexService != null) {
                elasticsearchAutoIndexService.onChapterDeleted(chapter.getId());
            }
        }

        // Update novel statistics
        updateNovelStatistics(novelId);

        // Invalidate all chapter caches for this novel
        redisUtil.invalidateChapterCaches(novelId);
    }

    public UUID getNextChapterUuid(UUID currentChapterUuid) {
        Chapter current = chapterMapper.selectByUuid(currentChapterUuid);
        if (current == null) {
            return null;
        }

        Chapter next = chapterMapper.selectNextChapter(current.getNovelId(), current.getChapterNumber());
        return next != null ? next.getUuid() : null;
    }

    public UUID getPreviousChapterUuid(UUID currentChapterUuid) {
        Chapter current = chapterMapper.selectByUuid(currentChapterUuid);
        if (current == null) {
            return null;
        }

        Chapter prev = chapterMapper.selectPreviousChapter(current.getNovelId(), current.getChapterNumber());
        return prev != null ? prev.getUuid() : null;
    }

    public boolean chapterExists(Integer novelId, Integer chapterNumber) {
        return chapterMapper.existsByNovelIdAndChapterNumber(novelId, chapterNumber);
    }

    public Integer getNextAvailableChapterNumber(Integer novelId) {
        Integer maxChapter = chapterMapper.selectMaxChapterNumberByNovelId(novelId);
        return maxChapter != null ? maxChapter + 1 : 1;
    }

    /**
     * Update novel's chapter count and word count statistics
     * Called after chapter creation, update, or deletion
     * Only counts published chapters (is_valid = true and publish_time <= NOW())
     */
    @Transactional
    public void updateNovelStatistics(Integer novelId) {
        // Count only published chapters (is_valid = true and publish_time <= NOW())
        long chapterCount = chapterMapper.countPublishedByNovelId(novelId);
        long wordCount = chapterMapper.sumPublishedWordCountByNovelId(novelId);

        // Use NovelService to update statistics
        novelService.updateNovelStatistics(novelId, (int) chapterCount, wordCount);
    }

    /**
     * Admin-only: Delete a chapter without author validation
     */
    @Transactional
    public void adminDeleteChapter(UUID uuid) {
        Chapter chapter = chapterMapper.selectByUuid(uuid);
        if (chapter == null) {
            throw new ResourceNotFoundException("chapter not found");
        }

        chapterMapper.softDeleteByUuid(uuid);

        // Update novel statistics
        updateNovelStatistics(chapter.getNovelId());

        // Invalidate chapter caches
        redisUtil.deleteChapterCache(uuid);
        redisUtil.deleteChapterCacheByNovelAndNumber(chapter.getNovelId(), chapter.getChapterNumber());
        redisUtil.invalidateChapterCaches(chapter.getNovelId());

        // Auto-remove from Elasticsearch
        if (elasticsearchAutoIndexService != null) {
            elasticsearchAutoIndexService.onChapterDeleted(chapter.getId());
        }
    }

    /**
     * Admin-only: Delete all chapters of a novel without author validation
     */
    @Transactional
    public void adminDeleteChaptersByNovelId(Integer novelId) {
        // Validate novel exists
        Novel novel = novelService.getNovelEntity(novelId);
        if (novel == null || novel.getStatus().equals(NovelStatus.ARCHIVED.getValue())) {
            throw new ResourceNotFoundException("novel not found");
        }

        List<Chapter> chapters = chapterMapper.selectByNovelId(novelId);
        for (Chapter chapter : chapters) {
            chapterMapper.softDeleteByPrimaryKey(chapter.getId());
        }

        // Update novel statistics
        updateNovelStatistics(novelId);

        // Invalidate all chapter caches for this novel
        redisUtil.invalidateChapterCaches(novelId);
    }

    // Helper methods
    private ChapterSummaryDTO toSummary(Chapter chapter) {
        String preview = chapter.getContent() != null && chapter.getContent().length() > 200
                ? chapter.getContent().substring(0, 200)
                : chapter.getContent();

        return new ChapterSummaryDTO(
                chapter.getId(),
                chapter.getUuid(),
                chapter.getNovelId(),
                chapter.getChapterNumber(),
                chapter.getTitle(),
                preview,
                chapter.getWordCnt(),
                chapter.getIsPremium(),
                chapter.getYuanCost(),
                chapter.getViewCnt(),
                chapter.getIsValid(),
                chapter.getCreateTime(),
                chapter.getUpdateTime(),
                chapter.getPublishTime()
        );
    }

    private ChapterDetailResponseDTO toDetailResponse(Chapter chapter) {
        String preview = chapter.getContent() != null && chapter.getContent().length() > 200
                ? chapter.getContent().substring(0, 200)
                : chapter.getContent();

        return new ChapterDetailResponseDTO(
                chapter.getId(),
                chapter.getUuid(),
                chapter.getNovelId(),
                chapter.getChapterNumber(),
                chapter.getTitle(),
                chapter.getContent(),
                preview,
                chapter.getWordCnt(),
                chapter.getIsPremium(),
                chapter.getYuanCost(),
                chapter.getViewCnt(),
                chapter.getIsValid(),
                chapter.getCreateTime(),
                chapter.getUpdateTime(),
                chapter.getPublishTime()
        );
    }
    
    /**
     * Search chapters with dynamic criteria
     */
    public PageResponseDTO<ChapterSummaryDTO> searchChapters(ChapterSearchRequestDTO searchRequest) {
        // Get chapters with search criteria
        List<Chapter> chapters = chapterMapper.selectChaptersWithSearch(searchRequest);
        
        // Get total count
        long totalCount = chapterMapper.countChaptersWithSearch(searchRequest);
        
        // Convert to response DTOs
        List<ChapterSummaryDTO> chapterSummaries = chapters.stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
        
        // Return using PageResponseDTO
        return PageResponseDTO.of(chapterSummaries, totalCount, searchRequest.getPage() - 1, searchRequest.getPageSize());
    }
}