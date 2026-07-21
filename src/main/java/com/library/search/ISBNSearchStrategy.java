package com.library.search;

import com.library.model.Book;
import java.util.List;
import java.util.Locale;

public final class ISBNSearchStrategy implements SearchStrategy<Book> {
    @Override
    public String label() { return "Isbn"; }

    @Override
    public List<Book> search(List<Book> books, String query) {
        String q = query == null ? "" : query.toLowerCase(Locale.ROOT);
        if (q.isEmpty()) return books;
        return books.stream().filter(b -> {
            var v = b.getIsbn();
            return v != null && v.toString().toLowerCase(Locale.ROOT).contains(q);
        }).toList();
    }
}
