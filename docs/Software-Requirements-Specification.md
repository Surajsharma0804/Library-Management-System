# Software Requirements Specification
## Library Management System (Core Java)

**Version:** 1.0  
**Date:** 2026-07-21  
**Status:** Final  

---

## 1. Introduction

### 1.1 Purpose

This document specifies the functional and non-functional requirements for a
Library Management System (LMS) implemented entirely in Core Java 21 with no
web framework, no ORM, and no external runtime dependencies (JUnit 5 is used
only in test scope). The system manages books, students, borrowing,
reservations, fines, and administrative workflows for a single-library
institution.

### 1.2 Scope

The LMS provides:

- Role-based access control for three actor types: **Admin**, **Librarian**,
  and **Student**.
- Full book lifecycle management: add, search, update, archive/restore, mark
  lost/damaged, delete, and CSV bulk import/export.
- Student registration and membership management.
- Circulation operations: issue, return, renew, with automatic overdue fine
  calculation.
- Reservation system with status tracking.
- Fine management with payment tracking.
- Audit logging of all state-changing operations.
- Analytics and reporting across books, circulation, and members.
- Backup and restore of all persisted data.
- JSON file-based persistence with atomic writes.

### 1.3 Definitions and Acronyms

| Term       | Definition                                                  |
|------------|-------------------------------------------------------------|
| LMS        | Library Management System                                   |
| RBAC       | Role-Based Access Control                                   |
| ISBN       | International Standard Book Number (ISBN-10 or ISBN-13)     |
| CRUD       | Create, Read, Update, Delete                                |
| PBKDF2     | Password-Based Key Derivation Function 2                    |
| SRS        | Software Requirements Specification                         |
| Paise      | Indian sub-currency unit (1 rupee = 100 paise), used as the internal integer currency unit to avoid floating-point rounding errors |
| Archival   | Soft-delete: book is hidden from active views but retained  |

### 1.4 References

- Java Language Specification, Java SE 21 LTS
- JUnit 5 User Guide
- RFC 4122 — Universally Unique Identifiers (UUID)
- PBKDF2 (RFC 2898 / NIST SP 800-132)

### 1.5 Overview

Section 2 describes the overall product context and user characteristics.
Section 3 lists functional requirements grouped by subsystem. Section 4
lists non-functional requirements. Section 5 defines the technology
constraints. Section 6 describes the data persistence model.

---

## 2. Overall Description

### 2.1 Product Context

The LMS is a standalone console application. It reads from and writes to JSON
files on the local filesystem. There is no network layer, no application
server, and no database. The application is intended for a single library
branch with up to a few thousand books and members.

### 2.2 User Classes and Roles

| Role      | Capabilities                                                                 |
|-----------|------------------------------------------------------------------------------|
| Admin     | Full system access: manage staff, students, books, config, backup, analytics, all circulation |
| Librarian | Manage books, students, circulation, reservations, fines; view analytics     |
| Student   | Self-service: search books, view own borrow history, reserve books, pay fines, view own notifications |

### 2.3 Operating Environment

- Java 21 LTS runtime
- Any OS with a JVM (developed and tested on Linux)
- Local filesystem read/write access for the data directory

### 2.4 Assumptions and Dependencies

- The data directory is writable and has sufficient disk space.
- A default admin account is seeded on first run (username `admin`, default
  password) to allow initial configuration.
- The system clock is accurate for due-date and overdue calculations.

---

## 3. Functional Requirements

### 3.1 Authentication and Authorization

| ID    | Requirement                                                                                          | Priority |
|-------|------------------------------------------------------------------------------------------------------|----------|
| AU-1  | The system shall authenticate users by username and password.                                        | Must     |
| AU-2  | Passwords shall be hashed with PBKDF2-HMAC-SHA256 using a per-password random salt (≥16 bytes).      | Must     |
| AU-3  | The system shall reject login for inactive user accounts.                                            | Must     |
| AU-4  | Authenticated users shall receive a session token with role, user ID, and timestamp.                 | Must     |
| AU-5  | The system shall enforce RBAC: every privileged operation checks the caller's role against a permission catalogue. | Must |
| AU-6  | Users shall be able to change their own password.                                                    | Must     |
| AU-7  | The system shall log all login attempts (success and failure).                                       | Should   |

### 3.2 Book Management

