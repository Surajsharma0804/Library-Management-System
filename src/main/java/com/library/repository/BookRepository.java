package com.library.repository;

import com.library.config.Constants;
import com.library.mapper.BookMapper;
import com.library.model.Book;

/**
 * JSON-backed repository for {@link Book} entities, keyed by book id.
 */
public final class BookRepository extends JsonRepository<Book, String> {

    public BookRepository() {
        super(Constants.BOOKS_FILE, new BookMapper(), Book::getId);
    }

    /** Returns the first book whose ISBN matches (case-insensitive). */
    public Book findByIsbn(String isbn) {
        return findAll(b -> b.getIsbn().equalsIgnoreCase(isbn)).stream().findFirst().orElse(null);
    }

    public boolean isbnExists(String isbn) {
        return exists(b -> b.getIsbn().equalsIgnoreCase(isbn));
    }

    public Book findByBarcode(String barcode) {
        if (barcode == null) {
            return null;
        }
        return findAll(b -> barcode.equals(b.getBarcode())).stream().findFirst().orElse(null);
    }
}
