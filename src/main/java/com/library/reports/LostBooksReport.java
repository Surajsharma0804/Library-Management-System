package com.library.reports;

import com.library.enums.BookStatus;
import com.library.repository.BookRepository;
import java.util.List;

public final class LostBooksReport implements ReportStrategy {
    private final BookRepository repo;
    public LostBooksReport(BookRepository repo) { this.repo = repo; }
    @Override public String id() { return "lost-books"; }
    @Override public String title() { return "Lost Books Report"; }
    @Override public ReportData generate() {
        var lost = repo.findAll(b -> b.getStatus() == BookStatus.LOST);
        var rows = lost.stream().map(b -> List.of(b.getId(), b.getIsbn(), b.getTitle(), b.getAuthor())).toList();
        return new ReportData(title(), List.of("ID", "ISBN", "Title", "Author"), rows, "Lost books: " + lost.size());
    }
}
