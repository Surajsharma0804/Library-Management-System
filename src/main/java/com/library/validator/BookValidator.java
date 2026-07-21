package com.library.validator;

import com.library.enums.BookStatus;
import com.library.model.Book;

/**
 * Validator for book-related business rules.
 */
public final class BookValidator {
    private BookValidator() {}

    public static boolean canIssue(Book book) {
        return book != null
                && book.getStatus() == BookStatus.AVAILABLE
                && book.getAvailableQuantity() > 0;
    }

    public static boolean canReserve(Book book) {
        return book != null
                && book.getAvailableQuantity() == 0
                && book.getStatus() != BookStatus.ARCHIVED;
    }

    public static boolean canDelete(Book book) {
        return book != null
                && book.getBorrowedQuantity() == 0
                && book.getReservedQuantity() == 0;
    }
}
