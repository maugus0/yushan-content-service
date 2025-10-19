package com.yushan.content_service.service;

import com.yushan.content_service.dao.NovelMapper;
import com.yushan.content_service.dto.common.PageResponseDTO;
import com.yushan.content_service.dto.novel.NovelCreateRequestDTO;
import com.yushan.content_service.dto.novel.NovelDetailResponseDTO;
import com.yushan.content_service.dto.novel.NovelSearchRequestDTO;
import com.yushan.content_service.dto.novel.NovelUpdateRequestDTO;
import com.yushan.content_service.entity.Novel;
import com.yushan.content_service.enums.NovelStatus;
import com.yushan.content_service.exception.ResourceNotFoundException;
import com.yushan.content_service.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for novel management operations.
 * Handles business logic for novel CRUD operations.
 */
@Service
public class NovelService {

    @Autowired
    private NovelMapper novelMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private KafkaEventProducerService kafkaEventProducerService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private CategoryService categoryService;

    /**
     * Create a new novel
     */
    @Transactional
    public NovelDetailResponseDTO createNovel(UUID userId, String authorName, NovelCreateRequestDTO request) {
        if (request.getCategoryId() == null) {
            throw new IllegalArgumentException("category not found");
        }
        
        // Validate category exists and get category name
        com.yushan.content_service.entity.Category category = categoryService.getCategoryById(request.getCategoryId());
        if (category == null || !category.getIsActive()) {
            throw new IllegalArgumentException("category not found or inactive");
        }
        
        Novel novel = new Novel();
        novel.setUuid(UUID.randomUUID());
        novel.setTitle(request.getTitle());
        novel.setAuthorId(userId);
        novel.setAuthorName(authorName);
        novel.setCategoryId(request.getCategoryId());
        novel.setSynopsis(request.getSynopsis());
        
        // Process cover image after novel is created (to get novelId)
        if (request.getCoverImgBase64() != null && !request.getCoverImgBase64().trim().isEmpty()) {
            novel.setCoverImgUrl(convertBase64ToUrl(request.getCoverImgBase64()));
        }
        
        novel.setStatus(NovelStatus.DRAFT.getValue());
        novel.setIsCompleted(Boolean.TRUE.equals(request.getIsCompleted()));
        novel.setChapterCnt(0);
        novel.setWordCnt(0L);
        novel.setAvgRating(0.0f);
        novel.setReviewCnt(0);
        novel.setViewCnt(0L);
        novel.setVoteCnt(0);
        novel.setYuanCnt(0.0f);
        
        Date now = new Date();
        novel.setCreateTime(now);
        novel.setUpdateTime(now);
        novel.setPublishTime(null);
        
        novelMapper.insertSelective(novel);
        
        // Cache the new novel
        redisUtil.cacheNovel(novel.getId(), novel);
        
        // Publish Kafka event
        kafkaEventProducerService.publishNovelCreatedEvent(novel, userId);
        
        return toResponse(novel);
    }

    /**
     * Update novel statistics (chapter count and word count)
     * For internal use by other services
     */
    @Transactional
    public void updateNovelStatistics(Integer novelId, int chapterCount, long wordCount) {
        Novel novel = getNovelEntity(novelId);
        if (novel == null) {
            return;
        }

        novel.setChapterCnt(chapterCount);
        novel.setWordCnt(wordCount);
        novel.setUpdateTime(new Date());

        novelMapper.updateByPrimaryKeySelective(novel);
        
        // Invalidate cache since novel was updated
        redisUtil.invalidateNovelCaches(novelId);
        
        // Cache the updated novel
        redisUtil.cacheNovel(novelId, novel);
    }

    /**
     * Get novel entity by ID (for internal use by other services)
     */
    public Novel getNovelEntity(Integer id) {
        // Try to get from cache first
        Novel cachedNovel = redisUtil.getCachedNovel(id, Novel.class);
        if (cachedNovel != null) {
            return cachedNovel;
        }
        
        // Cache miss - get from database
        Novel novel = novelMapper.selectByPrimaryKey(id);
        if (novel == null) {
            return null;
        }
        
        // Cache the novel for future requests
        redisUtil.cacheNovel(id, novel);
        
        return novel;
    }

