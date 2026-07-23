package com.library.model;

import com.library.enums.AcquisitionStatus;
import com.library.util.DateUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a book acquisition request submitted by library staff.
 */
public class Acquisition {

    private final String id;
    private String requestedTitle;
    private String author;
    private String isbn;
    private int quantity;
    private long estimatedCostPaise;
    private final String requestedBy;
    private final LocalDate requestedDate;
    private AcquisitionStatus status;
    private String reviewerNotes;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Acquisition(Builder b) {
        this.id = Objects.requireNonNull(b.id, "acquisition id");
        this.requestedBy = Objects.requireNonNull(b.requestedBy, "requestedBy");
        this.requestedTitle = b.requestedTitle;
        this.author = b.author;
        this.isbn = b.isbn;
        this.quantity = b.quantity;
        this.estimatedCostPaise = b.estimatedCostPaise;
        this.requestedDate = b.requestedDate == null ? LocalDate.now() : b.requestedDate;
        this.status = b.status == null ? AcquisitionStatus.PENDING : b.status;
        this.reviewerNotes = b.reviewerNotes;
        this.createdAt = b.createdAt == null ? DateUtils.now() : b.createdAt;
        this.updatedAt = b.updatedAt == null ? this.createdAt : b.updatedAt;
    }

    // --- Getters ---

    public String getId() { return id; }
    public String getRequestedTitle() { return requestedTitle; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public int getQuantity() { return quantity; }
    public long getEstimatedCostPaise() { return estimatedCostPaise; }
    public String getRequestedBy() { return requestedBy; }
    public LocalDate getRequestedDate() { return requestedDate; }
    public AcquisitionStatus getStatus() { return status; }
    public String getReviewerNotes() { return reviewerNotes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // --- Setters for mutable fields (each calls touch()) ---

    public void setRequestedTitle(String requestedTitle) { this.requestedTitle = requestedTitle; touch(); }
    public void setAuthor(String author) { this.author = author; touch(); }
    public void setIsbn(String isbn) { this.isbn = isbn; touch(); }
    public void setQuantity(int quantity) { this.quantity = quantity; touch(); }
    public void setEstimatedCostPaise(long estimatedCostPaise) { this.estimatedCostPaise = estimatedCostPaise; touch(); }
    public void setStatus(AcquisitionStatus status) { this.status = status; touch(); }
    public void setReviewerNotes(String reviewerNotes) { this.reviewerNotes = reviewerNotes; touch(); }

    private void touch() {
        this.updatedAt = DateUtils.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String requestedTitle;
        private String author;
        private String isbn;
        private int quantity;
        private long estimatedCostPaise;
        private String requestedBy;
        private LocalDate requestedDate;
        private AcquisitionStatus status;
        private String reviewerNotes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(String v) { this.id = v; return this; }
        public Builder requestedTitle(String v) { this.requestedTitle = v; return this; }
        public Builder author(String v) { this.author = v; return this; }
        public Builder isbn(String v) { this.isbn = v; return this; }
        public Builder quantity(int v) { this.quantity = v; return this; }
        public Builder estimatedCostPaise(long v) { this.estimatedCostPaise = v; return this; }
        public Builder requestedBy(String v) { this.requestedBy = v; return this; }
        public Builder requestedDate(LocalDate v) { this.requestedDate = v; return this; }
        public Builder status(AcquisitionStatus v) { this.status = v; return this; }
        public Builder reviewerNotes(String v) { this.reviewerNotes = v; return this; }
        public Builder createdAt(LocalDateTime v) { this.createdAt = v; return this; }
        public Builder updatedAt(LocalDateTime v) { this.updatedAt = v; return this; }

        public Acquisition build() {
            return new Acquisition(this);
        }
    }
}
