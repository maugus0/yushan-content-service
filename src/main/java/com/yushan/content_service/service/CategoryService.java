package com.yushan.content_service.service;

import com.yushan.content_service.dao.CategoryMapper;
import com.yushan.content_service.entity.Category;
import com.yushan.content_service.exception.ResourceNotFoundException;
import com.yushan.content_service.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * Get all categories (including inactive ones)
     */
    public List<Category> getAllCategories() {
        // Try to get from cache first
        @SuppressWarnings("unchecked")
        List<Category> cached = redisUtil.getCachedCategories("all", List.class);
        if (cached != null) {
            return cached;
        }

        List<Category> categories = categoryMapper.selectAll();
        
        // Cache the result
        redisUtil.cacheCategories("all", categories);
        
        return categories;
    }

    /**
     * Get only active categories
     */
    public List<Category> getActiveCategories() {
        // Try to get from cache first
        @SuppressWarnings("unchecked")
        List<Category> cached = redisUtil.getCachedCategories("active", List.class);
        if (cached != null) {
            return cached;
        }

        List<Category> categories = categoryMapper.selectActiveCategories();
        
        // Cache the result
        redisUtil.cacheCategories("active", categories);
        
        return categories;
    }

    /**
     * Get category by ID
     * Throws ResourceNotFoundException if not found
     */
    public Category getCategoryById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Category ID cannot be null");
        }

        // Try to get from cache first
        Category cached = redisUtil.getCachedCategory(id, Category.class);
        if (cached != null) {
            return cached;
        }

        Category category = categoryMapper.selectByPrimaryKey(id);
        if (category == null) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }

        // Cache the result
        redisUtil.cacheCategory(id, category);

        return category;
    }

    /**
     * Get category by slug
     * Throws ResourceNotFoundException if not found
     */
    public Category getCategoryBySlug(String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            throw new IllegalArgumentException("Category slug cannot be empty");
        }

        // Try to get from cache first
        Category cached = redisUtil.getCachedCategoryBySlug(slug, Category.class);
        if (cached != null) {
            return cached;
        }

        Category category = categoryMapper.selectBySlug(slug);
        if (category == null) {
            throw new ResourceNotFoundException("Category not found with slug: " + slug);
        }

        // Cache the result
        redisUtil.cacheCategoryBySlug(slug, category);

        return category;
    }

    /**
     * Create a new category
     * Automatically generates slug from name
     */
    @Transactional
    public Category createCategory(String name, String description) {
        // Validate name
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }

        String trimmedName = name.trim();

        // Check if name already exists (case-insensitive)
        if (categoryNameExists(trimmedName)) {
            throw new IllegalArgumentException("Category with name '" + trimmedName + "' already exists");
        }

        // Generate slug from name
        String slug = generateSlug(trimmedName);

        // Ensure slug uniqueness
        if (categorySlugExists(slug)) {
            slug = generateUniqueSlug(slug);
        }

        // Create category
        Category category = new Category();
        category.setName(trimmedName);
        category.setDescription(description != null ? description.trim() : null);
        category.setSlug(slug);
        category.setIsActive(true);
        Date now = new Date();
        category.setCreateTime(now);
        category.setUpdateTime(now);

        categoryMapper.insertSelective(category);

        // Clear cache after creation
        redisUtil.invalidateCategoryCaches();

        return category;
    }

    /**
     * Update an existing category
     * Only updates fields that are provided (not null)
     */
    @Transactional
    public Category updateCategory(Integer id, String name, String description, Boolean isActive) {
        Category existing = categoryMapper.selectByPrimaryKey(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }

        boolean hasChanges = false;

        // Update name if provided
        if (name != null && !name.trim().isEmpty()) {
            String trimmedName = name.trim();
            if (!trimmedName.equals(existing.getName())) {
                // Check if new name already exists
                if (categoryNameExists(trimmedName, id)) {
                    throw new IllegalArgumentException("Category with name '" + trimmedName + "' already exists");
                }
                existing.setName(trimmedName);

                // Regenerate slug based on new name
                String newSlug = generateSlug(trimmedName);
                if (categorySlugExists(newSlug, id)) {
                    newSlug = generateUniqueSlug(newSlug);
                }
                existing.setSlug(newSlug);
                hasChanges = true;
            }
        }

        // Update description if provided
        if (description != null) {
            String trimmedDescription = description.trim();
            if (!trimmedDescription.equals(existing.getDescription())) {
                existing.setDescription(trimmedDescription);
                hasChanges = true;
            }
        }

        // Update active status if provided
        if (isActive != null && !isActive.equals(existing.getIsActive())) {
            existing.setIsActive(isActive);
            hasChanges = true;
        }

        // Only update if there are changes
        if (hasChanges) {
            existing.setUpdateTime(new Date());
            categoryMapper.updateByPrimaryKeySelective(existing);
            
            // Clear cache after update
            redisUtil.invalidateCategoryCaches();
        }

        return existing;
    }

    /**
     * Soft delete - set isActive to false
     */
    @Transactional
    public boolean deleteCategory(Integer id) {
        Category existing = categoryMapper.selectByPrimaryKey(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }

        // Check if category has novels
        int novelCount = categoryMapper.countActiveNovelsByCategory(id);
        if (novelCount > 0) {
            throw new IllegalArgumentException("Cannot delete category with active novels. Please reassign novels first.");
        }

        existing.setIsActive(false);
        existing.setUpdateTime(new Date());
        int result = categoryMapper.updateByPrimaryKeySelective(existing);
        
        // Clear cache after deletion
        redisUtil.invalidateCategoryCaches();
        
        return result > 0;
    }

    /**
     * Hard delete - permanently remove from database
     * Use with caution!
     */
    @Transactional
    public boolean hardDeleteCategory(Integer id) {
        Category existing = categoryMapper.selectByPrimaryKey(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }

        // Check if category has novels
        int novelCount = categoryMapper.countNovelsByCategory(id);
        if (novelCount > 0) {
            throw new IllegalArgumentException("Cannot delete category with novels. Please reassign novels first.");
        }

        int result = categoryMapper.deleteByPrimaryKey(id);
        
        // Clear cache after deletion
        redisUtil.invalidateCategoryCaches();
        
        return result > 0;
    }

    /**
     * Batch fetches categories by IDs and returns them as a map.
     * @param ids List of category IDs.
     * @return A map where the key is the category ID and the value is the category name.
     */
    public Map<Integer, String> getCategoryMapByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Category> categories = categoryMapper.selectByIds(ids);
        return categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
    }

    /**
     * Get category statistics
     */
    public Map<String, Object> getCategoryStatistics(Integer categoryId) {
        Category category = getCategoryById(categoryId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("category", category);
        stats.put("totalNovels", categoryMapper.countNovelsByCategory(categoryId));
        stats.put("activeNovels", categoryMapper.countActiveNovelsByCategory(categoryId));
        
        return stats;
    }

    /**
     * Check if category name already exists (case-insensitive)
     */
    private boolean categoryNameExists(String name) {
        return categoryNameExists(name, null);
    }

    private boolean categoryNameExists(String name, Integer excludeId) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        Category existing;
        if (excludeId != null) {
            existing = categoryMapper.selectByNameExcludingId(name.trim(), excludeId);
        } else {
            existing = categoryMapper.selectByName(name.trim());
        }
        
        return existing != null;
    }

    /**
     * Check if slug already exists
     */
    private boolean categorySlugExists(String slug) {
        return categorySlugExists(slug, null);
    }

    private boolean categorySlugExists(String slug, Integer excludeId) {
        if (slug == null || slug.trim().isEmpty()) {
            return false;
        }
        
        Category existing;
        if (excludeId != null) {
            existing = categoryMapper.selectBySlugExcludingId(slug.trim(), excludeId);
        } else {
            existing = categoryMapper.selectBySlug(slug.trim());
        }
        
        return existing != null;
    }

    /**
     * Generate URL-friendly slug from input string
     * Example: "Science Fiction" -> "science-fiction"
     */
    private String generateSlug(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        // Normalize string (remove accents)
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String withoutAccents = pattern.matcher(normalized).replaceAll("");

        // Convert to lowercase and replace spaces/special chars with hyphens
        String slug = withoutAccents.toLowerCase(Locale.ENGLISH)
                .replaceAll("[^a-z0-9]+", "-");  // Replace non-alphanumeric with hyphen
        
        // ReDoS-safe cleanup using String methods instead of regex
        // Remove leading hyphens
        while (slug.startsWith("-")) {
            slug = slug.substring(1);
        }
        // Remove trailing hyphens  
        while (slug.endsWith("-")) {
            slug = slug.substring(0, slug.length() - 1);
        }
        // Replace multiple consecutive hyphens with single hyphen
        while (slug.contains("--")) {
            slug = slug.replace("--", "-");
        }

        return slug;
    }

    /**
     * Generate unique slug by appending counter
     * Example: "science-fiction" -> "science-fiction-1"
     */
    private String generateUniqueSlug(String baseSlug) {
        String uniqueSlug = baseSlug;
        int counter = 1;

        while (categorySlugExists(uniqueSlug)) {
            uniqueSlug = baseSlug + "-" + counter;
            counter++;
        }

        return uniqueSlug;
    }

}
