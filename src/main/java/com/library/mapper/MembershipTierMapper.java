package com.library.mapper;

import com.library.interfaces.JsonMappable;
import com.library.model.MembershipTier;
import com.library.util.DateUtils;
import com.library.util.JsonUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps {@link MembershipTier} to and from JSON-ready maps.
 */
public final class MembershipTierMapper implements JsonMappable<MembershipTier> {

    @Override
    public Map<String, Object> toMap(MembershipTier tier) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", tier.getId());
        m.put("tierName", tier.getTierName());
        m.put("borrowLimit", tier.getBorrowLimit());
        m.put("loanPeriodDays", tier.getLoanPeriodDays());
        m.put("renewalLimit", tier.getRenewalLimit());
        m.put("maxActiveReservations", tier.getMaxActiveReservations());
        m.put("createdAt", DateUtils.formatDateTime(tier.getCreatedAt()));
        m.put("updatedAt", DateUtils.formatDateTime(tier.getUpdatedAt()));
        return m;
    }

    @Override
    public MembershipTier fromMap(Map<String, Object> m) {
        MembershipTier.Builder b = MembershipTier.builder()
                .id(JsonUtils.requireString(m, "id"))
                .tierName(JsonUtils.getString(m, "tierName"))
                .borrowLimit(JsonUtils.getInt(m, "borrowLimit") == null ? 0 : JsonUtils.getInt(m, "borrowLimit"))
                .loanPeriodDays(JsonUtils.getInt(m, "loanPeriodDays") == null ? 0 : JsonUtils.getInt(m, "loanPeriodDays"))
                .renewalLimit(JsonUtils.getInt(m, "renewalLimit") == null ? 0 : JsonUtils.getInt(m, "renewalLimit"))
                .maxActiveReservations(JsonUtils.getInt(m, "maxActiveReservations") == null ? 0 : JsonUtils.getInt(m, "maxActiveReservations"))
                .createdAt(DateUtils.parseDateTime(JsonUtils.getString(m, "createdAt")))
                .updatedAt(DateUtils.parseDateTime(JsonUtils.getString(m, "updatedAt")));
        return b.build();
    }
}
