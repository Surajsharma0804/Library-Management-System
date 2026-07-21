package com.library.service;

import com.library.model.LibraryConfig;
import com.library.repository.LibraryConfigRepository;
import com.library.security.Session;

import java.time.LocalDate;

/**
 * Service for system-wide configuration read/update.
 */
public final class ConfigService {
    private final LibraryConfigRepository repo;
    public ConfigService(LibraryConfigRepository repo) { this.repo = repo; }

    public LibraryConfig get() { return repo.get(); }

    public void updateConfig(LibraryConfig config) { repo.save(config); }

    public LibraryConfig updateLoanPeriod(Session session, int days) {
        LibraryConfig c = repo.get();
        c.setLoanPeriodDays(days);
        repo.save(c);
        return c;
    }

    public LibraryConfig updateMaxRenewals(Session session, int max) {
        LibraryConfig c = repo.get();
        c.setMaxRenewals(max);
        repo.save(c);
        return c;
    }

    public LibraryConfig updateBorrowLimit(Session session, int limit) {
        LibraryConfig c = repo.get();
        c.setDefaultBorrowLimit(limit);
        repo.save(c);
        return c;
    }

    public LibraryConfig updateMaxReservations(Session session, int max) {
        LibraryConfig c = repo.get();
        c.setMaxReservations(max);
        repo.save(c);
        return c;
    }

    public LibraryConfig updateFinePerDay(Session session, long paise) {
        LibraryConfig c = repo.get();
        c.setFinePerDayPaise(paise);
        repo.save(c);
        return c;
    }

    public LibraryConfig updateReservationHoldDays(Session session, int days) {
        LibraryConfig c = repo.get();
        c.setReservationHoldDays(days);
        repo.save(c);
        return c;
    }

    public LibraryConfig updateMembershipMonths(Session session, int months) {
        LibraryConfig c = repo.get();
        c.setMembershipMonths(months);
        repo.save(c);
        return c;
    }

    public LibraryConfig addHoliday(Session session, LocalDate date) {
        LibraryConfig c = repo.get();
        c.addHoliday(date);
        repo.save(c);
        return c;
    }

    public LibraryConfig removeHoliday(Session session, LocalDate date) {
        LibraryConfig c = repo.get();
        c.removeHoliday(date);
        repo.save(c);
        return c;
    }
}
