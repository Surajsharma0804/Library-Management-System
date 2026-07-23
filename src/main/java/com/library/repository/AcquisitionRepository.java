package com.library.repository;

import com.library.config.Constants;
import com.library.enums.AcquisitionStatus;
import com.library.mapper.AcquisitionMapper;
import com.library.model.Acquisition;

import java.util.List;

/**
 * JSON-backed repository for {@link Acquisition} entities.
 * Uses secondary indexes for O(1) lookups by requestedBy and status.
 */
public final class AcquisitionRepository extends IndexedRepository<Acquisition, String> {

    public AcquisitionRepository() {
        super(Constants.ACQUISITIONS_FILE, new AcquisitionMapper(), Acquisition::getId);
        registerSecondaryIndex("requestedBy");
        registerSecondaryIndex("status");
    }

    @Override
    protected String secondaryKey(String indexName, Acquisition entity) {
        return switch (indexName) {
            case "requestedBy" -> entity.getRequestedBy();
            case "status"      -> entity.getStatus() != null ? entity.getStatus().name() : null;
            default            -> null;
        };
    }

    /**
     * Returns all acquisition requests submitted by the given user identifier.
     *
     * @param requestedBy the username or identifier of the requester;
     *                    returns an empty list if {@code null}
     * @return unmodifiable list of matching {@link Acquisition}s
     */
    public List<Acquisition> findByRequestedBy(String requestedBy) {
        if (requestedBy == null) {
            return List.of();
        }
        return findAllBySecondaryKey("requestedBy", requestedBy);
    }

    /**
     * Returns all acquisitions with the given status.
     *
     * @param status the acquisition status to filter by; returns an empty list if {@code null}
     * @return unmodifiable list of matching {@link Acquisition}s
     */
    public List<Acquisition> findByStatus(AcquisitionStatus status) {
        if (status == null) {
            return List.of();
        }
        return findAllBySecondaryKey("status", status.name());
    }
}
