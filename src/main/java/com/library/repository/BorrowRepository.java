package com.library.repository;

import com.library.config.Constants;
import com.library.mapper.BorrowMapper;
import com.library.model.BorrowRecord;

import java.util.List;

/**
 * JSON-backed repository for {@link BorrowRecord} entities.
 * Uses secondary indexes for O(1) lookups by registrationNumber and bookId.
 */
public final class BorrowRepository extends IndexedRepository<BorrowRecord, String> {

    public BorrowRepository() {
        super(Constants.BORROW_RECORDS_FILE, new BorrowMapper(), BorrowRecord::getId);
        registerSecondaryIndex("registrationNumber");
        registerSecondaryIndex("bookId");
    }

    @Override
    protected String secondaryKey(String indexName, BorrowRecord entity) {
        return switch (indexName) {
            case "registrationNumber" -> entity.getRegistrationNumber();
            case "bookId"             -> entity.getBookId();
            default                   -> null;
        };
    }

    public List<BorrowRecord> findByRegistrationNumber(String reg) {
        return findAllBySecondaryKey("registrationNumber", reg);
    }

    public List<BorrowRecord> findActiveByRegistrationNumber(String reg) {
        return findAllBySecondaryKey("registrationNumber", reg).stream()
                .filter(r -> r.getStatus() == com.library.enums.BorrowStatus.ACTIVE)
                .toList();
    }

    public List<BorrowRecord> findByBookId(String bookId) {
        return findAllBySecondaryKey("bookId", bookId);
    }

    public List<BorrowRecord> findActiveByBookId(String bookId) {
        return findAllBySecondaryKey("bookId", bookId).stream()
                .filter(r -> r.getStatus() == com.library.enums.BorrowStatus.ACTIVE)
                .toList();
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
