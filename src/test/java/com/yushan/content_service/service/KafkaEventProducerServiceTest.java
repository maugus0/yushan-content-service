package com.yushan.content_service.service;

import com.yushan.content_service.entity.Chapter;
import com.yushan.content_service.entity.Novel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaEventProducerServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private KafkaEventProducerService kafkaEventProducerService;

    private Novel testNovel;
    private Chapter testChapter;

    @BeforeEach
    void setUp() {
        testNovel = new Novel();
        testNovel.setId(1);
        testNovel.setUuid(UUID.randomUUID());
        testNovel.setTitle("Test Novel");
        testNovel.setAuthorName("Test Author");
        testNovel.setStatus(2); // PUBLISHED
        testNovel.setCreateTime(new Date());
        testNovel.setUpdateTime(new Date());

        testChapter = new Chapter();
        testChapter.setId(1);
        testChapter.setUuid(UUID.randomUUID());
        testChapter.setNovelId(1);
        testChapter.setTitle("Test Chapter");
        testChapter.setContent("Test content");
        testChapter.setIsValid(true);
        testChapter.setCreateTime(new Date());
        testChapter.setUpdateTime(new Date());

        // Mock KafkaTemplate.send to return a successful CompletableFuture
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(any(), anyString(), any())).thenReturn(future);
    }

    @Test
    void publishNovelViewEvent_ShouldPublishEvent() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String userAgent = "test-agent";
        String ipAddress = "127.0.0.1";
        String referrer = "test-referrer";

        // Act
        kafkaEventProducerService.publishNovelViewEvent(testNovel, userId, userAgent, ipAddress, referrer);

        // Assert
        verify(kafkaTemplate).send(any(), anyString(), any());
    }

    @Test
    void publishNovelCreatedEvent_ShouldPublishEvent() {
        // Arrange
        UUID userId = UUID.randomUUID();

        // Act
        kafkaEventProducerService.publishNovelCreatedEvent(testNovel, userId);

        // Assert
        verify(kafkaTemplate).send(any(), anyString(), any());
    }

    @Test
    void publishNovelUpdatedEvent_ShouldPublishEvent() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String[] changedFields = {"title", "description"};

        // Act
        kafkaEventProducerService.publishNovelUpdatedEvent(testNovel, userId, changedFields);

        // Assert
        verify(kafkaTemplate).send(any(), anyString(), any());
    }

    @Test
    void publishNovelStatusChangedEvent_ShouldPublishEvent() {
        // Arrange
        String oldStatus = "DRAFT";
        String newStatus = "PUBLISHED";
        UUID userId = UUID.randomUUID();
        String reason = "Approved by admin";

        // Act
        kafkaEventProducerService.publishNovelStatusChangedEvent(testNovel, oldStatus, newStatus, userId, reason);

        // Assert
        verify(kafkaTemplate).send(any(), anyString(), any());
    }

    @Test
    void publishChapterCreatedEvent_ShouldPublishEvent() {
        // Arrange
        UUID userId = UUID.randomUUID();

        // Act
        kafkaEventProducerService.publishChapterCreatedEvent(testChapter, testNovel, userId);

        // Assert
        verify(kafkaTemplate).send(any(), anyString(), any());
    }

    @Test
    void publishChapterUpdatedEvent_ShouldPublishEvent() {
        // Arrange
        UUID userId = UUID.randomUUID();

        // Act
        kafkaEventProducerService.publishChapterUpdatedEvent(testChapter, testNovel, userId);

        // Assert
        verify(kafkaTemplate).send(any(), anyString(), any());
    }

    @Test
    void publishChapterPublishedEvent_ShouldPublishEvent() {
        // Arrange
        UUID userId = UUID.randomUUID();

        // Act
        kafkaEventProducerService.publishChapterPublishedEvent(testChapter, testNovel, userId);

        // Assert
        verify(kafkaTemplate).send(any(), anyString(), any());
    }

    @Test
    void publishChapterViewEvent_ShouldPublishEvent() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String userAgent = "test-agent";
        String ipAddress = "127.0.0.1";
        String referrer = "test-referrer";

        // Act
        kafkaEventProducerService.publishChapterViewEvent(testChapter, testNovel, userId, userAgent, ipAddress, referrer);

        // Assert
        verify(kafkaTemplate).send(any(), anyString(), any());
    }

}
