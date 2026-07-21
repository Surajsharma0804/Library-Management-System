package com.library.dto;

import com.library.enums.FineStatus;

public class FineDTO {
    private String id;
    private String registrationNumber;
    private String bookId;
    private long amountPaise;
    private FineStatus status;
    private String reason;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String v) { this.registrationNumber = v; }
    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }
    public long getAmountPaise() { return amountPaise; }
    public void setAmountPaise(long v) { this.amountPaise = v; }
    public FineStatus getStatus() { return status; }
    public void setStatus(FineStatus status) { this.status = status; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
