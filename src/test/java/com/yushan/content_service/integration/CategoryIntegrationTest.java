package com.yushan.content_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yushan.content_service.TestcontainersConfiguration;
import com.yushan.content_service.dao.CategoryMapper;
import com.yushan.content_service.dto.category.CategoryCreateRequestDTO;
import com.yushan.content_service.dto.category.CategoryUpdateRequestDTO;
import com.yushan.content_service.entity.Category;
import com.yushan.content_service.util.JwtTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("integration-test")
@Import(TestcontainersConfiguration.class)
@Transactional
@org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable(named = "CI", matches = "true")
public class CategoryIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private JwtTestUtil jwtTestUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private String adminToken;
    private String authorToken;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Configure PostgreSQL
        registry.add("spring.datasource.url", TestcontainersConfiguration.postgres::getJdbcUrl);
        registry.add("spring.datasource.username", TestcontainersConfiguration.postgres::getUsername);
        registry.add("spring.datasource.password", TestcontainersConfiguration.postgres::getPassword);
        
        // Configure Redis
        registry.add("spring.data.redis.host", TestcontainersConfiguration.redis::getHost);
        registry.add("spring.data.redis.port", () -> TestcontainersConfiguration.redis.getMappedPort(6379));
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        adminToken = generateTestToken(UUID.randomUUID(), "ADMIN");
        authorToken = generateTestToken(UUID.randomUUID(), "AUTHOR");
    }

    private String generateTestToken(UUID userId, String role) {
        if ("ADMIN".equals(role)) {
            return jwtTestUtil.generateTestAdminToken();
        } else if ("AUTHOR".equals(role)) {
            return jwtTestUtil.generateTestAuthorToken();
        }
        return jwtTestUtil.generateTestAuthorToken();
    }

    @Test
    void getAllCategories_ShouldReturnAllCategories() throws Exception {
        // Given - Seed data is already in V1 migration
        List<Category> categories = categoryMapper.selectAll();
        assertThat(categories).isNotEmpty();

        // When & Then
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Categories retrieved successfully"))
                .andExpect(jsonPath("$.data.categories").isArray())
                .andExpect(jsonPath("$.data.totalCount").value(categories.size()));
    }

    @Test
    void getActiveCategories_ShouldReturnOnlyActiveCategories() throws Exception {
        // Given - Seed data is already in V1 migration
        List<Category> activeCategories = categoryMapper.selectActiveCategories();
        assertThat(activeCategories).isNotEmpty();

        // When & Then
        mockMvc.perform(get("/api/v1/categories/active"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Active categories retrieved successfully"))
                .andExpect(jsonPath("$.data.categories").isArray())
                .andExpect(jsonPath("$.data.totalCount").value(activeCategories.size()));
    }

    @Test
    void getCategoryById_ShouldReturnCategory() throws Exception {
        // Given
        Category category = categoryMapper.selectAll().get(0);
        assertThat(category).isNotNull();

        // When & Then
        mockMvc.perform(get("/api/v1/categories/{id}", category.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Category retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(category.getId()))
                .andExpect(jsonPath("$.data.name").value(category.getName()))
                .andExpect(jsonPath("$.data.slug").value(category.getSlug()));
    }

    @Test
    void getCategoryById_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/categories/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCategoryBySlug_ShouldReturnCategory() throws Exception {
        // Given
        Category category = categoryMapper.selectAll().get(0);
        assertThat(category).isNotNull();

        // When & Then
        mockMvc.perform(get("/api/v1/categories/slug/{slug}", category.getSlug()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Category retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(category.getId()))
                .andExpect(jsonPath("$.data.name").value(category.getName()))
                .andExpect(jsonPath("$.data.slug").value(category.getSlug()));
    }

    @Test
    void getCategoryBySlug_ShouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/categories/slug/{slug}", "nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCategoryStatistics_ShouldReturnStatistics() throws Exception {
        // Given
        Category category = categoryMapper.selectAll().get(0);
        assertThat(category).isNotNull();

        // When & Then
        mockMvc.perform(get("/api/v1/categories/{id}/statistics", category.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Category statistics retrieved successfully"))
                .andExpect(jsonPath("$.data.category.id").value(category.getId()))
                .andExpect(jsonPath("$.data.category.name").value(category.getName()))
                .andExpect(jsonPath("$.data.totalNovels").isNumber())
                .andExpect(jsonPath("$.data.activeNovels").isNumber());
    }

    @Test
    void createCategory_ShouldCreateCategoryWithAdminToken() throws Exception {
        // Given
        CategoryCreateRequestDTO request = new CategoryCreateRequestDTO();
        request.setName("Test Category");
        request.setDescription("Test category description");

        // When & Then
        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Category created successfully"))
                .andExpect(jsonPath("$.data.name").value("Test Category"))
                .andExpect(jsonPath("$.data.description").value("Test category description"))
                .andExpect(jsonPath("$.data.slug").value("test-category"))
                .andExpect(jsonPath("$.data.isActive").value(true));
    }

    @Test
    void createCategory_ShouldReturnUnauthorizedWithoutToken() throws Exception {
        // Given
        CategoryCreateRequestDTO request = new CategoryCreateRequestDTO();
        request.setName("Test Category");
        request.setDescription("Test category description");

        // When & Then
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createCategory_ShouldReturnForbiddenWithAuthorToken() throws Exception {
        // Given
        CategoryCreateRequestDTO request = new CategoryCreateRequestDTO();
        request.setName("Test Category");
        request.setDescription("Test category description");

        // When & Then
        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + authorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createCategory_ShouldReturnBadRequestWithInvalidData() throws Exception {
        // Given
        CategoryCreateRequestDTO request = new CategoryCreateRequestDTO();
        request.setName(""); // Empty name
        request.setDescription("Test description");

        // When & Then
        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCategory_ShouldUpdateCategoryWithAdminToken() throws Exception {
        // Given
        Category existingCategory = categoryMapper.selectAll().get(0);
        CategoryUpdateRequestDTO request = new CategoryUpdateRequestDTO();
        request.setName("Updated Category");
        request.setDescription("Updated description");
        request.setIsActive(false);

        // When & Then
        mockMvc.perform(put("/api/v1/categories/{id}", existingCategory.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Category updated successfully"))
                .andExpect(jsonPath("$.data.name").value("Updated Category"))
                .andExpect(jsonPath("$.data.description").value("Updated description"))
                .andExpect(jsonPath("$.data.isActive").value(false));
    }

    @Test
    void updateCategory_ShouldReturnUnauthorizedWithoutToken() throws Exception {
        // Given
        Category existingCategory = categoryMapper.selectAll().get(0);
        CategoryUpdateRequestDTO request = new CategoryUpdateRequestDTO();
        request.setName("Updated Category");
        request.setDescription("Updated description");

        // When & Then
        mockMvc.perform(put("/api/v1/categories/{id}", existingCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateCategory_ShouldReturnNotFoundForNonExistentCategory() throws Exception {
        // Given
        CategoryUpdateRequestDTO request = new CategoryUpdateRequestDTO();
        request.setName("Updated Category");
        request.setDescription("Updated description");

        // When & Then
        mockMvc.perform(put("/api/v1/categories/{id}", 999)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCategory_ShouldSoftDeleteCategoryWithAdminToken() throws Exception {
        // Given - Create a new category without novels for testing delete
        CategoryCreateRequestDTO createRequest = new CategoryCreateRequestDTO();
        createRequest.setName("Temp Category for Delete");
        createRequest.setDescription("Temporary category for testing delete");

        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        Category categoryToDelete = categoryMapper.selectBySlug("temp-category-for-delete");
        assertThat(categoryToDelete).isNotNull();
        assertThat(categoryMapper.countActiveNovelsByCategory(categoryToDelete.getId())).isEqualTo(0);

        // When & Then
        mockMvc.perform(delete("/api/v1/categories/{id}", categoryToDelete.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Category deactivated successfully"));

        // Verify it's soft deleted in the database
        Category deletedCategory = categoryMapper.selectByPrimaryKey(categoryToDelete.getId());
        assertThat(deletedCategory).isNotNull();
        assertThat(deletedCategory.getIsActive()).isFalse();
    }

    @Test
    void deleteCategory_ShouldReturnUnauthorizedWithoutToken() throws Exception {
        // Given
        Category existingCategory = categoryMapper.selectAll().get(0);

        // When & Then
        mockMvc.perform(delete("/api/v1/categories/{id}", existingCategory.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteCategory_ShouldReturnNotFoundForNonExistentCategory() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/categories/{id}", 999)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void hardDeleteCategory_ShouldPermanentlyDeleteCategoryWithAdminToken() throws Exception {
        // Given - Create a new category without novels for testing hard delete
        CategoryCreateRequestDTO createRequest = new CategoryCreateRequestDTO();
        createRequest.setName("Temp Category for Hard Delete");
        createRequest.setDescription("Temporary category for testing hard delete");

        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        Category categoryToHardDelete = categoryMapper.selectBySlug("temp-category-for-hard-delete");
        assertThat(categoryToHardDelete).isNotNull();
        assertThat(categoryMapper.countNovelsByCategory(categoryToHardDelete.getId())).isEqualTo(0);

        // When & Then
        mockMvc.perform(delete("/api/v1/categories/{id}/hard", categoryToHardDelete.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Category permanently deleted"));

        // Verify it's deleted from the database
        Category deletedCategory = categoryMapper.selectByPrimaryKey(categoryToHardDelete.getId());
        assertThat(deletedCategory).isNull();
    }

    @Test
    void hardDeleteCategory_ShouldReturnUnauthorizedWithoutToken() throws Exception {
        // Given
        Category existingCategory = categoryMapper.selectAll().get(0);

        // When & Then
        mockMvc.perform(delete("/api/v1/categories/{id}/hard", existingCategory.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void hardDeleteCategory_ShouldReturnNotFoundForNonExistentCategory() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/categories/{id}/hard", 999)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCategory_ShouldReturnConflictWhenNameExists() throws Exception {
        // Given
        Category existingCategory = categoryMapper.selectAll().get(0);
        CategoryCreateRequestDTO request = new CategoryCreateRequestDTO();
        request.setName(existingCategory.getName()); // Use existing name
        request.setDescription("Test description");

        // When & Then
        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateCategory_ShouldReturnConflictWhenNameExists() throws Exception {
        // Given
        List<Category> categories = categoryMapper.selectAll();
        Category category1 = categories.get(0);
        Category category2 = categories.get(1);

        CategoryUpdateRequestDTO request = new CategoryUpdateRequestDTO();
        request.setName(category2.getName()); // Use name from another category
        request.setDescription("Updated description");

        // When & Then
        mockMvc.perform(put("/api/v1/categories/{id}", category1.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}