    /**
     * Get novel by ID with Redis caching
     */
    public NovelDetailResponseDTO getNovel(Integer id) {
        // Try to get from cache first
        Novel cachedNovel = redisUtil.getCachedNovel(id, Novel.class);
        if (cachedNovel != null) {
            // Check if novel is not archived
            if (cachedNovel.getStatus().equals(NovelStatus.ARCHIVED.getValue())) {
                throw new ResourceNotFoundException("novel not found");
            }
            return toResponse(cachedNovel);
        }
        
        // Cache miss - get from database
        Novel novel = novelMapper.selectByPrimaryKey(id);
        if (novel == null) {
            throw new ResourceNotFoundException("novel not found");
        }
        if (novel.getStatus().equals(NovelStatus.ARCHIVED.getValue())) {
            throw new ResourceNotFoundException("novel not found");
        }
        
        // Cache the novel for future requests
        redisUtil.cacheNovel(id, novel);
        
        return toResponse(novel);
    }

    /**
     * Get novel by UUID
     */
    public NovelDetailResponseDTO getNovelByUuid(UUID uuid) {
        Novel novel = novelMapper.selectByUuid(uuid);
        if (novel == null) {
            throw new ResourceNotFoundException("novel not found");
        }
        if (novel.getStatus().equals(NovelStatus.ARCHIVED.getValue())) {
            throw new ResourceNotFoundException("novel not found");
        }
        return toResponse(novel);
    }

    /**
     * Update novel
     */
    @Transactional
    public NovelDetailResponseDTO updateNovel(Integer id, NovelUpdateRequestDTO request) {
        Novel existing = novelMapper.selectByPrimaryKey(id);
        if (existing == null) {
            throw new ResourceNotFoundException("novel not found");
        }

        // Check if novel can be edited - allow DRAFT, PUBLISHED, HIDDEN, and UNDER_REVIEW novels to be edited
        // Note: UNDER_REVIEW novels can only be edited by ADMIN (authorization is handled by NovelGuard)
        int currentStatus = existing.getStatus();
        if (currentStatus != NovelStatus.DRAFT.getValue() && 
            currentStatus != NovelStatus.PUBLISHED.getValue() && 
            currentStatus != NovelStatus.HIDDEN.getValue() &&
            currentStatus != NovelStatus.UNDER_REVIEW.getValue()) {
            throw new IllegalArgumentException("only draft, published, hidden, or under review novels can be edited");
        }

        boolean changeOtherFieldsNotIsCompleted = false;
        boolean changeStatus = false;
        List<String> updatedFields = new ArrayList<>();
        
        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            if (!request.getTitle().equals(existing.getTitle())) {
                existing.setTitle(request.getTitle());
                updatedFields.add("title");
                changeOtherFieldsNotIsCompleted = true;
            }
        }
        if (request.getSynopsis() != null && !request.getSynopsis().trim().isEmpty()) {
            if (!request.getSynopsis().equals(existing.getSynopsis())) {
                existing.setSynopsis(request.getSynopsis());
                updatedFields.add("synopsis");
                changeOtherFieldsNotIsCompleted = true;
            }
        }
        if (request.getCategoryId() != null && request.getCategoryId() > 0) {
            // Validate category exists and get category name
            com.yushan.content_service.entity.Category category = categoryService.getCategoryById(request.getCategoryId());
            if (category == null || !category.getIsActive()) {
                throw new IllegalArgumentException("category not found or inactive");
            }
            
            if (!request.getCategoryId().equals(existing.getCategoryId())) {
                existing.setCategoryId(request.getCategoryId());
                updatedFields.add("categoryId");
                changeOtherFieldsNotIsCompleted = true;
            }
        }
        String oldCoverUrl = null;
        if (request.getCoverImgBase64() != null && !request.getCoverImgBase64().trim().isEmpty()) {
            String newCoverUrl = convertBase64ToUrl(request.getCoverImgBase64());
            if (!newCoverUrl.equals(existing.getCoverImgUrl())) {
                // Store old image URL for deletion after successful database update
                oldCoverUrl = existing.getCoverImgUrl();
                existing.setCoverImgUrl(newCoverUrl);
                updatedFields.add("coverImgUrl");
                changeOtherFieldsNotIsCompleted = true;
            }
        }
        if (request.getIsCompleted() != null) {
            if (!request.getIsCompleted().equals(existing.getIsCompleted())) {
                existing.setIsCompleted(request.getIsCompleted());
                updatedFields.add("isCompleted");
            }
        }
        
