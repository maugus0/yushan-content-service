package com.yushan.content_service.dto.common;

import java.util.List;

/**
 * Generic pagination response DTO.
 * Contains paginated data with metadata.
 */
public class PageResponseDTO<T> {
    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int size;
    private boolean first;
    private boolean last;
    private boolean hasNext;
    private boolean hasPrevious;
    
    // Constructors
    public PageResponseDTO() {
    }
    
    public PageResponseDTO(List<T> content, long totalElements, int currentPage, int size) {
        this.content = content != null ? new java.util.ArrayList<>(content) : new java.util.ArrayList<>();
        this.totalElements = totalElements;
        this.currentPage = currentPage;
        this.size = size;
        this.totalPages = (int) Math.ceil((double) totalElements / size);
        this.first = currentPage == 0;
        this.last = currentPage >= totalPages - 1;
        this.hasNext = !this.last;
        this.hasPrevious = !this.first;
    }
    
    public static <T> PageResponseDTO<T> of(List<T> content, long totalElements, int currentPage, int size) {
        return new PageResponseDTO<>(content, totalElements, currentPage, size);
    }
    
    // Getters and Setters
    public List<T> getContent() {
        return content != null ? new java.util.ArrayList<>(content) : new java.util.ArrayList<>();
    }
    
    public void setContent(List<T> content) {
        this.content = content != null ? new java.util.ArrayList<>(content) : new java.util.ArrayList<>();
    }
    
    public long getTotalElements() {
        return totalElements;
    }
    
    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    
    public int getCurrentPage() {
        return currentPage;
    }
    
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = size;
    }
    
    public boolean isFirst() {
        return first;
    }
    
    public void setFirst(boolean first) {
        this.first = first;
    }
    
    public boolean isLast() {
        return last;
    }
    
    public void setLast(boolean last) {
        this.last = last;
    }
    
    public boolean isHasNext() {
        return hasNext;
    }
    
    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }
    
    public boolean isHasPrevious() {
        return hasPrevious;
    }
    
    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
}
