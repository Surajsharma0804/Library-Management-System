package com.library.builder;

import com.library.enums.BookStatus;
import com.library.model.Book;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Standalone builder for {@link Book} entities.
 * Delegates to the inner Builder, providing a fluent API at the package level.
 */
public class BookBuilder {
    private final Book.Builder delegate = Book.builder();

    public BookBuilder id(String id) { delegate.id(id); return this; }
    public BookBuilder isbn(String isbn) { delegate.isbn(isbn); return this; }
    public BookBuilder title(String title) { delegate.title(title); return this; }
    public BookBuilder author(String author) { delegate.author(author); return this; }
    public BookBuilder publisher(String publisher) { delegate.publisher(publisher); return this; }
    public BookBuilder edition(String edition) { delegate.edition(edition); return this; }
    public BookBuilder language(String language) { delegate.language(language); return this; }
    public BookBuilder category(String category) { delegate.category(category); return this; }
    public BookBuilder publicationYear(int year) { delegate.publicationYear(year); return this; }
    public BookBuilder totalQuantity(int total) { delegate.totalQuantity(total); return this; }
    public BookBuilder availableQuantity(int available) { delegate.availableQuantity(available); return this; }
    public BookBuilder reservedQuantity(int reserved) { delegate.reservedQuantity(reserved); return this; }
    public BookBuilder status(BookStatus status) { delegate.status(status); return this; }
    public BookBuilder rack(String rack) { delegate.rack(rack); return this; }
    public BookBuilder shelf(String shelf) { delegate.shelf(shelf); return this; }
    public BookBuilder description(String description) { delegate.description(description); return this; }
    public BookBuilder coAuthors(List<String> coAuthors) { delegate.coAuthors(coAuthors); return this; }
    public BookBuilder createdAt(LocalDateTime createdAt) { delegate.createdAt(createdAt); return this; }

    public Book build() {
        return delegate.build();
    }
}
