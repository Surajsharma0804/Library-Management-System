package com.library.model;

import com.library.model.Book;

import com.library.enums.BookStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Book model tests")
class BookTest {

    private Book createBook() {
        return Book.builder()
                .id("BK-000001").isbn("9780306406157").title("Clean Code")
                .author("Robert Martin").totalQuantity(3).availableQuantity(3)
                .status(BookStatus.AVAILABLE).build();
    }

    @Test
    @DisplayName("Mark issued decreases available count")
    void markIssued() {
        Book book = createBook();
        book.markIssued();
        assertEquals(2, book.getAvailableQuantity());
        assertEquals(BookStatus.AVAILABLE, book.getStatus());
    }

    @Test
    @DisplayName("Mark issued on zero available throws")
    void markIssuedZeroThrows() {
        Book book = createBook();
        book.markIssued();
        book.markIssued();
        book.markIssued();
        assertThrows(IllegalStateException.class, book::markIssued);
    }

    @Test
    @DisplayName("Mark returned increases available count")
    void markReturned() {
        Book book = createBook();
        book.markIssued();
        book.markReturned();
        assertEquals(3, book.getAvailableQuantity());
    }

    @Test
    @DisplayName("Mark returned beyond total throws")
    void markReturnedOverflowThrows() {
        Book book = createBook();
        assertThrows(IllegalStateException.class, book::markReturned);
    }

    @Test
    @DisplayName("Mark reserved decreases available and increases reserved")
    void markReserved() {
        Book book = createBook();
        book.markReserved();
        assertEquals(2, book.getAvailableQuantity());
        assertEquals(1, book.getReservedQuantity());
        assertEquals(BookStatus.AVAILABLE, book.getStatus());
    }

    @Test
    @DisplayName("Release reservation restores available")
    void releaseReservation() {
        Book book = createBook();
        book.markReserved();
        book.releaseReservation();
        assertEquals(3, book.getAvailableQuantity());
        assertEquals(0, book.getReservedQuantity());
    }

    @Test
    @DisplayName("Mark lost decreases total")
    void markLost() {
        Book book = createBook();
        book.markLost();
        assertEquals(2, book.getTotalQuantity());
    }

    @Test
    @DisplayName("Archive sets archived status")
    void archive() {
        Book book = createBook();
        book.archive();
        assertEquals(BookStatus.ARCHIVED, book.getStatus());
    }

    @Test
    @DisplayName("Restore from archived returns to available")
    void restore() {
        Book book = createBook();
        book.archive();
        book.restore();
        assertNotEquals(BookStatus.ARCHIVED, book.getStatus());
    }

    @Test
    @DisplayName("Borrowed quantity is computed correctly")
    void borrowedQuantity() {
        Book book = createBook();
        book.markIssued();
        book.markIssued();
        assertEquals(2, book.getBorrowedQuantity());
    }
}
