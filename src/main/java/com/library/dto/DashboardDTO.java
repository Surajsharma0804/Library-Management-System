package com.library.dto;

/**
 * Data Transfer Object for dashboard display.
 * Aggregates key metrics for role-specific dashboards.
 */
public class DashboardDTO {
    private int totalBooks;
    private int availableBooks;
    private int borrowedBooks;
    private int totalStudents;
    private int activeStudents;
    private int overdueBooks;
    private int pendingReservations;
    private int pendingFines;
    private long totalFineAmountPaise;
    private int booksBorrowedByCurrentUser;
    private int remainingBorrowLimit;
    private long currentUserFinePaise;

    public int getTotalBooks() { return totalBooks; }
    public void setTotalBooks(int totalBooks) { this.totalBooks = totalBooks; }
    public int getAvailableBooks() { return availableBooks; }
    public void setAvailableBooks(int availableBooks) { this.availableBooks = availableBooks; }
    public int getBorrowedBooks() { return borrowedBooks; }
    public void setBorrowedBooks(int borrowedBooks) { this.borrowedBooks = borrowedBooks; }
    public int getTotalStudents() { return totalStudents; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }
    public int getActiveStudents() { return activeStudents; }
    public void setActiveStudents(int activeStudents) { this.activeStudents = activeStudents; }
    public int getOverdueBooks() { return overdueBooks; }
    public void setOverdueBooks(int overdueBooks) { this.overdueBooks = overdueBooks; }
    public int getPendingReservations() { return pendingReservations; }
    public void setPendingReservations(int pendingReservations) { this.pendingReservations = pendingReservations; }
    public int getPendingFines() { return pendingFines; }
    public void setPendingFines(int pendingFines) { this.pendingFines = pendingFines; }
    public long getTotalFineAmountPaise() { return totalFineAmountPaise; }
    public void setTotalFineAmountPaise(long totalFineAmountPaise) { this.totalFineAmountPaise = totalFineAmountPaise; }
    public int getBooksBorrowedByCurrentUser() { return booksBorrowedByCurrentUser; }
    public void setBooksBorrowedByCurrentUser(int booksBorrowedByCurrentUser) { this.booksBorrowedByCurrentUser = booksBorrowedByCurrentUser; }
    public int getRemainingBorrowLimit() { return remainingBorrowLimit; }
    public void setRemainingBorrowLimit(int remainingBorrowLimit) { this.remainingBorrowLimit = remainingBorrowLimit; }
    public long getCurrentUserFinePaise() { return currentUserFinePaise; }
    public void setCurrentUserFinePaise(long currentUserFinePaise) { this.currentUserFinePaise = currentUserFinePaise; }
}
