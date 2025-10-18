package com.yushan.content_service.dto.event;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Novel view event published when a user views a novel
 * 
 * Consumed by:
 * - Analytics Service: Track view analytics
 * - Gamification Service: Award points for viewing
 */
@Data
@NoArgsConstructor
public class NovelViewEvent {
    
    /**
     * Event type identifier
     */
    private String eventType = "NOVEL_VIEW";
    
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
    public static NovelViewEventBuilder builder() {
        return new NovelViewEventBuilder();
    }
    
    public static class NovelViewEventBuilder {
        private String eventType = "NOVEL_VIEW";
        private Integer novelId;
        private UUID novelUuid;
        private String novelTitle;
        private UUID authorId;
        private String authorName;
        private Integer categoryId;
        private UUID userId;
        private LocalDateTime timestamp;
        private String serviceName = "content-service";
        private String eventVersion = "1.0";
        private String userAgent;
        private String ipAddress;
        private String referrer;
        private String sessionId;
        private Map<String, Object> metadata;
        
        public NovelViewEventBuilder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }
        
        public NovelViewEventBuilder novelId(Integer novelId) {
            this.novelId = novelId;
            return this;
        }
        
        public NovelViewEventBuilder novelUuid(UUID novelUuid) {
            this.novelUuid = novelUuid;
            return this;
        }
        
        public NovelViewEventBuilder novelTitle(String novelTitle) {
            this.novelTitle = novelTitle;
            return this;
        }
        
        public NovelViewEventBuilder authorId(UUID authorId) {
            this.authorId = authorId;
            return this;
        }
        
        public NovelViewEventBuilder authorName(String authorName) {
            this.authorName = authorName;
            return this;
        }
        
        public NovelViewEventBuilder categoryId(Integer categoryId) {
            this.categoryId = categoryId;
            return this;
        }
        
        public NovelViewEventBuilder userId(UUID userId) {
            this.userId = userId;
            return this;
        }
        
        public NovelViewEventBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public NovelViewEventBuilder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }
        
        public NovelViewEventBuilder eventVersion(String eventVersion) {
            this.eventVersion = eventVersion;
            return this;
        }
        
        public NovelViewEventBuilder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }
        
        public NovelViewEventBuilder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }
        
        public NovelViewEventBuilder referrer(String referrer) {
            this.referrer = referrer;
            return this;
        }
        
        public NovelViewEventBuilder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }
        
        public NovelViewEventBuilder metadata(Map<String, Object> metadata) {
            this.metadata = metadata != null ? new HashMap<>(metadata) : null;
            return this;
        }
        
        public NovelViewEvent build() {
            NovelViewEvent event = new NovelViewEvent();
            event.eventType = eventType;
            event.novelId = novelId;
            event.novelUuid = novelUuid;
            event.novelTitle = novelTitle;
            event.authorId = authorId;
            event.authorName = authorName;
            event.categoryId = categoryId;
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
