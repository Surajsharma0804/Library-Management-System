package com.library.mapper;

import com.library.interfaces.JsonMappable;
import com.library.model.LibraryConfig;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class LibraryConfigMapper implements JsonMappable<LibraryConfig> {
    @Override
    public Map<String, Object> toMap(LibraryConfig c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", "config");
        m.put("loanPeriodDays", c.getLoanPeriodDays());
        m.put("maxRenewals", c.getMaxRenewals());
        m.put("defaultBorrowLimit", c.getDefaultBorrowLimit());
        m.put("maxReservations", c.getMaxReservations());
        m.put("finePerDayPaise", c.getFinePerDayPaise());
        m.put("reservationHoldDays", c.getReservationHoldDays());
        m.put("membershipMonths", c.getMembershipMonths());
        List<String> holidays = new ArrayList<>();
        for (LocalDate d : c.getHolidays()) holidays.add(d.toString());
        m.put("holidays", holidays);
        return m;
    }

    @Override
    @SuppressWarnings("unchecked")
    public LibraryConfig fromMap(Map<String, Object> m) {
        LibraryConfig c = new LibraryConfig();
        c.setLoanPeriodDays(asInt(m.get("loanPeriodDays"), 14));
        c.setMaxRenewals(asInt(m.get("maxRenewals"), 2));
        c.setDefaultBorrowLimit(asInt(m.get("defaultBorrowLimit"), 5));
        c.setMaxReservations(asInt(m.get("maxReservations"), 3));
        c.setFinePerDayPaise(asLong(m.get("finePerDayPaise"), 500));
        c.setReservationHoldDays(asInt(m.get("reservationHoldDays"), 7));
        c.setMembershipMonths(asInt(m.get("membershipMonths"), 12));
        Object h = m.get("holidays");
        if (h instanceof List<?> list) {
            for (Object o : list) { if (o instanceof String s) c.addHoliday(LocalDate.parse(s)); }
        }
        return c;
    }
    private int asInt(Object v, int def) { return v instanceof Number n ? n.intValue() : def; }
    private long asLong(Object v, long def) { return v instanceof Number n ? n.longValue() : def; }
}
