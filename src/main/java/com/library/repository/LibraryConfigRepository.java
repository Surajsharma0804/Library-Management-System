package com.library.repository;

import com.library.config.Constants;
import com.library.model.LibraryConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.library.util.JsonUtils;

/**
 * Repository for the singleton LibraryConfig entity.
 */
public final class LibraryConfigRepository {
    private final Path filePath = Path.of(Constants.SETTINGS_FILE);

    public LibraryConfig get() {
        if (!Files.exists(filePath)) return new LibraryConfig();
        try {
            String content = Files.readString(filePath);
            if (content.isBlank()) return new LibraryConfig();
            List<Map<String, Object>> list = JsonUtils.parseArray(content);
            for (Map<String, Object> m : list) {
                if ("config".equals(m.get("id"))) return fromMap(m);
            }
            return new LibraryConfig();
        } catch (IOException e) { return new LibraryConfig(); }
    }

    public void save(LibraryConfig config) {
        try {
            Files.createDirectories(filePath.getParent());
            Map<String, Object> m = toMap(config);
            Files.writeString(filePath, JsonUtils.prettyPrint(List.of(m)));
        } catch (IOException e) {
            throw new RuntimeException("Failed to write config", e);
        }
    }

    private Map<String, Object> toMap(LibraryConfig c) {
        Map<String, Object> m = new HashMap<>();
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

    @SuppressWarnings("unchecked")
    private LibraryConfig fromMap(Map<String, Object> m) {
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
