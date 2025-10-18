package com.yushan.content_service.dao;

import com.yushan.content_service.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper {
    
    // Basic CRUD operations
    int deleteByPrimaryKey(Integer id);
    
    int insert(Category record);
    
    int insertSelective(Category record);
    
    Category selectByPrimaryKey(Integer id);
    
    int updateByPrimaryKeySelective(Category record);
    
    int updateByPrimaryKey(Category record);
    
    // Additional query methods
    List<Category> selectAll();
    
    List<Category> selectActiveCategories();
    
    Category selectBySlug(@Param("slug") String slug);
    
    List<Category> selectByIds(@Param("ids") List<Integer> ids);
    
    // Validation methods
    Category selectByName(@Param("name") String name);
    
    Category selectByNameExcludingId(@Param("name") String name, @Param("excludeId") Integer excludeId);
    
    Category selectBySlugExcludingId(@Param("slug") String slug, @Param("excludeId") Integer excludeId);
    
    // Statistics methods
    int countNovelsByCategory(@Param("categoryId") Integer categoryId);
    
    int countActiveNovelsByCategory(@Param("categoryId") Integer categoryId);
}
