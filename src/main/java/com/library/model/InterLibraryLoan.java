package com.library.model;

import com.library.enums.ILLDirection;
import com.library.enums.ILLStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an Inter-Library Loan (ILL) transaction between this library and a partner library.
 */
public final class InterLibraryLoan {
    private final String id;
    private final ILLDirection direction;
    private String partnerLibraryName;
    private String bookTitle;
    private String bookIsbn;
    private String requestedBy;
    private final LocalDate requestedDate;
    private LocalDate expectedReturnDate;
    private LocalDate actualReturnDate;
    private ILLStatus status;
    private String notes;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private InterLibraryLoan(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id cannot be null");
        this.direction = Objects.requireNonNull(builder.direction, "direction cannot be null");
        this.partnerLibraryName = Objects.requireNonNull(builder.partnerLibraryName, "partnerLibraryName cannot be null");
        this.bookTitle = Objects.requireNonNull(builder.bookTitle, "bookTitle cannot be null");
        this.bookIsbn = builder.bookIsbn;
        this.requestedBy = builder.requestedBy;
        this.requestedDate = builder.requestedDate != null ? builder.requestedDate : LocalDate.now();
        this.expectedReturnDate = builder.expectedReturnDate;
        this.actualReturnDate = builder.actualReturnDate;
        this.status = builder.status != null ? builder.status : ILLStatus.REQUESTED;
        this.notes = builder.notes;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        this.updatedAt = builder.updatedAt != null ? builder.updatedAt : this.createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public String getId() {
        return id;
    }

    public ILLDirection getDirection() {
        return direction;
    }

    public String getPartnerLibraryName() {
        return partnerLibraryName;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getBookIsbn() {
        return bookIsbn;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public LocalDate getRequestedDate() {
        return requestedDate;
    }

    public LocalDate getExpectedReturnDate() {
        return expectedReturnDate;
    }

    public LocalDate getActualReturnDate() {
        return actualReturnDate;
    }

    public ILLStatus getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters for mutable fields
    public void setPartnerLibraryName(String partnerLibraryName) {
        this.partnerLibraryName = Objects.requireNonNull(partnerLibraryName, "partnerLibraryName cannot be null");
        touch();
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = Objects.requireNonNull(bookTitle, "bookTitle cannot be null");
        touch();
    }

    public void setBookIsbn(String bookIsbn) {
        this.bookIsbn = bookIsbn;
        touch();
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
        touch();
    }

    public void setExpectedReturnDate(LocalDate expectedReturnDate) {
        this.expectedReturnDate = expectedReturnDate;
        touch();
    }

    public void setActualReturnDate(LocalDate actualReturnDate) {
        this.actualReturnDate = actualReturnDate;
        touch();
    }

    public void setStatus(ILLStatus status) {
        this.status = status != null ? status : ILLStatus.REQUESTED;
        touch();
    }

    public void setNotes(String notes) {
        this.notes = notes;
        touch();
    }

    private void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    public static final class Builder {
        private String id;
        private ILLDirection direction;
        private String partnerLibraryName;
        private String bookTitle;
        private String bookIsbn;
        private String requestedBy;
        private LocalDate requestedDate;
        private LocalDate expectedReturnDate;
        private LocalDate actualReturnDate;
        private ILLStatus status;
        private String notes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        private Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder direction(ILLDirection direction) {
            this.direction = direction;
            return this;
        }

        public Builder partnerLibraryName(String partnerLibraryName) {
            this.partnerLibraryName = partnerLibraryName;
            return this;
        }

        public Builder bookTitle(String bookTitle) {
            this.bookTitle = bookTitle;
            return this;
        }

        public Builder bookIsbn(String bookIsbn) {
            this.bookIsbn = bookIsbn;
            return this;
        }

        public Builder requestedBy(String requestedBy) {
            this.requestedBy = requestedBy;
            return this;
        }

        public Builder requestedDate(LocalDate requestedDate) {
            this.requestedDate = requestedDate;
            return this;
        }

        public Builder expectedReturnDate(LocalDate expectedReturnDate) {
            this.expectedReturnDate = expectedReturnDate;
            return this;
        }

        public Builder actualReturnDate(LocalDate actualReturnDate) {
            this.actualReturnDate = actualReturnDate;
            return this;
        }

        public Builder status(ILLStatus status) {
            this.status = status;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public InterLibraryLoan build() {
            return new InterLibraryLoan(this);
        }
    }
}
