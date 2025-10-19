package com.yushan.content_service.dto.event;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Chapter view event published when a user views a chapter
 * 
 * Consumed by:
 * - Analytics Service: Track user reading behavior and content popularity
 * - Gamification Service: Award points for reading chapters
 * - Engagement Service: Update reading progress and activity feeds
 * - User Service: Update user reading statistics
 */
@Data
@NoArgsConstructor
public class ChapterViewEvent {
    
    /**
     * Event type identifier
     */
    private String eventType = "CHAPTER_VIEW";
    
    /**
     * Chapter ID
     */
    private Integer chapterId;
    
    /**
     * Chapter UUID
     */
    private UUID chapterUuid;
    
    /**
     * Chapter title
     */
    private String chapterTitle;
    
    /**
     * Chapter number
     */
    private Integer chapterNumber;
    
    /**
     * Novel ID
     */
    private Integer novelId;
    
    /**
     * Novel UUID
     */
    private UUID novelUuid;
    
    /**
     * Novel title
     */
    private String novelTitle;
    
    /**
     * Author ID
     */
    private UUID authorId;
    
    /**
     * Author name
     */
    private String authorName;
    
    /**
     * Category ID
     */
    private Integer categoryId;
    
    /**
     * Category name
     */
    private String categoryName;
    
    /**
     * User ID who triggered the event (if applicable)
     */
    private UUID userId;
    
    /**
     * Event timestamp
     */
    private LocalDateTime timestamp;
    
    /**
     * Service that published the event
     */
    private String serviceName = "content-service";
    
    /**
     * Event version for schema evolution
     */
    private String eventVersion = "1.0";
    
    /**
     * User agent string
     */
    private String userAgent;
    
    /**
     * IP address of the viewer
     */
    private String ipAddress;
    
    /**
     * Referrer URL
     */
    private String referrer;
    
    /**
     * Session ID
     */
    private String sessionId;
    
    /**
     * Additional metadata
     */
    private Map<String, Object> metadata;
    
    /**
     * Get metadata (defensive copy)
     * Override Lombok-generated method to ensure defensive copying
     */
    public Map<String, Object> getMetadata() {
        return metadata != null ? new HashMap<>(metadata) : null;
    }
    
    /**
     * Set metadata (defensive copy)
     * Override Lombok-generated method to ensure defensive copying
     */
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata != null ? new HashMap<>(metadata) : null;
    }
    
    // Manual builder
    public static ChapterViewEventBuilder builder() {
        return new ChapterViewEventBuilder();
    }
    
    public static class ChapterViewEventBuilder {
        private String eventType = "CHAPTER_VIEW";
        private Integer chapterId;
        private UUID chapterUuid;
        private String chapterTitle;
        private Integer chapterNumber;
        private Integer novelId;
        private UUID novelUuid;
        private String novelTitle;
        private UUID authorId;
        private String authorName;
        private Integer categoryId;
        private String categoryName;
        private UUID userId;
        private LocalDateTime timestamp;
        private String serviceName = "content-service";
        private String eventVersion = "1.0";
        private String userAgent;
        private String ipAddress;
        private String referrer;
        private String sessionId;
        private Map<String, Object> metadata;
        
        public ChapterViewEventBuilder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }
        
        public ChapterViewEventBuilder chapterId(Integer chapterId) {
            this.chapterId = chapterId;
            return this;
        }
        
        public ChapterViewEventBuilder chapterUuid(UUID chapterUuid) {
            this.chapterUuid = chapterUuid;
            return this;
        }
        
        public ChapterViewEventBuilder chapterTitle(String chapterTitle) {
            this.chapterTitle = chapterTitle;
            return this;
        }
        
        public ChapterViewEventBuilder chapterNumber(Integer chapterNumber) {
            this.chapterNumber = chapterNumber;
            return this;
        }
        
        public ChapterViewEventBuilder novelId(Integer novelId) {
            this.novelId = novelId;
            return this;
        }
        
        public ChapterViewEventBuilder novelUuid(UUID novelUuid) {
            this.novelUuid = novelUuid;
            return this;
        }
        
        public ChapterViewEventBuilder novelTitle(String novelTitle) {
            this.novelTitle = novelTitle;
            return this;
        }
        
        public ChapterViewEventBuilder authorId(UUID authorId) {
            this.authorId = authorId;
            return this;
        }
        
        public ChapterViewEventBuilder authorName(String authorName) {
            this.authorName = authorName;
            return this;
        }
        
        public ChapterViewEventBuilder categoryId(Integer categoryId) {
            this.categoryId = categoryId;
            return this;
        }
        
        public ChapterViewEventBuilder categoryName(String categoryName) {
            this.categoryName = categoryName;
            return this;
        }
        
        public ChapterViewEventBuilder userId(UUID userId) {
            this.userId = userId;
            return this;
        }
        
        public ChapterViewEventBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public ChapterViewEventBuilder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }
        
        public ChapterViewEventBuilder eventVersion(String eventVersion) {
            this.eventVersion = eventVersion;
            return this;
        }
        
        public ChapterViewEventBuilder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }
        
        public ChapterViewEventBuilder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }
        
        public ChapterViewEventBuilder referrer(String referrer) {
            this.referrer = referrer;
            return this;
        }
        
        public ChapterViewEventBuilder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }
        
        public ChapterViewEventBuilder metadata(Map<String, Object> metadata) {
            this.metadata = metadata != null ? new HashMap<>(metadata) : null;
            return this;
        }
        
        public ChapterViewEvent build() {
            ChapterViewEvent event = new ChapterViewEvent();
            event.eventType = eventType;
            event.chapterId = chapterId;
            event.chapterUuid = chapterUuid;
            event.chapterTitle = chapterTitle;
            event.chapterNumber = chapterNumber;
            event.novelId = novelId;
            event.novelUuid = novelUuid;
            event.novelTitle = novelTitle;
            event.authorId = authorId;
            event.authorName = authorName;
            event.categoryId = categoryId;
            event.categoryName = categoryName;
            event.userId = userId;
            event.timestamp = timestamp;
            event.serviceName = serviceName;
            event.eventVersion = eventVersion;
            event.userAgent = userAgent;
            event.ipAddress = ipAddress;
            event.referrer = referrer;
            event.sessionId = sessionId;
            event.metadata = metadata != null ? new HashMap<>(metadata) : null;
            return event;
        }
    }
}
