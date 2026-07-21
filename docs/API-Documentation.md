# API Documentation
## Library Management System (Core Java)

**Version:** 1.0  
**Date:** 2026-07-21  

---

## 1. Overview

This document describes the internal Java API of the Library Management
System. Since this is a console application with no HTTP/REST layer, the
"API" here refers to the public interfaces, service methods, and repository
contracts that the codebase exposes to its own layers.

---

## 2. Service Layer API

### 2.1 AuthenticationService

**File:** `service/AuthenticationService.java`  
**Interface:** `interfaces/AuthenticationService.java`

| Method | Signature | Description |
|--------|-----------|-------------|
| login | `Session login(String username, String password)` | Authenticates user, creates session |
| logout | `void logout(Session session)` | Invalidates session |
| changePassword | `void changePassword(Session session, String oldPwd, String newPwd)` | Changes password with verification |
| getActiveSessions | `List<Session> getActiveSessions()` | Returns all active sessions (admin) |

### 2.2 BookService

**File:** `service/BookService.java`

| Method | Signature | Description |
|--------|-----------|-------------|
| addBook | `Book addBook(Session session, String isbn, String title, String author, ...)` | Adds a new book with validation |
| searchBooks | `List<Book> searchBooks(Session session, String query, String strategy)` | Search by strategy name |
| getBook | `Book getBook(Session session, String bookId)` | Get book by ID |
| getBookByIsbn | `Book getBookByIsbn(Session session, String isbn)` | Get book by ISBN |
| updateBook | `void updateBook(Session session, String bookId, Map<String,Object> updates)` | Update book metadata |
| deleteBook | `void deleteBook(Session session, String bookId)` | Permanently delete book |
| archiveBook | `void archiveBook(Session session, String bookId)` | Soft-delete (archive) |
| restoreBook | `void restoreBook(Session session, String bookId)` | Restore archived book |
| markLost | `void markLost(Session session, String bookId)` | Mark book as lost |
| markDamaged | `void markDamaged(Session session, String bookId)` | Mark book as damaged |
| exportToCsv | `String exportToCsv(Session session)` | Export all books to CSV string |
| importFromCsv | `String importFromCsv(Session session, Path csvFile)` | Bulk import from CSV file |

### 2.3 StudentService

**File:** `service/StudentService.java`

| Method | Signature | Description |
|--------|-----------|-------------|
| registerStudent | `Student registerStudent(Session session, String name, String email, String phone, String regNo, String dept, String program)` | Register new student |
| getStudent | `Student getStudent(Session session, String studentId)` | Get student by ID |
| getStudentByRegNo | `Student getStudentByRegNo(Session session, String regNo)` | Get student by registration number |
| updateStudent | `void updateStudent(Session session, String studentId, Map<String,Object> updates)` | Update student info |
| deactivateStudent | `void deactivateStudent(Session session, String studentId)` | Deactivate membership |
| activateStudent | `void activateStudent(Session session, String studentId)` | Activate membership |
| getAllStudents | `List<Student> getAllStudents(Session session)` | List all students |

### 2.4 BorrowService

**File:** `service/BorrowService.java`

| Method | Signature | Description |
|--------|-----------|-------------|
| issueBook | `BorrowRecord issueBook(Session session, String studentId, String bookId)` | Issue book to student |
| returnBook | `void returnBook(Session session, String borrowId)` | Process book return, calculate fine if overdue |
| renewBook | `void renewBook(Session session, String borrowId)` | Renew borrow, extend due date |
| getActiveBorrows | `List<BorrowRecord> getActiveBorrows(Session session)` | All active borrows |
| getStudentBorrows | `List<BorrowRecord> getStudentBorrows(Session session, String studentId)` | Borrow history for student |
| getOverdueBorrows | `List<BorrowRecord> getOverdueBorrows(Session session)` | All overdue borrows |

### 2.5 ReservationService

**File:** `service/ReservationService.java`

| Method | Signature | Description |
|--------|-----------|-------------|
| reserveBook | `Reservation reserveBook(Session session, String studentId, String bookId)` | Create reservation |
| cancelReservation | `void cancelReservation(Session session, String reservationId)` | Cancel reservation |
| approveReservation | `void approveReservation(Session session, String reservationId)` | Approve pending reservation |
| rejectReservation | `void rejectReservation(Session session, String reservationId)` | Reject pending reservation |
| getStudentReservations | `List<Reservation> getStudentReservations(Session session, String studentId)` | Student's reservations |
| getAllReservations | `List<Reservation> getAllReservations(Session session)` | All reservations |

### 2.6 FineService

**File:** `service/FineService.java`

| Method | Signature | Description |
|--------|-----------|-------------|
| recordFine | `Fine recordFine(Session session, String studentId, String borrowId, String bookId, long amountPaise, String reason)` | Record a new fine |
| payFine | `void payFine(Session session, String fineId, long amountPaise)` | Pay fine, reduce student balance |
| waiveFine | `void waiveFine(Session session, String fineId)` | Waive fine (admin only) |
| getStudentFines | `List<Fine> getStudentFines(Session session, String studentId)` | Student's fine history |
| getPendingFines | `List<Fine> getPendingFines(Session session)` | All pending fines |

### 2.7 ReportService

**File:** `reports/ReportService.java`

| Method | Signature | Description |
|--------|-----------|-------------|
| generateReport | `ReportData generateReport(Session session, String reportName)` | Generate report by name |
| listAvailableReports | `List<String> listAvailableReports()` | List all report names |

### 2.8 BackupService

**File:** `backup/BackupService.java`

| Method | Signature | Description |
|--------|-----------|-------------|
| createBackup | `Path createBackup(Session session)` | Full backup to timestamped directory |
| restoreBackup | `void restoreBackup(Session session, Path backupDir)` | Restore from backup |

