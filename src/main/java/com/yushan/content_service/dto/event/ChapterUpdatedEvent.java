package com.yushan.content_service.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Chapter updated event published when a chapter is updated
 * 
 * Consumed by:
 * - Analytics Service: Track content modification metrics
 * - Gamification Service: Award points for content updates
 * - Engagement Service: Update activity feeds
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterUpdatedEvent {
    
    /**
     * Event type identifier
     */
    @Builder.Default
    private String eventType = "CHAPTER_UPDATED";
    
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
     * Word count
     */
    private Integer wordCount;
    
    /**
     * Is premium chapter
     */
    private Boolean isPremium;
    
    /**
     * Cost in yuan
     */
    private Float yuanCost;
    
    /**
     * Is valid/published
     */
    private Boolean isValid;
    
    /**
     * Publish time
     */
    private LocalDateTime publishTime;
    
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
}
