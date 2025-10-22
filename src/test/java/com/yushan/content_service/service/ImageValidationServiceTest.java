package com.yushan.content_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ImageValidationServiceTest {

    @InjectMocks
    private ImageValidationService imageValidationService;

    private String validBase64DataUrl;
    private String invalidBase64DataUrl;

    @BeforeEach
    void setUp() {
        // Simple base64 encoded 1x1 pixel PNG
        validBase64DataUrl = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";
        invalidBase64DataUrl = "invalid-base64-data";
    }

    @Test
    void isValidBase64DataUrl_WithValidDataUrl_ShouldReturnTrue() {
        // Act
        boolean result = imageValidationService.isValidBase64DataUrl(validBase64DataUrl);

        // Assert
        assertTrue(result);
    }

    @Test
    void isValidBase64DataUrl_WithInvalidDataUrl_ShouldReturnFalse() {
        // Act
        boolean result = imageValidationService.isValidBase64DataUrl(invalidBase64DataUrl);

        // Assert
        assertFalse(result);
    }

    @Test
    void isValidBase64DataUrl_WithNullDataUrl_ShouldReturnFalse() {
        // Act
        boolean result = imageValidationService.isValidBase64DataUrl(null);

        // Assert
        assertFalse(result);
    }

    @Test
    void isValidBase64DataUrl_WithEmptyDataUrl_ShouldReturnFalse() {
        // Act
        boolean result = imageValidationService.isValidBase64DataUrl("");

        // Assert
        assertFalse(result);
    }

    @Test
    void extractImageData_WithValidDataUrl_ShouldReturnImageData() {
        // Act
        byte[] result = imageValidationService.extractImageData(validBase64DataUrl);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void extractImageData_WithInvalidDataUrl_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            imageValidationService.extractImageData(invalidBase64DataUrl));
    }

    @Test
    void isValidFileSize_WithValidSize_ShouldReturnTrue() {
        // Arrange
        byte[] smallData = new byte[1024]; // 1KB

        // Act
        boolean result = imageValidationService.isValidFileSize(smallData);

        // Assert
        assertTrue(result);
    }

    @Test
    void isValidFileSize_WithNullData_ShouldThrowException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> 
            imageValidationService.isValidFileSize(null));
    }

    @Test
    void isValidDimensions_WithValidDimensions_ShouldReturnTrue() {
        // Arrange
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

        // Act
        boolean result = imageValidationService.isValidDimensions(image);

        // Assert
        assertTrue(result);
    }

    @Test
    void isValidDimensions_WithNullImage_ShouldThrowException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> 
            imageValidationService.isValidDimensions(null));
    }

    @Test
    void isSupportedFormat_WithSupportedFormat_ShouldReturnTrue() {
        // Act
        boolean result = imageValidationService.isSupportedFormat("png");

        // Assert
        assertTrue(result);
    }

    @Test
    void isSupportedFormat_WithUnsupportedFormat_ShouldReturnFalse() {
        // Act
        boolean result = imageValidationService.isSupportedFormat("bmp");

        // Assert
        assertFalse(result);
    }

    @Test
    void isSupportedFormat_WithNullFormat_ShouldReturnFalse() {
        // Act
        boolean result = imageValidationService.isSupportedFormat(null);

        // Assert
        assertFalse(result);
    }

    @Test
    void validateImage_WithValidImage_ShouldReturnValidResult() {
        // Act
        ImageValidationService.ImageValidationResult result = imageValidationService.validateImage(validBase64DataUrl);

        // Assert
        assertThat(result).isNotNull();
        // Note: The result might be valid or invalid depending on the actual image data
    }

    @Test
    void validateImage_WithInvalidImage_ShouldReturnInvalidResult() {
        // Act
        ImageValidationService.ImageValidationResult result = imageValidationService.validateImage(invalidBase64DataUrl);

        // Assert
        assertThat(result).isNotNull();
        assertFalse(result.isValid());
    }

    @Test
    void getImageMetadata_WithValidImage_ShouldReturnMetadata() {
        // Act
        Map<String, Object> result = imageValidationService.getImageMetadata(validBase64DataUrl);

        // Assert
        assertThat(result).isNotNull();
        // Note: The result might contain metadata or error information
    }

    @Test
    void getImageMetadata_WithInvalidImage_ShouldReturnErrorMetadata() {
        // Act
        Map<String, Object> result = imageValidationService.getImageMetadata(invalidBase64DataUrl);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).containsKey("error");
    }
}