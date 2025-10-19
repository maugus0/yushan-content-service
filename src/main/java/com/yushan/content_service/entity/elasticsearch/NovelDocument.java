package com.yushan.content_service.entity.elasticsearch;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

/**
 * Elasticsearch document for novels.
 * Maps to the novels index in Elasticsearch.
 */
@Document(indexName = "novels")
public class NovelDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String uuid;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String synopsis;
    
    @Field(type = FieldType.Keyword)
    private String authorId;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String authorName;
    
    @Field(type = FieldType.Integer)
    private Integer categoryId;
    
    @Field(type = FieldType.Keyword)
    private String status;
    
    @Field(type = FieldType.Boolean)
    private Boolean isCompleted;
    
    @Field(type = FieldType.Integer)
    private Integer chapterCnt;
    
    @Field(type = FieldType.Long)
    private Long wordCnt;
    
    @Field(type = FieldType.Double)
    private Double avgRating;
    
    @Field(type = FieldType.Integer)
    private Integer reviewCnt;
    
    @Field(type = FieldType.Long)
    private Long viewCnt;
    
    @Field(type = FieldType.Integer)
    private Integer voteCnt;
    
    @Field(type = FieldType.Double)
    private Double yuanCnt;
    
    @Field(type = FieldType.Date)
    private Date createTime;
    
    @Field(type = FieldType.Date)
    private Date updateTime;
    
    @Field(type = FieldType.Date)
    private Date publishTime;
    
    @Field(type = FieldType.Keyword)
    private String coverImgUrl;

    // Constructors
    public NovelDocument() {}

    public NovelDocument(String id, String uuid, String title, String synopsis, String authorId, 
                       String authorName, Integer categoryId, String status, Boolean isCompleted,
                       Integer chapterCnt, Long wordCnt, Double avgRating, Integer reviewCnt,
                       Long viewCnt, Integer voteCnt, Double yuanCnt, Date createTime,
                       Date updateTime, Date publishTime, String coverImgUrl) {
        this.id = id;
        this.uuid = uuid;
        this.title = title;
        this.synopsis = synopsis;
        this.authorId = authorId;
        this.authorName = authorName;
        this.categoryId = categoryId;
        this.status = status;
        this.isCompleted = isCompleted;
        this.chapterCnt = chapterCnt;
        this.wordCnt = wordCnt;
        this.avgRating = avgRating;
        this.reviewCnt = reviewCnt;
        this.viewCnt = viewCnt;
        this.voteCnt = voteCnt;
        this.yuanCnt = yuanCnt;
        this.createTime = createTime != null ? (Date) createTime.clone() : null;
        this.updateTime = updateTime != null ? (Date) updateTime.clone() : null;
        this.publishTime = publishTime != null ? (Date) publishTime.clone() : null;
        this.coverImgUrl = coverImgUrl;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSynopsis() { return synopsis; }
    public void setSynopsis(String synopsis) { this.synopsis = synopsis; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }

    public Integer getChapterCnt() { return chapterCnt; }
    public void setChapterCnt(Integer chapterCnt) { this.chapterCnt = chapterCnt; }

    public Long getWordCnt() { return wordCnt; }
    public void setWordCnt(Long wordCnt) { this.wordCnt = wordCnt; }

    public Double getAvgRating() { return avgRating; }
    public void setAvgRating(Double avgRating) { this.avgRating = avgRating; }

    public Integer getReviewCnt() { return reviewCnt; }
    public void setReviewCnt(Integer reviewCnt) { this.reviewCnt = reviewCnt; }

    public Long getViewCnt() { return viewCnt; }
    public void setViewCnt(Long viewCnt) { this.viewCnt = viewCnt; }

    public Integer getVoteCnt() { return voteCnt; }
    public void setVoteCnt(Integer voteCnt) { this.voteCnt = voteCnt; }

    public Double getYuanCnt() { return yuanCnt; }
    public void setYuanCnt(Double yuanCnt) { this.yuanCnt = yuanCnt; }

    public Date getCreateTime() { return createTime != null ? (Date) createTime.clone() : null; }
    public void setCreateTime(Date createTime) { this.createTime = createTime != null ? (Date) createTime.clone() : null; }

    public Date getUpdateTime() { return updateTime != null ? (Date) updateTime.clone() : null; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime != null ? (Date) updateTime.clone() : null; }

    public Date getPublishTime() { return publishTime != null ? (Date) publishTime.clone() : null; }
    public void setPublishTime(Date publishTime) { this.publishTime = publishTime != null ? (Date) publishTime.clone() : null; }

    public String getCoverImgUrl() { return coverImgUrl; }
    public void setCoverImgUrl(String coverImgUrl) { this.coverImgUrl = coverImgUrl; }
}
