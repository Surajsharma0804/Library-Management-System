package com.library.validator;

import com.library.exception.FinePendingException;
import com.library.exception.ValidationException;
import com.library.model.Fine;

public final class FineValidator {
    private FineValidator() {}

    public static void validate(Fine fine) {
        if (fine == null) throw new ValidationException("Fine cannot be null.");
        if (fine.getAmountPaise() < 0) throw new ValidationException("Fine amount cannot be negative.");
        if (fine.getRegistrationNumber() == null || fine.getRegistrationNumber().isBlank())
            throw new ValidationException("Registration number is required.");
    }

    public static void validatePayment(Fine fine) {
        if (fine == null) throw new ValidationException("Fine not found.");
        if (fine.isPending()) throw new FinePendingException("Fine is already pending.");
    }
}
