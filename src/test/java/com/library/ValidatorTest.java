package com.library;

import com.library.exception.ValidationException;
import com.library.validator.BusinessValidators;
import com.library.validator.FormatValidators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Validator tests")
class ValidatorTest {

    @Nested
    @DisplayName("ISBN validation")
    class IsbnTests {
        @Test
        @DisplayName("Valid ISBN-13 passes")
        void validIsbn13() {
            assertDoesNotThrow(() -> FormatValidators.validateIsbn("9780306406157"));
        }

        @Test
        @DisplayName("Invalid ISBN check digit fails")
        void invalidCheckDigit() {
            assertThrows(ValidationException.class, () -> FormatValidators.validateIsbn("9780306406158"));
        }

        @Test
        @DisplayName("Blank ISBN fails")
        void blankIsbn() {
            assertThrows(ValidationException.class, () -> FormatValidators.validateIsbn(""));
        }
    }

    @Nested
    @DisplayName("Email validation")
    class EmailTests {
        @Test
        @DisplayName("Valid email passes")
        void validEmail() {
            assertDoesNotThrow(() -> FormatValidators.validateEmail("user@example.com"));
        }

        @Test
        @DisplayName("Invalid email fails")
        void invalidEmail() {
            assertThrows(com.library.exception.ValidationException.class, () -> FormatValidators.validateEmail("not-an-email"));
        }
    }

    @Nested
    @DisplayName("Password validation")
    class PasswordTests {
        @Test
        @DisplayName("Strong password passes")
        void strongPassword() {
            assertDoesNotThrow(() -> BusinessValidators.validatePassword("Strong1Pass"));
        }

        @Test
        @DisplayName("Weak password fails")
        void weakPassword() {
            assertThrows(ValidationException.class, () -> BusinessValidators.validatePassword("weak"));
        }
    }

    @Nested
    @DisplayName("Name validation")
    class NameTests {
        @Test
        @DisplayName("Valid name passes")
        void validName() {
            assertDoesNotThrow(() -> FormatValidators.validateName("John", "First name"));
        }

        @Test
        @DisplayName("Blank name fails")
        void blankName() {
            assertThrows(ValidationException.class, () -> FormatValidators.validateName("", "First name"));
        }
    }
}
