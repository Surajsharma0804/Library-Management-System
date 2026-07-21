package com.library.factory;

import com.library.enums.MembershipStatus;
import com.library.model.Book;
import com.library.model.AuditLog;
import com.library.model.BorrowRecord;
import com.library.model.Fine;
import com.library.model.Reservation;
import com.library.model.Librarian;
import com.library.model.Student;
import com.library.repository.CountersRepository;
import com.library.security.PasswordHasher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Factory for creating domain entities with auto-generated IDs.
 */
public final class EntityFactory {
    private final CountersRepository counters;
    public EntityFactory(CountersRepository counters) { this.counters = counters; }

    public Book createBook(String isbn, String title, String author, int totalQuantity) {
        return Book.builder().id(counters.nextId("book")).isbn(isbn).title(title)
                .author(author).totalQuantity(totalQuantity).availableQuantity(totalQuantity).build();
    }

    public Student createStudent(String firstName, String lastName, String email, String phone,
                                  String department, String course, int semester, String section) {
        String regNo = counters.nextId("REG");
        String cardNo = counters.nextId("LIB");
        return Student.builder().id(counters.nextId("student")).firstName(firstName).lastName(lastName)
                .email(email).phone(phone).registrationNumber(regNo).libraryCardNumber(cardNo)
                .department(department).course(course).semester(semester).section(section)
                .borrowLimit(5).membershipStatus(MembershipStatus.ACTIVE)
                .joiningDate(LocalDate.now()).membershipExpiry(LocalDate.now().plusMonths(12))
                .passwordHash(PasswordHasher.hash("changeme123")).build();
    }

    public Librarian createLibrarian(String firstName, String lastName, String email, String phone,
                                      String username, String password, Set<String> permissions) {
        return Librarian.builder().id(counters.nextId("librarian")).firstName(firstName).lastName(lastName)
                .email(email).phone(phone).username(username).passwordHash(PasswordHasher.hash(password))
                .permissions(permissions).build();
    }

    public com.library.model.Administrator createAdmin(String firstName, String lastName,
                                                               String email, String phone,
                                                               String username, String password) {
        return com.library.model.Administrator.builder()
                .id(counters.nextId("admin")).username(username).firstName(firstName).lastName(lastName)
                .email(email).phone(phone).passwordHash(PasswordHasher.hash(password)).active(true).build();
    }

    public BorrowRecord createBorrow(String bookId, String registrationNumber, int loanPeriodDays, String issuedBy) {
        return BorrowRecord.builder().id(counters.nextId("borrow")).bookId(bookId)
                .registrationNumber(registrationNumber).issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(loanPeriodDays)).issuedBy(issuedBy).build();
    }

    public Fine createFine(String registrationNumber, String borrowId, String bookId,
                            long amountPaise, String createdBy, String reason) {
        return Fine.builder().id(counters.nextId("fine")).registrationNumber(registrationNumber)
                .borrowId(borrowId).bookId(bookId).amountPaise(amountPaise)
                .createdBy(createdBy).reason(reason).build();
    }

    public Reservation createReservation(String bookId, String registrationNumber, int queuePosition, int status) {
        return Reservation.builder().id(counters.nextId("reservation")).bookId(bookId)
                .registrationNumber(registrationNumber).queuePosition(queuePosition).build();
    }

    public AuditLog createAuditLog(String actorId, String actorRole, String action,
                                    String targetType, String targetId, String details) {
        return new AuditLog("audit-" + System.nanoTime(), actorId, actorRole, action,
                targetType, targetId, details, LocalDateTime.now());
    }
}
