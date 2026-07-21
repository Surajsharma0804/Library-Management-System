package com.library.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Runtime configuration for the library system.
 */
public final class LibraryConfig {
    private int loanPeriodDays = 14;
    private int maxRenewals = 2;
    private int defaultBorrowLimit = 5;
    private int maxReservations = 3;
    private long finePerDayPaise = 500;
    private int reservationHoldDays = 7;
    private int membershipMonths = 12;
    private final List<LocalDate> holidays = new ArrayList<>();

    public int getLoanPeriodDays() { return loanPeriodDays; }
    public void setLoanPeriodDays(int v) { this.loanPeriodDays = v; }
    public int getMaxRenewals() { return maxRenewals; }
    public void setMaxRenewals(int v) { this.maxRenewals = v; }
    public int getDefaultBorrowLimit() { return defaultBorrowLimit; }
    public void setDefaultBorrowLimit(int v) { this.defaultBorrowLimit = v; }
    public int getMaxReservations() { return maxReservations; }
    public void setMaxReservations(int v) { this.maxReservations = v; }
    public long getFinePerDayPaise() { return finePerDayPaise; }
    public long getFinePerDay() { return finePerDayPaise; }
    public void setFinePerDayPaise(long v) { this.finePerDayPaise = v; }
    public int getReservationHoldDays() { return reservationHoldDays; }
    public void setReservationHoldDays(int v) { this.reservationHoldDays = v; }
    public int getMembershipMonths() { return membershipMonths; }
    public void setMembershipMonths(int v) { this.membershipMonths = v; }
    public List<LocalDate> getHolidays() { return new ArrayList<>(holidays); }
    public void addHoliday(LocalDate d) { holidays.add(d); }
    public void removeHoliday(LocalDate d) { holidays.remove(d); }
}
