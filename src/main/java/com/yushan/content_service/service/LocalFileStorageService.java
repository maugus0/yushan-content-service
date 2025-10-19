package com.yushan.content_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Local file storage implementation for development environment
 * Simple implementation with single image size
 */
@Service
public class LocalFileStorageService implements FileStorageService {
    
    @Autowired
    private ImageValidationService imageValidationService;
    
    @Autowired
    private ImageProcessingService imageProcessingService;
    
    @Value("${app.storage.local.path:uploads}")
    private String uploadPath;
    
    @Value("${app.storage.local.base-url:http://localhost:8082}")
    private String baseUrl;
    
    // Directory structure
    private static final String COVERS_DIR = "covers";
    
    @Override
    public String uploadImage(String base64Data, String fileName) {
        try {
            // Validate image
            ImageValidationService.ImageValidationResult validation = imageValidationService.validateImage(base64Data);
            if (!validation.isValid()) {
                throw new IllegalArgumentException("Invalid image: " + validation.getErrorMessage());
            }
            
            // Extract image data
            byte[] imageData = imageValidationService.extractImageData(base64Data);
            String format = imageValidationService.getImageFormat(base64Data);
            
            // Resize to standard size (800x600)
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageData));
            BufferedImage resizedImage = imageProcessingService.resizeImage(originalImage, 800, 600);
            
            // Compress image
            byte[] compressedData = imageProcessingService.compressImage(resizedImage, format);
            
            // Generate unique filename
            String uniqueFileName = generateUniqueFileName(fileName, format);
            
            // Create directory if not exists
            Path coversDir = Paths.get(uploadPath, COVERS_DIR);
            Files.createDirectories(coversDir);
            
            // Save file
            Path filePath = coversDir.resolve(uniqueFileName);
            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                fos.write(compressedData);
            }
            
            // Return public URL
            return baseUrl + "/" + uploadPath + "/" + COVERS_DIR + "/" + uniqueFileName;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean deleteImage(String imageUrl) {
        try {
            if (imageUrl == null || !imageUrl.startsWith(baseUrl)) {
                return false;
            }
            
            // Extract file path from URL
            String relativePath = imageUrl.substring(baseUrl.length() + 1);
            Path filePath = Paths.get(relativePath);
            
            // Check if file exists
            if (!Files.exists(filePath)) {
                return false;
            }
            
            // Delete file
            Files.delete(filePath);
            return true;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete image: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean validateImage(String base64Data) {
        ImageValidationService.ImageValidationResult result = imageValidationService.validateImage(base64Data);
        return result.isValid();
    }
    
    @Override
    public boolean imageExists(String imageUrl) {
        try {
            if (imageUrl == null || !imageUrl.startsWith(baseUrl)) {
                return false;
            }
            
            String relativePath = imageUrl.substring(baseUrl.length() + 1);
            Path filePath = Paths.get(relativePath);
            
            return Files.exists(filePath);
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Generate unique filename
     */
    private String generateUniqueFileName(String originalFileName, String format) {
        String baseName = getBaseFileName(originalFileName);
        return baseName + "_" + UUID.randomUUID().toString() + "." + format;
    }
    
    /**
     * Extract base filename without extension
     */
    private String getBaseFileName(String fileName) {
        if (fileName == null) {
            return "image";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }
        
        return fileName;
    }
    
    /**
     * Initialize storage directories
     */
    public void initializeDirectories() {
        try {
            Path uploadDir = Paths.get(uploadPath);
            Path coversDir = Paths.get(uploadPath, COVERS_DIR);
            
            Files.createDirectories(uploadDir);
            Files.createDirectories(coversDir);
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize storage directories: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get storage statistics
     */
    public Map<String, Object> getStorageStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            Path coversDir = Paths.get(uploadPath, COVERS_DIR);
            
            long coversCount = 0;
            if (Files.exists(coversDir)) {
                try (var stream = Files.list(coversDir)) {
                    coversCount = stream.count();
                }
            }
            
            stats.put("coversCount", coversCount);
            stats.put("totalFiles", coversCount);
            stats.put("uploadPath", uploadPath);
            stats.put("baseUrl", baseUrl);
            
        } catch (IOException e) {
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }
}
