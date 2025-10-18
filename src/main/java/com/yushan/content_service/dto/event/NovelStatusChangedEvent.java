package com.yushan.content_service.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Novel status changed event published when novel status changes
 * 
 * Consumed by:
 * - Analytics Service: Track status change metrics
 * - Gamification Service: Award points for publishing
 * - Engagement Service: Notify followers of status changes
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NovelStatusChangedEvent {
    
    /**
     * Event type identifier
     */
    @Builder.Default
    private String eventType = "NOVEL_STATUS_CHANGED";
    
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
     * Previous status
     */
    private String previousStatus;
    
    /**
     * New status
     */
    private String newStatus;
    
    /**
     * Reason for status change
     */
    private String reason;
    
    /**
     * Admin ID who changed the status (if applicable)
     */
    private UUID adminId;
}
