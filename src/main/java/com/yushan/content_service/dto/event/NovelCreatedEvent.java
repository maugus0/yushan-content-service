package com.yushan.content_service.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Novel created event published when a new novel is created
 * 
 * Consumed by:
 * - Analytics Service: Track novel creation metrics
 * - Gamification Service: Award points for novel creation
 * - Engagement Service: Notify followers
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NovelCreatedEvent {
    
    /**
     * Event type identifier
     */
    @Builder.Default
    private String eventType = "NOVEL_CREATED";
    
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
    @Builder.Default
    private String serviceName = "content-service";
    
    /**
     * Event version for schema evolution
     */
    @Builder.Default
    private String eventVersion = "1.0";
    
    /**
     * Novel synopsis
     */
    private String synopsis;
    
    /**
     * Cover image URL
     */
    private String coverImageUrl;
    
    /**
     * Initial status
     */
    private String status;
}