        // Status change is only allowed for admin - this should be handled at controller level
        // but we add validation here as well for safety
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            NovelStatus s = NovelStatus.fromName(request.getStatus());
            int newStatus = s.getValue();
            if (newStatus != existing.getStatus()) {
                existing.setStatus(newStatus);
                updatedFields.add("status");
                
                // Set publish time if publishing
                if (s == NovelStatus.PUBLISHED) {
                    existing.setPublishTime(new Date());
                    updatedFields.add("publishTime");
                }
                changeStatus = true;
            }
        }
        
        // New logic: If editing a published or hidden novel and there are changes other than isCompleted, 
        // change status to UNDER_REVIEW
        if ((currentStatus == NovelStatus.PUBLISHED.getValue() || currentStatus == NovelStatus.HIDDEN.getValue()) 
            && changeOtherFieldsNotIsCompleted && !changeStatus) {
            existing.setStatus(NovelStatus.UNDER_REVIEW.getValue());
            updatedFields.add("status");
        }
        
        existing.setUpdateTime(new Date());

        novelMapper.updateByPrimaryKeySelective(existing);
        
        // Invalidate caches since novel was updated
        redisUtil.invalidateNovelCaches(id);
        
        // Cache the updated novel
        redisUtil.cacheNovel(id, existing);
        
        // Publish Kafka event only if there were actual changes
        if (!updatedFields.isEmpty()) {
            kafkaEventProducerService.publishNovelUpdatedEvent(existing, existing.getAuthorId(), 
                updatedFields.toArray(new String[0]));
        }
        
        // Delete old image after successful database update
        if (oldCoverUrl != null && !oldCoverUrl.trim().isEmpty()) {
            fileStorageService.deleteImage(oldCoverUrl);
        }
        
        return toResponse(existing);
    }

    /**
     * Archive novel (soft delete by setting status to ARCHIVED)
     */
    @Transactional
    public NovelDetailResponseDTO archiveNovel(Integer id) {
        Novel existing = novelMapper.selectByPrimaryKey(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Novel not found with id: " + id);
        }
        if (existing.getStatus().equals(NovelStatus.ARCHIVED.getValue())) {
            throw new ResourceNotFoundException("Novel not found with id: " + id);
        }

        // Check if novel can be archived - only DRAFT, PUBLISHED, or HIDDEN novels can be archived
        int currentStatus = existing.getStatus();
        if (currentStatus != NovelStatus.DRAFT.getValue() && 
            currentStatus != NovelStatus.PUBLISHED.getValue() && 
            currentStatus != NovelStatus.HIDDEN.getValue()) {
            throw new IllegalArgumentException("only draft, published, or hidden novels can be archived");
        }

        existing.setStatus(NovelStatus.ARCHIVED.getValue());
        existing.setUpdateTime(new Date());
        novelMapper.updateByPrimaryKeySelective(existing);

        // Invalidate all caches since novel is archived
        redisUtil.invalidateNovelCaches(id);

        return toResponse(existing);
    }

    /**
     * Increment view count for a novel with Redis caching
     */
    @Transactional
    public void incrementViewCount(Integer id, UUID userId, String userAgent, String ipAddress) {
        // Check if novel exists and is not archived
        Novel novel = novelMapper.selectByPrimaryKey(id);
        if (novel == null) {
            throw new ResourceNotFoundException("novel not found");
        }
        if (novel.getStatus().equals(NovelStatus.ARCHIVED.getValue())) {
            throw new ResourceNotFoundException("novel not found");
        }
        
        // Increment view count in Redis cache first (for performance)
        redisUtil.incrementCachedViewCount(id);
        
        // Update database (async or batch update could be implemented here)
        novelMapper.incrementViewCount(id);
        
        // Get updated novel data from database to ensure consistency
        Novel updatedNovel = novelMapper.selectByPrimaryKey(id);
        
        // Cache the updated novel data
        redisUtil.cacheNovel(id, updatedNovel);
        
        // Publish Kafka event
        kafkaEventProducerService.publishNovelViewEvent(novel, userId, userAgent, ipAddress, null);
    }

    /**
     * Get novels with pagination and Redis caching for popular queries
     */
    public PageResponseDTO<NovelDetailResponseDTO> listNovelsWithPagination(NovelSearchRequestDTO request) {
        // Check if this is a popular query (no filters, default sorting)
        boolean isPopularQuery = isPopularQuery(request);
        
        if (isPopularQuery) {
            String cacheKey = generatePopularCacheKey(request);
            @SuppressWarnings("unchecked")
            PageResponseDTO<NovelDetailResponseDTO> cachedResult = redisUtil.getCachedPopularNovels(cacheKey, PageResponseDTO.class);
            if (cachedResult != null) {
                return cachedResult;
            }
        }
        
        // Get from database
        PageResponseDTO<NovelDetailResponseDTO> result = getNovelsWithPagination(request, false);
        
        // Cache popular queries
        if (isPopularQuery) {
            String cacheKey = generatePopularCacheKey(request);
            redisUtil.cachePopularNovels(cacheKey, result);
        }
        
        return result;
    }

    /**
     * Get all novels for admin view (including ARCHIVED novels)
     */
    public PageResponseDTO<NovelDetailResponseDTO> getAllNovelsAdmin(NovelSearchRequestDTO request) {
        return getNovelsWithPagination(request, true);
    }

    /**
     * Common method for getting novels with pagination
     * @param request Search request parameters
     * @param includeArchived Whether to include ARCHIVED novels
     * @return Paginated novels
     */
    private PageResponseDTO<NovelDetailResponseDTO> getNovelsWithPagination(NovelSearchRequestDTO request, boolean includeArchived) {
        // Validate and set defaults
        if (request.getPage() == null || request.getPage() < 0) {
            request.setPage(0);
        }
        if (request.getSize() == null || request.getSize() <= 0) {
            request.setSize(10);
        }
        if (request.getSize() > 100) {
            request.setSize(100);
        }
        if (request.getSort() == null || request.getSort().trim().isEmpty()) {
            request.setSort("createTime");
        }
        if (request.getOrder() == null || (!request.getOrder().equalsIgnoreCase("asc") && !request.getOrder().equalsIgnoreCase("desc"))) {
            request.setOrder("desc");
        }

        // Get novels with pagination
        List<Novel> novels = includeArchived 
            ? novelMapper.selectAllNovelsWithPagination(request)
            : novelMapper.selectNovelsWithPagination(request);
        
        // Get total count
        long totalElements = includeArchived 
            ? novelMapper.countAllNovels(request)
            : novelMapper.countNovels(request);
        
        // Convert to DTOs using batch loading to avoid N+1 problem
        List<NovelDetailResponseDTO> novelDTOs = toResponseList(novels);
        return new PageResponseDTO<>(novelDTOs, totalElements, request.getPage(), request.getSize());
    }

    /**
     * Get novels by author
     */
    public List<NovelDetailResponseDTO> getNovelsByAuthor(UUID authorId) {
        NovelSearchRequestDTO request = new NovelSearchRequestDTO();
        request.setAuthorId(authorId.toString());
        request.setPage(0);
        request.setSize(100); // Get all novels by author
        
        List<Novel> novels = novelMapper.selectNovelsWithPagination(request);
        return toResponseList(novels);
    }

    /**
     * Get novels by category
     */
    public List<NovelDetailResponseDTO> getNovelsByCategory(Integer categoryId) {
        NovelSearchRequestDTO request = new NovelSearchRequestDTO();
        request.setCategoryId(categoryId);
        request.setPage(0);
        request.setSize(100); // Get all novels by category
        
        List<Novel> novels = novelMapper.selectNovelsWithPagination(request);
        return toResponseList(novels);
    }

    /**
     * Get novel count
     */
    public long getNovelCount(NovelSearchRequestDTO request) {
        return novelMapper.countNovels(request);
    }

    /**
     * Submit novel for review (Author only)
     */
    public NovelDetailResponseDTO submitForReview(Integer novelId, UUID userId) {
        Novel novel = novelMapper.selectByPrimaryKey(novelId);
        if (novel == null) {
            throw new ResourceNotFoundException("novel not found");
        }
        
        // Check if user is the author
        if (!novel.getAuthorId().equals(userId)) {
            throw new IllegalArgumentException("only the author can submit for review");
        }
        
        // Check if novel is in DRAFT status
        if (!novel.getStatus().equals(NovelStatus.DRAFT.getValue())) {
            throw new IllegalArgumentException("only draft novels can be submitted for review");
        }
        
        String previousStatus = NovelStatus.DRAFT.toString();
        novel.setStatus(NovelStatus.UNDER_REVIEW.getValue());
        novel.setUpdateTime(new Date());
        novelMapper.updateByPrimaryKeySelective(novel);
        
        // Publish Kafka event
        kafkaEventProducerService.publishNovelStatusChangedEvent(novel, previousStatus, NovelStatus.UNDER_REVIEW.toString(), userId, "Submitted for review");
        
        return toResponse(novel);
    }

    /**
     * Approve novel for publishing (Admin only)
     */
    public NovelDetailResponseDTO approveNovel(Integer novelId) {
        return changeNovelStatus(novelId, NovelStatus.PUBLISHED, NovelStatus.UNDER_REVIEW, 
                "only novels under review can be approved");
    }

    /**
     * Reject novel (Admin only)
     */
    public NovelDetailResponseDTO rejectNovel(Integer novelId) {
        return changeNovelStatus(novelId, NovelStatus.DRAFT, NovelStatus.UNDER_REVIEW, 
                "only novels under review can be rejected");
    }

    /**
     * Hide novel - only published novels can be hidden
     */
    public NovelDetailResponseDTO hideNovel(Integer novelId) {
        return changeNovelStatus(novelId, NovelStatus.HIDDEN, NovelStatus.PUBLISHED, 
                "only published novels can be hidden");
    }

    /**
     * Unhide novel - only hidden novels can be unhidden, will return to published status
     */
    public NovelDetailResponseDTO unhideNovel(Integer novelId) {
        return changeNovelStatus(novelId, NovelStatus.PUBLISHED, NovelStatus.HIDDEN, 
                "only hidden novels can be unhidden");
    }

    /**
     * Get novels under review (Admin only)
     */
    public PageResponseDTO<NovelDetailResponseDTO> getNovelsUnderReview(int page, int size) {
        NovelSearchRequestDTO request = new NovelSearchRequestDTO(page, size, "createTime", "desc", 
                null, "UNDER_REVIEW", null, null, null, null);
        return listNovelsWithPagination(request);
    }

    /**
     * Change novel status (Admin only)
     */
    private NovelDetailResponseDTO changeNovelStatus(Integer novelId, NovelStatus newStatus, NovelStatus requiredCurrentStatus, String errorMessage) {
        Novel novel = novelMapper.selectByPrimaryKey(novelId);
        if (novel == null) {
            throw new ResourceNotFoundException("novel not found");
        }
        
        // Check current status if required
        if (requiredCurrentStatus != null && !novel.getStatus().equals(requiredCurrentStatus.getValue())) {
            throw new IllegalArgumentException(errorMessage);
        }
        
        novel.setStatus(newStatus.getValue());
        novel.setUpdateTime(new Date());
        
        // Set publish time if publishing
        if (newStatus == NovelStatus.PUBLISHED) {
            novel.setPublishTime(new Date());
        }
        
        novelMapper.updateByPrimaryKeySelective(novel);
        
        // Publish Kafka event
        kafkaEventProducerService.publishNovelStatusChangedEvent(novel, 
            requiredCurrentStatus != null ? requiredCurrentStatus.toString() : "UNKNOWN", 
            newStatus.toString(), null, "Status changed by admin");
        
        return toResponse(novel);
    }

    /**
     * Convert Novel entity to NovelDetailResponseDTO
     */
    private NovelDetailResponseDTO toResponse(Novel novel) {
        NovelDetailResponseDTO dto = new NovelDetailResponseDTO();
        dto.setId(novel.getId());
        dto.setUuid(novel.getUuid());
        dto.setTitle(novel.getTitle());
        dto.setAuthorId(novel.getAuthorId());
        dto.setAuthorUsername(novel.getAuthorName());
        dto.setCategoryId(novel.getCategoryId());
        // Get category name from CategoryService with caching
        try {
            com.yushan.content_service.entity.Category category = categoryService.getCategoryById(novel.getCategoryId());
            dto.setCategoryName(category.getName());
        } catch (Exception e) {
            dto.setCategoryName(null); // Fallback if category not found
        }
        dto.setSynopsis(novel.getSynopsis());
        dto.setCoverImgUrl(novel.getCoverImgUrl());
        dto.setStatus(reverseStatus(novel.getStatus()));
        dto.setIsCompleted(novel.getIsCompleted());
        dto.setChapterCnt(novel.getChapterCnt());
        dto.setWordCnt(novel.getWordCnt());
        dto.setAvgRating(novel.getAvgRating());
        dto.setReviewCnt(novel.getReviewCnt());
        dto.setViewCnt(novel.getViewCnt());
        dto.setVoteCnt(novel.getVoteCnt());
        dto.setYuanCnt(novel.getYuanCnt());
        dto.setPublishTime(novel.getPublishTime());
        dto.setCreateTime(novel.getCreateTime());
        dto.setUpdateTime(novel.getUpdateTime());
        return dto;
    }

    /**
     * Convert list of Novel entities to NovelDetailResponseDTO with batch category loading
     * This method optimizes category loading to avoid N+1 query problem
     */
    private List<NovelDetailResponseDTO> toResponseList(List<Novel> novels) {
        if (novels == null || novels.isEmpty()) {
            return new ArrayList<>();
        }

        // Batch load all category IDs
        List<Integer> categoryIds = novels.stream()
                .map(Novel::getCategoryId)
                .distinct()
                .collect(Collectors.toList());

        // Get all categories at once
        Map<Integer, String> categoryMap = categoryService.getCategoryMapByIds(categoryIds);

        // Convert novels to DTOs using the category map
        return novels.stream()
                .map(novel -> {
                    NovelDetailResponseDTO dto = new NovelDetailResponseDTO();
                    dto.setId(novel.getId());
                    dto.setUuid(novel.getUuid());
                    dto.setTitle(novel.getTitle());
                    dto.setAuthorId(novel.getAuthorId());
                    dto.setAuthorUsername(novel.getAuthorName());
                    dto.setCategoryId(novel.getCategoryId());
                    dto.setCategoryName(categoryMap.get(novel.getCategoryId())); // Use cached category name
                    dto.setSynopsis(novel.getSynopsis());
                    dto.setCoverImgUrl(novel.getCoverImgUrl());
                    dto.setStatus(reverseStatus(novel.getStatus()));
                    dto.setIsCompleted(novel.getIsCompleted());
                    dto.setChapterCnt(novel.getChapterCnt());
                    dto.setWordCnt(novel.getWordCnt());
                    dto.setAvgRating(novel.getAvgRating());
                    dto.setReviewCnt(novel.getReviewCnt());
                    dto.setViewCnt(novel.getViewCnt());
                    dto.setVoteCnt(novel.getVoteCnt());
                    dto.setYuanCnt(novel.getYuanCnt());
                    dto.setPublishTime(novel.getPublishTime());
                    dto.setCreateTime(novel.getCreateTime());
                    dto.setUpdateTime(novel.getUpdateTime());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Check if the request is a popular query (no filters, default sorting)
     */
    private boolean isPopularQuery(NovelSearchRequestDTO request) {
        return request.getCategoryId() == null &&
               (request.getStatus() == null || request.getStatus().isEmpty()) &&
               (request.getSearch() == null || request.getSearch().isEmpty()) &&
               (request.getAuthorId() == null || request.getAuthorId().isEmpty()) &&
               request.getIsCompleted() == null &&
               ("createTime".equals(request.getSort()) || request.getSort() == null) &&
               ("desc".equals(request.getOrder()) || request.getOrder() == null);
    }

    /**
     * Generate cache key for popular queries
     */
    private String generatePopularCacheKey(NovelSearchRequestDTO request) {
        return String.format("popular:%d:%d:%s:%s", 
            request.getPage(), 
            request.getSize(), 
            request.getSort() != null ? request.getSort() : "createTime",
            request.getOrder() != null ? request.getOrder() : "desc");
    }

    /**
     * Convert integer status to NovelStatus string
     */
    private String reverseStatus(Integer status) {
        if (status == null) return null;
        try {
            return NovelStatus.fromValue(status).name();
        } catch (IllegalArgumentException e) {
            return NovelStatus.DRAFT.name();
        }
    }

    /**
     * Get novels by IDs (batch operation)
     */
    public List<NovelDetailResponseDTO> getNovelsByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Remove duplicates and limit to reasonable size
        List<Integer> uniqueIds = ids.stream()
            .distinct()
            .limit(100) // Limit to 100 novels per request
            .collect(Collectors.toList());
        
        // Get novels from database
        List<Novel> novels = novelMapper.selectByIds(uniqueIds);
        
        // Convert to response DTOs
        return novels.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Convert Base64 data URL to a regular URL
     */
    private String convertBase64ToUrl(String base64DataUrl) {
        if (base64DataUrl == null || base64DataUrl.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Generate a unique filename
            String fileName = "novel-" + System.currentTimeMillis();
            
            // Upload image and get URL
            return fileStorageService.uploadImage(base64DataUrl, fileName);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert base64 to URL: " + e.getMessage(), e);
        }
    }
}
