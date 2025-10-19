package com.yushan.content_service.dao;

import com.yushan.content_service.dto.chapter.ChapterSearchRequestDTO;
import com.yushan.content_service.entity.Chapter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

/**
 * MyBatis Mapper interface for Chapter entity.
 * Provides database operations for chapter management.
 */
@Mapper
public interface ChapterMapper {
    
    // Basic CRUD operations
    int deleteByPrimaryKey(Integer id);
    
    int insert(Chapter record);
    
    int insertSelective(Chapter record);
    
    Chapter selectByPrimaryKey(Integer id);
    
    int updateByPrimaryKeySelective(Chapter record);
    
    int updateByPrimaryKey(Chapter record);
    
    // Additional query methods
    Chapter selectByUuid(@Param("uuid") UUID uuid);
    
    List<Chapter> selectByIds(@Param("ids") List<Integer> ids);
    
    // Novel-specific chapter queries
    List<Chapter> selectByNovelId(@Param("novelId") Integer novelId);
    
    List<Chapter> selectPublishedByNovelId(@Param("novelId") Integer novelId);
    
    List<Chapter> selectByNovelIdWithPagination(@Param("novelId") Integer novelId,
                                                @Param("offset") int offset,
                                                @Param("limit") int limit);
    
    List<Chapter> selectPublishedByNovelIdWithPagination(@Param("novelId") Integer novelId,
                                                         @Param("offset") int offset,
                                                         @Param("limit") int limit);
    
    long countByNovelId(@Param("novelId") Integer novelId);
    
    long countPublishedByNovelId(@Param("novelId") Integer novelId);
    
    // Chapter navigation
    Chapter selectNextChapter(@Param("novelId") Integer novelId,
                              @Param("chapterNumber") Integer chapterNumber);
    
    Chapter selectPreviousChapter(@Param("novelId") Integer novelId,
                                  @Param("chapterNumber") Integer chapterNumber);
    
    // Specific chapter selection
    Chapter selectByNovelIdAndChapterNumber(@Param("novelId") Integer novelId,
                                            @Param("chapterNumber") Integer chapterNumber);
    
    // View count management
    int incrementViewCount(@Param("id") Integer id);
    
    // Chapter existence checks
    boolean existsByNovelIdAndChapterNumber(@Param("novelId") Integer novelId,
                                            @Param("chapterNumber") Integer chapterNumber);
    
    // Get max chapter number for a novel
    Integer selectMaxChapterNumberByNovelId(@Param("novelId") Integer novelId);
    
    // Batch operations
    int batchInsert(@Param("chapters") List<Chapter> chapters);
    
    // Soft delete
    int softDeleteByPrimaryKey(@Param("id") Integer id);
    
    int softDeleteByUuid(@Param("uuid") UUID uuid);
    
    // Author/Admin queries - get drafts
    List<Chapter> selectDraftsByNovelId(@Param("novelId") Integer novelId);
    
    List<Chapter> selectScheduledByNovelId(@Param("novelId") Integer novelId);
    
    // Statistics
    long sumWordCountByNovelId(@Param("novelId") Integer novelId);
    long sumPublishedWordCountByNovelId(@Param("novelId") Integer novelId);
    
    // Bulk status updates
    int updatePublishStatusByIds(@Param("ids") List<Integer> ids,
                                 @Param("isValid") Boolean isValid);
    
    // Dynamic search methods
    List<Chapter> selectChaptersWithSearch(@Param("req") ChapterSearchRequestDTO req);
    
    long countChaptersWithSearch(@Param("req") ChapterSearchRequestDTO req);
}
