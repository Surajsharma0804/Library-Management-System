package com.library.repository;

import com.library.config.Constants;
import com.library.mapper.LostBookMapper;
import com.library.model.LostBookRecord;

import java.util.List;

/**
 * JSON-backed repository for {@link LostBookRecord} entities.
 * Uses a secondary index for O(1) lookups by registrationNumber.
 */
public final class LostBookRepository extends IndexedRepository<LostBookRecord, String> {

    public LostBookRepository() {
        super(Constants.LOST_BOOKS_FILE, new LostBookMapper(), LostBookRecord::getId);
        registerSecondaryIndex("registrationNumber");
    }

    @Override
    protected String secondaryKey(String indexName, LostBookRecord entity) {
        return switch (indexName) {
            case "registrationNumber" -> entity.getRegistrationNumber();
            default                   -> null;
        };
    }

    /**
     * Returns all lost-book records for the given student registration number.
     *
     * @param reg the student registration number; returns an empty list if {@code null}
     * @return unmodifiable list of matching {@link LostBookRecord}s
     */
    public List<LostBookRecord> findByRegistrationNumber(String reg) {
        if (reg == null) {
            return List.of();
        }
        return findAllBySecondaryKey("registrationNumber", reg);
    }
}
