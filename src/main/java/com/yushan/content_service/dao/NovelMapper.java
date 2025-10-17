package com.yushan.content_service.dao;

import com.yushan.content_service.dto.novel.NovelSearchRequestDTO;
import com.yushan.content_service.entity.Novel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

/**
 * MyBatis Mapper interface for Novel entity.
 * Provides database operations for novel management.
 */
@Mapper
public interface NovelMapper {
    
    // Basic CRUD operations
    int deleteByPrimaryKey(Integer id);
    
    int insert(Novel record);
    
    int insertSelective(Novel record);
    
    Novel selectByPrimaryKey(Integer id);
    
    int updateByPrimaryKeySelective(Novel record);
    
    int updateByPrimaryKey(Novel record);
    
    // Additional query methods
    Novel selectByUuid(@Param("uuid") UUID uuid);
    
    // Pagination and filtering methods
    List<Novel> selectNovelsWithPagination(@Param("req") NovelSearchRequestDTO req);
    
    long countNovels(@Param("req") NovelSearchRequestDTO req);
    
    // Admin methods (including ARCHIVED novels)
    List<Novel> selectAllNovelsWithPagination(@Param("req") NovelSearchRequestDTO req);
    
    long countAllNovels(@Param("req") NovelSearchRequestDTO req);
    
    // Statistics and counter methods
    int incrementViewCount(@Param("novelId") Integer novelId);
    
    int incrementVoteCount(@Param("novelId") Integer novelId);
    
    int decrementVoteCount(@Param("novelId") Integer novelId);
    
    int updateChapterCount(@Param("novelId") Integer novelId, @Param("chapterCnt") Integer chapterCnt);
    
    int updateWordCount(@Param("novelId") Integer novelId, @Param("wordCnt") Long wordCnt);
    
    int updateRating(@Param("novelId") Integer novelId, @Param("avgRating") Float avgRating, @Param("reviewCnt") Integer reviewCnt);
    
    // Status update methods
    int updateStatus(@Param("novelId") Integer novelId, @Param("status") Integer status);
    
    int updatePublishTime(@Param("novelId") Integer novelId, @Param("publishTime") java.util.Date publishTime);
    
    // Ranking and search methods
    List<Novel> selectNovelsByRanking(@Param("categoryId") Integer categoryId,
                                     @Param("sortType") String sortType,
                                     @Param("offset") int offset,
                                     @Param("limit") int limit);
    
    long countNovelsByRanking(@Param("categoryId") Integer categoryId);
    
    // Batch operations
    List<Novel> selectByIds(@Param("ids") List<Integer> ids);
    
    List<Novel> selectByUuids(@Param("uuids") List<UUID> uuids);
    
    // Statistics for admin dashboard
    List<Novel> selectNovelsUnderReview(@Param("offset") int offset, @Param("limit") int limit);
    
    long countNovelsUnderReview();
}
