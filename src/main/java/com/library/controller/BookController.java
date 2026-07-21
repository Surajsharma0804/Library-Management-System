package com.library.controller;

import com.library.enums.BookStatus;
import com.library.facade.LibraryFacade;
import com.library.model.Book;
import com.library.security.Permissions;
import com.library.security.Session;
import com.library.search.BookSearchStrategies;
import com.library.search.SearchStrategy;

import java.util.Comparator;
import java.util.List;

/**
 * Controller for book inventory operations. Enforces RBAC then
 * delegates to {@link com.library.service.BookService}.
 */
public final class BookController extends BaseController {

    public BookController(LibraryFacade facade) {
        super(facade);
    }

    public Book addBook(Session session, String isbn, String title, String author, int totalQuantity) {
        require(session, Permissions.BOOK_ADD);
        return facade.books().addBook(session, isbn, title, author, totalQuantity);
    }

    public Book updateBook(Session session, Book book) {
        require(session, Permissions.BOOK_UPDATE);
        return facade.books().updateBook(session, book);
    }

    public boolean deleteBook(Session session, String bookId) {
        require(session, Permissions.BOOK_DELETE);
        return facade.books().deleteBook(session, bookId);
    }

    public Book archiveBook(Session session, String bookId) {
        require(session, Permissions.BOOK_ARCHIVE);
        return facade.books().archiveBook(session, bookId);
    }

    public Book restoreBook(Session session, String bookId) {
        require(session, Permissions.BOOK_RESTORE);
        return facade.books().restoreBook(session, bookId);
    }

    public Book markLost(Session session, String bookId, String reason) {
        require(session, Permissions.BOOK_MARK_LOST);
        return facade.books().markLost(session, bookId, reason);
    }

    public Book markDamaged(Session session, String bookId, String reason) {
        require(session, Permissions.BOOK_MARK_DAMAGED);
        return facade.books().markDamaged(session, bookId, reason);
    }

    public Book markUnderRepair(Session session, String bookId) {
        require(session, Permissions.BOOK_REPAIR);
        return facade.books().markUnderRepair(session, bookId);
    }

    public Book markAvailable(Session session, String bookId) {
        require(session, Permissions.BOOK_REPAIR);
        return facade.books().markAvailable(session, bookId);
    }

    public Book findById(Session session, String bookId) {
        require(session, Permissions.BOOK_VIEW);
        return facade.books().findById(bookId);
    }

    public List<Book> findAll(Session session) {
        require(session, Permissions.BOOK_VIEW);
        return facade.books().findAll();
    }

    public List<Book> findByStatus(Session session, BookStatus status) {
        require(session, Permissions.BOOK_VIEW);
        return facade.books().findByStatus(status);
    }

    public List<Book> search(Session session, String field, String query) {
        require(session, Permissions.BOOK_VIEW);
        SearchStrategy strategy = strategyForField(field);
        return facade.books().search(strategy, query);
    }

    public List<Book> searchSorted(Session session, String field, String query, String sortBy) {
        require(session, Permissions.BOOK_VIEW);
        SearchStrategy strategy = strategyForField(field);
        Comparator<Book> sorter = sorterForField(sortBy);
        return facade.books().searchAndSort(strategy, query, sorter);
    }

    public List<Book> findDuplicates(Session session) {
        require(session, Permissions.BOOK_VIEW);
        return facade.books().findDuplicates();
    }

    private SearchStrategy strategyForField(String field) {
        return switch (field.toLowerCase(java.util.Locale.ROOT)) {
            case "title" -> BookSearchStrategies.byTitle();
            case "author" -> BookSearchStrategies.byAuthor();
            case "isbn" -> BookSearchStrategies.byIsbn();
            case "barcode" -> BookSearchStrategies.byBarcode();
            case "publisher" -> BookSearchStrategies.byPublisher();
            case "category" -> BookSearchStrategies.byCategory();
            case "subject" -> BookSearchStrategies.bySubject();
            case "keyword" -> BookSearchStrategies.byKeyword();
            case "rack" -> BookSearchStrategies.byRack();
            case "shelf" -> BookSearchStrategies.byShelf();
            case "language" -> BookSearchStrategies.byLanguage();
            default -> BookSearchStrategies.byTitle();
        };
    }

    private Comparator<Book> sorterForField(String sortBy) {
        return switch (sortBy == null ? "" : sortBy.toLowerCase(java.util.Locale.ROOT)) {
            case "author" -> Comparator.comparing(Book::getAuthor, String.CASE_INSENSITIVE_ORDER);
            case "year" -> Comparator.comparingInt(Book::getPublicationYear).reversed();
            case "available" -> Comparator.comparingInt(Book::getAvailableQuantity).reversed();
            case "category" -> Comparator.comparing(Book::getCategory, String.CASE_INSENSITIVE_ORDER).thenComparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER);
        };
    }
}
