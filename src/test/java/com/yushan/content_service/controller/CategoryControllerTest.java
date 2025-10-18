package com.yushan.content_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yushan.content_service.config.TestSecurityContextConfig;
import com.yushan.content_service.dto.category.CategoryCreateRequestDTO;
import com.yushan.content_service.dto.category.CategoryResponseDTO;
import com.yushan.content_service.dto.category.CategoryUpdateRequestDTO;
import com.yushan.content_service.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Simple controller tests without Spring context.
 * Tests basic controller functionality with mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
public class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController).build();
        objectMapper = new ObjectMapper();
        TestSecurityContextConfig.setupMockUser();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        TestSecurityContextConfig.clearSecurityContext();
    }

    @Test
    void getAllCategories_ShouldReturnAllCategories() throws Exception {
        // Arrange
        com.yushan.content_service.entity.Category category1 = new com.yushan.content_service.entity.Category();
        category1.setId(1);
        category1.setName("Fantasy");
        category1.setDescription("Fantasy novels with magical elements");
        category1.setSlug("fantasy");
        category1.setIsActive(true);
        category1.setCreateTime(new Date());
        category1.setUpdateTime(new Date());

        com.yushan.content_service.entity.Category category2 = new com.yushan.content_service.entity.Category();
        category2.setId(2);
        category2.setName("Romance");
        category2.setDescription("Romance novels focusing on love stories");
        category2.setSlug("romance");
        category2.setIsActive(true);
        category2.setCreateTime(new Date());
        category2.setUpdateTime(new Date());

        when(categoryService.getAllCategories()).thenReturn(Arrays.asList(category1, category2));

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalCount").value(2))
                .andExpect(jsonPath("$.data.categories[0].name").value("Fantasy"))
                .andExpect(jsonPath("$.data.categories[1].name").value("Romance"));

        verify(categoryService).getAllCategories();
    }

    @Test
    void getActiveCategories_ShouldReturnActiveCategories() throws Exception {
        // Arrange
        com.yushan.content_service.entity.Category category1 = new com.yushan.content_service.entity.Category();
        category1.setId(1);
        category1.setName("Fantasy");
        category1.setIsActive(true);

        com.yushan.content_service.entity.Category category2 = new com.yushan.content_service.entity.Category();
        category2.setId(2);
        category2.setName("Romance");
        category2.setIsActive(true);

        when(categoryService.getActiveCategories()).thenReturn(Arrays.asList(category1, category2));

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories/active")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalCount").value(2))
                .andExpect(jsonPath("$.data.categories[0].name").value("Fantasy"));

        verify(categoryService).getActiveCategories();
    }

    @Test
    void getCategoryById_WithValidId_ShouldReturnCategory() throws Exception {
        // Arrange
        Integer categoryId = 1;
        com.yushan.content_service.entity.Category category = new com.yushan.content_service.entity.Category();
        category.setId(categoryId);
        category.setName("Fantasy");
        category.setDescription("Fantasy novels with magical elements");
        category.setSlug("fantasy");
        category.setIsActive(true);
        category.setCreateTime(new Date());
        category.setUpdateTime(new Date());

        when(categoryService.getCategoryById(categoryId)).thenReturn(category);

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories/{id}", categoryId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(categoryId))
                .andExpect(jsonPath("$.data.name").value("Fantasy"))
                .andExpect(jsonPath("$.data.slug").value("fantasy"));

        verify(categoryService).getCategoryById(categoryId);
    }

    @Test
    void getCategoryBySlug_WithValidSlug_ShouldReturnCategory() throws Exception {
        // Arrange
        String slug = "fantasy";
        com.yushan.content_service.entity.Category category = new com.yushan.content_service.entity.Category();
        category.setId(1);
        category.setName("Fantasy");
        category.setSlug(slug);
        category.setIsActive(true);

        when(categoryService.getCategoryBySlug(slug)).thenReturn(category);

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories/slug/{slug}", slug)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("Fantasy"))
                .andExpect(jsonPath("$.data.slug").value(slug));

        verify(categoryService).getCategoryBySlug(slug);
    }

    @Test
    void getCategoryStatistics_WithValidId_ShouldReturnStatistics() throws Exception {
        // Arrange
        Integer categoryId = 1;
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("category", new CategoryResponseDTO());
        statistics.put("totalNovels", 10);
        statistics.put("activeNovels", 8);

        when(categoryService.getCategoryStatistics(categoryId)).thenReturn(statistics);

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories/{id}/statistics", categoryId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalNovels").value(10))
                .andExpect(jsonPath("$.data.activeNovels").value(8));

        verify(categoryService).getCategoryStatistics(categoryId);
    }

    @Test
    void createCategory_WithValidData_ShouldReturnCreated() throws Exception {
        // Arrange
        CategoryCreateRequestDTO request = new CategoryCreateRequestDTO();
        request.setName("Mystery");
        request.setDescription("Mystery novels with suspense and intrigue");

        com.yushan.content_service.entity.Category category = new com.yushan.content_service.entity.Category();
        category.setId(3);
        category.setName("Mystery");
        category.setDescription("Mystery novels with suspense and intrigue");
        category.setSlug("mystery");
        category.setIsActive(true);
        category.setCreateTime(new Date());
        category.setUpdateTime(new Date());

        when(categoryService.createCategory(eq("Mystery"), eq("Mystery novels with suspense and intrigue"))).thenReturn(category);

        // Act & Assert
        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("Mystery"))
                .andExpect(jsonPath("$.data.slug").value("mystery"))
                .andExpect(jsonPath("$.data.isActive").value(true));

        verify(categoryService).createCategory(eq("Mystery"), eq("Mystery novels with suspense and intrigue"));
    }

    @Test
    void createCategory_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CategoryCreateRequestDTO request = new CategoryCreateRequestDTO();
        // Missing required fields

        // Act & Assert
        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCategory_WithValidData_ShouldReturnUpdatedCategory() throws Exception {
        // Arrange
        Integer categoryId = 1;
        CategoryUpdateRequestDTO request = new CategoryUpdateRequestDTO();
        request.setName("Updated Fantasy");
        request.setDescription("Updated fantasy novels description");
        request.setIsActive(true);

        com.yushan.content_service.entity.Category category = new com.yushan.content_service.entity.Category();
        category.setId(categoryId);
        category.setName("Updated Fantasy");
        category.setDescription("Updated fantasy novels description");
        category.setSlug("updated-fantasy");
        category.setIsActive(true);
        category.setUpdateTime(new Date());

        when(categoryService.updateCategory(eq(categoryId), eq("Updated Fantasy"), eq("Updated fantasy novels description"), eq(true))).thenReturn(category);

        // Act & Assert
        mockMvc.perform(put("/api/v1/categories/{id}", categoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("Updated Fantasy"))
                .andExpect(jsonPath("$.data.isActive").value(true));

        verify(categoryService).updateCategory(eq(categoryId), eq("Updated Fantasy"), eq("Updated fantasy novels description"), eq(true));
    }

    @Test
    void deleteCategory_WithValidId_ShouldReturnSuccess() throws Exception {
        // Arrange
        Integer categoryId = 1;
        when(categoryService.deleteCategory(categoryId)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/categories/{id}", categoryId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Category deactivated successfully"));

        verify(categoryService).deleteCategory(categoryId);
    }

    @Test
    void hardDeleteCategory_WithValidId_ShouldReturnSuccess() throws Exception {
        // Arrange
        Integer categoryId = 1;
        when(categoryService.hardDeleteCategory(categoryId)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/categories/{id}/hard", categoryId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Category permanently deleted"));

        verify(categoryService).hardDeleteCategory(categoryId);
    }
}
