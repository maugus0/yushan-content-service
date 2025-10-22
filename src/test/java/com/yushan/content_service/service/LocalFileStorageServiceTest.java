package com.yushan.content_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class LocalFileStorageServiceTest {

    @InjectMocks
    private LocalFileStorageService localFileStorageService;

    @Mock
    private ImageValidationService imageValidationService;

    @Mock
    private ImageProcessingService imageProcessingService;

    private String testBase64Data;

    @BeforeEach
    void setUp() throws IOException {
        // Simple base64 encoded 1x1 pixel PNG
        testBase64Data = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";
        
        // Set paths for testing
        ReflectionTestUtils.setField(localFileStorageService, "uploadPath", System.getProperty("java.io.tmpdir"));
        ReflectionTestUtils.setField(localFileStorageService, "baseUrl", "http://localhost:8082");
        
        // Mock imageValidationService methods
        ImageValidationService.ImageValidationResult validResult = new ImageValidationService.ImageValidationResult();
        validResult.setValid(true);
        lenient().when(imageValidationService.validateImage(anyString())).thenReturn(validResult);
        
        // Mock imageProcessingService methods with correct parameters
        BufferedImage mockResizedImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        byte[] mockCompressedData = new byte[]{1, 2, 3, 4};
        lenient().when(imageProcessingService.resizeImage(any(BufferedImage.class), eq(800), eq(600))).thenReturn(mockResizedImage);
        lenient().when(imageProcessingService.compressImage(any(BufferedImage.class), anyString())).thenReturn(mockCompressedData);
        
        // Mock extractImageData and getImageFormat for all tests
        lenient().when(imageValidationService.extractImageData(anyString())).thenReturn(new byte[]{1, 2, 3, 4});
        lenient().when(imageValidationService.getImageFormat(anyString())).thenReturn("jpg");
    }

    @Test
    void validateImage_WithValidBase64Data_ShouldReturnTrue() {
        // Act
        boolean result = localFileStorageService.validateImage(testBase64Data);

        // Assert
        assertTrue(result);
    }

    @Test
    void validateImage_WithInvalidBase64Data_ShouldReturnFalse() {
        // Arrange
        String invalidBase64Data = "invalid-base64-data";
        ImageValidationService.ImageValidationResult invalidResult = new ImageValidationService.ImageValidationResult();
        invalidResult.setValid(false);
        when(imageValidationService.validateImage(invalidBase64Data)).thenReturn(invalidResult);

        // Act
        boolean result = localFileStorageService.validateImage(invalidBase64Data);

        // Assert
        assertFalse(result);
    }

    @Test
    void validateImage_WithNullBase64Data_ShouldReturnFalse() {
        // Arrange
        ImageValidationService.ImageValidationResult invalidResult = new ImageValidationService.ImageValidationResult();
        invalidResult.setValid(false);
        when(imageValidationService.validateImage(null)).thenReturn(invalidResult);

        // Act
        boolean result = localFileStorageService.validateImage(null);

        // Assert
        assertFalse(result);
    }

    @Test
    void imageExists_WithNullImageUrl_ShouldReturnFalse() {
        // Act
        boolean result = localFileStorageService.imageExists(null);

        // Assert
        assertFalse(result);
    }

    @Test
    void imageExists_WithNonExistingImage_ShouldReturnFalse() {
        // Arrange
        String nonExistingImageUrl = "/non/existing/image.png";

        // Act
        boolean result = localFileStorageService.imageExists(nonExistingImageUrl);

        // Assert
        assertFalse(result);
    }

    @Test
    void deleteImage_WithNullImageUrl_ShouldReturnFalse() {
        // Act
        boolean result = localFileStorageService.deleteImage(null);

        // Assert
        assertFalse(result);
    }

    @Test
    void deleteImage_WithInvalidImageUrl_ShouldReturnFalse() {
        // Arrange
        String invalidImageUrl = "invalid-url";

        // Act
        boolean result = localFileStorageService.deleteImage(invalidImageUrl);

        // Assert
        assertFalse(result);
    }

    @Test
    void getStorageStats_WithExistingDirectory_ShouldReturnCorrectCount() throws IOException {
        // Arrange
        Path coversDir = Paths.get(System.getProperty("java.io.tmpdir"), "covers");
        Files.createDirectories(coversDir);
        
        // Clean up any existing files first
        try (var stream = Files.list(coversDir)) {
            stream.forEach(file -> {
                try {
                    Files.deleteIfExists(file);
                } catch (IOException e) {
                    // Ignore cleanup errors
                }
            });
        }
        
        // Create some test files
        Files.createFile(coversDir.resolve("test1.jpg"));
        Files.createFile(coversDir.resolve("test2.png"));

        // Act
        Map<String, Object> result = localFileStorageService.getStorageStats();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.get("coversCount")).isEqualTo(2L);
        assertThat(result.get("totalFiles")).isEqualTo(2L);
        assertThat(result.get("uploadPath")).isEqualTo(System.getProperty("java.io.tmpdir"));
        assertThat(result.get("baseUrl")).isEqualTo("http://localhost:8082");
        
        // Cleanup
        Files.deleteIfExists(coversDir.resolve("test1.jpg"));
        Files.deleteIfExists(coversDir.resolve("test2.png"));
    }

    @Test
    void uploadImage_WithInvalidData_ShouldThrowException() {
        // Arrange
        String invalidData = "invalid-data";
        ImageValidationService.ImageValidationResult invalidResult = new ImageValidationService.ImageValidationResult();
        invalidResult.setValid(false);
        invalidResult.addError("Invalid format");
        when(imageValidationService.validateImage(invalidData)).thenReturn(invalidResult);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> localFileStorageService.uploadImage(invalidData, "test.jpg"));
        
        assertThat(exception.getMessage()).contains("Invalid image: Invalid format");
    }

    @Test
    void initializeDirectories_ShouldCreateDirectories() {
        // Act
        localFileStorageService.initializeDirectories();

        // Assert - verify directories are created
        Path uploadDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Path coversDir = Paths.get(System.getProperty("java.io.tmpdir"), "covers");
        
        assertThat(Files.exists(uploadDir)).isTrue();
        assertThat(Files.exists(coversDir)).isTrue();
    }

    @Test
    void getStorageStats_WithNonExistingDirectory_ShouldReturnZeroCount() {
        // Arrange - set upload path to non-existing directory
        ReflectionTestUtils.setField(localFileStorageService, "uploadPath", "/non/existing/path");

        // Act
        Map<String, Object> result = localFileStorageService.getStorageStats();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.get("coversCount")).isEqualTo(0L);
        assertThat(result.get("totalFiles")).isEqualTo(0L);
        assertThat(result.get("uploadPath")).isEqualTo("/non/existing/path");
        assertThat(result.get("baseUrl")).isEqualTo("http://localhost:8082");
    }
}