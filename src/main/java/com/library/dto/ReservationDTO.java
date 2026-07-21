package com.library.dto;

import java.time.LocalDate;

/**
 * Data Transfer Object for reservation display.
 */
public class ReservationDTO {
    private String id;
    private String bookId;
    private String bookTitle;
    private String studentId;
    private String studentName;
    private LocalDate reservedAt;
    private LocalDate expiresAt;
    private String status;

    public ReservationDTO() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public LocalDate getReservedAt() { return reservedAt; }
    public void setReservedAt(LocalDate reservedAt) { this.reservedAt = reservedAt; }
    public LocalDate getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDate expiresAt) { this.expiresAt = expiresAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
