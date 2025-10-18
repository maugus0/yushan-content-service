package com.yushan.content_service.service;

import com.yushan.content_service.dao.CategoryMapper;
import com.yushan.content_service.entity.Category;
import com.yushan.content_service.util.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private RedisUtil redisUtil;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;
    private List<Category> testCategories;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1);
        testCategory.setName("Fantasy");
        testCategory.setDescription("Fantasy novels with magical elements");
        testCategory.setSlug("fantasy");
        testCategory.setIsActive(true);
        testCategory.setCreateTime(new Date());
        testCategory.setUpdateTime(new Date());

        Category category2 = new Category();
        category2.setId(2);
        category2.setName("Romance");
        category2.setDescription("Romance novels");
        category2.setSlug("romance");
        category2.setIsActive(true);
        category2.setCreateTime(new Date());
        category2.setUpdateTime(new Date());

        Category category3 = new Category();
        category3.setId(3);
        category3.setName("Mystery");
        category3.setDescription("Mystery novels");
        category3.setSlug("mystery");
        category3.setIsActive(false);
        category3.setCreateTime(new Date());
        category3.setUpdateTime(new Date());

        testCategories = Arrays.asList(testCategory, category2, category3);
    }

    @Test
    void getAllCategories_ShouldReturnAllCategories() {
        // Given
        when(categoryMapper.selectAll()).thenReturn(testCategories);

        // When
        List<Category> result = categoryService.getAllCategories();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactlyInAnyOrderElementsOf(testCategories);
        verify(categoryMapper).selectAll();
    }

    @Test
    void getAllCategories_ShouldReturnCachedCategories() {
        // Given
        when(redisUtil.getCachedCategories("all", List.class)).thenReturn(testCategories);

        // When
        List<Category> result = categoryService.getAllCategories();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactlyInAnyOrderElementsOf(testCategories);
        verify(redisUtil).getCachedCategories("all", List.class);
        verify(categoryMapper, never()).selectAll();
    }

    @Test
    void getActiveCategories_ShouldReturnOnlyActiveCategories() {
        // Given
        List<Category> activeCategories = Arrays.asList(testCategories.get(0), testCategories.get(1));
        when(categoryMapper.selectActiveCategories()).thenReturn(activeCategories);

        // When
        List<Category> result = categoryService.getActiveCategories();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(Category::getIsActive);
        verify(categoryMapper).selectActiveCategories();
    }

    @Test
    void getActiveCategories_ShouldReturnCachedActiveCategories() {
        // Given
        List<Category> activeCategories = Arrays.asList(testCategories.get(0), testCategories.get(1));
        when(redisUtil.getCachedCategories("active", List.class)).thenReturn(activeCategories);

        // When
        List<Category> result = categoryService.getActiveCategories();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(Category::getIsActive);
        verify(redisUtil).getCachedCategories("active", List.class);
        verify(categoryMapper, never()).selectActiveCategories();
    }

    @Test
    void getCategoryById_ShouldReturnCategory() {
        // Given
        when(categoryMapper.selectByPrimaryKey(1)).thenReturn(testCategory);

        // When
        Category result = categoryService.getCategoryById(1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("Fantasy");
        verify(categoryMapper).selectByPrimaryKey(1);
    }

    @Test
    void getCategoryById_ShouldReturnCachedCategory() {
        // Given
        when(redisUtil.getCachedCategory(1, Category.class)).thenReturn(testCategory);

        // When
        Category result = categoryService.getCategoryById(1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        verify(redisUtil).getCachedCategory(1, Category.class);
        verify(categoryMapper, never()).selectByPrimaryKey(1);
    }

    @Test
    void getCategoryById_ShouldThrowExceptionWhenNotFound() {
        // Given
        when(categoryMapper.selectByPrimaryKey(999)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> categoryService.getCategoryById(999))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    void getCategoryBySlug_ShouldReturnCategory() {
        // Given
        when(categoryMapper.selectBySlug("fantasy")).thenReturn(testCategory);

        // When
        Category result = categoryService.getCategoryBySlug("fantasy");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSlug()).isEqualTo("fantasy");
        verify(categoryMapper).selectBySlug("fantasy");
    }

    @Test
    void getCategoryBySlug_ShouldReturnCachedCategory() {
        // Given
        when(redisUtil.getCachedCategoryBySlug("fantasy", Category.class)).thenReturn(testCategory);

        // When
        Category result = categoryService.getCategoryBySlug("fantasy");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSlug()).isEqualTo("fantasy");
        verify(redisUtil).getCachedCategoryBySlug("fantasy", Category.class);
        verify(categoryMapper, never()).selectBySlug("fantasy");
    }

    @Test
    void getCategoryBySlug_ShouldThrowExceptionWhenNotFound() {
        // Given
        when(categoryMapper.selectBySlug("nonexistent")).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> categoryService.getCategoryBySlug("nonexistent"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    void createCategory_ShouldCreateAndReturnCategory() {
        // Given
        Category newCategory = new Category();
        newCategory.setName("Sci-Fi");
        newCategory.setDescription("Science fiction novels");
        newCategory.setSlug("sci-fi");
        newCategory.setIsActive(true);

        when(categoryMapper.selectByName("Sci-Fi")).thenReturn(null);
        when(categoryMapper.insertSelective(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(4); // Simulate auto-generated ID
            return 1;
        });

        // When
        Category result = categoryService.createCategory("Sci-Fi", "Science fiction novels");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Sci-Fi");
        assertThat(result.getDescription()).isEqualTo("Science fiction novels");
        assertThat(result.getSlug()).isEqualTo("sci-fi");
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getId()).isNotNull();

        verify(categoryMapper).selectByName("Sci-Fi");
        verify(categoryMapper).insertSelective(any(Category.class));
        verify(redisUtil).invalidateCategoryCaches();
    }

    @Test
    void createCategory_ShouldThrowExceptionWhenNameExists() {
        // Given
        when(categoryMapper.selectByName("Fantasy")).thenReturn(testCategory);

        // When & Then
        assertThatThrownBy(() -> categoryService.createCategory("Fantasy", "Description"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category with name 'Fantasy' already exists");
    }

    @Test
    void updateCategory_ShouldUpdateAndReturnCategory() {
        // Given
        Category existingCategory = new Category();
        existingCategory.setId(1);
        existingCategory.setName("Fantasy");
        existingCategory.setDescription("Old description");
        existingCategory.setSlug("fantasy");
        existingCategory.setIsActive(true);

        when(categoryMapper.selectByPrimaryKey(1)).thenReturn(existingCategory);
        when(categoryMapper.updateByPrimaryKeySelective(any(Category.class))).thenReturn(1);

        // When
        Category result = categoryService.updateCategory(1, "Fantasy", "New description", true);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Fantasy");
        assertThat(result.getDescription()).isEqualTo("New description");
        assertThat(result.getIsActive()).isTrue();

        verify(categoryMapper).selectByPrimaryKey(1);
        verify(categoryMapper).updateByPrimaryKeySelective(any(Category.class));
        verify(redisUtil).invalidateCategoryCaches();
    }

    @Test
    void updateCategory_ShouldThrowExceptionWhenCategoryNotFound() {
        // Given
        when(categoryMapper.selectByPrimaryKey(999)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> categoryService.updateCategory(999, "Name", "Description", true))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    void updateCategory_ShouldThrowExceptionWhenNameExists() {
        // Given
        Category existingCategory = new Category();
        existingCategory.setId(1);
        existingCategory.setName("Fantasy");

        Category anotherCategory = new Category();
        anotherCategory.setId(2);
        anotherCategory.setName("Romance");

        when(categoryMapper.selectByPrimaryKey(1)).thenReturn(existingCategory);
        when(categoryMapper.selectByNameExcludingId("Romance", 1)).thenReturn(anotherCategory);

        // When & Then
        assertThatThrownBy(() -> categoryService.updateCategory(1, "Romance", "Description", true))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category with name 'Romance' already exists");
    }

    @Test
    void deleteCategory_ShouldSoftDeleteCategory() {
        // Given
        when(categoryMapper.selectByPrimaryKey(1)).thenReturn(testCategory);
        when(categoryMapper.countActiveNovelsByCategory(1)).thenReturn(0);
        when(categoryMapper.updateByPrimaryKeySelective(any(Category.class))).thenReturn(1);

        // When
        boolean result = categoryService.deleteCategory(1);

        // Then
        assertThat(result).isTrue();
        verify(categoryMapper).selectByPrimaryKey(1);
        verify(categoryMapper).countActiveNovelsByCategory(1);
        verify(categoryMapper).updateByPrimaryKeySelective(any(Category.class));
        verify(redisUtil).invalidateCategoryCaches();
    }

    @Test
    void deleteCategory_ShouldThrowExceptionWhenCategoryNotFound() {
        // Given
        when(categoryMapper.selectByPrimaryKey(999)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> categoryService.deleteCategory(999))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    void hardDeleteCategory_ShouldPermanentlyDeleteCategory() {
        // Given
        when(categoryMapper.selectByPrimaryKey(1)).thenReturn(testCategory);
        when(categoryMapper.deleteByPrimaryKey(1)).thenReturn(1);

        // When
        boolean result = categoryService.hardDeleteCategory(1);

        // Then
        assertThat(result).isTrue();
        verify(categoryMapper).selectByPrimaryKey(1);
        verify(categoryMapper).deleteByPrimaryKey(1);
        verify(redisUtil).invalidateCategoryCaches();
    }

    @Test
    void hardDeleteCategory_ShouldThrowExceptionWhenCategoryNotFound() {
        // Given
        when(categoryMapper.selectByPrimaryKey(999)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> categoryService.hardDeleteCategory(999))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    void getCategoryStatistics_ShouldReturnStatistics() {
        // Given
        when(categoryMapper.selectByPrimaryKey(1)).thenReturn(testCategory);
        when(categoryMapper.countNovelsByCategory(1)).thenReturn(8);
        when(categoryMapper.countActiveNovelsByCategory(1)).thenReturn(5);

        // When
        Map<String, Object> result = categoryService.getCategoryStatistics(1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get("category")).isNotNull();
        assertThat(result.get("totalNovels")).isEqualTo(8);
        assertThat(result.get("activeNovels")).isEqualTo(5);

        verify(categoryMapper).selectByPrimaryKey(1);
        verify(categoryMapper).countNovelsByCategory(1);
        verify(categoryMapper).countActiveNovelsByCategory(1);
    }

    @Test
    void getCategoryStatistics_ShouldThrowExceptionWhenCategoryNotFound() {
        // Given
        when(categoryMapper.selectByPrimaryKey(999)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> categoryService.getCategoryStatistics(999))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    void getCategoryMapByIds_ShouldReturnMapOfCategories() {
        // Given
        List<Integer> categoryIds = Arrays.asList(1, 2);
        List<Category> categories = Arrays.asList(testCategories.get(0), testCategories.get(1));
        when(categoryMapper.selectByIds(categoryIds)).thenReturn(categories);

        // When
        Map<Integer, String> result = categoryService.getCategoryMapByIds(categoryIds);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(1)).isNotNull();
        assertThat(result.get(2)).isNotNull();
        assertThat(result.get(1)).isEqualTo("Fantasy");
        assertThat(result.get(2)).isEqualTo("Romance");

        verify(categoryMapper).selectByIds(categoryIds);
    }

    @Test
    void getCategoryMapByIds_ShouldReturnEmptyMapWhenNoCategories() {
        // Given
        List<Integer> categoryIds = Arrays.asList(999, 998);
        when(categoryMapper.selectByIds(categoryIds)).thenReturn(Collections.emptyList());

        // When
        Map<Integer, String> result = categoryService.getCategoryMapByIds(categoryIds);

        // Then
        assertThat(result).isEmpty();
        verify(categoryMapper).selectByIds(categoryIds);
    }

    @Test
    void generateSlug_ShouldGenerateValidSlug() {
        // Test cases for slug generation - using reflection to test private method
        try {
            java.lang.reflect.Method method = CategoryService.class.getDeclaredMethod("generateSlug", String.class);
            method.setAccessible(true);
            
            assertThat(method.invoke(categoryService, "Science Fiction")).isEqualTo("science-fiction");
            assertThat(method.invoke(categoryService, "Fantasy & Magic")).isEqualTo("fantasy-magic");
            assertThat(method.invoke(categoryService, "Romance")).isEqualTo("romance");
            assertThat(method.invoke(categoryService, "Mystery/Thriller")).isEqualTo("mystery-thriller");
            assertThat(method.invoke(categoryService, "Sci-Fi")).isEqualTo("sci-fi");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void generateSlug_ShouldHandleSpecialCharacters() {
        try {
            java.lang.reflect.Method method = CategoryService.class.getDeclaredMethod("generateSlug", String.class);
            method.setAccessible(true);
            
            assertThat(method.invoke(categoryService, "Action & Adventure")).isEqualTo("action-adventure");
            assertThat(method.invoke(categoryService, "Horror/Thriller")).isEqualTo("horror-thriller");
            assertThat(method.invoke(categoryService, "Drama & Romance")).isEqualTo("drama-romance");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
