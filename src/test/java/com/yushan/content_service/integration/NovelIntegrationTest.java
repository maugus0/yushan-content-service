package com.yushan.content_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yushan.content_service.TestcontainersConfiguration;
import com.yushan.content_service.dao.NovelMapper;
import com.yushan.content_service.dto.novel.NovelSearchRequestDTO;
import com.yushan.content_service.entity.Novel;
import com.yushan.content_service.service.KafkaEventProducerService;
import com.yushan.content_service.util.JwtTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Novel management with real PostgreSQL
 * 
 * This test class verifies:
 * - Novel CRUD operations with database persistence
 * - Author permissions and access control
 * - Novel search and filtering with database queries
 * - Novel status updates and publishing flow
 * - Database transactions and data integrity
 */
@SpringBootTest
@ActiveProfiles("integration-test")
@Import(TestcontainersConfiguration.class)
@Transactional
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=",
    "spring.kafka.enabled=false",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
@org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable(named = "CI", matches = "true")
public class NovelIntegrationTest {

    @MockBean
    private KafkaEventProducerService kafkaEventProducerService;
    
    // Mock KafkaTemplate to prevent actual Kafka connection
    @MockBean
    private org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate;
    
    // Mock Elasticsearch services
    @MockBean
    private com.yushan.content_service.service.ElasticsearchIndexService elasticsearchIndexService;
    @MockBean
    private com.yushan.content_service.service.ElasticsearchSearchService elasticsearchSearchService;
    @MockBean
    private com.yushan.content_service.service.ElasticsearchAutoIndexService elasticsearchAutoIndexService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private NovelMapper novelMapper;

    @Autowired
    private JwtTestUtil jwtTestUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private String authorToken;
    private String adminToken;
    private UUID testAuthorId;

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
        // Mock Kafka events to prevent connection issues
        doNothing().when(kafkaEventProducerService).publishNovelCreatedEvent(any(), any());
        doNothing().when(kafkaEventProducerService).publishNovelUpdatedEvent(any(), any(), any());
        doNothing().when(kafkaEventProducerService).publishNovelStatusChangedEvent(any(), any(), any(), any(), any());
        doNothing().when(kafkaEventProducerService).publishNovelViewEvent(any(), any(), any(), any(), any());
        
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .apply(springSecurity())
            .build();

