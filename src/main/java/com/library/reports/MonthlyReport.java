package com.library.reports;

import com.library.service.AnalyticsService;
import java.util.List;

public final class MonthlyReport implements ReportStrategy {
    private final AnalyticsService analytics;
    private final int year;
    public MonthlyReport(AnalyticsService analytics, int year) { this.analytics = analytics; this.year = year; }
    @Override public String id() { return "monthly"; }
    @Override public String title() { return "Monthly Report " + year; }
    @Override public ReportData generate() {
        var monthly = analytics.monthlyBorrowCounts(year);
        var rows = monthly.entrySet().stream().map(e -> List.of(e.getKey(), String.valueOf(e.getValue()))).toList();
        return new ReportData(title(), List.of("Month", "Borrows"), rows, "Year: " + year);
    }
}
