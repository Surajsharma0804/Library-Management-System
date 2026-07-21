package com.library.validator;

import com.library.exception.ValidationException;
import java.util.regex.Pattern;

/**
 * Format validators for ISBN, email, phone, names, years, etc.
 */
public final class FormatValidators {
    private FormatValidators() {}
    private static final Pattern ISBN_13 = Pattern.compile("^\\d{13}$");
    private static final Pattern ISBN_10 = Pattern.compile("^\\d{9}[\\dXx]$");
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^[+]?[\\d\\s-]{10,15}$");
    private static final Pattern NAME = Pattern.compile("^[A-Za-z][A-Za-z\\s.'-]{1,49}$");

    public static void validateIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) throw new ValidationException("ISBN cannot be empty.");
        String clean = isbn.replace("-", "").replace(" ", "");
        if (ISBN_13.matcher(clean).matches()) {
            int sum = 0;
            for (int i = 0; i < 12; i++) { int d = clean.charAt(i) - '0'; sum += (i % 2 == 0) ? d : d * 3; }
            int check = (10 - (sum % 10)) % 10;
            if (check != (clean.charAt(12) - '0')) throw new ValidationException("Invalid ISBN check digit: " + isbn);
        } else if (ISBN_10.matcher(clean).matches()) {
            int sum = 0;
            for (int i = 0; i < 9; i++) sum += (clean.charAt(i) - '0') * (10 - i);
            char last = clean.charAt(9);
            int check = last == 'X' || last == 'x' ? 10 : (last - '0');
            if ((sum + check) % 11 != 0) throw new ValidationException("Invalid ISBN check digit: " + isbn);
        } else throw new ValidationException("Invalid ISBN: " + isbn);
    }

    public static void validateEmail(String email) {
        if (email == null || email.isBlank()) return;
        if (!EMAIL.matcher(email).matches()) throw new com.library.exception.ValidationException("Invalid email: " + email);
    }

    public static void validatePhone(String phone) {
        if (phone == null || phone.isBlank()) return;
        if (!PHONE.matcher(phone).matches()) throw new com.library.exception.ValidationException("Invalid phone: " + phone);
    }

    public static void validateName(String name) { validateName(name, "Name"); }
    public static void validateName(String name, String field) {
        if (name == null || name.isBlank()) throw new ValidationException(field + " cannot be empty.");
        if (!NAME.matcher(name).matches()) throw new ValidationException("Invalid " + field + ": " + name);
    }

    public static void validateYear(int year) {
        int current = java.time.LocalDate.now().getYear();
        if (year <= 1000 || year > current + 1) throw new ValidationException("Invalid year: " + year);
    }

    public static void validatePublicationYear(int year) {
        int current = java.time.LocalDate.now().getYear();
        if (year <= 1000 || year > current + 1) throw new ValidationException("Invalid publication year: " + year);
    }

    public static void validateRegistrationNumber(String reg) {
        if (reg == null || reg.isBlank()) throw new com.library.exception.InvalidRegistrationException("Registration number cannot be empty.");
        if (!reg.matches("^[A-Za-z][A-Za-z0-9-]{2,19}$")) throw new com.library.exception.InvalidRegistrationException("Invalid registration number: " + reg);
    }
}
