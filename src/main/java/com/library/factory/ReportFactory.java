package com.library.factory;

import com.library.reports.ReportStrategy;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRepository;
import com.library.repository.FineRepository;
import com.library.repository.ReservationRepository;
import com.library.repository.UserRepository;
import com.library.service.AnalyticsService;

public final class ReportFactory {

    private final BookRepository bookRepo;
    private final BorrowRepository borrowRepo;
    private final FineRepository fineRepo;
    private final ReservationRepository reservationRepo;
    private final UserRepository userRepo;
    private final AnalyticsService analytics;

    public ReportFactory(BookRepository bookRepo, BorrowRepository borrowRepo,
                         FineRepository fineRepo, ReservationRepository reservationRepo,
                         UserRepository userRepo, AnalyticsService analytics) {
        this.bookRepo = bookRepo;
        this.borrowRepo = borrowRepo;
        this.fineRepo = fineRepo;
        this.reservationRepo = reservationRepo;
        this.userRepo = userRepo;
        this.analytics = analytics;
    }

    public ReportStrategy createInventoryReport() {
        return com.library.reports.ReportFactory.inventoryReport(bookRepo);
    }

    public ReportStrategy createBorrowReport() {
        return com.library.reports.ReportFactory.borrowReport(borrowRepo, bookRepo);
    }

    public ReportStrategy createFineReport() {
        return com.library.reports.ReportFactory.fineReport(fineRepo, userRepo);
    }

    public ReportStrategy createOverdueReport() {
        return com.library.reports.ReportFactory.overdueReport(borrowRepo, bookRepo);
    }

    public ReportStrategy createReservationReport() {
        return com.library.reports.ReportFactory.reservationReport(reservationRepo);
    }

    public ReportStrategy createLostBooksReport() {
        return com.library.reports.ReportFactory.lostBooksReport(bookRepo);
    }

    public ReportStrategy createDamagedBooksReport() {
        return com.library.reports.ReportFactory.damagedBooksReport(bookRepo);
    }

    public ReportStrategy createPopularBooksReport() {
        return com.library.reports.ReportFactory.popularBooksReport(analytics);
    }

    public ReportStrategy createInactiveMembersReport() {
        return com.library.reports.ReportFactory.inactiveMembersReport(analytics);
    }

    public ReportStrategy createMonthlyReport(int year) {
        return com.library.reports.ReportFactory.monthlyReport(analytics, year);
    }

    public ReportStrategy createYearlyReport(int year) {
        return com.library.reports.ReportFactory.yearlyReport(borrowRepo, year);
    }
}
