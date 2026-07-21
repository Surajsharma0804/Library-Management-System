package com.library.validator;

import com.library.exception.DuplicateUserException;
import com.library.exception.ValidationException;
import com.library.model.User;

import java.util.function.Predicate;

public final class UserValidator {
    private UserValidator() {}

    public static void validate(User user, Predicate<String> usernameExists) {
        if (user == null) throw new ValidationException("User cannot be null.");
        if (user.getUsername() == null || user.getUsername().isBlank())
            throw new ValidationException("Username is required.");
        if (user.getFirstName() == null || user.getFirstName().isBlank())
            throw new ValidationException("First name is required.");
        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank())
            throw new ValidationException("Password hash is required.");
        if (usernameExists != null && usernameExists.test(user.getUsername()))
            throw new DuplicateUserException("Username " + user.getUsername() + " already exists.");
    }
}
