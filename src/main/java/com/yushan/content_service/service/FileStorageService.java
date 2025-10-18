package com.yushan.content_service.service;

/**
 * Interface for file storage operations.
 * Provides abstraction for different storage implementations (local, S3, etc.)
 */
public interface FileStorageService {
    
    /**
     * Upload an image from base64 data
     * @param base64Data Base64 encoded image data
     * @param fileName Desired file name
     * @return Public URL to access the uploaded image
     */
    String uploadImage(String base64Data, String fileName);
    
    /**
     * Delete an image by its URL
     * @param imageUrl URL of the image to delete
     * @return true if deletion was successful
     */
    boolean deleteImage(String imageUrl);
    
    /**
     * Validate image data
     * @param base64Data Base64 encoded image data
     * @return true if image is valid
     */
    boolean validateImage(String base64Data);
    
    /**
     * Check if image URL exists
     * @param imageUrl URL to check
     * @return true if image exists
     */
    boolean imageExists(String imageUrl);
}
