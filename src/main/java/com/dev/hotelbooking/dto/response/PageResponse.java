package com.dev.hotelbooking.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PageResponse<T> {
    private int totalPages;
    private long totalElements;
    private T content;
}
