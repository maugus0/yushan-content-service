package com.yushan.content_service.controller;

import com.yushan.content_service.dto.common.ApiResponse;
import com.yushan.content_service.dto.category.*;
import com.yushan.content_service.entity.Category;
import com.yushan.content_service.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Category Management", description = "APIs for managing novel categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * Get all categories (public endpoint)
     * Returns all categories including inactive ones
     */
    @Operation(summary = "[PUBLIC] Get all categories", description = "Retrieves all categories including inactive ones. Used for admin interfaces.")
    @GetMapping
    public ApiResponse<CategoryListResponseDTO> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        CategoryListResponseDTO response = CategoryListResponseDTO.fromEntities(categories);
        return ApiResponse.success("Categories retrieved successfully", response);
    }

    /**
     * Get only active categories (public endpoint)
     * Most commonly used endpoint for frontend dropdowns
     */
    @Operation(summary = "[PUBLIC] Get active categories", description = "Retrieves only active categories. Most commonly used for frontend dropdowns and public interfaces.")
    @GetMapping("/active")
    public ApiResponse<CategoryListResponseDTO> getActiveCategories() {
        List<Category> categories = categoryService.getActiveCategories();
        CategoryListResponseDTO response = CategoryListResponseDTO.fromActiveEntities(categories);
        return ApiResponse.success("Active categories retrieved successfully", response);
    }

    /**
     * Get category by ID (public endpoint)
     */
    @Operation(summary = "[PUBLIC] Get category by ID", description = "Retrieves a specific category by its ID.")
    @GetMapping("/{id}")
    public ApiResponse<CategoryResponseDTO> getCategoryById(@PathVariable Integer id) {
        Category category = categoryService.getCategoryById(id);
        CategoryResponseDTO response = CategoryResponseDTO.fromEntity(category);
        return ApiResponse.success("Category retrieved successfully", response);
    }

    /**
     * Get category by slug (public endpoint)
     * Useful for SEO-friendly URLs like /categories/science-fiction
     */
    @Operation(summary = "[PUBLIC] Get category by slug", description = "Retrieves a specific category by its URL-friendly slug. Useful for SEO-friendly URLs.")
    @GetMapping("/slug/{slug}")
    public ApiResponse<CategoryResponseDTO> getCategoryBySlug(@PathVariable String slug) {
        Category category = categoryService.getCategoryBySlug(slug);
        CategoryResponseDTO response = CategoryResponseDTO.fromEntity(category);
        return ApiResponse.success("Category retrieved successfully", response);
    }

    /**
     * Get category statistics (public endpoint)
     */
    @Operation(summary = "[PUBLIC] Get category statistics", description = "Retrieves statistics for a specific category including novel count, view count, etc.")
    @GetMapping("/{id}/statistics")
    public ApiResponse<Map<String, Object>> getCategoryStatistics(@PathVariable Integer id) {
        Map<String, Object> statistics = categoryService.getCategoryStatistics(id);
        return ApiResponse.success("Category statistics retrieved successfully", statistics);
    }

    /**
     * Create a new category (admin only)
     */
    @Operation(summary = "[ADMIN] Create category", description = "Creates a new category. Requires ADMIN role.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CategoryResponseDTO> createCategory(
            @Valid @RequestBody CategoryCreateRequestDTO request) {
        Category category = categoryService.createCategory(
                request.getName(),
                request.getDescription()
        );
        CategoryResponseDTO response = CategoryResponseDTO.fromEntity(category);
        return ApiResponse.success("Category created successfully", response);
    }

    /**
     * Update an existing category (admin only)
     */
    @Operation(summary = "[ADMIN] Update category", description = "Updates an existing category. Requires ADMIN role.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryResponseDTO> updateCategory(
            @PathVariable Integer id,
            @Valid @RequestBody CategoryUpdateRequestDTO request) {
        Category category = categoryService.updateCategory(
                id,
                request.getName(),
                request.getDescription(),
                request.getIsActive()
        );
        CategoryResponseDTO response = CategoryResponseDTO.fromEntity(category);
        return ApiResponse.success("Category updated successfully", response);
    }

    /**
     * Soft delete a category (admin only)
     * Sets isActive to false instead of removing from database
     */
    @Operation(summary = "[ADMIN] Soft delete category", description = "Soft deletes a category by setting isActive to false. Requires ADMIN role.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> deleteCategory(@PathVariable Integer id) {
        boolean deleted = categoryService.deleteCategory(id);
        if (deleted) {
            return ApiResponse.success("Category deactivated successfully");
        }
        return ApiResponse.error(400, "Failed to deactivate category");
    }

    /**
     * Hard delete a category (admin only)
     * Permanently removes from database - use with caution!
     */
    @Operation(summary = "[ADMIN] Hard delete category", description = "Permanently deletes a category from database. Use with caution! Requires ADMIN role.")
    @DeleteMapping("/{id}/hard")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> hardDeleteCategory(@PathVariable Integer id) {
        boolean deleted = categoryService.hardDeleteCategory(id);
        if (deleted) {
            return ApiResponse.success("Category permanently deleted");
        }
        return ApiResponse.error(400, "Failed to delete category");
    }
}
