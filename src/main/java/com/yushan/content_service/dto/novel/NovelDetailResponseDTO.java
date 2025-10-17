package com.yushan.content_service.dto.novel;

import java.util.Date;
import java.util.UUID;

/**
 * DTO for novel detail response.
 * Contains all novel information including metadata, statistics, and timestamps.
 */
public class NovelDetailResponseDTO {
    
    // Primary identifiers
    private Integer id;
    private UUID uuid;
    private String title;

    // Author information
    private UUID authorId;
    private String authorUsername;

    // Category information
    private Integer categoryId;
    private String categoryName;

    // Content information
    private String synopsis;
    private String coverImgUrl;
    private String status;
    private Boolean isCompleted;

    // Statistics
    private Integer chapterCnt;
    private Long wordCnt;
    private Float avgRating;
    private Integer reviewCnt;
    private Long viewCnt;
    private Integer voteCnt;
    private Float yuanCnt;

    // Timestamps
    private Date createTime;
    private Date updateTime;
    private Date publishTime;

    // Constructors
    public NovelDetailResponseDTO() {
    }

    public NovelDetailResponseDTO(Integer id, UUID uuid, String title, UUID authorId, String authorUsername,
                                 Integer categoryId, String categoryName, String synopsis, String coverImgUrl,
                                 String status, Boolean isCompleted, Integer chapterCnt, Long wordCnt,
                                 Float avgRating, Integer reviewCnt, Long viewCnt, Integer voteCnt,
                                 Float yuanCnt, Date createTime, Date updateTime, Date publishTime) {
        this.id = id;
        this.uuid = uuid;
        this.title = title;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.synopsis = synopsis;
        this.coverImgUrl = coverImgUrl;
        this.status = status;
        this.isCompleted = isCompleted;
        this.chapterCnt = chapterCnt;
        this.wordCnt = wordCnt;
        this.avgRating = avgRating;
        this.reviewCnt = reviewCnt;
        this.viewCnt = viewCnt;
        this.voteCnt = voteCnt;
        this.yuanCnt = yuanCnt;
        this.createTime = createTime != null ? new Date(createTime.getTime()) : null;
        this.updateTime = updateTime != null ? new Date(updateTime.getTime()) : null;
        this.publishTime = publishTime != null ? new Date(publishTime.getTime()) : null;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public void setAuthorId(UUID authorId) {
        this.authorId = authorId;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public String getCoverImgUrl() {
        return coverImgUrl;
    }

    public void setCoverImgUrl(String coverImgUrl) {
        this.coverImgUrl = coverImgUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public Integer getChapterCnt() {
        return chapterCnt;
    }

    public void setChapterCnt(Integer chapterCnt) {
        this.chapterCnt = chapterCnt;
    }

    public Long getWordCnt() {
        return wordCnt;
    }

    public void setWordCnt(Long wordCnt) {
        this.wordCnt = wordCnt;
    }

    public Float getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(Float avgRating) {
        this.avgRating = avgRating;
    }

    public Integer getReviewCnt() {
        return reviewCnt;
    }

    public void setReviewCnt(Integer reviewCnt) {
        this.reviewCnt = reviewCnt;
    }

    public Long getViewCnt() {
        return viewCnt;
    }

    public void setViewCnt(Long viewCnt) {
        this.viewCnt = viewCnt;
    }

    public Integer getVoteCnt() {
        return voteCnt;
    }

    public void setVoteCnt(Integer voteCnt) {
        this.voteCnt = voteCnt;
    }

    public Float getYuanCnt() {
        return yuanCnt;
    }

    public void setYuanCnt(Float yuanCnt) {
        this.yuanCnt = yuanCnt;
    }

    public Date getCreateTime() {
        return createTime != null ? new Date(createTime.getTime()) : null;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime != null ? new Date(createTime.getTime()) : null;
    }

    public Date getUpdateTime() {
        return updateTime != null ? new Date(updateTime.getTime()) : null;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime != null ? new Date(updateTime.getTime()) : null;
    }

    public Date getPublishTime() {
        return publishTime != null ? new Date(publishTime.getTime()) : null;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime != null ? new Date(publishTime.getTime()) : null;
    }

    @Override
    public String toString() {
        return "NovelDetailResponseDTO{" +
                "id=" + id +
                ", uuid=" + uuid +
                ", title='" + title + '\'' +
                ", authorId=" + authorId +
                ", authorUsername='" + authorUsername + '\'' +
                ", categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                ", status='" + status + '\'' +
                ", isCompleted=" + isCompleted +
                ", chapterCnt=" + chapterCnt +
                ", wordCnt=" + wordCnt +
                ", viewCnt=" + viewCnt +
                ", createTime=" + createTime +
                '}';
    }
}
