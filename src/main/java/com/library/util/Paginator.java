package com.library.util;

import java.util.List;

/** Stateless pagination utility. */
public final class Paginator {

    private Paginator() {}

    /**
     * @throws IllegalArgumentException if pageNumber < 1 or pageSize < 1 or pageSize > 500
     */
    public static <T> Page<T> paginate(List<T> source, int pageNumber, int pageSize) {
        if (pageNumber < 1 || pageSize < 1 || pageSize > 500)
            throw new IllegalArgumentException("Invalid page params: page=" + pageNumber + " size=" + pageSize);
        int total    = source.size();
        int pages    = total == 0 ? 1 : (int) Math.ceil((double) total / pageSize);
        int from     = Math.min((pageNumber - 1) * pageSize, total);
        int to       = Math.min(from + pageSize, total);
        List<T> slice = (from >= total) ? List.of() : source.subList(from, to);
        return new Page<>(List.copyOf(slice), pageNumber, pages, total, pageSize);
    }
}
