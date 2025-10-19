package com.yushan.content_service.service;

import com.yushan.content_service.dto.event.*;
import com.yushan.content_service.entity.Novel;
import com.yushan.content_service.entity.Chapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Kafka event producer service for publishing novel and chapter events
 * 
 * This service publishes events to Kafka topics for consumption by:
 * - Analytics Service: Track user behavior and content metrics
 * - Gamification Service: Award points and unlock achievements
 * - Engagement Service: Notify followers and update feeds
 * - User Service: Update user statistics
 */
@Slf4j
@Service
public class KafkaEventProducerService {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.producer.topic.novel-events:novel-events}")
    private String novelEventsTopic;

    @Value("${spring.kafka.producer.topic.novel-views:novel-views}")
    private String novelViewsTopic;

    @Value("${spring.kafka.producer.topic.novel-status-changes:novel-status-changes}")
    private String novelStatusChangesTopic;

    @Value("${spring.kafka.producer.topic.chapter-events:chapter-events}")
    private String chapterEventsTopic;

    @Value("${spring.kafka.producer.topic.chapter-views:chapter-views}")
    private String chapterViewsTopic;

    @Value("${spring.kafka.producer.topic.chapter-published:chapter-published}")
    private String chapterPublishedTopic;

    @Value("${spring.application.name:content-service}")
    private String serviceName;

    private static final String EVENT_VERSION = "1.0";

    /**
     * Publish novel view event
     */
    public void publishNovelViewEvent(Novel novel, UUID userId, String userAgent, String ipAddress, String referrer) {
        try {
            NovelViewEvent event = NovelViewEvent.builder()
                    .eventType("NOVEL_VIEW")
                    .novelId(novel.getId())
                    .novelUuid(novel.getUuid())
                    .novelTitle(novel.getTitle())
                    .authorId(novel.getAuthorId())
                    .authorName(novel.getAuthorName())
                    .categoryId(novel.getCategoryId())
                    .userId(userId)
                    .userAgent(userAgent)
                    .ipAddress(ipAddress)
                    .referrer(referrer)
                    .timestamp(LocalDateTime.now())
                    .serviceName(serviceName)
                    .eventVersion(EVENT_VERSION)
                    .build();

            publishEvent(novelViewsTopic, event.getNovelId().toString(), event);
            log.info("Published novel view event for novel ID: {}, user ID: {}", novel.getId(), userId);
        } catch (Exception e) {
            log.error("Failed to publish novel view event for novel ID: {}", novel.getId(), e);
        }
    }

    /**
     * Publish novel created event
     */
    public void publishNovelCreatedEvent(Novel novel, UUID userId) {
        try {
            NovelCreatedEvent event = NovelCreatedEvent.builder()
                    .eventType("NOVEL_CREATED")
                    .novelId(novel.getId())
                    .novelUuid(novel.getUuid())
                    .novelTitle(novel.getTitle())
                    .authorId(novel.getAuthorId())
                    .authorName(novel.getAuthorName())
                    .categoryId(novel.getCategoryId())
                    .userId(userId)
                    .synopsis(novel.getSynopsis())
                    .coverImageUrl(novel.getCoverImgUrl())
                    .status(novel.getStatus().toString())
                    .timestamp(LocalDateTime.now())
                    .serviceName(serviceName)
                    .eventVersion(EVENT_VERSION)
                    .build();

            publishEvent(novelEventsTopic, event.getNovelId().toString(), event);
            log.info("Published novel created event for novel ID: {}", novel.getId());
        } catch (Exception e) {
            log.error("Failed to publish novel created event for novel ID: {}", novel.getId(), e);
        }
    }

    /**
     * Publish novel updated event
     */
    public void publishNovelUpdatedEvent(Novel novel, UUID userId, String[] updatedFields) {
        try {
            NovelUpdatedEvent event = NovelUpdatedEvent.builder()
                    .eventType("NOVEL_UPDATED")
                    .novelId(novel.getId())
                    .novelUuid(novel.getUuid())
                    .novelTitle(novel.getTitle())
                    .authorId(novel.getAuthorId())
                    .authorName(novel.getAuthorName())
                    .categoryId(novel.getCategoryId())
                    .userId(userId)
                    .updatedFields(updatedFields)
                    .timestamp(LocalDateTime.now())
                    .serviceName(serviceName)
                    .eventVersion(EVENT_VERSION)
                    .build();

            publishEvent(novelEventsTopic, event.getNovelId().toString(), event);
            log.info("Published novel updated event for novel ID: {}", novel.getId());
        } catch (Exception e) {
            log.error("Failed to publish novel updated event for novel ID: {}", novel.getId(), e);
        }
    }

    /**
     * Publish novel status changed event
     */
    public void publishNovelStatusChangedEvent(Novel novel, String previousStatus, String newStatus, UUID userId, String reason) {
        try {
            NovelStatusChangedEvent event = NovelStatusChangedEvent.builder()
                    .eventType("NOVEL_STATUS_CHANGED")
                    .novelId(novel.getId())
                    .novelUuid(novel.getUuid())
                    .novelTitle(novel.getTitle())
                    .authorId(novel.getAuthorId())
                    .authorName(novel.getAuthorName())
                    .categoryId(novel.getCategoryId())
                    .userId(userId)
                    .previousStatus(previousStatus)
                    .newStatus(newStatus)
                    .reason(reason)
                    .timestamp(LocalDateTime.now())
                    .serviceName(serviceName)
                    .eventVersion(EVENT_VERSION)
                    .build();

            publishEvent(novelStatusChangesTopic, event.getNovelId().toString(), event);
            log.info("Published novel status changed event for novel ID: {} from {} to {}", 
                    novel.getId(), previousStatus, newStatus);
        } catch (Exception e) {
            log.error("Failed to publish novel status changed event for novel ID: {}", novel.getId(), e);
        }
    }

    // ==================== CHAPTER EVENTS ====================

    /**
     * Publish chapter created event
     */
    public void publishChapterCreatedEvent(Chapter chapter, Novel novel, UUID userId) {
        try {
            ChapterCreatedEvent event = ChapterCreatedEvent.builder()
                    .chapterId(chapter.getId())
                    .chapterUuid(chapter.getUuid())
                    .chapterTitle(chapter.getTitle())
                    .chapterNumber(chapter.getChapterNumber())
                    .novelId(novel.getId())
                    .novelUuid(novel.getUuid())
                    .novelTitle(novel.getTitle())
                    .authorId(novel.getAuthorId())
                    .authorName(novel.getAuthorName())
                    .categoryId(novel.getCategoryId())
                    .categoryName(null) // Will be populated by consumer services
                    .wordCount(chapter.getWordCnt())
                    .isPremium(chapter.getIsPremium())
                    .yuanCost(chapter.getYuanCost())
                    .isValid(chapter.getIsValid())
                    .publishTime(chapter.getPublishTime() != null ? 
                        chapter.getPublishTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
                    .timestamp(LocalDateTime.now())
                    .serviceName(serviceName)
                    .eventVersion(EVENT_VERSION)
                    .build();

            publishEvent(chapterEventsTopic, event.getChapterId().toString(), event);
            log.info("Published chapter created event for chapter ID: {}, novel ID: {}", chapter.getId(), novel.getId());
        } catch (Exception e) {
            log.error("Failed to publish chapter created event for chapter ID: {}", chapter.getId(), e);
        }
    }

    /**
     * Publish chapter updated event
     */
    public void publishChapterUpdatedEvent(Chapter chapter, Novel novel, UUID userId) {
        try {
            ChapterUpdatedEvent event = ChapterUpdatedEvent.builder()
                    .chapterId(chapter.getId())
                    .chapterUuid(chapter.getUuid())
                    .chapterTitle(chapter.getTitle())
                    .chapterNumber(chapter.getChapterNumber())
                    .novelId(novel.getId())
                    .novelUuid(novel.getUuid())
                    .novelTitle(novel.getTitle())
                    .authorId(novel.getAuthorId())
                    .authorName(novel.getAuthorName())
                    .categoryId(novel.getCategoryId())
                    .categoryName(null) // Will be populated by consumer services
                    .wordCount(chapter.getWordCnt())
                    .isPremium(chapter.getIsPremium())
                    .yuanCost(chapter.getYuanCost())
                    .isValid(chapter.getIsValid())
                    .publishTime(chapter.getPublishTime() != null ? 
                        chapter.getPublishTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
                    .timestamp(LocalDateTime.now())
                    .serviceName(serviceName)
                    .eventVersion(EVENT_VERSION)
                    .build();

            publishEvent(chapterEventsTopic, event.getChapterId().toString(), event);
            log.info("Published chapter updated event for chapter ID: {}, novel ID: {}", chapter.getId(), novel.getId());
        } catch (Exception e) {
            log.error("Failed to publish chapter updated event for chapter ID: {}", chapter.getId(), e);
        }
    }

    /**
     * Publish chapter published event
     */
    public void publishChapterPublishedEvent(Chapter chapter, Novel novel, UUID userId) {
        try {
            ChapterPublishedEvent event = ChapterPublishedEvent.builder()
                    .chapterId(chapter.getId())
                    .chapterUuid(chapter.getUuid())
                    .chapterTitle(chapter.getTitle())
                    .chapterNumber(chapter.getChapterNumber())
                    .novelId(novel.getId())
                    .novelUuid(novel.getUuid())
                    .novelTitle(novel.getTitle())
                    .authorId(novel.getAuthorId())
                    .authorName(novel.getAuthorName())
                    .categoryId(novel.getCategoryId())
                    .categoryName(null) // Will be populated by consumer services
                    .isValid(chapter.getIsValid())
                    .publishTime(chapter.getPublishTime() != null ? 
                        chapter.getPublishTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
                    .timestamp(LocalDateTime.now())
                    .serviceName(serviceName)
                    .eventVersion(EVENT_VERSION)
                    .build();

            publishEvent(chapterPublishedTopic, event.getChapterId().toString(), event);
            log.info("Published chapter published event for chapter ID: {}, novel ID: {}", chapter.getId(), novel.getId());
        } catch (Exception e) {
            log.error("Failed to publish chapter published event for chapter ID: {}", chapter.getId(), e);
        }
    }

    /**
     * Publish chapter view event
     */
    public void publishChapterViewEvent(Chapter chapter, Novel novel, UUID userId, String userAgent, String ipAddress, String referrer) {
        try {
            ChapterViewEvent event = ChapterViewEvent.builder()
                    .chapterId(chapter.getId())
                    .chapterUuid(chapter.getUuid())
                    .chapterTitle(chapter.getTitle())
                    .chapterNumber(chapter.getChapterNumber())
                    .novelId(novel.getId())
                    .novelUuid(novel.getUuid())
                    .novelTitle(novel.getTitle())
                    .authorId(novel.getAuthorId())
                    .authorName(novel.getAuthorName())
                    .categoryId(novel.getCategoryId())
                    .categoryName(null) // Will be populated by consumer services
                    .userId(userId)
                    .userAgent(userAgent)
                    .ipAddress(ipAddress)
                    .referrer(referrer)
                    .timestamp(LocalDateTime.now())
                    .serviceName(serviceName)
                    .eventVersion(EVENT_VERSION)
                    .build();

            publishEvent(chapterViewsTopic, event.getChapterId().toString(), event);
            log.info("Published chapter view event for chapter ID: {}, user ID: {}", chapter.getId(), userId);
        } catch (Exception e) {
            log.error("Failed to publish chapter view event for chapter ID: {}", chapter.getId(), e);
        }
    }

    /**
     * Generic method to publish events to Kafka
     */
    private void publishEvent(String topic, String key, Object event) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.debug("Successfully sent event to topic: {}, partition: {}, offset: {}", 
                        topic, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send event to topic: {}", topic, ex);
            }
        });
    }
}
