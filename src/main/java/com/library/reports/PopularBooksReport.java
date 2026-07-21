package com.library.reports;

import com.library.service.AnalyticsService;
import java.util.List;

public final class PopularBooksReport implements ReportStrategy {
    private final AnalyticsService analytics;
    public PopularBooksReport(AnalyticsService analytics) { this.analytics = analytics; }
    @Override public String id() { return "popular-books"; }
    @Override public String title() { return "Popular Books Report"; }
    @Override public ReportData generate() {
        var popular = analytics.popularBooks(10);
        var rows = popular.stream().map(e -> List.of(e.getKey(), String.valueOf(e.getValue()))).toList();
        return new ReportData(title(), List.of("Book ID", "Borrow Count"), rows, "Top " + popular.size() + " most borrowed books");
    }
}
