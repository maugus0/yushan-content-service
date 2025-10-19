package com.yushan.content_service.config;

import com.yushan.content_service.service.ElasticsearchIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Auto-index Elasticsearch data when application starts.
 * Runs after all beans are initialized and application is ready.
 */
@Component
@ConditionalOnProperty(name = "search.elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class ElasticsearchStartupIndexer {

    @Autowired
    private ElasticsearchIndexService elasticsearchIndexService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            // Index all data
            elasticsearchIndexService.reindexAllData();
        } catch (Exception e) {
            // Don't fail application startup if indexing fails
        }
    }
}
