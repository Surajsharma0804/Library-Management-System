package com.library.validator;

import com.library.enums.MembershipStatus;
import com.library.exception.*;
import com.library.model.LibraryConfig;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.Reservation;
import com.library.model.Student;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Business-rule validators that enforce domain invariants.
 */
public final class BusinessValidators {
    private BusinessValidators() {}
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    public static void validatePassword(String password) {
        if (password == null || password.isBlank()) throw new ValidationException("Password cannot be empty.");
        if (password.length() < 8) throw new ValidationException("Password must be at least 8 characters.");
        if (!PASSWORD_PATTERN.matcher(password).matches())
            throw new ValidationException("Password must contain at least one uppercase, one lowercase, and one digit.");
    }

    public static void validateBook(Book book, Predicate<String> isbnExists) {
        if (book == null) throw new ValidationException("Book cannot be null.");
        if (book.getIsbn() == null || book.getIsbn().isBlank()) throw new ValidationException("ISBN is required.");
        if (book.getTitle() == null || book.getTitle().isBlank()) throw new ValidationException("Title is required.");
        if (book.getAuthor() == null || book.getAuthor().isBlank()) throw new ValidationException("Author is required.");
        if (book.getTotalQuantity() < 0) throw new ValidationException("Total quantity cannot be negative.");
        if (book.getAvailableQuantity() > book.getTotalQuantity())
            throw new ValidationException("Available quantity cannot exceed total quantity.");
        if (book.getAvailableQuantity() + book.getReservedQuantity() > book.getTotalQuantity())
            throw new ValidationException("Available + reserved cannot exceed total quantity.");
        if (isbnExists != null && isbnExists.test(book.getIsbn()))
            throw new DuplicateBookException("ISBN " + book.getIsbn() + " already exists.");
    }

    public static void validateStudent(Student student, Predicate<String> regExists, Predicate<String> cardExists) {
        if (student == null) throw new ValidationException("Student cannot be null.");
        if (student.getRegistrationNumber() == null || student.getRegistrationNumber().isBlank())
            throw new ValidationException("Registration number is required.");
        if (student.getLibraryCardNumber() == null || student.getLibraryCardNumber().isBlank())
            throw new ValidationException("Library card number is required.");
        if (regExists != null && regExists.test(student.getRegistrationNumber()))
            throw new DuplicateUserException("Registration number " + student.getRegistrationNumber() + " already exists.");
        if (cardExists != null && cardExists.test(student.getLibraryCardNumber()))
            throw new DuplicateUserException("Library card " + student.getLibraryCardNumber() + " already exists.");
    }

    public static void validateCanBorrow(Student student, Book book, LibraryConfig config) {
        if (student == null) throw new ValidationException("Student is required.");
        if (book == null) throw new ValidationException("Book is required.");
        if (student.membershipExpired())
            throw new MembershipExpiredException("Membership expired for " + student.getRegistrationNumber());
        if (student.getMembershipStatus() != MembershipStatus.ACTIVE)
            throw new ValidationException("Membership is not active.");
        if (student.remainingBorrowSlots() <= 0)
            throw new BorrowLimitExceededException("Borrow limit reached for " + student.getRegistrationNumber());
        if (book.getAvailableQuantity() <= 0 || book.getStatus() != com.library.enums.BookStatus.AVAILABLE)
            throw new BookUnavailableException("No available copies of '" + book.getTitle() + "'.");
        if (student.getFineBalancePaise() > 0)
            throw new FinePendingException("Outstanding fine of Rs." + student.getFineBalance() + " must be paid first.");
    }

    public static void validateCanRenew(BorrowRecord record, LibraryConfig config, List<Reservation> reservations) {
        if (record == null) throw new ValidationException("Borrow record is required.");
        if (record.getRenewCount() >= config.getMaxRenewals())
            throw new ValidationException("Maximum renewals (" + config.getMaxRenewals() + ") reached.");
        if (reservations != null && !reservations.isEmpty())
            throw new ReservationException("Cannot renew: book has pending reservations.");
        if (!record.canRenew())
            throw new ValidationException("This book cannot be renewed.");
    }

    public static void validateCanReserve(Student student, Book book, List<Reservation> activeForMember, LibraryConfig config) {
        if (student == null) throw new ValidationException("Student is required.");
        if (book == null) throw new ValidationException("Book is required.");
        if (student.membershipExpired())
            throw new MembershipExpiredException("Membership expired for " + student.getRegistrationNumber());
        if (book.getAvailableQuantity() > 0)
            throw new ReservationException("Book is available for borrowing - no need to reserve.");
        if (activeForMember != null && activeForMember.size() >= config.getMaxReservations())
            throw new ReservationException("Maximum reservations (" + config.getMaxReservations() + ") reached.");
    }

    public static void validateBorrowLimit(int limit) {
        if (limit < 1 || limit > 20) throw new ValidationException("Borrow limit must be between 1 and 20.");
    }
    public static void validateFinePerDay(long paise) {
        if (paise < 0) throw new ValidationException("Fine per day cannot be negative.");
    }
}
