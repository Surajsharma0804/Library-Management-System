package com.library.repository;

import com.library.config.Constants;
import com.library.mapper.BranchMapper;
import com.library.model.Branch;

/**
 * JSON-backed repository for {@link Branch} entities.
 * No secondary indexes are required; all lookups use the primary ID index.
 */
public final class BranchRepository extends IndexedRepository<Branch, String> {

    public BranchRepository() {
        super(Constants.BRANCHES_FILE, new BranchMapper(), Branch::getId);
    }

    @Override
    protected String secondaryKey(String indexName, Branch entity) {
        return null;
    }
}
