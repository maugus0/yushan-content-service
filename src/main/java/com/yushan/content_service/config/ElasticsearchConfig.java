package com.yushan.content_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Elasticsearch configuration for the Content Service.
 * Configures Elasticsearch client and enables repositories.
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.yushan.content_service.repository.elasticsearch")
@ConditionalOnProperty(name = "search.elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.rest.uris:http://localhost:9200}")
    private String elasticsearchUrl;

    @Value("${spring.elasticsearch.rest.connection-timeout:5}")
    private int connectionTimeoutSeconds;

    @Value("${spring.elasticsearch.rest.read-timeout:30}")
    private int readTimeoutSeconds;

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(elasticsearchUrl.replace("http://", ""))
                .withConnectTimeout(java.time.Duration.ofSeconds(connectionTimeoutSeconds))
                .withSocketTimeout(java.time.Duration.ofSeconds(readTimeoutSeconds))
                .build();
    }

    @Override
    public boolean writeTypeHints() {
        return false;
    }
}
