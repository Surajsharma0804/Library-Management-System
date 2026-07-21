package com.library.validator;

import com.library.enums.BookStatus;
import com.library.enums.MembershipStatus;
import com.library.enums.ReservationStatus;
import com.library.exception.BookUnavailableException;
import com.library.exception.BorrowLimitExceededException;
import com.library.exception.FinePendingException;
import com.library.exception.MembershipExpiredException;
import com.library.exception.ReservationException;
import com.library.exception.ValidationException;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.LibraryConfig;
import com.library.model.Reservation;
import com.library.model.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Business validators")
class BusinessValidatorsTest {

    private Book validBook() {
        return Book.builder()
                .id("BK-000001").isbn("9780306406157").title("Clean Code")
                .author("Robert Martin").totalQuantity(3).availableQuantity(3)
                .status(BookStatus.AVAILABLE).build();
    }

    private Student validStudent() {
        return Student.builder()
                .id("STU-000001").username("stu1").firstName("Alice").lastName("Smith")
                .passwordHash("hash").active(true)
                .registrationNumber("REG-2024-00001").libraryCardNumber("LC-AB12CD34")
                .department("CS").course("BSc").semester(3).section("A")
                .joiningDate(LocalDate.now()).membershipExpiry(LocalDate.now().plusYears(2))
                .borrowLimit(5).currentBorrowCount(0).fineBalancePaise(0)
                .membershipStatus(MembershipStatus.ACTIVE).build();
    }

    private LibraryConfig defaultConfig() {
        return new LibraryConfig();
    }

    @Test
    @DisplayName("Valid book passes validation")
    void validBookPasses() {
        assertDoesNotThrow(() -> BusinessValidators.validateBook(validBook(), isbn -> false));
    }

    @Test
    @DisplayName("Book with negative total quantity fails")
    void negativeTotalFails() {
        Book book = Book.builder().id("BK-1").isbn("9780306406157").title("Test")
                .author("Author").totalQuantity(-1).availableQuantity(0).status(BookStatus.AVAILABLE).build();
        assertThrows(ValidationException.class, () -> BusinessValidators.validateBook(book, isbn -> false));
    }

    @Test
    @DisplayName("Available + reserved exceeding total fails")
    void quantityOverflowFails() {
        Book book = Book.builder().id("BK-1").isbn("9780306406157").title("Test")
                .author("Author").totalQuantity(2).availableQuantity(2).reservedQuantity(1)
                .status(BookStatus.AVAILABLE).build();
        assertThrows(ValidationException.class, () -> BusinessValidators.validateBook(book, isbn -> false));
    }

    @Test
    @DisplayName("Valid student passes validation")
    void validStudentPasses() {
        assertDoesNotThrow(() -> BusinessValidators.validateStudent(validStudent(), reg -> false, card -> false));
    }

    @Test
    @DisplayName("Can borrow when all conditions met")
    void canBorrowSuccess() {
        assertDoesNotThrow(() -> BusinessValidators.validateCanBorrow(validStudent(), validBook(), defaultConfig()));
    }

    @Test
    @DisplayName("Cannot borrow with expired membership")
    void cannotBorrowExpired() {
        Student s = validStudent();
        s.setMembershipExpiry(LocalDate.now().minusDays(1));
        assertThrows(MembershipExpiredException.class,
                () -> BusinessValidators.validateCanBorrow(s, validBook(), defaultConfig()));
    }

    @Test
    @DisplayName("Cannot borrow with pending fine")
    void cannotBorrowWithFine() {
        Student s = validStudent();
        s.addFine(500);
        assertThrows(FinePendingException.class,
                () -> BusinessValidators.validateCanBorrow(s, validBook(), defaultConfig()));
    }

    @Test
    @DisplayName("Cannot borrow when limit reached")
    void cannotBorrowLimitReached() {
        Student s = validStudent();
        s.setBorrowLimit(1);
        s.incrementBorrowCount();
        assertThrows(BorrowLimitExceededException.class,
                () -> BusinessValidators.validateCanBorrow(s, validBook(), defaultConfig()));
    }

    @Test
    @DisplayName("Cannot borrow unavailable book")
    void cannotBorrowUnavailable() {
        Book b = validBook();
        b.markDamaged();
        assertThrows(BookUnavailableException.class,
                () -> BusinessValidators.validateCanBorrow(validStudent(), b, defaultConfig()));
    }

    @Test
    @DisplayName("Cannot renew when max renewals reached")
    void cannotRenewMaxReached() {
        BorrowRecord record = BorrowRecord.builder()
                .id("BRW-1").bookId("BK-1").registrationNumber("REG-2024-00001")
                .issueDate(LocalDate.now()).dueDate(LocalDate.now().plusDays(7))
                .renewCount(2).status(com.library.enums.BorrowStatus.ACTIVE).build();
        LibraryConfig config = newConfigWithRenewals(2);
        assertThrows(ValidationException.class,
                () -> BusinessValidators.validateCanRenew(record, config, List.of()));
    }

    @Test
    @DisplayName("Cannot renew when others are waiting")
    void cannotRenewWithQueue() {
        BorrowRecord record = BorrowRecord.builder()
                .id("BRW-1").bookId("BK-1").registrationNumber("REG-2024-00001")
                .issueDate(LocalDate.now()).dueDate(LocalDate.now().plusDays(7))
                .renewCount(0).status(com.library.enums.BorrowStatus.ACTIVE).build();
        Reservation waiting = Reservation.builder()
                .id("RSV-1").bookId("BK-1").registrationNumber("REG-2024-00002")
                .reservationDate(LocalDate.now()).queuePosition(1)
                .status(ReservationStatus.PENDING).build();
        assertThrows(ReservationException.class,
                () -> BusinessValidators.validateCanRenew(record, defaultConfig(), List.of(waiting)));
    }

    @Test
    @DisplayName("Cannot reserve when book is available")
    void cannotReserveAvailable() {
        assertThrows(ReservationException.class,
                () -> BusinessValidators.validateCanReserve(validStudent(), validBook(), List.of(), defaultConfig()));
    }

    @Test
    @DisplayName("Password validation rejects weak passwords")
    void weakPasswordFails() {
        assertThrows(ValidationException.class, () -> BusinessValidators.validatePassword("short"));
        assertThrows(ValidationException.class, () -> BusinessValidators.validatePassword("allletters"));
        assertThrows(ValidationException.class, () -> BusinessValidators.validatePassword("12345678"));
    }

    @Test
    @DisplayName("Password validation accepts strong passwords")
    void strongPasswordPasses() {
        assertDoesNotThrow(() -> BusinessValidators.validatePassword("Secure123"));
    }

    private static LibraryConfig newConfigWithRenewals(int max) {
        LibraryConfig config = new LibraryConfig();
        config.setMaxRenewals(max);
        return config;
    }
}

