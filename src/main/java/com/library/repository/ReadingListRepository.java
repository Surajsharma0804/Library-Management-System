package com.library.repository;

import com.library.config.Constants;
import com.library.mapper.ReadingListMapper;
import com.library.model.ReadingList;

import java.util.List;

/**
 * JSON-backed repository for {@link ReadingList} entities.
 * Uses a secondary index for O(1) lookups by registrationNumber.
 */
public final class ReadingListRepository extends IndexedRepository<ReadingList, String> {

    public ReadingListRepository() {
        super(Constants.READING_LISTS_FILE, new ReadingListMapper(), ReadingList::getId);
        registerSecondaryIndex("registrationNumber");
    }

    @Override
    protected String secondaryKey(String indexName, ReadingList entity) {
        return switch (indexName) {
            case "registrationNumber" -> entity.getRegistrationNumber();
            default                   -> null;
        };
    }

    /**
     * Returns all reading lists owned by the given student registration number.
     *
     * @param reg the student registration number; returns an empty list if {@code null}
     * @return unmodifiable list of matching {@link ReadingList}s
     */
    public List<ReadingList> findByRegistrationNumber(String reg) {
        if (reg == null) {
            return List.of();
        }
        return findAllBySecondaryKey("registrationNumber", reg);
    }
}
