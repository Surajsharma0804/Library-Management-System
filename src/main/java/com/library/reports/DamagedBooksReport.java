package com.library.reports;

import com.library.enums.BookStatus;
import com.library.repository.BookRepository;
import java.util.List;

public final class DamagedBooksReport implements ReportStrategy {
    private final BookRepository repo;
    public DamagedBooksReport(BookRepository repo) { this.repo = repo; }
    @Override public String id() { return "damaged-books"; }
    @Override public String title() { return "Damaged Books Report"; }
    @Override public ReportData generate() {
        var damaged = repo.findAll(b -> b.getStatus() == BookStatus.DAMAGED);
        var rows = damaged.stream().map(b -> List.of(b.getId(), b.getIsbn(), b.getTitle(), b.getAuthor())).toList();
        return new ReportData(title(), List.of("ID", "ISBN", "Title", "Author"), rows, "Damaged books: " + damaged.size());
    }
}
