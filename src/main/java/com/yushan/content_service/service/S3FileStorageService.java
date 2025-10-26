package com.yushan.content_service.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.UUID;

/**
 * S3-compatible file storage implementation for DigitalOcean Spaces
 * Handles image uploads to DigitalOcean Spaces with proper URL generation
 */
@Service
public class S3FileStorageService implements FileStorageService {
    
    @Autowired
    private ImageValidationService imageValidationService;
    
    @Autowired
    private ImageProcessingService imageProcessingService;
    
    @Value("${app.storage.s3.access-key:test-access-key}")
    private String accessKeyId;
    
    @Value("${app.storage.s3.secret-key:test-secret-key}")
    private String secretAccessKey;
    
    @Value("${app.storage.s3.endpoint:https://test.endpoint.com}")
    private String endpoint;
    
    @Value("${app.storage.s3.bucket-name:test-bucket}")
    private String bucketName;
    
    @Value("${app.storage.s3.region:us-east-1}")
    private String region;
    
    private AmazonS3 s3Client;
    
    // Directory structure
    private static final String COVERS_DIR = "covers";
    
    private AmazonS3 getS3Client() {
        if (s3Client == null) {
            BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKeyId, secretAccessKey);
            
            s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                    .build();
        }
        return s3Client;
    }
    
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
            String s3Key = COVERS_DIR + "/" + uniqueFileName;
            
            // Upload to S3/Spaces
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(compressedData.length);
            metadata.setContentType("image/" + format.toLowerCase());
            metadata.setCacheControl("public, max-age=31536000"); // 1 year cache
            
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketName, 
                    s3Key, 
                    new ByteArrayInputStream(compressedData), 
                    metadata
            );
            
            getS3Client().putObject(putObjectRequest);
            
            // Return public URL - use CDN URL for DigitalOcean Spaces
            // Remove trailing slash from endpoint if exists
            String cleanEndpoint = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
            
            // Check if this is DigitalOcean Spaces and use CDN URL
            if (cleanEndpoint.contains("digitaloceanspaces.com")) {
                // Parse region from endpoint (e.g., https://sgp1.digitaloceanspaces.com -> sgp1)
                String parsedRegion = extractRegionFromEndpoint(cleanEndpoint);
                // Build CDN URL: https://bucket.region.cdn.digitaloceanspaces.com/key
                return "https://" + bucketName + "." + parsedRegion + ".cdn.digitaloceanspaces.com/" + s3Key;
            }
            
            // Fallback to standard endpoint URL
            return cleanEndpoint + "/" + bucketName + "/" + s3Key;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image to S3: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean deleteImage(String imageUrl) {
        try {
            // Extract S3 key from URL
            String s3Key = extractS3KeyFromUrl(imageUrl);
            if (s3Key == null) {
                return false;
            }
            
            // Delete from S3/Spaces
            DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, s3Key);
            getS3Client().deleteObject(deleteObjectRequest);
            
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete image from S3: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean validateImage(String base64Data) {
        return imageValidationService.validateImage(base64Data).isValid();
    }
    
    @Override
    public boolean imageExists(String imageUrl) {
        try {
            // Extract S3 key from URL
            String s3Key = extractS3KeyFromUrl(imageUrl);
            if (s3Key == null) {
                return false;
            }
            
            // Check if object exists in S3/Spaces
            return getS3Client().doesObjectExist(bucketName, s3Key);
        } catch (Exception e) {
            return false;
        }
    }
    
    private String generateUniqueFileName(String fileName, String format) {
        String baseName = fileName != null ? fileName : "image";
        String uuid = UUID.randomUUID().toString();
        return baseName + "-" + System.currentTimeMillis() + "_" + uuid + "." + format.toLowerCase();
    }
    
    private String extractS3KeyFromUrl(String imageUrl) {
        try {
            // URL format: https://bucket.region.digitaloceanspaces.com/path/to/file
            // or: https://endpoint/bucket/path/to/file
            if (imageUrl.contains(bucketName)) {
                String[] parts = imageUrl.split(bucketName + "/");
                if (parts.length > 1) {
                    return parts[1];
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    private String extractRegionFromEndpoint(String endpoint) {
        try {
            // Extract region from endpoint like: https://sgp1.digitaloceanspaces.com
            // Returns "sgp1"
            if (endpoint.contains("digitaloceanspaces.com")) {
                // https://sgp1.digitaloceanspaces.com -> extract sgp1
                String host = endpoint.replace("https://", "").replace("http://", "");
                String[] parts = host.split("\\.");
                if (parts.length > 0) {
                    return parts[0]; // Returns "sgp1"
                }
            }
            // Fallback to default region
            return region;
        } catch (Exception e) {
            return region;
        }
    }
}
