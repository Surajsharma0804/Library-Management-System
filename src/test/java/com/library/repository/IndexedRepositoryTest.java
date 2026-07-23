package com.library.repository;

import com.library.enums.BookStatus;
import com.library.interfaces.JsonMappable;
import com.library.mapper.BookMapper;
import com.library.model.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IndexedRepository unit tests")
class IndexedRepositoryTest {

    @TempDir
    Path tempDir;

    /**
     * Concrete IndexedRepository for testing, using Book with isbn secondary index.
     */
    private static class TestBookRepository extends IndexedRepository<Book, String> {
        TestBookRepository(Path file) {
            super(file, new BookMapper(), Book::getId);
            registerSecondaryIndex("isbn");
        }

        @Override
        protected String secondaryKey(String indexName, Book entity) {
            if ("isbn".equals(indexName)) {
                return entity.getIsbn() != null ? entity.getIsbn().toLowerCase() : null;
            }
            return null;
        }
    }

    private TestBookRepository createRepo() {
        TestBookRepository repo = new TestBookRepository(tempDir.resolve("books-indexed.json"));
        return repo;
    }

    private Book buildBook(String id, String isbn) {
        return Book.builder()
                .id(id)
                .isbn(isbn)
                .title("Test Book " + id)
                .author("Test Author")
                .totalQuantity(3)
                .availableQuantity(3)
                .reservedQuantity(0)
                .status(BookStatus.AVAILABLE)
                .build();
    }

    @Test
    @DisplayName("save() updates the secondary index so book can be found by ISBN")
    void saveUpdatesSecondaryIndex() {
        TestBookRepository repo = createRepo();
        Book book = buildBook("BK-1", "978-0-306-40615-7");
        repo.save(book);

        Optional<Book> found = repo.findBySecondaryKey("isbn", "978-0-306-40615-7");
        assertTrue(found.isPresent());
        assertEquals("BK-1", found.get().getId());
    }

    @Test
    @DisplayName("save() with a new entity (same ID, different ISBN) updates secondary index buckets")
    void saveWithChangedIsbnUpdatesIndex() {
        TestBookRepository repo = createRepo();
        Book original = buildBook("BK-1", "111-1111111111");
        repo.save(original);

        // Verify old ISBN is indexed
        assertTrue(repo.findBySecondaryKey("isbn", "111-1111111111").isPresent());

        // Save a NEW book object with same ID but different ISBN
        // (simulates a replace/update scenario where a fresh object is constructed)
        Book updated = Book.builder()
                .id("BK-1")
                .isbn("999-9999999999")
                .title("Test Book BK-1 Updated")
                .author("Test Author")
                .totalQuantity(3)
                .availableQuantity(3)
                .reservedQuantity(0)
                .status(BookStatus.AVAILABLE)
                .build();
        repo.save(updated);

        // Old ISBN should be gone from index
        assertFalse(repo.findBySecondaryKey("isbn", "111-1111111111").isPresent(),
                "Old ISBN should be removed from secondary index after update");

        // New ISBN should be in index
        Optional<Book> found = repo.findBySecondaryKey("isbn", "999-9999999999");
        assertTrue(found.isPresent());
        assertEquals("BK-1", found.get().getId());
    }

    @Test
    @DisplayName("deleteById() removes from primary AND secondary index")
    void deleteByIdRemovesFromBothIndexes() {
        TestBookRepository repo = createRepo();
        Book book = buildBook("BK-2", "222-2222222222");
        repo.save(book);

        // Verify it exists in both
        assertTrue(repo.findById("BK-2").isPresent());
        assertTrue(repo.findBySecondaryKey("isbn", "222-2222222222").isPresent());

        // Delete
        boolean deleted = repo.deleteById("BK-2");
        assertTrue(deleted);

        // Should be gone from both
        assertFalse(repo.findById("BK-2").isPresent());
        assertFalse(repo.findBySecondaryKey("isbn", "222-2222222222").isPresent());
    }

    @Test
    @DisplayName("count() returns correct count after saves and deletes")
    void countReturnsCorrectCount() {
        TestBookRepository repo = createRepo();
        assertEquals(0, repo.count());

        repo.save(buildBook("BK-1", "111-0000000001"));
        assertEquals(1, repo.count());

        repo.save(buildBook("BK-2", "111-0000000002"));
        repo.save(buildBook("BK-3", "111-0000000003"));
        assertEquals(3, repo.count());

        repo.deleteById("BK-2");
        assertEquals(2, repo.count());
    }

    @Test
    @DisplayName("deleteById() returns false for non-existent ID")
    void deleteNonExistentReturnsFalse() {
        TestBookRepository repo = createRepo();
        assertFalse(repo.deleteById("NON-EXISTENT"));
    }

    @Test
    @DisplayName("Multiple books with different ISBNs can all be found by secondary index")
    void multipleBooksByIsbn() {
        TestBookRepository repo = createRepo();
        repo.save(buildBook("BK-1", "ISBN-AAA"));
        repo.save(buildBook("BK-2", "ISBN-BBB"));
        repo.save(buildBook("BK-3", "ISBN-CCC"));

        assertEquals("BK-1", repo.findBySecondaryKey("isbn", "isbn-aaa").get().getId());
        assertEquals("BK-2", repo.findBySecondaryKey("isbn", "isbn-bbb").get().getId());
        assertEquals("BK-3", repo.findBySecondaryKey("isbn", "isbn-ccc").get().getId());
    }
}
