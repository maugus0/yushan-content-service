package com.yushan.content_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yushan.content_service.TestcontainersConfiguration;
import com.yushan.content_service.dao.ChapterMapper;
import com.yushan.content_service.dao.NovelMapper;
import com.yushan.content_service.dto.chapter.*;
import com.yushan.content_service.entity.Chapter;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Simple integration tests for Chapter management with real PostgreSQL
 */
@SpringBootTest
@ActiveProfiles("integration-test")
@Import(TestcontainersConfiguration.class)
@Transactional
@org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable(named = "CI", matches = "true")
public class ChapterIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ChapterMapper chapterMapper;

    @Autowired
    private NovelMapper novelMapper;

    @MockBean
    private KafkaEventProducerService kafkaEventProducerService;
    
    // Mock Elasticsearch services
    @MockBean
    private com.yushan.content_service.service.ElasticsearchIndexService elasticsearchIndexService;
    @MockBean
    private com.yushan.content_service.service.ElasticsearchSearchService elasticsearchSearchService;
    @MockBean
    private com.yushan.content_service.service.ElasticsearchAutoIndexService elasticsearchAutoIndexService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    @Autowired
    private JwtTestUtil jwtTestUtil;

    private UUID authorId;
    private String authorToken;
    private Novel testNovel;
    private Chapter testChapter1;


    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Configure PostgreSQL - Testcontainers will provide dynamic ports
        registry.add("spring.datasource.url", TestcontainersConfiguration.postgres::getJdbcUrl);
        registry.add("spring.datasource.username", TestcontainersConfiguration.postgres::getUsername);
        registry.add("spring.datasource.password", TestcontainersConfiguration.postgres::getPassword);
        
        // Configure Redis - Testcontainers will provide dynamic ports
        registry.add("spring.data.redis.host", TestcontainersConfiguration.redis::getHost);
        registry.add("spring.data.redis.port", () -> TestcontainersConfiguration.redis.getMappedPort(6379));
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        objectMapper = new ObjectMapper();

        // Setup test users - Use fixed UUID to match JWT token
        authorId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        authorToken = jwtTestUtil.generateTestAuthorToken();

        // Mock Kafka events
        doNothing().when(kafkaEventProducerService).publishChapterCreatedEvent(any(), any(), any());

        // Create test data
        setupTestData();
    }

    private void setupTestData() {
        // Create test novel
        testNovel = new Novel();
        testNovel.setUuid(UUID.randomUUID());
        testNovel.setAuthorId(authorId);
        testNovel.setTitle("Test Novel");
        testNovel.setSynopsis("Test novel description");
        testNovel.setCategoryId(1);
        testNovel.setStatus(1); // ACTIVE
        testNovel.setCreateTime(new Date());
        testNovel.setUpdateTime(new Date());
        novelMapper.insertSelective(testNovel);

        // Create test chapter
        testChapter1 = new Chapter();
        testChapter1.setUuid(UUID.randomUUID());
        testChapter1.setNovelId(testNovel.getId());
        testChapter1.setChapterNumber(1);
        testChapter1.setTitle("Chapter 1");
        testChapter1.setContent("This is the content of chapter 1.");
        testChapter1.setWordCnt(10);
        testChapter1.setIsPremium(false);
        testChapter1.setYuanCost(0.0f);
        testChapter1.setViewCnt(0L);
        testChapter1.setIsValid(true);
        testChapter1.setCreateTime(new Date());
        testChapter1.setUpdateTime(new Date());
        testChapter1.setPublishTime(new Date());
        chapterMapper.insertSelective(testChapter1);
    }

    @Test
    void testGetChapterByUuid_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/chapters/{uuid}", testChapter1.getUuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Chapter retrieved successfully"))
                .andExpect(jsonPath("$.data.title").value("Chapter 1"))
                .andExpect(jsonPath("$.data.content").value("This is the content of chapter 1."))
                .andExpect(jsonPath("$.data.wordCnt").value(10))
                .andExpect(jsonPath("$.data.isPremium").value(false));
    }

    @Test
    void testGetChaptersByNovelId_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/chapters/novel/{novelId}", testNovel.getId())
                .param("page", "1")
                .param("pageSize", "10")
                .param("publishedOnly", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Chapters retrieved successfully"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.currentPage").value(0))
                .andExpect(jsonPath("$.data.size").value(10));
    }

    @Test
    void testGetChapterStatistics_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/chapters/novel/{novelId}/statistics", testNovel.getId())
                .header("Authorization", "Bearer " + authorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Statistics retrieved successfully"))
                .andExpect(jsonPath("$.data.totalChapters").value(1))
                .andExpect(jsonPath("$.data.totalWordCount").value(10))
                .andExpect(jsonPath("$.data.totalViewCount").value(0))
                .andExpect(jsonPath("$.data.totalRevenue").value(0.0))
                .andExpect(jsonPath("$.data.freeChapters").value(1))
                .andExpect(jsonPath("$.data.premiumChapters").value(0));
    }

    @Test
    void testUnauthorizedAccess_Returns401() throws Exception {
        // When & Then - Try to get statistics without token
        mockMvc.perform(get("/api/v1/chapters/novel/{novelId}/statistics", testNovel.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateChapter_Success() throws Exception {
        // Given
        ChapterCreateRequestDTO createRequest = new ChapterCreateRequestDTO();
        createRequest.setNovelId(testNovel.getId());
        createRequest.setChapterNumber(2);
        createRequest.setTitle("Chapter 2");
        createRequest.setContent("This is the content of chapter 2.");
        createRequest.setWordCnt(15);
        createRequest.setIsPremium(false);
        createRequest.setYuanCost(0.0f);

        // When & Then
        mockMvc.perform(post("/api/v1/chapters")
                .header("Authorization", "Bearer " + authorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Chapter created successfully"))
                .andExpect(jsonPath("$.data.title").value("Chapter 2"))
                .andExpect(jsonPath("$.data.content").value("This is the content of chapter 2."))
                .andExpect(jsonPath("$.data.wordCnt").value(15))
                .andExpect(jsonPath("$.data.isPremium").value(false));
    }

    @Test
    void testCreateChapter_InvalidData_Returns400() throws Exception {
        // Given - Invalid data (missing required fields)
        ChapterCreateRequestDTO createRequest = new ChapterCreateRequestDTO();
        createRequest.setNovelId(testNovel.getId());
        // Missing title, content, etc.

        // When & Then
        mockMvc.perform(post("/api/v1/chapters")
                .header("Authorization", "Bearer " + authorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateChapter_Success() throws Exception {
        // Given
        ChapterUpdateRequestDTO updateRequest = new ChapterUpdateRequestDTO();
        updateRequest.setUuid(testChapter1.getUuid());
        updateRequest.setTitle("Updated Chapter 1");
        updateRequest.setContent("This is the updated content of chapter 1.");
        updateRequest.setWordCnt(20);

        // When & Then
        mockMvc.perform(put("/api/v1/chapters")
                .header("Authorization", "Bearer " + authorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Chapter updated successfully"))
                .andExpect(jsonPath("$.data.title").value("Updated Chapter 1"))
                .andExpect(jsonPath("$.data.content").value("This is the updated content of chapter 1."))
                .andExpect(jsonPath("$.data.wordCnt").value(20));
    }

    @Test
    void testDeleteChapter_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/chapters/{uuid}", testChapter1.getUuid())
                .header("Authorization", "Bearer " + authorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Chapter deleted successfully"));
    }

    @Test
    void testGetChapterByNovelIdAndNumber_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/chapters/novel/{novelId}/number/{chapterNumber}", 
                testNovel.getId(), testChapter1.getChapterNumber()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Chapter retrieved successfully"))
                .andExpect(jsonPath("$.data.title").value("Chapter 1"))
                .andExpect(jsonPath("$.data.chapterNumber").value(1));
    }

    @Test
    void testGetChapterByNovelIdAndNumber_NotFound() throws Exception {
        // When & Then - Try to get non-existent chapter
        mockMvc.perform(get("/api/v1/chapters/novel/{novelId}/number/{chapterNumber}", 
                testNovel.getId(), 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSearchChapters_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/chapters/search")
                .param("novelId", testNovel.getId().toString())
                .param("titleKeyword", "Chapter")
                .param("page", "1")
                .param("pageSize", "10")
                .param("sortBy", "chapterNumber")
                .param("sortOrder", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void testIncrementViewCount_Success() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/chapters/{uuid}/view", testChapter1.getUuid())
                .header("User-Agent", "Test Agent")
                .header("X-Forwarded-For", "192.168.1.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("View count incremented"));
    }

    @Test
    void testGetNextChapterUuid_Success() throws Exception {
        // Given - Create a second chapter
        Chapter testChapter2 = new Chapter();
        testChapter2.setUuid(UUID.randomUUID());
        testChapter2.setNovelId(testNovel.getId());
        testChapter2.setChapterNumber(2);
        testChapter2.setTitle("Chapter 2");
        testChapter2.setContent("This is the content of chapter 2.");
        testChapter2.setWordCnt(15);
        testChapter2.setIsPremium(false);
        testChapter2.setYuanCost(0.0f);
        testChapter2.setViewCnt(0L);
        testChapter2.setIsValid(true);
        testChapter2.setCreateTime(new Date());
        testChapter2.setUpdateTime(new Date());
        testChapter2.setPublishTime(new Date());
        chapterMapper.insertSelective(testChapter2);

        // When & Then
        mockMvc.perform(get("/api/v1/chapters/{uuid}/next", testChapter1.getUuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Next chapter retrieved"))
                .andExpect(jsonPath("$.data").value(testChapter2.getUuid().toString()));
    }

    @Test
    void testGetNextChapterUuid_NotFound() throws Exception {
        // When & Then - Try to get next chapter when there's no next chapter
        // This should return 200 with null data, not 404
        mockMvc.perform(get("/api/v1/chapters/{uuid}/next", testChapter1.getUuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Next chapter retrieved"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testGetPreviousChapterUuid_Success() throws Exception {
        // Given - Create a second chapter
        Chapter testChapter2 = new Chapter();
        testChapter2.setUuid(UUID.randomUUID());
        testChapter2.setNovelId(testNovel.getId());
        testChapter2.setChapterNumber(2);
        testChapter2.setTitle("Chapter 2");
        testChapter2.setContent("This is the content of chapter 2.");
        testChapter2.setWordCnt(15);
        testChapter2.setIsPremium(false);
        testChapter2.setYuanCost(0.0f);
        testChapter2.setViewCnt(0L);
        testChapter2.setIsValid(true);
        testChapter2.setCreateTime(new Date());
        testChapter2.setUpdateTime(new Date());
        testChapter2.setPublishTime(new Date());
        chapterMapper.insertSelective(testChapter2);

        // When & Then
        mockMvc.perform(get("/api/v1/chapters/{uuid}/previous", testChapter2.getUuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Previous chapter retrieved"))
                .andExpect(jsonPath("$.data").value(testChapter1.getUuid().toString()));
    }

    @Test
    void testGetPreviousChapterUuid_NotFound() throws Exception {
        // When & Then - Try to get previous chapter when there's no previous chapter
        // This should return 200 with null data, not 404
        mockMvc.perform(get("/api/v1/chapters/{uuid}/previous", testChapter1.getUuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Previous chapter retrieved"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testGetNextAvailableChapterNumber_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/chapters/novel/{novelId}/next-number", testNovel.getId())
                .header("Authorization", "Bearer " + authorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Next chapter number retrieved"))
                .andExpect(jsonPath("$.data").value(2)); // Next available chapter number
    }

    @Test
    void testPublishChapter_Success() throws Exception {
        // Given
        ChapterPublishRequestDTO publishRequest = new ChapterPublishRequestDTO();
        publishRequest.setUuid(testChapter1.getUuid());
        publishRequest.setIsValid(true);
        publishRequest.setPublishTime(new Date());

        // When & Then
        mockMvc.perform(patch("/api/v1/chapters/publish")
                .header("Authorization", "Bearer " + authorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(publishRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("Chapter publish status updated successfully"));
    }

    @Test
    void testBatchPublishChapters_Success() throws Exception {
        // Given - Create another chapter
        Chapter testChapter2 = new Chapter();
        testChapter2.setUuid(UUID.randomUUID());
        testChapter2.setNovelId(testNovel.getId());
        testChapter2.setChapterNumber(2);
        testChapter2.setTitle("Chapter 2");
        testChapter2.setContent("This is the content of chapter 2.");
        testChapter2.setWordCnt(15);
        testChapter2.setIsPremium(false);
        testChapter2.setYuanCost(0.0f);
        testChapter2.setViewCnt(0L);
        testChapter2.setIsValid(false); // Not published yet
        testChapter2.setCreateTime(new Date());
        testChapter2.setUpdateTime(new Date());
        testChapter2.setPublishTime(new Date());
        chapterMapper.insertSelective(testChapter2);

        // When & Then
        mockMvc.perform(patch("/api/v1/chapters/novel/{novelId}/publish", testNovel.getId())
                .header("Authorization", "Bearer " + authorToken)
                .param("isValid", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("Chapters publish status updated successfully"));
    }

    @Test
    void testDeleteAllChapters_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/chapters/novel/{novelId}", testNovel.getId())
                .header("Authorization", "Bearer " + authorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("All chapters deleted successfully"));
    }

    @Test
    void testGetChapterByUuid_NotFound() throws Exception {
        // When & Then - Try to get non-existent chapter
        UUID nonExistentUuid = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/chapters/{uuid}", nonExistentUuid))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetChaptersByNovelId_NotFound() throws Exception {
        // When & Then - Try to get chapters for non-existent novel
        mockMvc.perform(get("/api/v1/chapters/novel/{novelId}", 999)
                .param("page", "1")
                .param("pageSize", "10"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetChapterStatistics_NotFound() throws Exception {
        // When & Then - Try to get statistics for non-existent novel
        mockMvc.perform(get("/api/v1/chapters/novel/{novelId}/statistics", 999)
                .header("Authorization", "Bearer " + authorToken))
                .andExpect(status().isNotFound());
    }
}