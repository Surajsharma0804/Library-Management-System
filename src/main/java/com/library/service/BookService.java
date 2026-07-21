package com.library.service;

import com.library.service.AuditService;
import com.library.enums.BookStatus;
import com.library.exception.BookNotFoundException;
import com.library.exception.DuplicateBookException;
import com.library.factory.EntityFactory;
import com.library.model.Book;
import com.library.repository.BookRepository;
import com.library.security.Session;
import com.library.search.BookSearchEngine;
import com.library.search.SearchStrategy;
import com.library.validator.BusinessValidators;
import com.library.validator.FormatValidators;

import java.util.Comparator;
import java.util.List;

/**
 * Service for all book inventory operations. Enforces validation,
 * duplicate detection, and audit logging; delegates persistence to
 * {@link BookRepository}.
 */
public final class BookService {

    private final BookRepository repo;
    private final EntityFactory factory;
    private final AuditService auditService;

    public BookService(BookRepository repo, EntityFactory factory, AuditService auditService) {
        this.repo = repo;
        this.factory = factory;
        this.auditService = auditService;
    }

    public Book addBook(Session session, String isbn, String title, String author, int totalQuantity) {
        FormatValidators.validateIsbn(isbn);
        if (repo.isbnExists(isbn)) {
            throw new DuplicateBookException("A book with ISBN " + isbn + " already exists.");
        }
        Book book = factory.createBook(isbn, title, author, totalQuantity);
        BusinessValidators.validateBook(book, null);
        repo.save(book);
        auditService.record(session, "BOOK_ADD", "Book", book.getId(),
                "Added '" + title + "' by " + author + " (qty " + totalQuantity + ")");
        return book;
    }

    public Book updateBook(Session session, Book updated) {
        Book existing = repo.findById(updated.getId())
                .orElseThrow(() -> new BookNotFoundException("Book not found: " + updated.getId()));
        if (!existing.getIsbn().equalsIgnoreCase(updated.getIsbn()) && repo.isbnExists(updated.getIsbn())) {
            throw new DuplicateBookException("ISBN " + updated.getIsbn() + " belongs to another book.");
        }
        BusinessValidators.validateBook(updated, isbn -> repo.isbnExists(isbn) && !isbn.equalsIgnoreCase(updated.getIsbn()));
        repo.save(updated);
        auditService.record(session, "BOOK_UPDATE", "Book", updated.getId(),
                "Updated '" + updated.getTitle() + "'");
        return updated;
    }

    public boolean deleteBook(Session session, String bookId) {
        Book book = repo.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found: " + bookId));
        if (book.getAvailableQuantity() < book.getTotalQuantity()) {
            throw new com.library.exception.ValidationException(
                    "Cannot delete book '" + book.getTitle() + "': some copies are currently borrowed.");
        }
        boolean removed = repo.deleteById(bookId);
        if (removed) {
            auditService.record(session, "BOOK_DELETE", "Book", bookId,
                    "Deleted '" + book.getTitle() + "'");
        }
        return removed;
    }

    public Book archiveBook(Session session, String bookId) {
        Book book = repo.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found: " + bookId));
        book.archive();
        repo.save(book);
        auditService.record(session, "BOOK_ARCHIVE", "Book", bookId,
                "Archived '" + book.getTitle() + "'");
        return book;
    }

    public Book restoreBook(Session session, String bookId) {
        Book book = repo.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found: " + bookId));
        book.restore();
        repo.save(book);
        auditService.record(session, "BOOK_RESTORE", "Book", bookId,
                "Restored '" + book.getTitle() + "'");
        return book;
    }

    public Book markLost(Session session, String bookId, String reason) {
        Book book = repo.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found: " + bookId));
        book.markLost();
        repo.save(book);
        auditService.record(session, "BOOK_MARK_LOST", "Book", bookId,
                "Marked lost: " + reason);
        return book;
    }

    public Book markDamaged(Session session, String bookId, String reason) {
        Book book = repo.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found: " + bookId));
        book.markDamaged();
        repo.save(book);
        auditService.record(session, "BOOK_MARK_DAMAGED", "Book", bookId,
                "Marked damaged: " + reason);
        return book;
    }

    public Book markUnderRepair(Session session, String bookId) {
        Book book = repo.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found: " + bookId));
        book.markUnderRepair();
        repo.save(book);
        auditService.record(session, "BOOK_REPAIR", "Book", bookId,
                "Sent for repair: '" + book.getTitle() + "'");
        return book;
    }

    public Book markAvailable(Session session, String bookId) {
        Book book = repo.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found: " + bookId));
        book.markAvailable();
        repo.save(book);
        auditService.record(session, "BOOK_AVAILABLE", "Book", bookId,
                "Marked available: '" + book.getTitle() + "'");
        return book;
    }

    public Book findById(String bookId) {
        return repo.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found: " + bookId));
    }

    public Book findByIsbn(String isbn) {
        Book book = repo.findByIsbn(isbn);
        if (book == null) {
            throw new BookNotFoundException("No book with ISBN: " + isbn);
        }
        return book;
    }

    public List<Book> findAll() {
        return repo.findAll();
    }

    public List<Book> findByStatus(BookStatus status) {
        return repo.findAll(b -> b.getStatus() == status);
    }

    public List<Book> search(SearchStrategy strategy, String query) {
        return new BookSearchEngine(repo.findAll()).search(strategy, query);
    }

    public List<Book> searchAndSort(SearchStrategy strategy, String query, Comparator<Book> sorter) {
        return new BookSearchEngine(repo.findAll()).searchAndSort(strategy, query, sorter);
    }

    public List<Book> findDuplicates() {
        List<Book> all = repo.findAll();
        return all.stream()
                .filter(b -> all.stream()
                        .anyMatch(other -> other != b && other.getIsbn().equalsIgnoreCase(b.getIsbn())))
                .toList();
    }

    public long count() {
        return repo.count();
    }

    /** Persists a book (used internally by borrow/reservation services). */
    public Book save(Book book) {
        return repo.save(book);
    }
}