| ID    | Requirement                                                                                          | Priority |
|-------|------------------------------------------------------------------------------------------------------|----------|
| BK-1  | Admins and Librarians shall be able to add books with ISBN, title, author, publisher, edition, publication year, genre, language, total copies, and location. | Must |
| BK-2  | ISBN shall be validated as ISBN-10 or ISBN-13 with correct check digit.                              | Must     |
| BK-3  | Duplicate ISBNs shall be rejected.                                                                   | Must     |
| BK-4  | The system shall maintain available, borrowed, and reserved copy counts derived from total copies and circulation state. | Must |
| BK-5  | Admins and Librarians shall be able to search books by title, author, ISBN, genre, publisher, language, status, year range, keyword, or any combination via a search engine. | Must |
| BK-6  | Admins and Librarians shall be able to update book metadata.                                         | Must     |
| BK-7  | Admins shall be able to archive and restore books (soft-delete).                                     | Must     |
| BK-8  | Admins shall be able to mark books as lost or damaged, adjusting copy counts accordingly.            | Must     |
| BK-9  | Admins shall be able to permanently delete books.                                                    | Must     |
| BK-10 | Admins shall be able to bulk-import books from CSV. Rows with duplicate ISBN or invalid ISBN shall be skipped with a count reported. | Must |
| BK-11 | Admins shall be able to export all books to CSV.                                                     | Should   |

### 3.3 Student Management

| ID    | Requirement                                                                                          | Priority |
|-------|------------------------------------------------------------------------------------------------------|----------|
| ST-1  | Librarians and Admins shall be able to register students with name, email, phone, registration number, department, and program. | Must |
| ST-2  | Email shall be validated for RFC-conformant format.                                                  | Must     |
| ST-3  | Phone shall be validated (10-digit Indian format).                                                   | Must     |
| ST-4  | Registration numbers shall be unique.                                                                | Must     |
| ST-5  | Duplicate registrations (same email or registration number) shall be rejected.                       | Must     |
| ST-6  | The system shall track each student's outstanding fine balance in paise (integer).                   | Must     |
| ST-7  | Admins shall be able to activate, deactivate, and update student memberships.                        | Must     |
| ST-8  | Students shall be able to view their own profile and borrow history.                                 | Must     |

### 3.4 Circulation (Borrowing)

| ID    | Requirement                                                                                          | Priority |
|-------|------------------------------------------------------------------------------------------------------|----------|
| CI-1  | Librarians and Admins shall be able to issue a book to a student if copies are available and the student has no outstanding fines and is below the borrow limit. | Must |
| CI-2  | The system shall enforce a configurable maximum concurrent borrows per student (default 3).         | Must     |
| CI-3  | The system shall set a configurable loan period (default 14 days) and compute the due date.         | Must     |
| CI-4  | Librarians and Admins shall be able to process book returns.                                         | Must     |
| CI-5  | On return, if the book is overdue, the system shall calculate a fine of ₹1/day (100 paise/day) and record it. | Must |
| CI-6  | The fine shall be added to the student's balance exactly once per overdue return.                    | Must     |
| CI-7  | Librarians and Admins shall be able to renew a borrow, extending the due date, if no reservation is pending. | Must |
| CI-8  | Renewals shall be limited to a configurable maximum (default 2).                                     | Must     |
| CI-9  | The system shall prevent issuing books to students with expired memberships.                        | Must     |
| CI-10 | The system shall prevent issuing books to students with outstanding fines.                          | Should   |

### 3.5 Reservations

| ID    | Requirement                                                                                          | Priority |
|-------|------------------------------------------------------------------------------------------------------|----------|
| RE-1  | Students shall be able to reserve a book that is currently unavailable (all copies borrowed).       | Must     |
| RE-2  | The system shall track reservation status: PENDING, READY, FULFILLED, CANCELLED, EXPIRED.           | Must     |
| RE-3  | Students shall be able to cancel their own pending reservations.                                     | Must     |
| RE-4  | Librarians shall be able to approve or reject pending reservation requests.                         | Should   |
| RE-5  | When a reserved book is returned, the reservation shall move to READY and the student shall be notified. | Should |
| RE-6  | Reservations shall expire after a configurable hold period (default 2 days).                        | Should   |

### 3.6 Fine Management

| ID    | Requirement                                                                                          | Priority |
|-------|------------------------------------------------------------------------------------------------------|----------|
| FN-1  | The system shall record each fine with amount, reason, associated borrow record, and status (PENDING, PAID, WAIVED). | Must |
| FN-2  | Students shall be able to pay fines, reducing their balance.                                        | Must     |
| FN-3  | Admins shall be able to waive fines.                                                                 | Must     |
| FN-4  | The system shall maintain a fine history per student.                                               | Must     |

### 3.7 Audit Logging

| ID    | Requirement                                                                                          | Priority |
|-------|------------------------------------------------------------------------------------------------------|----------|
| AL-1  | The system shall log every state-changing operation with actor, action type, entity, timestamp.     | Must     |
| AL-2  | Audit logs shall be persisted to a JSON file and queryable by actor, action, or date range.         | Should   |

### 3.8 Analytics and Reports

| ID    | Requirement                                                                                          | Priority |
|-------|------------------------------------------------------------------------------------------------------|----------|
| AN-1  | The system shall provide total book count, available vs. borrowed counts, and active member count.  | Must     |
| AN-2  | The system shall provide overdue books, top borrowed books, and category distribution reports.       | Should   |
| AN-3  | Reports shall be generated via a strategy pattern, allowing new report types without modifying existing code. | Must |
| AN-4  | Admins shall be able to view all reports; Librarians shall be able to view circulation reports.      | Must     |

