package com.library.repository;

import com.library.config.Constants;
import com.library.mapper.BookMapper;
import com.library.model.Book;

/**
 * JSON-backed repository for {@link Book} entities, keyed by book id.
 * Uses secondary indexes for O(1) lookups by ISBN, barcode, and branchId.
 */
public final class BookRepository extends IndexedRepository<Book, String> {

    public BookRepository() {
        super(Constants.BOOKS_FILE, new BookMapper(), Book::getId);
        registerSecondaryIndex("isbn");
        registerSecondaryIndex("barcode");
        registerSecondaryIndex("branchId");
    }

    @Override
    protected String secondaryKey(String indexName, Book entity) {
        return switch (indexName) {
            case "isbn"     -> entity.getIsbn() != null ? entity.getIsbn().toLowerCase() : null;
            case "barcode"  -> entity.getBarcode();
            case "branchId" -> entity.getBranchId();
            default         -> null;
        };
    }

    /** Returns the first book whose ISBN matches (case-insensitive). */
    public Book findByIsbn(String isbn) {
        return findBySecondaryKey("isbn", isbn.toLowerCase()).orElse(null);
    }

    public boolean isbnExists(String isbn) {
        return findBySecondaryKey("isbn", isbn.toLowerCase()).isPresent();
    }

    public Book findByBarcode(String barcode) {
        if (barcode == null) {
            return null;
        }
        return findBySecondaryKey("barcode", barcode).orElse(null);
    }
}
