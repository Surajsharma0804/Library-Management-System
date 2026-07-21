package com.library.reports;

import com.library.repository.BookRepository;
import com.library.repository.BorrowRepository;
import com.library.repository.FineRepository;
import com.library.repository.ReservationRepository;
import com.library.repository.UserRepository;
import com.library.service.AnalyticsService;

public final class ReportFactory {
    private ReportFactory() {}

    public static ReportStrategy inventoryReport(BookRepository bookRepo) {
        return new InventoryReport(bookRepo);
    }
    public static ReportStrategy borrowReport(BorrowRepository borrowRepo, BookRepository bookRepo) {
        return new BorrowReport(borrowRepo, bookRepo);
    }
    public static ReportStrategy fineReport(FineRepository fineRepo, UserRepository userRepo) {
        return new FineReport(fineRepo, userRepo);
    }
    public static ReportStrategy overdueReport(BorrowRepository borrowRepo, BookRepository bookRepo) {
        return new OverdueReport(borrowRepo, bookRepo);
    }
    public static ReportStrategy reservationReport(ReservationRepository reservationRepo) {
        return new ReservationReport(reservationRepo);
    }
    public static ReportStrategy lostBooksReport(BookRepository bookRepo) {
        return new LostBooksReport(bookRepo);
    }
    public static ReportStrategy damagedBooksReport(BookRepository bookRepo) {
        return new DamagedBooksReport(bookRepo);
    }
    public static ReportStrategy popularBooksReport(AnalyticsService analytics) {
        return new PopularBooksReport(analytics);
    }
    public static ReportStrategy inactiveMembersReport(AnalyticsService analytics) {
        return new InactiveMembersReport(analytics);
    }
    public static ReportStrategy monthlyReport(AnalyticsService analytics, int year) {
        return new MonthlyReport(analytics, year);
    }
    public static ReportStrategy yearlyReport(BorrowRepository borrowRepo, int year) {
        return new YearlyReport(borrowRepo, year);
    }
}
