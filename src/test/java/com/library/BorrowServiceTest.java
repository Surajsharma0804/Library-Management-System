package com.library;

import com.library.enums.BookStatus;
import com.library.enums.BorrowStatus;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Borrow service tests")
class BorrowServiceTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Create and save borrow record")
    void createAndSaveBorrow() {
        BorrowRepository repo = new BorrowRepository();
        repo.setOverrideFile(tempDir.resolve("borrow_records.json"));

        BorrowRecord record = BorrowRecord.builder()
                .id("BR-1").bookId("BK-1").registrationNumber("REG-001")
                .issueDate(LocalDate.now()).dueDate(LocalDate.now().plusDays(14))
                .issuedBy("librarian").build();
        repo.save(record);
        assertTrue(repo.findById("BR-1").isPresent());
    }

    @Test
    @DisplayName("Find active borrows")
    void findActiveBorrows() {
        BorrowRepository repo = new BorrowRepository();
        repo.setOverrideFile(tempDir.resolve("borrow_records.json"));

        BorrowRecord active = BorrowRecord.builder()
                .id("BR-1").bookId("BK-1").registrationNumber("REG-001")
                .issueDate(LocalDate.now()).dueDate(LocalDate.now().plusDays(14))
                .issuedBy("librarian").build();
        repo.save(active);
        assertEquals(1, repo.findAllActive().size());
    }
}