        testAuthorId = UUID.randomUUID();
        authorToken = generateTestToken(testAuthorId, "AUTHOR");
        adminToken = generateTestToken(UUID.randomUUID(), "ADMIN");
    }

    @Test
    void createNovel_WithValidData_ShouldPersistToDatabase() throws Exception {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("title", "Integration Test Novel");
        request.put("categoryId", 1);
        request.put("isCompleted", false);

        // Act
        mockMvc.perform(post("/api/v1/novels")
                .header("Authorization", "Bearer " + authorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("Integration Test Novel"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));

        // Assert - Verify data was persisted by searching for the specific novel
        NovelSearchRequestDTO searchRequest = new NovelSearchRequestDTO();
        searchRequest.setPage(0);
        searchRequest.setSize(100); // Get more results to find our novel
        searchRequest.setSearch("Integration Test Novel"); // Search by title
        List<Novel> novels = novelMapper.selectNovelsWithPagination(searchRequest);
        assertThat(novels).hasSize(1);
        assertThat(novels.get(0).getTitle()).isEqualTo("Integration Test Novel");
        assertThat(novels.get(0).getAuthorId()).isEqualTo(UUID.fromString("550e8400-e29b-41d4-a716-446655440001"));
        assertThat(novels.get(0).getStatus()).isEqualTo(0); // DRAFT
    }

    @Test
    void getNovels_ShouldReturnFromDatabase() throws Exception {
        // Arrange - Insert test data directly
        Novel novel1 = createTestNovel("Test Novel 1");
        Novel novel2 = createTestNovel("Test Novel 2");
        novelMapper.insertSelective(novel1);
        novelMapper.insertSelective(novel2);

        // Act & Assert - Search for our specific test novels
        mockMvc.perform(get("/api/v1/novels")
                .param("page", "0")
                .param("size", "100")
                .param("search", "Test Novel")) // Search for our test novels
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content.length()").value(2));
    }

    @Test
    void updateNovel_ShouldUpdateInDatabase() throws Exception {
        // Arrange
        Novel novel = createTestNovel("Original Title");
        // Use the same userId as in the JWT token
        novel.setAuthorId(UUID.fromString("550e8400-e29b-41d4-a716-446655440001"));
        novelMapper.insertSelective(novel);

        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("title", "Updated Title");
        updateRequest.put("categoryId", 2);

        // Act
        mockMvc.perform(put("/api/v1/novels/{id}", novel.getId())
                .header("Authorization", "Bearer " + authorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("Updated Title"));

        // Assert - Verify update in database
        Novel updatedNovel = novelMapper.selectByPrimaryKey(novel.getId());
        assertThat(updatedNovel.getTitle()).isEqualTo("Updated Title");
        assertThat(updatedNovel.getCategoryId()).isEqualTo(2);
    }

    @Test
    void submitForReview_ShouldUpdateStatusInDatabase() throws Exception {
        // Arrange
        Novel novel = createTestNovel("Test Novel");
        novel.setAuthorId(UUID.fromString("550e8400-e29b-41d4-a716-446655440001"));
        novelMapper.insertSelective(novel);

        // Act
        mockMvc.perform(post("/api/v1/novels/{id}/submit-review", novel.getId())
                .header("Authorization", "Bearer " + authorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("UNDER_REVIEW"));

        // Assert - Verify status update in database
        Novel updatedNovel = novelMapper.selectByPrimaryKey(novel.getId());
        assertThat(updatedNovel.getStatus()).isEqualTo(1); // UNDER_REVIEW
    }

    @Test
    void approveNovel_ShouldUpdateStatusInDatabase() throws Exception {
        // Arrange
        Novel novel = createTestNovel("Test Novel");
        novel.setStatus(1); // UNDER_REVIEW
        novelMapper.insertSelective(novel);

        // Act
        mockMvc.perform(post("/api/v1/novels/{id}/approve", novel.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));

        // Assert - Verify status update in database
        Novel updatedNovel = novelMapper.selectByPrimaryKey(novel.getId());
        assertThat(updatedNovel.getStatus()).isEqualTo(2); // PUBLISHED
    }

    @Test
    void getAllNovelsAdmin_ShouldReturnAllNovelsIncludingArchived() throws Exception {
        // Arrange - Insert novels with different statuses
        Novel publishedNovel = createTestNovel("Admin Test Published Novel");
        publishedNovel.setStatus(2); // PUBLISHED
        novelMapper.insertSelective(publishedNovel);

        Novel archivedNovel = createTestNovel("Admin Test Archived Novel");
        archivedNovel.setStatus(4); // ARCHIVED
        novelMapper.insertSelective(archivedNovel);

        // Act & Assert - Search for our specific test novels
        mockMvc.perform(get("/api/v1/novels/admin/all")
                .header("Authorization", "Bearer " + adminToken)
                .param("page", "0")
                .param("size", "100")
                .param("search", "Admin Test")) // Search for our test novels
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content.length()").value(2));
    }

    @Test
    void incrementViewCount_ShouldIncrementInDatabase() throws Exception {
        // Arrange
        Novel novel = createTestNovel("Test Novel");
        novel.setViewCnt(10L);
        novelMapper.insertSelective(novel);

        // Act
        mockMvc.perform(post("/api/v1/novels/{id}/view", novel.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // Assert - Verify view count increment in database
        Novel updatedNovel = novelMapper.selectByPrimaryKey(novel.getId());
        assertThat(updatedNovel.getViewCnt()).isEqualTo(11L);
    }

    @Test
    void searchNovels_WithFilters_ShouldReturnFilteredResults() throws Exception {
        // Arrange
        Novel fantasyNovel = createTestNovel("Search Test Fantasy Novel");
        fantasyNovel.setCategoryId(1); // Fantasy
        fantasyNovel.setStatus(2); // PUBLISHED
        novelMapper.insertSelective(fantasyNovel);

        Novel romanceNovel = createTestNovel("Search Test Romance Novel");
        romanceNovel.setCategoryId(2); // Romance
        romanceNovel.setStatus(2); // PUBLISHED
        novelMapper.insertSelective(romanceNovel);

        // Act & Assert - Search by category and search filter
        mockMvc.perform(get("/api/v1/novels")
                .param("category", "1")  // Use correct parameter name
                .param("status", "PUBLISHED")
                .param("search", "Search Test") // Filter by our test novels
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalElements").value(1)) // Only fantasy novel matches categoryId=1
                .andExpect(jsonPath("$.data.content[0].title").value("Search Test Fantasy Novel"));
    }

    private Novel createTestNovel(String title) {
        Novel novel = new Novel();
        novel.setUuid(UUID.randomUUID());
        novel.setTitle(title);
        novel.setAuthorId(testAuthorId);
        novel.setAuthorName("test-author");
        novel.setCategoryId(1);
        novel.setSynopsis("Test synopsis");
        novel.setStatus(0); // DRAFT
        novel.setIsCompleted(false);
        novel.setChapterCnt(0);
        novel.setWordCnt(0L);
        novel.setAvgRating(0.0f);
        novel.setReviewCnt(0);
        novel.setViewCnt(0L);
        novel.setVoteCnt(0);
        novel.setYuanCnt(0.0f);
        novel.setCreateTime(new Date());
        novel.setUpdateTime(new Date());
        return novel;
    }

    private String generateTestToken(UUID userId, String role) {
        if ("ADMIN".equals(role)) {
            return jwtTestUtil.generateTestAdminToken();
        } else {
            return jwtTestUtil.generateTestAuthorToken();
        }
    }
}
