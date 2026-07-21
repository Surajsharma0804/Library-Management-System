package com.library.model;

import com.library.enums.BorrowStatus;
import com.library.util.DateUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A single loan transaction linking a book to a member.
 */
public class BorrowRecord {

    private final String id;
    private final String bookId;
    private final String registrationNumber;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private int renewCount;
    private long finePaise;
    private String issuedBy;
    private String receivedBy;
    private BorrowStatus status;
    private String remarks;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BorrowRecord(Builder b) {
        this.id = Objects.requireNonNull(b.id, "borrow id");
        this.bookId = Objects.requireNonNull(b.bookId, "book id");
        this.registrationNumber = Objects.requireNonNull(b.registrationNumber, "registration number");
        this.issueDate = Objects.requireNonNull(b.issueDate, "issue date");
        this.dueDate = Objects.requireNonNull(b.dueDate, "due date");
        this.returnDate = b.returnDate;
        this.renewCount = b.renewCount;
        this.finePaise = b.finePaise;
        this.issuedBy = b.issuedBy;
        this.receivedBy = b.receivedBy;
        this.status = b.status == null ? BorrowStatus.ACTIVE : b.status;
        this.remarks = b.remarks;
        this.createdAt = b.createdAt == null ? DateUtils.now() : b.createdAt;
        this.updatedAt = b.updatedAt == null ? this.createdAt : b.updatedAt;
    }

    public String getId() { return id; }
    public String getBookId() { return bookId; }
    public String getRegistrationNumber() { return registrationNumber; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public int getRenewCount() { return renewCount; }
    public long getFinePaise() { return finePaise; }
    public double getFine() { return finePaise / 100.0; }
    public String getIssuedBy() { return issuedBy; }
    public String getReceivedBy() { return receivedBy; }
    public BorrowStatus getStatus() { return status; }
    public String getRemarks() { return remarks; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; touch(); }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; touch(); }
    public void setFinePaise(long finePaise) { this.finePaise = finePaise; touch(); }
    public void setReceivedBy(String receivedBy) { this.receivedBy = receivedBy; touch(); }
    public void setStatus(BorrowStatus status) { this.status = status; touch(); }
    public void setRemarks(String remarks) { this.remarks = remarks; touch(); }
    public void incrementRenewCount() { this.renewCount++; touch(); }

    public boolean isOverdue() {
        return status == BorrowStatus.ACTIVE && dueDate.isBefore(DateUtils.today());
    }

    public boolean canRenew() {
        return status == BorrowStatus.ACTIVE && renewCount < 2;
    }

    public long overdueDays() {
        if (!isOverdue()) {
            return 0;
        }
        return DateUtils.daysBetween(dueDate, DateUtils.today());
    }

    public long remainingDays() {
        if (status != BorrowStatus.ACTIVE) {
            return 0;
        }
        return Math.max(0, DateUtils.daysBetween(DateUtils.today(), dueDate));
    }

    private void touch() {
        this.updatedAt = DateUtils.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String bookId;
        private String registrationNumber;
        private LocalDate issueDate;
        private LocalDate dueDate;
        private LocalDate returnDate;
        private int renewCount;
        private long finePaise;
        private String issuedBy;
        private String receivedBy;
        private BorrowStatus status;
        private String remarks;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(String v) { this.id = v; return this; }
        public Builder bookId(String v) { this.bookId = v; return this; }
        public Builder registrationNumber(String v) { this.registrationNumber = v; return this; }
        public Builder issueDate(LocalDate v) { this.issueDate = v; return this; }
        public Builder dueDate(LocalDate v) { this.dueDate = v; return this; }
        public Builder returnDate(LocalDate v) { this.returnDate = v; return this; }
        public Builder renewCount(int v) { this.renewCount = v; return this; }
        public Builder finePaise(long v) { this.finePaise = v; return this; }
        public Builder issuedBy(String v) { this.issuedBy = v; return this; }
        public Builder receivedBy(String v) { this.receivedBy = v; return this; }
        public Builder status(BorrowStatus v) { this.status = v; return this; }
        public Builder remarks(String v) { this.remarks = v; return this; }
        public Builder createdAt(LocalDateTime v) { this.createdAt = v; return this; }
        public Builder updatedAt(LocalDateTime v) { this.updatedAt = v; return this; }

        public BorrowRecord build() {
            return new BorrowRecord(this);
        }
    }
}
