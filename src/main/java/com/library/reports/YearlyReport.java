package com.library.reports;

import com.library.repository.BorrowRepository;
import java.util.List;

public final class YearlyReport implements ReportStrategy {
    private final BorrowRepository borrowRepo;
    private final int year;

    public YearlyReport(BorrowRepository borrowRepo, int year) {
        this.borrowRepo = borrowRepo;
        this.year = year;
    }

    @Override public String id() { return "yearly-" + year; }
    @Override public String title() { return "Yearly Report " + year; }

    @Override
    public ReportData generate() {
        var all = borrowRepo.findAll();
        var yearBorrows = all.stream().filter(b -> b.getIssueDate() != null && b.getIssueDate().getYear() == year).toList();
        var rows = List.of(
            List.of("Total Borrows", String.valueOf(yearBorrows.size())),
            List.of("Active", String.valueOf(yearBorrows.stream().filter(b -> b.getStatus() == com.library.enums.BorrowStatus.ACTIVE).count())),
            List.of("Returned", String.valueOf(yearBorrows.stream().filter(b -> b.getStatus() == com.library.enums.BorrowStatus.RETURNED).count()))
        );
        return new ReportData(title(), List.of("Metric", "Value"), rows, "Year: " + year);
    }
}
