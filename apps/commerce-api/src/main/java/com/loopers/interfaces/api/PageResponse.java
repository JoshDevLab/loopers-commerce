package com.loopers.interfaces.api;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private int totalPages;
    private long totalCount;

    public PageResponse(List<T> content, int page, int size, int totalPages, long totalCount) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalPages = totalPages;
        this.totalCount = totalCount;
    }

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalPages(),
                page.getTotalElements()
        );
    }
}
