package com.library.repository;

import com.library.config.Constants;
import com.library.enums.FineStatus;
import com.library.mapper.FineMapper;
import com.library.model.Fine;

import java.util.List;

/**
 * JSON-backed repository for {@link Fine} entities.
 * Uses secondary index for O(1) lookups by registrationNumber.
 */
public final class FineRepository extends IndexedRepository<Fine, String> {

    public FineRepository() {
        super(Constants.FINES_FILE, new FineMapper(), Fine::getId);
        registerSecondaryIndex("registrationNumber");
    }

    @Override
    protected String secondaryKey(String indexName, Fine entity) {
        return switch (indexName) {
            case "registrationNumber" -> entity.getRegistrationNumber();
            default                   -> null;
        };
    }

    public List<Fine> findByRegistrationNumber(String reg) {
        return findAllBySecondaryKey("registrationNumber", reg);
    }

    public List<Fine> findPendingByRegistrationNumber(String reg) {
        return findAllBySecondaryKey("registrationNumber", reg).stream()
                .filter(f -> f.getStatus() == FineStatus.PENDING)
                .toList();
    }

    public List<Fine> findAllPending() {
        return findAll(f -> f.getStatus() == FineStatus.PENDING);
    }
}
