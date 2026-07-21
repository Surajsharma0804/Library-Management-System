package com.library.validator;

import com.library.exception.DuplicateBookException;
import com.library.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Format validators")
class FormatValidatorsTest {

    @Nested
    @DisplayName("ISBN validation")
    class IsbnTests {

        @Test
        @DisplayName("Valid ISBN-13 passes")
        void validIsbn13() {
            assertDoesNotThrow(() -> FormatValidators.validateIsbn("9780306406157"));
        }

        @Test
        @DisplayName("Valid ISBN-10 passes")
        void validIsbn10() {
            assertDoesNotThrow(() -> FormatValidators.validateIsbn("0306406152"));
        }

        @Test
        @DisplayName("ISBN-10 with X check digit passes")
        void isbn10WithX() {
            assertDoesNotThrow(() -> FormatValidators.validateIsbn("080442957X"));
        }

        @Test
        @DisplayName("ISBN with hyphens is accepted")
        void isbnWithHyphens() {
            assertDoesNotThrow(() -> FormatValidators.validateIsbn("978-0-306-40615-7"));
        }

        @Test
        @DisplayName("Invalid ISBN check digit fails")
        void invalidCheckDigit() {
            assertThrows(ValidationException.class, () -> FormatValidators.validateIsbn("9780306406158"));
        }

        @Test
        @DisplayName("Wrong length fails")
        void wrongLength() {
            assertThrows(ValidationException.class, () -> FormatValidators.validateIsbn("12345"));
        }

        @Test
        @DisplayName("Blank ISBN fails")
        void blankIsbn() {
            assertThrows(ValidationException.class, () -> FormatValidators.validateIsbn(""));
        }

        @Test
        @DisplayName("Null ISBN fails")
        void nullIsbn() {
            assertThrows(ValidationException.class, () -> FormatValidators.validateIsbn(null));
        }
    }

    @Nested
    @DisplayName("Email validation")
    class EmailTests {

        @Test
        @DisplayName("Valid email passes")
        void validEmail() {
            assertDoesNotThrow(() -> FormatValidators.validateEmail("user@library.edu"));
        }

        @Test
        @DisplayName("Invalid email fails")
        void invalidEmail() {
            assertThrows(ValidationException.class, () -> FormatValidators.validateEmail("not-an-email"));
        }

        @Test
        @DisplayName("Null email passes (optional)")
        void nullEmail() {
            assertDoesNotThrow(() -> FormatValidators.validateEmail(null));
        }

        @Test
        @DisplayName("Blank email passes (optional)")
        void blankEmail() {
            assertDoesNotThrow(() -> FormatValidators.validateEmail(""));
        }
    }

    @Nested
    @DisplayName("Phone validation")
    class PhoneTests {

        @Test
        @DisplayName("Valid phone passes")
        void validPhone() {
            assertDoesNotThrow(() -> FormatValidators.validatePhone("+1-555-123-4567"));
        }

        @Test
        @DisplayName("Too short fails")
        void tooShort() {
            assertThrows(ValidationException.class, () -> FormatValidators.validatePhone("123"));
        }

        @Test
        @DisplayName("Invalid characters fail")
        void invalidChars() {
            assertThrows(ValidationException.class, () -> FormatValidators.validatePhone("abc1234567"));
        }
    }

    @Nested
    @DisplayName("Registration number validation")
    class RegistrationTests {

        @Test
        @DisplayName("Valid registration passes")
        void validReg() {
            assertDoesNotThrow(() -> FormatValidators.validateRegistrationNumber("REG-2024-00001"));
        }

        @Test
        @DisplayName("Invalid format fails")
        void invalidReg() {
            assertThrows(com.library.exception.InvalidRegistrationException.class,
                    () -> FormatValidators.validateRegistrationNumber("2024-0001"));
        }

        @Test
        @DisplayName("Null fails")
        void nullReg() {
            assertThrows(com.library.exception.InvalidRegistrationException.class,
                    () -> FormatValidators.validateRegistrationNumber(null));
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
        @DisplayName("Name with apostrophe passes")
        void nameWithApostrophe() {
            assertDoesNotThrow(() -> FormatValidators.validateName("O'Brien", "Last name"));
        }

        @Test
        @DisplayName("Name starting with number fails")
        void nameStartingWithNumber() {
            assertThrows(ValidationException.class, () -> FormatValidators.validateName("2John", "First name"));
        }

        @Test
        @DisplayName("Blank name fails")
        void blankName() {
            assertThrows(ValidationException.class, () -> FormatValidators.validateName("", "First name"));
        }
    }

    @Nested
    @DisplayName("Publication year validation")
    class YearTests {

        @Test
        @DisplayName("Valid year passes")
        void validYear() {
            assertDoesNotThrow(() -> FormatValidators.validatePublicationYear(2020));
        }

        @Test
        @DisplayName("Ancient year fails")
        void ancientYear() {
            assertThrows(ValidationException.class, () -> FormatValidators.validatePublicationYear(1000));
        }

        @Test
        @DisplayName("Future year fails")
        void futureYear() {
            assertThrows(ValidationException.class,
                    () -> FormatValidators.validatePublicationYear(java.time.LocalDate.now().getYear() + 5));
        }
    }
}
