package com.library.model;

import com.library.enums.FineStatus;
import com.library.util.DateUtils;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A single fine levied on a member, linked to the borrow that caused it.
 */
public class Fine {

    private final String id;
    private final String registrationNumber;
    private final String borrowId;
    private final String bookId;
    private long amountPaise;
    private FineStatus status;
    private String createdBy;
    private String settledBy;
    private String reason;
    private final LocalDateTime createdAt;
    private LocalDateTime settledAt;
    private LocalDateTime updatedAt;

    public Fine(Builder b) {
        this.id = Objects.requireNonNull(b.id, "fine id");
        this.registrationNumber = Objects.requireNonNull(b.registrationNumber, "registration number");
        this.borrowId = b.borrowId;
        this.bookId = b.bookId;
        this.amountPaise = b.amountPaise;
        this.status = b.status == null ? FineStatus.PENDING : b.status;
        this.createdBy = b.createdBy;
        this.settledBy = b.settledBy;
        this.reason = b.reason;
        this.createdAt = b.createdAt == null ? DateUtils.now() : b.createdAt;
        this.settledAt = b.settledAt;
        this.updatedAt = b.updatedAt == null ? this.createdAt : b.updatedAt;
    }

    public String getId() { return id; }
    public String getRegistrationNumber() { return registrationNumber; }
    public String getBorrowId() { return borrowId; }
    public String getBookId() { return bookId; }
    public long getAmountPaise() { return amountPaise; }
    public double getAmount() { return amountPaise / 100.0; }
    public FineStatus getStatus() { return status; }
    public String getCreatedBy() { return createdBy; }
    public String getSettledBy() { return settledBy; }
    public String getReason() { return reason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getSettledAt() { return settledAt; }
    public LocalDateTime getPaidAt() { return settledAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setAmountPaise(long amountPaise) { this.amountPaise = amountPaise; touch(); }
    public void setStatus(FineStatus status) { this.status = status; touch(); }
    public void setSettledBy(String settledBy) { this.settledBy = settledBy; touch(); }
    public void setSettledAt(LocalDateTime settledAt) { this.settledAt = settledAt; touch(); }
    public void setReason(String reason) { this.reason = reason; touch(); }

    public boolean isPending() { return status == FineStatus.PENDING; }

    private void touch() {
        this.updatedAt = DateUtils.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String registrationNumber;
        private String borrowId;
        private String bookId;
        private long amountPaise;
        private FineStatus status;
        private String createdBy;
        private String settledBy;
        private String reason;
        private LocalDateTime createdAt;
        private LocalDateTime settledAt;
        private LocalDateTime updatedAt;

        public Builder id(String v) { this.id = v; return this; }
        public Builder registrationNumber(String v) { this.registrationNumber = v; return this; }
        public Builder borrowId(String v) { this.borrowId = v; return this; }
        public Builder bookId(String v) { this.bookId = v; return this; }
        public Builder amountPaise(long v) { this.amountPaise = v; return this; }
        public Builder status(FineStatus v) { this.status = v; return this; }
        public Builder createdBy(String v) { this.createdBy = v; return this; }
        public Builder settledBy(String v) { this.settledBy = v; return this; }
        public Builder reason(String v) { this.reason = v; return this; }
        public Builder createdAt(LocalDateTime v) { this.createdAt = v; return this; }
        public Builder settledAt(LocalDateTime v) { this.settledAt = v; return this; }
        public Builder updatedAt(LocalDateTime v) { this.updatedAt = v; return this; }

        public Fine build() {
            return new Fine(this);
        }
    }
}
