package com.library.repository;

import com.library.config.Constants;
import com.library.mapper.BorrowMapper;
import com.library.model.BorrowRecord;

import java.util.List;

/**
 * JSON-backed repository for {@link BorrowRecord} entities.
 */
public final class BorrowRepository extends JsonRepository<BorrowRecord, String> {

    public BorrowRepository() {
        super(Constants.BORROW_RECORDS_FILE, new BorrowMapper(), BorrowRecord::getId);
    }

    public List<BorrowRecord> findByRegistrationNumber(String reg) {
        return findAll(r -> reg != null && reg.equals(r.getRegistrationNumber()));
    }

    public List<BorrowRecord> findActiveByRegistrationNumber(String reg) {
        return findAll(r -> reg != null && reg.equals(r.getRegistrationNumber())
                && r.getStatus() == com.library.enums.BorrowStatus.ACTIVE);
    }

    public List<BorrowRecord> findByBookId(String bookId) {
        return findAll(r -> bookId != null && bookId.equals(r.getBookId()));
    }

    public List<BorrowRecord> findActiveByBookId(String bookId) {
        return findAll(r -> bookId != null && bookId.equals(r.getBookId())
                && r.getStatus() == com.library.enums.BorrowStatus.ACTIVE);
    }

    public List<BorrowRecord> findAllActive() {
        return findAll(r -> r.getStatus() == com.library.enums.BorrowStatus.ACTIVE);
    }

    public List<BorrowRecord> findAllOverdue() {
        return findAllActive().stream()
                .filter(BorrowRecord::isOverdue)
                .toList();
    }
}
