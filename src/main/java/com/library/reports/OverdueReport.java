package com.library.reports;

import com.library.model.Book;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRepository;
import java.util.List;

public final class OverdueReport implements ReportStrategy {
    private final BorrowRepository borrowRepo;
    private final BookRepository bookRepo;
    public OverdueReport(BorrowRepository borrowRepo, BookRepository bookRepo) { this.borrowRepo = borrowRepo; this.bookRepo = bookRepo; }
    @Override public String id() { return "overdue"; }
    @Override public String title() { return "Overdue Books Report"; }
    @Override public ReportData generate() {
        var overdue = borrowRepo.findAll().stream()
                .filter(r -> r.getDueDate() != null && r.getDueDate().isBefore(java.time.LocalDate.now())).toList();
        var rows = overdue.stream().map(r -> {
            Book b = bookRepo.findById(r.getBookId()).orElse(null);
            return List.of(r.getId(), r.getRegistrationNumber(), b != null ? b.getTitle() : r.getBookId(),
                    r.getDueDate() != null ? r.getDueDate().toString() : "");
        }).toList();
        return new ReportData(title(), List.of("Borrow ID", "Student", "Book", "Due Date"), rows, "Overdue books: " + overdue.size());
    }
}
