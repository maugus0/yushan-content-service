package com.yushan.content_service.service;

import com.yushan.content_service.dto.event.NovelCreatedEvent;
import com.yushan.content_service.dto.event.NovelStatusChangedEvent;
import com.yushan.content_service.dto.event.NovelUpdatedEvent;
import com.yushan.content_service.dto.event.NovelViewEvent;
import com.yushan.content_service.entity.Novel;
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
 * Kafka event producer service for publishing novel events
 * 
 * This service publishes events to Kafka topics for consumption by:
 * - Analytics Service: Track user behavior and novel metrics
 * - Gamification Service: Award points and unlock achievements
 * - Engagement Service: Notify followers and update feeds
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
