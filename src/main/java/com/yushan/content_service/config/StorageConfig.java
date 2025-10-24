package com.yushan.content_service.config;

import com.yushan.content_service.service.FileStorageService;
import com.yushan.content_service.service.LocalFileStorageService;
import com.yushan.content_service.service.S3FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration for file storage service selection
 * Chooses between LocalFileStorageService and S3FileStorageService based on profile
 */
@Configuration
public class StorageConfig {
    
    @Value("${app.storage.type:local}")
    private String storageType;
    
    @Bean
    @Primary
    public FileStorageService fileStorageService(
            LocalFileStorageService localFileStorageService,
            S3FileStorageService s3FileStorageService) {
        
        if ("s3".equalsIgnoreCase(storageType) || "spaces".equalsIgnoreCase(storageType)) {
            return s3FileStorageService;
        } else {
            return localFileStorageService;
        }
    }
}
