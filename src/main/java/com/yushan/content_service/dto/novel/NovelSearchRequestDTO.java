package com.yushan.content_service.dto.novel;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * DTO for novel search and pagination requests.
 * Contains all filtering and sorting parameters.
 */
public class NovelSearchRequestDTO {
    
    @Min(value = 0, message = "Page number must be >= 0")
    private Integer page = 0;
    
    @Min(value = 1, message = "Page size must be >= 1")
    @Max(value = 100, message = "Page size must be <= 100")
    private Integer size = 10;
    
    private String sort = "createTime";
    private String order = "desc";
    
    private Integer categoryId;
    private String status;
    private Boolean isCompleted;
    private String search;
    private String authorName;
    private String authorId;
    
    // Constructors
    public NovelSearchRequestDTO() {
        this.page = 0;
        this.size = 10;
        this.sort = "createTime";
        this.order = "desc";
    }
    
    public NovelSearchRequestDTO(Integer page, Integer size, String sort, String order, 
                               Integer categoryId, String status, Boolean isCompleted, 
                               String search, String authorName, String authorId) {
        this.page = page != null ? page : 0;
        this.size = size != null ? size : 10;
        this.sort = sort != null ? sort : "createTime";
        this.order = order != null ? order : "desc";
        this.categoryId = categoryId;
        this.status = status;
        this.isCompleted = isCompleted;
        this.search = search;
        this.authorName = authorName;
        this.authorId = authorId;
    }
    
    // Helper methods
    public boolean hasCategoryFilter() {
        return categoryId != null && categoryId > 0;
    }
    
    public boolean hasStatusFilter() {
        return status != null && !status.trim().isEmpty();
    }
    
    public boolean hasIsCompletedFilter() {
        return isCompleted != null;
    }
    
    public boolean hasSearchFilter() {
        return search != null && !search.trim().isEmpty();
    }
    
    public boolean hasAuthorFilter() {
        return authorName != null && !authorName.trim().isEmpty();
    }
    
    public boolean hasAuthorIdFilter() {
        return authorId != null && !authorId.trim().isEmpty();
    }
    
    public boolean isAscending() {
        return "asc".equalsIgnoreCase(order);
    }
    
    public boolean isDescending() {
        return "desc".equalsIgnoreCase(order);
    }
    
    // Getters and Setters
    public Integer getPage() {
        return page;
    }
    
    public void setPage(Integer page) {
        this.page = page;
    }
    
    public Integer getSize() {
        return size;
    }
    
    public void setSize(Integer size) {
        this.size = size;
    }
    
    public String getSort() {
        return sort;
    }
    
    public void setSort(String sort) {
        this.sort = sort;
    }
    
    public String getOrder() {
        return order;
    }
    
    public void setOrder(String order) {
        this.order = order;
    }
    
    public Integer getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
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
    
    public String getSearch() {
        return search;
    }
    
    public void setSearch(String search) {
        this.search = search;
    }
    
    public String getAuthorName() {
        return authorName;
    }
    
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
    
    public String getAuthorId() {
        return authorId;
    }
    
    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }
}
