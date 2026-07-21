package com.library.validator;

import com.library.exception.ReservationException;
import com.library.exception.ValidationException;
import com.library.model.Book;
import com.library.model.Reservation;
import com.library.model.Student;

import java.util.List;

public final class ReservationValidator {
    private ReservationValidator() {}

    public static void validateCreate(Student student, Book book, List<Reservation> activeForMember, int maxReservations) {
        if (student == null) throw new ValidationException("Student is required.");
        if (book == null) throw new ValidationException("Book is required.");
        if (book.getAvailableQuantity() > 0)
            throw new ReservationException("Book is available - no need to reserve.");
        if (activeForMember != null && activeForMember.size() >= maxReservations)
            throw new ReservationException("Maximum reservations (" + maxReservations + ") reached.");
    }

    public static void validateCancel(Reservation reservation, String registrationNumber) {
        if (reservation == null) throw new ValidationException("Reservation not found.");
        if (!reservation.getRegistrationNumber().equals(registrationNumber))
            throw new ReservationException("Cannot cancel another user's reservation.");
    }
}
