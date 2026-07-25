package com.library.api;

import com.library.facade.LibraryFacade;
import com.library.model.Book;
import com.library.security.Session;
import io.javalin.http.Context;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST endpoints for book catalog operations.
 * Supports full CRUD and keyword search across title/author/ISBN.
 */
public final class RestBookController extends BaseRestController {

    public RestBookController(LibraryFacade facade) {
        super(facade);
    }

    /** GET /api/books — Lists all books in the catalog. */
    public void list(Context ctx) {
        requireSession(ctx);
        List<Book> books = facade.bookRepo().findAll();
        ctx.json(books.stream().map(this::toMap).toList());
    }

    /** GET /api/books/search?q=keyword — Searches books by title, author, or ISBN. */
    public void search(Context ctx) {
        requireSession(ctx);
        String query = ctx.queryParam("q");
        if (query == null || query.isBlank()) {
            ctx.json(List.of());
            return;
        }
        String q = query.toLowerCase();
        List<Book> results = facade.bookRepo().findAll().stream()
                .filter(b -> (b.getTitle() != null && b.getTitle().toLowerCase().contains(q))
                        || (b.getAuthor() != null && b.getAuthor().toLowerCase().contains(q))
                        || (b.getIsbn() != null && b.getIsbn().toLowerCase().contains(q)))
                .toList();
        ctx.json(results.stream().map(this::toMap).toList());
    }

    /** POST /api/books — Adds a new book to the catalog. */
    public void add(Context ctx) {
        Session session = requireSession(ctx);
        var body = ctx.bodyAsClass(BookRequest.class);
        Book book = facade.books().addBook(session, body.isbn, body.title, body.author, body.totalQuantity);
        ctx.status(201).json(toMap(book));
    }

    /** PUT /api/books/{id} — Updates an existing book's metadata. */
    public void update(Context ctx) {
        Session session = requireSession(ctx);
        String bookId = ctx.pathParam("id");
        Book book = facade.books().findById(bookId);
        if (book == null) {
            ctx.status(404).json(Map.of("error", "Book not found"));
            return;
        }
        var body = ctx.bodyAsClass(BookRequest.class);
        if (body.title != null) book.setTitle(body.title);
        if (body.author != null) book.setAuthor(body.author);
        if (body.isbn != null) book.setIsbn(body.isbn);
        if (body.publisher != null) book.setPublisher(body.publisher);
        if (body.category != null) book.setCategory(body.category);
        Book updated = facade.books().updateBook(session, book);
        ctx.json(toMap(updated));
    }

    /** DELETE /api/books/{id} — Removes a book from the catalog. */
    public void remove(Context ctx) {
        Session session = requireSession(ctx);
        String bookId = ctx.pathParam("id");
        facade.books().deleteBook(session, bookId);
        ctx.json(Map.of("message", "Book removed"));
    }

    // ── Serialization helper ────────────────────────────────────────
    private Map<String, Object> toMap(Book b) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", b.getId());
        m.put("isbn", b.getIsbn());
        m.put("title", b.getTitle());
        m.put("author", b.getAuthor());
        m.put("publisher", b.getPublisher());
        m.put("category", b.getCategory());
        m.put("totalQuantity", b.getTotalQuantity());
        m.put("availableQuantity", b.getAvailableQuantity());
        m.put("status", b.getStatus() != null ? b.getStatus().name() : null);
        return m;
    }

    public static class BookRequest {
        public String isbn;
        public String title;
        public String author;
        public String publisher;
        public String category;
        public int totalQuantity;
    }
}
