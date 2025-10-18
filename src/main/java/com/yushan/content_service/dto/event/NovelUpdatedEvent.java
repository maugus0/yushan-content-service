package com.yushan.content_service.dto.event;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Novel updated event published when novel metadata is updated
 * 
 * Consumed by:
 * - Analytics Service: Track update metrics
 * - Engagement Service: Notify followers of updates
 */
@Data
@NoArgsConstructor
public class NovelUpdatedEvent {
    
    /**
     * Event type identifier
     */
    private String eventType = "NOVEL_UPDATED";
    
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
     * Fields that were updated
     */
    private String[] updatedFields;
    
    /**
     * Get updated fields (defensive copy)
     * Override Lombok-generated method to ensure defensive copying
     */
    public String[] getUpdatedFields() {
        return updatedFields != null ? updatedFields.clone() : null;
    }
    
    /**
     * Set updated fields (defensive copy)
     * Override Lombok-generated method to ensure defensive copying
     */
    public void setUpdatedFields(String[] updatedFields) {
        this.updatedFields = updatedFields != null ? updatedFields.clone() : null;
    }
    
    // Manual constructor for defensive copying
    public NovelUpdatedEvent(String eventType, Integer novelId, UUID novelUuid, String novelTitle,
                           UUID authorId, String authorName, Integer categoryId, UUID userId,
                           LocalDateTime timestamp, String serviceName, String eventVersion,
                           String[] updatedFields, String previousTitle, String previousSynopsis,
                           Integer previousCategoryId) {
        this.eventType = eventType;
        this.novelId = novelId;
        this.novelUuid = novelUuid;
        this.novelTitle = novelTitle;
        this.authorId = authorId;
        this.authorName = authorName;
        this.categoryId = categoryId;
        this.userId = userId;
        this.timestamp = timestamp;
        this.serviceName = serviceName;
        this.eventVersion = eventVersion;
        this.updatedFields = updatedFields != null ? updatedFields.clone() : null;
        this.previousTitle = previousTitle;
        this.previousSynopsis = previousSynopsis;
        this.previousCategoryId = previousCategoryId;
    }
    
    // Manual builder
    public static NovelUpdatedEventBuilder builder() {
        return new NovelUpdatedEventBuilder();
    }
    
    public static class NovelUpdatedEventBuilder {
        private String eventType = "NOVEL_UPDATED";
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
        private String[] updatedFields;
        private String previousTitle;
        private String previousSynopsis;
        private Integer previousCategoryId;
        
        public NovelUpdatedEventBuilder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }
        
        public NovelUpdatedEventBuilder novelId(Integer novelId) {
            this.novelId = novelId;
            return this;
        }
        
        public NovelUpdatedEventBuilder novelUuid(UUID novelUuid) {
            this.novelUuid = novelUuid;
            return this;
        }
        
        public NovelUpdatedEventBuilder novelTitle(String novelTitle) {
            this.novelTitle = novelTitle;
            return this;
        }
        
        public NovelUpdatedEventBuilder authorId(UUID authorId) {
            this.authorId = authorId;
            return this;
        }
        
        public NovelUpdatedEventBuilder authorName(String authorName) {
            this.authorName = authorName;
            return this;
        }
        
        public NovelUpdatedEventBuilder categoryId(Integer categoryId) {
            this.categoryId = categoryId;
            return this;
        }
        
        public NovelUpdatedEventBuilder userId(UUID userId) {
            this.userId = userId;
            return this;
        }
        
        public NovelUpdatedEventBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public NovelUpdatedEventBuilder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }
        
        public NovelUpdatedEventBuilder eventVersion(String eventVersion) {
            this.eventVersion = eventVersion;
            return this;
        }
        
        public NovelUpdatedEventBuilder updatedFields(String[] updatedFields) {
            this.updatedFields = updatedFields != null ? updatedFields.clone() : null;
            return this;
        }
        
        public NovelUpdatedEventBuilder previousTitle(String previousTitle) {
            this.previousTitle = previousTitle;
            return this;
        }
        
        public NovelUpdatedEventBuilder previousSynopsis(String previousSynopsis) {
            this.previousSynopsis = previousSynopsis;
            return this;
        }
        
        public NovelUpdatedEventBuilder previousCategoryId(Integer previousCategoryId) {
            this.previousCategoryId = previousCategoryId;
            return this;
        }
        
        public NovelUpdatedEvent build() {
            return new NovelUpdatedEvent(eventType, novelId, novelUuid, novelTitle, authorId, authorName,
                    categoryId, userId, timestamp, serviceName, eventVersion, updatedFields,
                    previousTitle, previousSynopsis, previousCategoryId);
        }
    }
    
    /**
     * Previous values (if needed for rollback)
     */
    private String previousTitle;
    private String previousSynopsis;
    private Integer previousCategoryId;
}
