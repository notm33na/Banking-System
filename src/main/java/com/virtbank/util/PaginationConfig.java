package com.virtbank.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Enforces a maximum page size of 100 on all paginated endpoints.
 */
public final class PaginationConfig {

    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_PAGE_SIZE = 20;

    private PaginationConfig() {}

    /**
     * Returns a safe Pageable, capping the page size at MAX_PAGE_SIZE.
     */
    public static Pageable cap(Pageable pageable) {
        int size = Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);
        return PageRequest.of(pageable.getPageNumber(), size, pageable.getSort());
    }

    /**
     * Overload for explicit page + size parameters.
     */
    public static Pageable of(int page, int size) {
        return PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), MAX_PAGE_SIZE));
    }
}
