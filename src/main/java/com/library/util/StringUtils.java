package com.library.util;

public final class StringUtils {
    private StringUtils() {}
    public static String pad(String s, int width) { if (s == null) s = ""; if (s.length() >= width) return s.substring(0, width); return s + " ".repeat(width - s.length()); }
    public static String truncate(String s, int maxLen) { if (s == null) return ""; return s.length() <= maxLen ? s : s.substring(0, maxLen); }
    public static boolean isBlank(String s) { return s == null || s.isBlank(); }
}
