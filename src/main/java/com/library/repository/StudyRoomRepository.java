package com.library.repository;

import com.library.config.Constants;
import com.library.mapper.StudyRoomMapper;
import com.library.model.StudyRoom;

import java.util.List;

/**
 * JSON-backed repository for {@link StudyRoom} entities.
 * Uses a secondary index for O(1) lookups by branchId.
 */
public final class StudyRoomRepository extends IndexedRepository<StudyRoom, String> {

    public StudyRoomRepository() {
        super(Constants.STUDY_ROOMS_FILE, new StudyRoomMapper(), StudyRoom::getId);
        registerSecondaryIndex("branchId");
    }

    @Override
    protected String secondaryKey(String indexName, StudyRoom entity) {
        return switch (indexName) {
            case "branchId" -> entity.getBranchId();
            default         -> null;
        };
    }

    /**
     * Returns all study rooms belonging to the given branch.
     *
     * @param branchId the branch identifier; returns an empty list if {@code null}
     * @return unmodifiable list of matching {@link StudyRoom}s
     */
    public List<StudyRoom> findByBranchId(String branchId) {
        if (branchId == null) {
            return List.of();
        }
        return findAllBySecondaryKey("branchId", branchId);
    }
}
