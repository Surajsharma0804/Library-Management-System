package com.library.model;

/**
 * Data model for the admin dashboard view.
 */
public class DashboardSummary {
    private final int totalBooks;
    private final int totalStudents;
    private final int activeStudents;
    private final int totalStaff;
    private final int overdueBooks;
    private final int pendingReservations;
    private final int pendingFines;
    private final long totalFineAmountPaise;
    private final int totalBorrows;
    private final int totalReturns;

    public DashboardSummary(int totalBooks, int totalStudents, int activeStudents,
                          int totalStaff, int overdueBooks, int pendingReservations,
                          int pendingFines, long totalFineAmountPaise,
                          int totalBorrows, int totalReturns) {
        this.totalBooks = totalBooks;
        this.totalStudents = totalStudents;
        this.activeStudents = activeStudents;
        this.totalStaff = totalStaff;
        this.overdueBooks = overdueBooks;
        this.pendingReservations = pendingReservations;
        this.pendingFines = pendingFines;
        this.totalFineAmountPaise = totalFineAmountPaise;
        this.totalBorrows = totalBorrows;
        this.totalReturns = totalReturns;
    }

    public int getTotalBooks() { return totalBooks; }
    public int getTotalStudents() { return totalStudents; }
    public int getActiveStudents() { return activeStudents; }
    public int getTotalStaff() { return totalStaff; }
    public int getOverdueBooks() { return overdueBooks; }
    public int getPendingReservations() { return pendingReservations; }
    public int getPendingFines() { return pendingFines; }
    public long getTotalFineAmountPaise() { return totalFineAmountPaise; }
    public int getTotalBorrows() { return totalBorrows; }
    public int getTotalReturns() { return totalReturns; }
}
