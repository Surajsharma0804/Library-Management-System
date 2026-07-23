package com.library.util;

import java.util.List;

/** Immutable slice of a sorted result list. */
public record Page<T>(
    List<T> items,          // records on this page
    int currentPage,        // 1-based
    int totalPages,
    long totalRecords,
    int pageSize
) {
    public boolean hasPrevious() { return currentPage > 1; }
    public boolean hasNext()     { return currentPage < totalPages; }
}
