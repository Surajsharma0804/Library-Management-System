package com.library.controller;

import com.library.service.AnalyticsService;
import com.library.service.AuditService;
import com.library.service.BackupService;
import com.library.facade.LibraryFacade;
import com.library.model.AuditLog;
import com.library.model.Librarian;
import com.library.model.LibraryConfig;
import com.library.reports.ReportData;
import com.library.service.ReportService;
import com.library.security.Permissions;
import com.library.security.Session;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Controller for administrator-only operations: configuration, librarian
 * management, backups, analytics, reports, and audit-log review.
 */
public final class AdminController extends BaseController {

    public AdminController(LibraryFacade facade) {
        super(facade);
    }

    // ---- Configuration ----

    public LibraryConfig viewConfig(Session session) {
        require(session, Permissions.CONFIG_VIEW);
        return facade.config().get();
    }

    public LibraryConfig updateLoanPeriod(Session session, int days) {
        require(session, Permissions.CONFIG_UPDATE);
        return facade.config().updateLoanPeriod(session, days);
    }

    public LibraryConfig updateMaxRenewals(Session session, int max) {
        require(session, Permissions.CONFIG_UPDATE);
        return facade.config().updateMaxRenewals(session, max);
    }

    public LibraryConfig updateBorrowLimit(Session session, int limit) {
        require(session, Permissions.CONFIG_UPDATE);
        return facade.config().updateBorrowLimit(session, limit);
    }

    public LibraryConfig updateMaxReservations(Session session, int max) {
        require(session, Permissions.CONFIG_UPDATE);
        return facade.config().updateMaxReservations(session, max);
    }

    public LibraryConfig updateFinePerDay(Session session, long paise) {
        require(session, Permissions.CONFIG_UPDATE);
        return facade.config().updateFinePerDay(session, paise);
    }

    public LibraryConfig updateReservationHoldDays(Session session, int days) {
        require(session, Permissions.CONFIG_UPDATE);
        return facade.config().updateReservationHoldDays(session, days);
    }

    public LibraryConfig updateMembershipMonths(Session session, int months) {
        require(session, Permissions.CONFIG_UPDATE);
        return facade.config().updateMembershipMonths(session, months);
    }

    public LibraryConfig addHoliday(Session session, LocalDate date) {
        require(session, Permissions.CONFIG_UPDATE);
        return facade.config().addHoliday(session, date);
    }

    public LibraryConfig removeHoliday(Session session, LocalDate date) {
        require(session, Permissions.CONFIG_UPDATE);
        return facade.config().removeHoliday(session, date);
    }

    // ---- Librarian management ----

    public Librarian addLibrarian(Session session, String firstName, String lastName, String email,
                                  String phone, String username, String password,
                                  Set<String> permissions) {
        require(session, Permissions.LIBRARIAN_ADD);
        return facade.librarians().addLibrarian(session, firstName, lastName, email, phone,
                username, password, permissions);
    }

    public Librarian updateLibrarian(Session session, Librarian librarian) {
        require(session, Permissions.LIBRARIAN_UPDATE);
        return facade.librarians().updateLibrarian(session, librarian);
    }

    public boolean removeLibrarian(Session session, String librarianId) {
        require(session, Permissions.LIBRARIAN_REMOVE);
        return facade.librarians().removeLibrarian(session, librarianId);
    }

    public String resetLibrarianPassword(Session session, String librarianId, String tempPassword) {
        require(session, Permissions.LIBRARIAN_RESET_PASSWORD);
        return facade.librarians().resetPassword(session, librarianId, tempPassword);
    }

    public Librarian assignPermissions(Session session, String librarianId, Set<String> permissions) {
        require(session, Permissions.LIBRARIAN_ASSIGN_PERMISSIONS);
        return facade.librarians().assignPermissions(session, librarianId, permissions);
    }

    public List<Librarian> findAllLibrarians(Session session) {
        require(session, Permissions.LIBRARIAN_VIEW);
        return facade.librarians().findAll();
    }

    // ---- Backup ----

    public String createBackup(Session session) {
        require(session, Permissions.BACKUP_CREATE);
        return facade.backup().createBackup(session);
    }

    public String restoreBackup(Session session, String backupDir) {
        require(session, Permissions.BACKUP_RESTORE);
        return facade.backup().restoreBackup(session, backupDir);
    }

    public List<Path> listBackups(Session session) {
        require(session, Permissions.BACKUP_CREATE);
        return facade.backup().listBackups();
    }

    // ---- Analytics ----

    public AnalyticsService analytics(Session session) {
        require(session, Permissions.ANALYTICS_VIEW);
        return facade.analytics();
    }

    // ---- Reports ----

    public ReportData generateReport(Session session, String reportId) {
        require(session, Permissions.REPORT_GENERATE);
        return facade.reports().generate(reportId);
    }

    public String exportReport(Session session, String reportId) {
        require(session, Permissions.REPORT_GENERATE);
        return facade.reports().exportToCsv(session, reportId);
    }

    public List<String> availableReports(Session session) {
        require(session, Permissions.REPORT_VIEW);
        return facade.reports().availableReportIds();
    }

    public String renderReport(Session session, String reportId) {
        require(session, Permissions.REPORT_VIEW);
        ReportData data = facade.reports().generate(reportId);
        return facade.reports().renderToText(data);
    }

    // ---- Audit ----

    public List<AuditLog> viewAuditLogs(Session session) {
        require(session, Permissions.AUDIT_VIEW);
        return facade.audit().findAll();
    }

    public List<AuditLog> viewAuditLogsByActor(Session session, String actorId) {
        require(session, Permissions.AUDIT_VIEW);
        return facade.audit().findByActor(actorId);
    }
}
