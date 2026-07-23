package com.library.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a record of a lost book reported by a borrower.
 * Immutable after creation — use {@link Builder} to construct instances.
 */
public final class LostBookRecord {

    private final String id;
    private final String borrowRecordId;
    private final String bookId;
    private final String registrationNumber;
    private final long replacementCostPaise;
    private final LocalDate reportedDate;
    private final String notes;
    private final LocalDateTime createdAt;

    private LostBookRecord(Builder b) {
        this.id = Objects.requireNonNull(b.id, "id");
        this.borrowRecordId = Objects.requireNonNull(b.borrowRecordId, "borrowRecordId");
        this.bookId = Objects.requireNonNull(b.bookId, "bookId");
        this.registrationNumber = Objects.requireNonNull(b.registrationNumber, "registrationNumber");
        this.replacementCostPaise = b.replacementCostPaise;
        this.reportedDate = b.reportedDate == null ? LocalDate.now() : b.reportedDate;
        this.notes = b.notes;
        this.createdAt = b.createdAt == null ? LocalDateTime.now() : b.createdAt;
    }

    // --- Getters ---

    public String getId() { return id; }
    public String getBorrowRecordId() { return borrowRecordId; }
    public String getBookId() { return bookId; }
    public String getRegistrationNumber() { return registrationNumber; }
    public long getReplacementCostPaise() { return replacementCostPaise; }
    public LocalDate getReportedDate() { return reportedDate; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // --- Factory ---

    public static Builder builder() {
        return new Builder();
    }

    // --- Builder ---

    public static final class Builder {
        private String id;
        private String borrowRecordId;
        private String bookId;
        private String registrationNumber;
        private long replacementCostPaise;
        private LocalDate reportedDate;
        private String notes;
        private LocalDateTime createdAt;

        public Builder id(String v) { this.id = v; return this; }
        public Builder borrowRecordId(String v) { this.borrowRecordId = v; return this; }
        public Builder bookId(String v) { this.bookId = v; return this; }
        public Builder registrationNumber(String v) { this.registrationNumber = v; return this; }
        public Builder replacementCostPaise(long v) { this.replacementCostPaise = v; return this; }
        public Builder reportedDate(LocalDate v) { this.reportedDate = v; return this; }
        public Builder notes(String v) { this.notes = v; return this; }
        public Builder createdAt(LocalDateTime v) { this.createdAt = v; return this; }

        public LostBookRecord build() {
            return new LostBookRecord(this);
        }
    }
}