---

## 3. Repository Layer API

### 3.1 JsonRepository<E, ID> (Generic Base)

**File:** `repository/JsonRepository.java`

| Method | Signature | Description |
|--------|-----------|-------------|
| save | `E save(E entity)` | Insert or update entity by ID |
| findById | `Optional<E> findById(ID id)` | Find entity by ID |
| findAll | `List<E> findAll()` | Return all entities |
| delete | `boolean delete(ID id)` | Delete entity by ID |
| count | `long count()` | Total entity count |
| filter | `List<E> filter(Predicate<E> predicate)` | Filter by predicate |
| setOverrideFile | `void setOverrideFile(Path path)` | Redirect I/O (for testing) |

### 3.2 Concrete Repositories

| Repository | Key Query Methods |
|------------|-------------------|
| BookRepository | `findByIsbn(String)`, `findByStatus(BookStatus)`, `search(Predicate<Book>)` |
| StudentRepository | `findByRegistrationNumber(String)`, `findByEmail(String)` |
| StaffRepository | `findByUsername(String)` |
| BorrowRepository | `findActiveByStudent(String)`, `findActiveByBook(String)`, `findOverdue()` |
| ReservationRepository | `findPendingByBook(String)`, `findByStudent(String)` |
| FineRepository | `findPendingByStudent(String)`, `findByStudent(String)` |
| AuditLogRepository | `findByActor(String)`, `findByDateRange(LocalDate, LocalDate)` |
| LibraryConfigRepository | `load()`, `save(LibraryConfig)` (singleton) |
| CountersRepository | `getNextId(String prefix)`, `save(Counters)` |

---

## 4. Security API

### 4.1 RbacService

| Method | Signature | Description |
|--------|-----------|-------------|
| check | `void check(Session session, String permission)` | Throws UnauthorizedAccessException if not permitted |
| hasPermission | `boolean hasPermission(Session session, String permission)` | Boolean check |

### 4.2 SessionManager

| Method | Signature | Description |
|--------|-----------|-------------|
| createSession | `Session createSession(User user)` | Creates and stores session |
| getSession | `Optional<Session> getSession(String token)` | Retrieve session by token |
| invalidate | `void invalidate(String token)` | Destroy session |
| getActiveSessions | `List<Session> getActiveSessions()` | All active sessions |

---

## 5. Validator API

### 5.1 FormatValidators

| Method | Signature | Description |
|--------|-----------|-------------|
| validateIsbn | `ValidationResult validateIsbn(String isbn)` | ISBN-10/13 with check digit |
| validateEmail | `ValidationResult validateEmail(String email)` | RFC email format |
| validatePhone | `ValidationResult validatePhone(String phone)` | 10-digit Indian phone |
| validateName | `ValidationResult validateName(String name)` | Name length + charset |
| validatePublicationYear | `ValidationResult validatePublicationYear(int year)` | Year range 1000-current year |
| validateRegistrationNumber | `ValidationResult validateRegistrationNumber(String regNo)` | Format check |

### 5.2 BusinessValidators

| Method | Signature | Description |
|--------|-----------|-------------|
| canBorrow | `ValidationResult canBorrow(Student student, Book book, long activeBorrows, int maxBorrows)` | Check borrow eligibility |
| canRenew | `ValidationResult canRenew(BorrowRecord record, int maxRenewals, boolean hasPendingReservation)` | Check renewal eligibility |
| canReserve | `ValidationResult canReserve(Book book, long activeReservations)` | Check reservation eligibility |
| validatePasswordStrength | `ValidationResult validatePasswordStrength(String password)` | Password complexity rules |

---

## 6. Strategy API

### 6.1 BookSearchEngine

| Method | Signature | Description |
|--------|-----------|-------------|
| search | `List<Book> search(List<Book> books, String query, String strategyName)` | Execute named strategy |

### 6.2 Available Search Strategies

| Strategy Name | Description |
|---------------|-------------|
| by_title | Match title (case-insensitive substring) |
| by_author | Match author |
| by_isbn | Exact ISBN match |
| by_genre | Match genre |
| by_publisher | Match publisher |
| by_language | Match language |
| by_status | Filter by BookStatus |
| by_year_range | Filter by publication year range (e.g., "2000-2020") |
| by_keyword | Match across title + author + description |
| by_availability | Filter books with available copies |
| composite | Combine multiple strategies |

---

## 7. Observer API

### 7.1 NotificationPublisher

| Method | Signature | Description |
|--------|-----------|-------------|
| subscribe | `void subscribe(NotificationListener listener)` | Register listener |
| unsubscribe | `void unsubscribe(NotificationListener listener)` | Unregister listener |
| publish | `void publish(NotificationEvent event)` | Fire event to all listeners |

---

## 8. Error Codes

All custom exceptions extend `LibraryException`. Controllers catch and map
these to user-friendly messages:

| Exception | Meaning |
|-----------|---------|
| AuthenticationException | Invalid credentials |
| UnauthorizedAccessException | RBAC permission denied |
| BookNotFoundException | Book ID does not exist |
| BookUnavailableException | No copies available for issue |
| DuplicateBookException | Book with same ID already exists |
| DuplicateISBNException | ISBN already in catalog |
| DuplicateRegistrationException | Student already registered |
| BorrowLimitExceededException | Student at max concurrent borrows |
| FinePendingException | Student has unpaid fines |
| MembershipExpiredException | Student membership is inactive |
| ReservationException | Reservation operation failed |
| ValidationException | Input validation failed (base) |
| InvalidEmailException | Email format invalid |
| InvalidPhoneException | Phone format invalid |
| InvalidRegistrationException | Registration number invalid |
| DataPersistenceException | File I/O error |
| ConfigurationException | Config missing or corrupt |
