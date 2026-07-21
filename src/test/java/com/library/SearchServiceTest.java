package com.library;

import com.library.enums.BookStatus;
import com.library.model.Book;
import com.library.repository.BookRepository;
import com.library.search.BookSearchEngine;
import com.library.search.BookSearchStrategies;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Search service tests")
class SearchServiceTest {

    @Test
    @DisplayName("Search by title finds matching books")
    void searchByTitle() {
        List<Book> books = List.of(
                Book.builder().id("BK-1").isbn("9780306406157").title("Core Java")
                        .author("Horstmann").totalQuantity(1).availableQuantity(1)
                        .status(BookStatus.AVAILABLE).build(),
                Book.builder().id("BK-2").isbn("9780132350884").title("Clean Code")
                        .author("Martin").totalQuantity(1).availableQuantity(1)
                        .status(BookStatus.AVAILABLE).build()
        );
        BookSearchEngine engine = new BookSearchEngine(books);
        List<Book> results = engine.search(BookSearchStrategies.byTitle(), "java");
        assertEquals(1, results.size());
        assertEquals("Core Java", results.get(0).getTitle());
    }

    @Test
    @DisplayName("Search by author finds matching books")
    void searchByAuthor() {
        List<Book> books = List.of(
                Book.builder().id("BK-1").isbn("9780306406157").title("Core Java")
                        .author("Horstmann").totalQuantity(1).availableQuantity(1)
                        .status(BookStatus.AVAILABLE).build(),
                Book.builder().id("BK-2").isbn("9780132350884").title("Clean Code")
                        .author("Martin").totalQuantity(1).availableQuantity(1)
                        .status(BookStatus.AVAILABLE).build()
        );
        BookSearchEngine engine = new BookSearchEngine(books);
        List<Book> results = engine.search(BookSearchStrategies.byAuthor(), "martin");
        assertEquals(1, results.size());
        assertEquals("Clean Code", results.get(0).getTitle());
    }

    @Test
    @DisplayName("Search all returns all books for empty query")
    void searchAllEmptyQuery() {
        List<Book> books = List.of(
                Book.builder().id("BK-1").isbn("9780306406157").title("Core Java")
                        .author("Horstmann").totalQuantity(1).availableQuantity(1)
                        .status(BookStatus.AVAILABLE).build()
        );
        BookSearchEngine engine = new BookSearchEngine(books);
        List<Book> results = engine.search(BookSearchStrategies.byTitle(), "");
        assertEquals(1, results.size());
    }
}
