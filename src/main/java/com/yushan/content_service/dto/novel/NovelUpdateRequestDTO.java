package com.yushan.content_service.dto.novel;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating an existing novel.
 * All fields are optional for partial updates.
 */
public class NovelUpdateRequestDTO {
    
    @Size(max = 255, message = "Title must be at most 255 characters")
    private String title;

    @Size(max = 4000, message = "Synopsis must be at most 4000 characters")
    private String synopsis;

    private Integer categoryId;

    @Pattern(regexp = "^data:image/(jpeg|jpg|png|gif|webp);base64,[A-Za-z0-9+/]+=*$", 
             message = "Cover image must be a valid Base64 data URL for image")
    private String coverImgBase64;

    private Boolean isCompleted;

    private String status;  // For admin status changes

    // Constructors
    public NovelUpdateRequestDTO() {
    }

    public NovelUpdateRequestDTO(String title, String synopsis, Integer categoryId, 
                                String coverImgBase64, Boolean isCompleted, String status) {
        this.title = title;
        this.synopsis = synopsis;
        this.categoryId = categoryId;
        this.coverImgBase64 = coverImgBase64;
        this.isCompleted = isCompleted;
        this.status = status;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCoverImgBase64() {
        return coverImgBase64;
    }

    public void setCoverImgBase64(String coverImgBase64) {
        this.coverImgBase64 = coverImgBase64;
    }

    public Boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "NovelUpdateRequestDTO{" +
                "title='" + title + '\'' +
                ", synopsis='" + synopsis + '\'' +
                ", categoryId=" + categoryId +
                ", coverImgBase64='" + (coverImgBase64 != null ? "[BASE64_DATA]" : "null") + '\'' +
                ", isCompleted=" + isCompleted +
                ", status='" + status + '\'' +
                '}';
    }
}
