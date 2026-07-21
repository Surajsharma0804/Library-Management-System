package com.library.builder;

import com.library.enums.BorrowStatus;
import com.library.model.BorrowRecord;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Standalone builder for {@link BorrowRecord} entities.
 * Delegates to the inner Builder.
 */
public class BorrowRecordBuilder {
    private final BorrowRecord.Builder delegate = BorrowRecord.builder();

    public BorrowRecordBuilder id(String id) { delegate.id(id); return this; }
    public BorrowRecordBuilder bookId(String bookId) { delegate.bookId(bookId); return this; }
    public BorrowRecordBuilder registrationNumber(String regNo) { delegate.registrationNumber(regNo); return this; }
    public BorrowRecordBuilder issueDate(LocalDate date) { delegate.issueDate(date); return this; }
    public BorrowRecordBuilder dueDate(LocalDate date) { delegate.dueDate(date); return this; }
    public BorrowRecordBuilder returnDate(LocalDate date) { delegate.returnDate(date); return this; }
    public BorrowRecordBuilder renewCount(int count) { delegate.renewCount(count); return this; }
    public BorrowRecordBuilder finePaise(long paise) { delegate.finePaise(paise); return this; }
    public BorrowRecordBuilder issuedBy(String issuedBy) { delegate.issuedBy(issuedBy); return this; }
    public BorrowRecordBuilder receivedBy(String receivedBy) { delegate.receivedBy(receivedBy); return this; }
    public BorrowRecordBuilder status(BorrowStatus status) { delegate.status(status); return this; }
    public BorrowRecordBuilder remarks(String remarks) { delegate.remarks(remarks); return this; }
    public BorrowRecordBuilder createdAt(LocalDateTime createdAt) { delegate.createdAt(createdAt); return this; }

    public BorrowRecord build() {
        return delegate.build();
    }
}
