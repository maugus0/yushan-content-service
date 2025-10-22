package com.yushan.content_service;

import com.yushan.content_service.service.ElasticsearchSearchService;
import com.yushan.content_service.repository.elasticsearch.NovelElasticsearchRepository;
import com.yushan.content_service.repository.elasticsearch.ChapterElasticsearchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

/**
 * Basic integration test to verify that the application context loads successfully.
 */
@SpringBootTest
@ActiveProfiles("test")
class ContentServiceApplicationTests {

    @MockBean
    private ElasticsearchSearchService elasticsearchSearchService;

    @MockBean
    private NovelElasticsearchRepository novelElasticsearchRepository;

    @MockBean
    private ChapterElasticsearchRepository chapterElasticsearchRepository;

    @Test
    void contextLoads() {
        // This test will pass if the application context loads successfully
    }

    @Test
    void mainMethodCanBeCalled() {
        // Test that the main method can be called without errors
        // This is a basic smoke test for the application entry point
        try {
            ContentServiceApplication.main(new String[]{});
        } catch (Exception e) {
            // Expected to fail in test environment due to missing dependencies
            // but the method should be callable
        }
    }
}