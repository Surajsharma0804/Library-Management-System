package com.library.util;

import java.util.List;
import java.util.Scanner;

/**
 * Wrapper around Scanner providing validated input helpers.
 */
public final class ConsoleInput {
    private final Scanner scanner;
    public ConsoleInput(Scanner scanner) { this.scanner = scanner; }
    public String readLine(String prompt) { System.out.print(prompt); return scanner.nextLine().trim(); }
    public String readPassword(String prompt) { System.out.print(prompt); return scanner.nextLine().trim(); }
    public int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int v = Integer.parseInt(line);
                if (v >= min && v <= max) return v;
                System.out.println("Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) { System.out.println("Invalid number. Please try again."); }
        }
    }
    public long readLong(String prompt, long min, long max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                long v = Long.parseLong(line);
                if (v >= min && v <= max) return v;
                System.out.println("Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) { System.out.println("Invalid number. Please try again."); }
        }
    }
    public String readOptional(String prompt) { System.out.print(prompt); return scanner.nextLine().trim(); }
    public String readChoice(String prompt, List<String> options) {
        System.out.println(prompt);
        for (int i = 0; i < options.size(); i++) System.out.println((i + 1) + ". " + options.get(i));
        int choice = readInt("Choose: ", 0, options.size());
        if (choice == 0) return "";
        return options.get(choice - 1);
    }
    public java.time.LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try { return java.time.LocalDate.parse(line); }
            catch (Exception e) { System.out.println("Invalid date format. Use YYYY-MM-DD."); }
        }
    }
}
