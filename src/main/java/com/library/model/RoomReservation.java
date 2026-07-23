package com.library.model;

import com.library.enums.RoomReservationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A time-slotted booking of a {@link StudyRoom} by a student.
 */
public class RoomReservation {

    private final String id;
    private final String roomId;
    private final String registrationNumber;
    private final LocalDate date;
    private String startTime;
    private String endTime;
    private RoomReservationStatus status;
    private final LocalDateTime createdAt;

    public RoomReservation(Builder b) {
        this.id = Objects.requireNonNull(b.id, "room reservation id");
        this.roomId = Objects.requireNonNull(b.roomId, "room id");
        this.registrationNumber = Objects.requireNonNull(b.registrationNumber, "registration number");
        this.date = Objects.requireNonNull(b.date, "reservation date");
        this.startTime = b.startTime;
        this.endTime = b.endTime;
        this.status = b.status == null ? RoomReservationStatus.CONFIRMED : b.status;
        this.createdAt = b.createdAt == null ? LocalDateTime.now() : b.createdAt;
    }

    public String getId() { return id; }
    public String getRoomId() { return roomId; }
    public String getRegistrationNumber() { return registrationNumber; }
    public LocalDate getDate() { return date; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public RoomReservationStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void setStatus(RoomReservationStatus status) { this.status = status; }

    /** Cancels this reservation. */
    public void cancel() {
        this.status = RoomReservationStatus.CANCELLED;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String roomId;
        private String registrationNumber;
        private LocalDate date;
        private String startTime;
        private String endTime;
        private RoomReservationStatus status;
        private LocalDateTime createdAt;

        public Builder id(String v) { this.id = v; return this; }
        public Builder roomId(String v) { this.roomId = v; return this; }
        public Builder registrationNumber(String v) { this.registrationNumber = v; return this; }
        public Builder date(LocalDate v) { this.date = v; return this; }
        public Builder startTime(String v) { this.startTime = v; return this; }
        public Builder endTime(String v) { this.endTime = v; return this; }
        public Builder status(RoomReservationStatus v) { this.status = v; return this; }
        public Builder createdAt(LocalDateTime v) { this.createdAt = v; return this; }

        public RoomReservation build() {
            return new RoomReservation(this);
        }
    }
}
