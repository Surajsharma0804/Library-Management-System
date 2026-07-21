package com.library.repository;

import com.library.config.Constants;
import com.library.enums.FineStatus;
import com.library.mapper.FineMapper;
import com.library.model.Fine;

import java.util.List;

/**
 * JSON-backed repository for {@link Fine} entities.
 */
public final class FineRepository extends JsonRepository<Fine, String> {

    public FineRepository() {
        super(Constants.FINES_FILE, new FineMapper(), Fine::getId);
    }

    public List<Fine> findByRegistrationNumber(String reg) {
        return findAll(f -> reg != null && reg.equals(f.getRegistrationNumber()));
    }

    public List<Fine> findPendingByRegistrationNumber(String reg) {
        return findAll(f -> reg != null && reg.equals(f.getRegistrationNumber())
                && f.getStatus() == FineStatus.PENDING);
    }

    public List<Fine> findAllPending() {
        return findAll(f -> f.getStatus() == FineStatus.PENDING);
    }
}
