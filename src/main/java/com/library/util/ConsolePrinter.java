package com.library.util;

public final class ConsolePrinter {
    private ConsolePrinter() {}

    public static final String RESET = "\033[0m";
    public static final String BOLD = "\033[1m";
    public static final String RED = "\033[31m";
    public static final String GREEN = "\033[32m";
    public static final String YELLOW = "\033[33m";
    public static final String BLUE = "\033[34m";
    public static final String CYAN = "\033[36m";

    public static String colorize(String text, String color) { return color + text + RESET; }

    public static void printTitle(String title) {
        System.out.println(BOLD + "\n" + "=".repeat(60) + "\n  " + title + "\n" + "=".repeat(60) + RESET);
    }

    public static void printError(String message) { System.out.println(RED + "[ERROR] " + message + RESET); }
    public static void printSuccess(String message) { System.out.println(GREEN + "[OK] " + message + RESET); }
    public static void printWarning(String message) { System.out.println(YELLOW + "[WARN] " + message + RESET); }
    public static void printInfo(String message) { System.out.println(CYAN + "[INFO] " + message + RESET); }

    public static void printSection(String title) {
        System.out.println("\n" + "-".repeat(60) + "\n  " + title + "\n" + "-".repeat(60));
    }

    public static void printTable(java.util.List<String> headers, java.util.List<java.util.List<String>> rows) {
        int[] widths = new int[headers.size()];
        for (int i = 0; i < headers.size(); i++) widths[i] = headers.get(i).length();
        for (var row : rows) for (int i = 0; i < row.size() && i < widths.length; i++)
            widths[i] = Math.max(widths[i], row.get(i).length());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < headers.size(); i++) sb.append(String.format("%-" + (widths[i] + 3) + "s", headers.get(i)));
        System.out.println(sb);
        sb.setLength(0);
        for (int w : widths) sb.append("-".repeat(w + 3));
        System.out.println(sb);
        for (var row : rows) {
            sb.setLength(0);
            for (int i = 0; i < row.size() && i < widths.length; i++)
                sb.append(String.format("%-" + (widths[i] + 3) + "s", row.get(i)));
            System.out.println(sb);
        }
    }
}
