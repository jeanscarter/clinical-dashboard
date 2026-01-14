package com.cms.service.dto;

import java.util.List;

public class Page<T> {

    private final List<T> content;
    private final long totalElements;
    private final int totalPages;
    private final int currentPage;
    private final int pageSize;

    public Page(List<T> content, long totalElements, int currentPage, int pageSize) {
        this.content = content;
        this.totalElements = totalElements;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalPages = pageSize > 0 ? (int) Math.ceil((double) totalElements / pageSize) : 0;
    }

    public List<T> getContent() {
        return content;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public boolean hasNext() {
        return currentPage < totalPages - 1;
    }

    public boolean hasPrevious() {
        return currentPage > 0;
    }

    public boolean isFirst() {
        return currentPage == 0;
    }

    public boolean isLast() {
        return currentPage >= totalPages - 1;
    }

    public int getStartIndex() {
        return currentPage * pageSize + 1;
    }

    public int getEndIndex() {
        int end = (currentPage + 1) * pageSize;
        return (int) Math.min(end, totalElements);
    }

    public String getDisplayInfo() {
        if (totalElements == 0) {
            return "Sin registros";
        }
        return String.format("Mostrando %d-%d de %d", getStartIndex(), getEndIndex(), totalElements);
    }
}
