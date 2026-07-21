package com.library.reports;

import com.library.model.BorrowRecord;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRepository;

import java.util.List;

/**
 * Report strategy for borrow history.
 */
public final class BorrowReport implements ReportStrategy {

    private final BorrowRepository borrowRepo;
    private final BookRepository bookRepo;

    public BorrowReport(BorrowRepository borrowRepo, BookRepository bookRepo) {
        this.borrowRepo = borrowRepo;
        this.bookRepo = bookRepo;
    }

    @Override
    public String id() {
        return "borrows";
    }

    @Override
    public String title() {
        return "Borrow Report";
    }

    @Override
    public ReportData generate() {
        var records = borrowRepo.findAll();
        var rows = records.stream()
                .map(r -> List.of(
                        r.getId(),
                        r.getBookId(),
                        r.getRegistrationNumber(),
                        r.getIssueDate() != null ? r.getIssueDate().toString() : "",
                        r.getDueDate() != null ? r.getDueDate().toString() : "",
                        r.getStatus().name()
                ))
                .toList();

        return new ReportData(
                title(),
                List.of("Borrow ID", "Book ID", "Student Reg No", "Issue Date", "Due Date", "Status"),
                rows,
                "Total borrow records: " + records.size()
        );
    }
}
