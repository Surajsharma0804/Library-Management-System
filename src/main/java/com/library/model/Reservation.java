package com.library.model;

import com.library.enums.ReservationStatus;
import com.library.util.DateUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A member's reservation of a book, with a queue position and lifecycle.
 */
public class Reservation {

    private final String id;
    private final String bookId;
    private final String registrationNumber;
    private LocalDate reservationDate;
    private LocalDate expiryDate;
    private int queuePosition;
    private ReservationStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Reservation(Builder b) {
        this.id = Objects.requireNonNull(b.id, "reservation id");
        this.bookId = Objects.requireNonNull(b.bookId, "book id");
        this.registrationNumber = Objects.requireNonNull(b.registrationNumber, "registration number");
        this.reservationDate = Objects.requireNonNull(b.reservationDate, "reservation date");
        this.expiryDate = b.expiryDate;
        this.queuePosition = b.queuePosition;
        this.status = b.status == null ? ReservationStatus.PENDING : b.status;
        this.createdAt = b.createdAt == null ? DateUtils.now() : b.createdAt;
        this.updatedAt = b.updatedAt == null ? this.createdAt : b.updatedAt;
    }

    public String getId() { return id; }
    public String getBookId() { return bookId; }
    public String getRegistrationNumber() { return registrationNumber; }
    public LocalDate getReservationDate() { return reservationDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public int getQueuePosition() { return queuePosition; }
    public ReservationStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; touch(); }
    public void setQueuePosition(int queuePosition) { this.queuePosition = queuePosition; touch(); }
    public void setStatus(ReservationStatus status) { this.status = status; touch(); }

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
        private LocalDate reservationDate;
        private LocalDate expiryDate;
        private int queuePosition;
        private ReservationStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(String v) { this.id = v; return this; }
        public Builder bookId(String v) { this.bookId = v; return this; }
        public Builder registrationNumber(String v) { this.registrationNumber = v; return this; }
        public Builder reservationDate(LocalDate v) { this.reservationDate = v; return this; }
        public Builder expiryDate(LocalDate v) { this.expiryDate = v; return this; }
        public Builder queuePosition(int v) { this.queuePosition = v; return this; }
        public Builder status(ReservationStatus v) { this.status = v; return this; }
        public Builder createdAt(LocalDateTime v) { this.createdAt = v; return this; }
        public Builder updatedAt(LocalDateTime v) { this.updatedAt = v; return this; }

        public Reservation build() {
            return new Reservation(this);
        }
    }
}