### 3.9 Backup and Restore

| ID    | Requirement                                                                                          | Priority |
|-------|------------------------------------------------------------------------------------------------------|----------|
| BR-1  | Admins shall be able to create a full backup of all data files to a timestamped directory.          | Must     |
| BR-2  | Admins shall be able to restore from a backup directory.                                             | Should   |

### 3.10 Configuration

| ID    | Requirement                                                                                          | Priority |
|-------|------------------------------------------------------------------------------------------------------|----------|
| CF-1  | The system shall store configurable parameters: loan period, borrow limit, renewal limit, fine rate, reservation hold period. | Must |
| CF-2  | Admins shall be able to update configuration at runtime.                                             | Must     |

---

## 4. Non-Functional Requirements

### 4.1 Performance

| ID     | Requirement                                                                                  |
|--------|----------------------------------------------------------------------------------------------|
| NF-1   | Search queries across ≤5,000 books shall return in <200 ms (in-memory cache).                |
| NF-2   | All file writes shall be atomic (write-to-temp + move) to prevent corruption on crash.       |

### 4.2 Security

| ID     | Requirement                                                                                  |
|--------|----------------------------------------------------------------------------------------------|
| NF-3   | Passwords shall never be stored in plaintext; PBKDF2 with ≥10,000 iterations.                |
| NF-4   | Every privileged operation shall check RBAC permissions; no operation trusts the caller's self-reported role. |
| NF-5   | Session tokens shall be UUID-based and expire after a configurable timeout.                  |

### 4.3 Reliability

| ID     | Requirement                                                                                  |
|--------|----------------------------------------------------------------------------------------------|
| NF-6   | The system shall not corrupt data files on crash: all writes are atomic via temp-file + move. |
| NF-7   | The system shall validate all user input at system boundaries and reject invalid data with specific exceptions. |
| NF-8   | The system shall log all errors to a file-based log for post-mortem analysis.               |

### 4.4 Maintainability

| ID     | Requirement                                                                                  |
|--------|----------------------------------------------------------------------------------------------|
| NF-9   | The codebase shall follow a layered architecture with unidirectional dependencies (Presentation → Controller → Service → Repository → Persistence). |
| NF-10  | Design patterns shall be applied where they reduce coupling: Repository, Factory, Builder, Singleton, Strategy, Observer, Facade, Command. |
| NF-11  | All magic numbers shall be centralized in a constants class.                                |
| NF-12  | The system shall have ≥80% test coverage on validators, models, and core services.          |

### 4.5 Portability

| ID     | Requirement                                                                                  |
|--------|----------------------------------------------------------------------------------------------|
| NF-13  | The system shall run on any OS with Java 21 LTS; no native dependencies.                    |
| NF-14  | All file paths shall be configurable via the StoragePaths constants class.                  |

---

## 5. Technology Constraints

| Constraint            | Value                                                              |
|-----------------------|--------------------------------------------------------------------|
| Language              | Java 21 LTS                                                        |
| Build Tool            | Maven                                                              |
| External Libraries    | None at runtime; JUnit 5 (Jupiter) in test scope only             |
| Persistence           | JSON files on local filesystem (hand-written codec, no Jackson/Gson) |
| Database              | None                                                               |
| Frameworks            | None (no Spring, Hibernate, Jakarta EE, or similar)               |
| Architecture          | Layered, unidirectional dependencies                              |
| Testing               | JUnit 5 with @TempDir-based file isolation                         |

---

## 6. Data Persistence Model

All data is stored as JSON files under the `data/` directory:

| File                  | Content                                                        |
|-----------------------|----------------------------------------------------------------|
| `books.json`          | All book records                                               |
| `students.json`       | All student records                                            |
| `librarians.json`     | Librarian accounts                                             |
| `admins.json`         | Admin accounts (seed data)                                     |
| `borrow_records.json` | All borrow records (active and returned)                       |
| `reservations.json`   | All reservation records                                        |
| `fines.json`          | All fine records                                               |
| `notifications.json`  | Notification records                                           |
| `settings.json`       | Library configuration (loan period, limits, rates)            |
| `audit_logs.json`     | Audit log entries                                              |

Each file contains a JSON array of objects. Files are created on first write
if they do not exist. All writes are atomic (write to temp file, then move).

---

## 7. Acceptance Criteria

1. All 102 JUnit 5 tests pass with zero failures.
2. The application starts with `mvn exec:java` or `java -jar` and presents a
   role-based menu after login.
3. A default admin account is seeded on first run.
4. Books can be added, searched, borrowed, returned, and reserved end-to-end.
5. Overdue returns produce exactly one fine entry and increment the student's
   balance by exactly the fine amount.
6. CSV import skips duplicates and invalid ISBNs, reporting the skip count.
7. RBAC prevents students from accessing admin/librarian operations.
8. Backup creates a timestamped snapshot; restore repopulates all data files.
