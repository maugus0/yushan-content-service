package com.yushan.content_service.entity.elasticsearch;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

/**
 * Elasticsearch document for chapters.
 * Maps to the chapters index in Elasticsearch.
 */
@Document(indexName = "chapters")
public class ChapterDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String uuid;
    
    @Field(type = FieldType.Integer)
    private Integer novelId;
    
    @Field(type = FieldType.Integer)
    private Integer chapterNumber;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String content;
    
    @Field(type = FieldType.Integer)
    private Integer wordCnt;
    
    @Field(type = FieldType.Boolean)
    private Boolean isPremium;
    
    @Field(type = FieldType.Double)
    private Double yuanCost;
    
    @Field(type = FieldType.Long)
    private Long viewCnt;
    
    @Field(type = FieldType.Boolean)
    private Boolean isValid;
    
    @Field(type = FieldType.Date)
    private Date createTime;
    
    @Field(type = FieldType.Date)
    private Date updateTime;
    
    @Field(type = FieldType.Date)
    private Date publishTime;

    // Constructors
    public ChapterDocument() {}

    public ChapterDocument(String id, String uuid, Integer novelId, Integer chapterNumber,
                          String title, String content, Integer wordCnt, Boolean isPremium,
                          Double yuanCost, Long viewCnt, Boolean isValid, Date createTime,
                          Date updateTime, Date publishTime) {
        this.id = id;
        this.uuid = uuid;
        this.novelId = novelId;
        this.chapterNumber = chapterNumber;
        this.title = title;
        this.content = content;
        this.wordCnt = wordCnt;
        this.isPremium = isPremium;
        this.yuanCost = yuanCost;
        this.viewCnt = viewCnt;
        this.isValid = isValid;
        this.createTime = createTime != null ? (Date) createTime.clone() : null;
        this.updateTime = updateTime != null ? (Date) updateTime.clone() : null;
        this.publishTime = publishTime != null ? (Date) publishTime.clone() : null;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public Integer getNovelId() { return novelId; }
    public void setNovelId(Integer novelId) { this.novelId = novelId; }

    public Integer getChapterNumber() { return chapterNumber; }
    public void setChapterNumber(Integer chapterNumber) { this.chapterNumber = chapterNumber; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getWordCnt() { return wordCnt; }
    public void setWordCnt(Integer wordCnt) { this.wordCnt = wordCnt; }

    public Boolean getIsPremium() { return isPremium; }
    public void setIsPremium(Boolean isPremium) { this.isPremium = isPremium; }

    public Double getYuanCost() { return yuanCost; }
    public void setYuanCost(Double yuanCost) { this.yuanCost = yuanCost; }

    public Long getViewCnt() { return viewCnt; }
    public void setViewCnt(Long viewCnt) { this.viewCnt = viewCnt; }

    public Boolean getIsValid() { return isValid; }
    public void setIsValid(Boolean isValid) { this.isValid = isValid; }

    public Date getCreateTime() { return createTime != null ? (Date) createTime.clone() : null; }
    public void setCreateTime(Date createTime) { this.createTime = createTime != null ? (Date) createTime.clone() : null; }

    public Date getUpdateTime() { return updateTime != null ? (Date) updateTime.clone() : null; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime != null ? (Date) updateTime.clone() : null; }

    public Date getPublishTime() { return publishTime != null ? (Date) publishTime.clone() : null; }
    public void setPublishTime(Date publishTime) { this.publishTime = publishTime != null ? (Date) publishTime.clone() : null; }
}
