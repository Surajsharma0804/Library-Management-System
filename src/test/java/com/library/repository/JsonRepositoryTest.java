package com.library.repository;

import com.library.enums.BookStatus;
import com.library.model.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JSON repository tests")
class JsonRepositoryTest {

    @TempDir
    Path tempDir;

    private BookRepository createRepo() {
        BookRepository repo = new BookRepository();
        repo.setOverrideFile(tempDir.resolve("books.json"));
        return repo;
    }

    @Test
    @DisplayName("Save and find by id")
    void saveAndFindById() {
        BookRepository repo = createRepo();
        Book book = Book.builder().id("BK-1").isbn("9780306406157").title("Test")
                .author("Author").totalQuantity(1).availableQuantity(1)
                .status(BookStatus.AVAILABLE).build();
        repo.save(book);
        Optional<Book> found = repo.findById("BK-1");
        assertTrue(found.isPresent());
        assertEquals("Test", found.get().getTitle());
    }

    @Test
    @DisplayName("Delete by id")
    void deleteById() {
        BookRepository repo = createRepo();
        Book book = Book.builder().id("BK-1").isbn("9780306406157").title("Test")
                .author("Author").totalQuantity(1).availableQuantity(1)
                .status(BookStatus.AVAILABLE).build();
        repo.save(book);
        assertTrue(repo.deleteById("BK-1"));
        assertFalse(repo.findById("BK-1").isPresent());
    }

    @Test
    @DisplayName("Count returns correct number")
    void count() {
        BookRepository repo = createRepo();
        assertEquals(0, repo.count());
        repo.save(Book.builder().id("BK-1").isbn("9780306406157").title("A")
                .author("X").totalQuantity(1).availableQuantity(1)
                .status(BookStatus.AVAILABLE).build());
        assertEquals(1, repo.count());
    }

    @Test
    @DisplayName("Find by ISBN")
    void findByIsbn() {
        BookRepository repo = createRepo();
        Book book = Book.builder().id("BK-1").isbn("9780306406157").title("Test")
                .author("Author").totalQuantity(1).availableQuantity(1)
                .status(BookStatus.AVAILABLE).build();
        repo.save(book);
        assertNotNull(repo.findByIsbn("9780306406157"));
    }
}
