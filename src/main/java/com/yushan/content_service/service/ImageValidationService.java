package com.yushan.content_service.service;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Service for validating and processing images
 */
@Service
public class ImageValidationService {
    
    // Supported image formats
    private static final String[] SUPPORTED_FORMATS = {"jpeg", "jpg", "png", "gif", "webp"};
    
    // Image size limits
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_WIDTH = 10000;
    private static final int MAX_HEIGHT = 10000;
    private static final int MIN_WIDTH = 1;
    private static final int MIN_HEIGHT = 1;
    
    // Base64 data URL pattern
    private static final Pattern BASE64_PATTERN = Pattern.compile(
        "^data:image/(jpeg|jpg|png|gif|webp);base64,[A-Za-z0-9+/]+=*$"
    );
    
    /**
     * Validate base64 data URL format
     */
    public boolean isValidBase64DataUrl(String base64DataUrl) {
        if (base64DataUrl == null || base64DataUrl.trim().isEmpty()) {
            return false;
        }
        return BASE64_PATTERN.matcher(base64DataUrl).matches();
    }
    
    /**
     * Extract image data from base64 data URL
     */
    public byte[] extractImageData(String base64DataUrl) {
        if (!isValidBase64DataUrl(base64DataUrl)) {
            throw new IllegalArgumentException("Invalid base64 data URL format");
        }
        
        // Extract base64 part after comma
        String base64Data = base64DataUrl.substring(base64DataUrl.indexOf(",") + 1);
        return Base64.getDecoder().decode(base64Data);
    }
    
    /**
     * Get image format from base64 data URL
     */
    public String getImageFormat(String base64DataUrl) {
        if (!isValidBase64DataUrl(base64DataUrl)) {
            throw new IllegalArgumentException("Invalid base64 data URL format");
        }
        
        // Extract format from data URL
        String format = base64DataUrl.substring(base64DataUrl.indexOf("/") + 1, base64DataUrl.indexOf(";"));
        return format.toLowerCase();
    }
    
    /**
     * Validate image file size
     */
    public boolean isValidFileSize(byte[] imageData) {
        return imageData.length <= MAX_FILE_SIZE;
    }
    
    /**
     * Validate image dimensions
     */
    public boolean isValidDimensions(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        return width >= MIN_WIDTH && width <= MAX_WIDTH &&
               height >= MIN_HEIGHT && height <= MAX_HEIGHT;
    }
    
    /**
     * Check if image format is supported
     */
    public boolean isSupportedFormat(String format) {
        for (String supportedFormat : SUPPORTED_FORMATS) {
            if (supportedFormat.equalsIgnoreCase(format)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Comprehensive image validation
     */
    public ImageValidationResult validateImage(String base64DataUrl) {
        ImageValidationResult result = new ImageValidationResult();
        
        try {
            // Validate base64 format
            if (!isValidBase64DataUrl(base64DataUrl)) {
                result.addError("Invalid base64 data URL format");
                return result;
            }
            
            // Extract image data
            byte[] imageData = extractImageData(base64DataUrl);
            
            // Validate file size
            if (!isValidFileSize(imageData)) {
                result.addError("Image file size exceeds maximum limit of " + (MAX_FILE_SIZE / 1024 / 1024) + "MB");
                return result;
            }
            
            // Get image format
            String format = getImageFormat(base64DataUrl);
            if (!isSupportedFormat(format)) {
                result.addError("Unsupported image format: " + format + ". Supported formats: " + String.join(", ", SUPPORTED_FORMATS));
                return result;
            }
            
            // Validate image dimensions
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
            if (image == null) {
                result.addError("Unable to read image data");
                return result;
            }
            
            if (!isValidDimensions(image)) {
                result.addError("Invalid image dimensions. Must be between " + MIN_WIDTH + "x" + MIN_HEIGHT + 
                              " and " + MAX_WIDTH + "x" + MAX_HEIGHT);
                return result;
            }
            
            // Set metadata
            result.setValid(true);
            result.setWidth(image.getWidth());
            result.setHeight(image.getHeight());
            result.setFormat(format);
            result.setSize(imageData.length);
            
        } catch (Exception e) {
            result.addError("Image validation failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Get image metadata
     */
    public Map<String, Object> getImageMetadata(String base64DataUrl) {
        Map<String, Object> metadata = new HashMap<>();
        
        try {
            byte[] imageData = extractImageData(base64DataUrl);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
            
            if (image != null) {
                metadata.put("width", image.getWidth());
                metadata.put("height", image.getHeight());
                metadata.put("format", getImageFormat(base64DataUrl));
                metadata.put("size", imageData.length);
                metadata.put("aspectRatio", (double) image.getWidth() / image.getHeight());
            }
        } catch (Exception e) {
            metadata.put("error", e.getMessage());
        }
        
        return metadata;
    }
    
    /**
     * Image validation result class
     */
    public static class ImageValidationResult {
        private boolean valid = false;
        private int width;
        private int height;
        private String format;
        private long size;
        private java.util.List<String> errors = new java.util.ArrayList<>();
        
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public int getWidth() { return width; }
        public void setWidth(int width) { this.width = width; }
        
        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }
        
        public String getFormat() { return format; }
        public void setFormat(String format) { this.format = format; }
        
        public long getSize() { return size; }
        public void setSize(long size) { this.size = size; }
        
        public java.util.List<String> getErrors() { 
            return new java.util.ArrayList<>(errors); 
        }
        public void addError(String error) { this.errors.add(error); }
        
        public String getErrorMessage() {
            return String.join("; ", errors);
        }
    }
}
