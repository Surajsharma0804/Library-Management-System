package com.library.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Date and time formatting/parsing utilities.
 */
public final class DateUtils {

    private DateUtils() {}

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static LocalDate today() { return LocalDate.now(); }
    public static LocalDateTime now() { return LocalDateTime.now(); }

    public static String format(LocalDate date) {
        return date == null ? null : DATE_FMT.format(date);
    }

    public static String formatDateTime(LocalDateTime dt) {
        return dt == null ? null : DATETIME_FMT.format(dt);
    }

    public static LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        return LocalDate.parse(s, DATE_FMT);
    }

    public static LocalDateTime parseDateTime(String s) {
        if (s == null || s.isBlank()) return null;
        return LocalDateTime.parse(s, DATETIME_FMT);
    }

    public static long daysBetween(LocalDate start, LocalDate end) {
        return java.time.temporal.ChronoUnit.DAYS.between(start, end);
    }

    public static LocalDate plusDays(LocalDate date, int days) {
        return date != null ? date.plusDays(days) : null;
    }

    public static LocalDate plusMonths(LocalDate date, int months) {
        return date != null ? date.plusMonths(months) : null;
    }
}
