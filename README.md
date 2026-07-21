# Core Java Library Management System

A production-quality, pure Core Java library management system for a university library, featuring layered architecture, role-based access control, JSON persistence, and comprehensive unit tests.

## Quick Start

### Prerequisites
- Java 21 LTS
- Maven 3.9+

### Build and Run
```bash
mvn clean package
java -cp target/classes com.library.Main
```

### Default Login
- **Username:** `admin`
- **Password:** `admin@123`
- Change the password after first login.

### Run Tests
```bash
mvn test
```

## Architecture

The system follows a strict layered architecture with unidirectional dependencies:

```
Presentation (menu) → Controller → Service → Repository → Persistence (JSON)
```

Each layer has one responsibility:
- **Presentation:** Console UI, user interaction, input formatting
- **Controller:** RBAC enforcement, request validation, service delegation
- **Service:** Business logic, validation, audit logging, notifications
- **Repository:** Data access, CRUD operations, query filtering
- **Persistence:** JSON file I/O

No layer skips ahead — controllers never touch persistence, services never render UI, repositories never hold business rules.

## Package Structure

```
com.library
├── controller/       # RBAC-enforced request handlers
├── service/          # Business logic (auth, book, borrow, fine, etc.)
├── repository/       # JSON-backed data access
├── model/            # Domain entities (Book, Student, BorrowRecord, etc.)
├── dto/              # Transfer objects for controller-service communication
├── mapper/           # Entity ↔ JSON map converters
├── validator/        # Format and business-rule validators
├── exception/        # Custom domain exception hierarchy
├── util/             # JSON codec, password hashing, I/O, logging
├── config/           # Constants, bootstrap, and seeding
├── security/         # Session management, RBAC, permissions catalogue
├── notification/     # Notification events and publisher
├── reports/          # Strategy-based report generation and CSV export
├── enums/            # BookStatus, UserRole, MembershipStatus, etc.
├── interfaces/       # Repository, JsonMappable, Auditable contracts
├── factory/          # EntityFactory for ID and entity creation
├── builder/          # Fluent builders for domain entities
├── search/           # Book search strategies and engine
├── facade/           # Composition root (LibraryFacade)
└── menu/             # Console menus for all three roles
```

## Roles and Access Control

The system implements role-based access control (RBAC) with three roles:

### Administrator
Full system authority: configuration, librarian management, book inventory CRUD, student management, analytics, reports, audit logs, backup/restore.

### Librarian
Daily operations: issue/return/renew books, manage reservations, collect/waive fines, search members, view history, mark books lost/damaged/under-repair.

### Student
Self-service: dashboard with borrow status, profile, library card, book search, borrow history, reservations, fines, notifications, password change.

Every controller method calls `require(session, permission)` before delegating to a service. The `Permissions` class is the single source of truth for which roles may exercise which actions.

## Design Patterns

| Pattern | Implementation |
|---------|---------------|
| **Repository** | `JsonRepository<T,K>` base + concrete repositories |
| **Factory** | `EntityFactory` creates entities with monotonic IDs |
| **Builder** | Every model has a fluent builder |
| **Singleton** | `LibraryFacade` as composition root |
| **Strategy** | `SearchStrategy` + `ReportStrategy` interfaces |
| **Observer** | `NotificationPublisher` / notifiers |
| **Facade** | `LibraryFacade` wires all components; `ReportService` dispatches reports |

## Key Features

- **JSON persistence** with a hand-written, dependency-free JSON codec (parser + serializer)
- **PBKDF2-HMAC-SHA256 password hashing** with per-password random salt
- **ISBN-10 and ISBN-13 validation** with check-digit verification
- **Comprehensive validation**: email, phone, registration, library card, publication year, borrow limits, membership expiry, fine pending, book availability
- **Search engine** supporting multiple fields with case-insensitive partial matching
- **10+ report types** with CSV export
- **Audit logging** of every security-relevant action
- **Full backup and restore** via timestamped snapshots
- **Notification system** with per-student inboxes

## Testing

94 JUnit 5 tests covering:
- JSON codec (parsing, serialization, escapes, edge cases, error handling)
- Format validators (ISBN, email, phone, registration, name, publication year)
- Business validators (borrow rules, renewal rules, reservation rules, password complexity)
- Password hashing (verify, unique salts, corrupted hash handling)
- Book model invariants (quantity transitions, status transitions)
- Repository CRUD (save, find, delete, count, filter, ISBN lookup)
- Service-level tests (authentication, book service, borrow service, search, student)

```
Tests run: 94, Failures: 0, Errors: 0, Skipped: 0
```

## Java Features Used

- Records (`AuditLog`, `Session`, `NotificationEvent`, `ReportData`)
- Sealed switch expressions
- Streams, lambdas, method references
- Generics (`JsonRepository<T,K>`)
- `Optional` throughout the repository layer
- `try-with-resources` in file operations
- `SecureRandom` for salts and tokens
- PBKDF2 via `javax.crypto`

## Data Storage

All data is stored as JSON files in the `data/` directory:
- `books.json`, `users.json` (admins, librarians, and students)
- `borrow_records.json`, `reservations.json`, `fines.json`
- `notifications.json`, `audit_logs.json`, `library_config.json`, `counters.json`

Logs are written to `logs/`. Reports export to `exports/`. Backups go to `backups/`.

## Constraints Honoured

- **Java 21 LTS**
- **Maven** — build and dependency management
- **No external database** — JSON persistence only
- **No frameworks** — pure Core Java (no Spring, Hibernate, or Java EE)
- **Only dependency:** JUnit 5 (test scope)
