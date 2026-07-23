package com.library.repository;

import com.library.config.Constants;
import com.library.mapper.MembershipTierMapper;
import com.library.model.MembershipTier;

/**
 * JSON-backed repository for {@link MembershipTier} entities.
 * No secondary indexes are required; all lookups use the primary ID index.
 */
public final class MembershipTierRepository extends IndexedRepository<MembershipTier, String> {

    public MembershipTierRepository() {
        super(Constants.MEMBERSHIP_TIERS_FILE, new MembershipTierMapper(), MembershipTier::getId);
    }

    @Override
    protected String secondaryKey(String indexName, MembershipTier entity) {
        return null;
    }
}
