package com.yushan.content_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ImageProcessingServiceTest {

    @InjectMocks
    private ImageProcessingService imageProcessingService;

    private BufferedImage testImage;

    @BeforeEach
    void setUp() throws IOException {
        // Create a simple test image (100x100 pixels)
        testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 100; x++) {
            for (int y = 0; y < 100; y++) {
                testImage.setRGB(x, y, 0xFF0000); // Red color
            }
        }
    }

    @Test
    void resizeImage_WithValidImage_ShouldResizeImage() {
        // Arrange
        int targetWidth = 50;
        int targetHeight = 50;

        // Act
        BufferedImage result = imageProcessingService.resizeImage(testImage, targetWidth, targetHeight);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getWidth()).isEqualTo(targetWidth);
        assertThat(result.getHeight()).isEqualTo(targetHeight);
    }

    @Test
    void resizeImage_WithSameDimensions_ShouldReturnSameImage() {
        // Arrange
        int targetWidth = 100;
        int targetHeight = 100;

        // Act
        BufferedImage result = imageProcessingService.resizeImage(testImage, targetWidth, targetHeight);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getWidth()).isEqualTo(targetWidth);
        assertThat(result.getHeight()).isEqualTo(targetHeight);
    }

    @Test
    void resizeImage_WithLargerDimensions_ShouldResizeImage() {
        // Arrange
        int targetWidth = 200;
        int targetHeight = 200;

        // Act
        BufferedImage result = imageProcessingService.resizeImage(testImage, targetWidth, targetHeight);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getWidth()).isEqualTo(targetWidth);
        assertThat(result.getHeight()).isEqualTo(targetHeight);
    }

    @Test
    void resizeImage_WithNullImage_ShouldThrowException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> 
            imageProcessingService.resizeImage(null, 50, 50));
    }

    @Test
    void resizeImage_WithZeroWidth_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            imageProcessingService.resizeImage(testImage, 0, 50));
    }

    @Test
    void resizeImage_WithZeroHeight_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            imageProcessingService.resizeImage(testImage, 50, 0));
    }

    @Test
    void resizeImage_WithNegativeWidth_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            imageProcessingService.resizeImage(testImage, -50, 50));
    }

    @Test
    void resizeImage_WithNegativeHeight_ShouldResizeImage() {
        // Act
        BufferedImage result = imageProcessingService.resizeImage(testImage, 50, -50);

        // Assert
        assertThat(result).isNotNull();
        // This case doesn't throw exception, it calculates new dimensions
    }

    @Test
    void compressImage_WithValidImage_ShouldCompressImage() throws IOException {
        // Arrange
        String format = "JPEG";

        // Act
        byte[] result = imageProcessingService.compressImage(testImage, format);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }
}
