# Architecture Document
## Library Management System (Core Java)

**Version:** 1.0  
**Date:** 2026-07-21  

---

## 1. Overview

The Library Management System is a console-based Java 21 application with
zero external runtime dependencies. It uses a hand-written JSON codec for
persistence and follows a strict layered architecture with unidirectional
dependencies.

**Source files:** 118 main + 10 test = 128 Java files  
**Test suite:** 102 JUnit 5 tests, 0 failures

---

## 2. Layered Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                     │
│   MainMenu  StudentMenu  LibrarianMenu  AdminMenu        │
│   AbstractMenu                                            │
├─────────────────────────────────────────────────────────┤
│                    Controller Layer                      │
│   AuthController  BookController  StudentController       │
│   CirculationController  AdminController  BaseController │
├─────────────────────────────────────────────────────────┤
│                      Service Layer                       │
│   AuthenticationService  BookService  StudentService      │
│   BorrowService  FineService  ReservationService         │
│   LibrarianService  ConfigService                        │
│   AuditService  AnalyticsService  ReportService          │
│   BackupService  NotificationService                     │
├─────────────────────────────────────────────────────────┤
│                    Repository Layer                       │
│   JsonRepository<E,ID> (generic base)                    │
│   BookRepository  StudentRepository  StaffRepository     │
│   BorrowRepository  ReservationRepository                │
│   FineRepository  AuditLogRepository                     │
│   LibraryConfigRepository  CountersRepository            │
├─────────────────────────────────────────────────────────┤
│                    Persistence Layer                     │
│   Json (parser + serializer)  FileUtils (atomic I/O)     │
│   Mappers (entity ↔ JSON)  StoragePaths  Constants       │
└─────────────────────────────────────────────────────────┘
```

### Dependency Rule

Dependencies flow **downward only**. No layer references a layer above it:

- **Presentation** → Controller → Service → Repository → Persistence
- A Service never imports a Controller or Menu class.
- A Repository never imports a Service class.
- The Persistence layer has no knowledge of domain entities (it works with
  generic JSON objects; Mappers do the translation).

### Cross-Cutting Packages

These packages are referenced by multiple layers but do not violate the
dependency rule:

| Package         | Used by                  | Purpose                          |
|-----------------|--------------------------|----------------------------------|
| `model`         | All layers               | Domain entities + builders       |
| `enums`         | All layers               | Type-safe enumerations           |
| `exception`     | Service, Controller      | Custom exception hierarchy       |
| `validator`     | Service                  | Format + business validation     |
| `security`      | Controller, Service      | Session, RBAC, permissions       |
| `util`          | All layers               | JSON, I/O, logging, dates, IDs   |
| `factory`       | Service                  | Entity creation with IDs        |
| `facade`        | Presentation             | Composition root / wiring       |

---

## 3. Package Catalogue

| Package          | Files | Responsibility                                      |
|------------------|-------|-----------------------------------------------------|
| `model`          | 11    | Domain entities with Builder pattern                |
| `enums`          | 6     | BookStatus, UserRole, MembershipStatus, etc.       |
| `exception`      | 20    | LibraryException root + 19 specific exceptions     |
| `constants`      | 2     | ApplicationConstants, StoragePaths                 |
| `util`           | 10    | JSON codec, FileUtils, PasswordHasher, AppLogger   |
| `mapper`         | 9     | Entity ↔ JSON object conversion                    |
| `security`       | 4     | Session, SessionManager, RbacService, Permissions  |
| `validator`      | 3     | FormatValidators, BusinessValidators, ValidationResult |
| `repository`     | 10    | Generic JSON repo base + 9 concrete repositories    |
| `factory`        | 1     | EntityFactory (creates entities with monotonic IDs)|
| `strategy`       | 3     | Book search strategies + engine                    |
| `command`        | 2     | Command pattern for undoable circulation ops       |
| `observer`       | 3     | Notification event/listener/publisher              |
| `notification`   | 1     | In-memory notification store                       |
| `service`        | 8     | Business logic services                            |
| `audit`          | 1     | AuditService                                       |
| `analytics`      | 1     | AnalyticsService                                   |
| `reports`        | 4     | ReportData, ReportStrategy, ReportStrategies, ReportService |
| `backup`         | 1     | BackupService                                      |
| `facade`         | 1     | LibraryFacade (composition root)                   |
| `controller`     | 6     | BaseController + 5 controllers                    |
| `menu`           | 5     | AbstractMenu + 4 role-based menus                  |
| `config`         | 1     | ApplicationBootstrap (seeds default admin)         |
| `interfaces`     | 4     | Auditable, AuthenticationService, JsonMappable, Repository |

---

## 4. Design Patterns

### 4.1 Repository Pattern

`JsonRepository<E, ID>` is a generic abstract base providing CRUD operations
backed by JSON files. Concrete repositories extend it with type-specific
queries:

```
JsonRepository<E,ID> (abstract)
  ├── BookRepository        → findByIsbn(), findByStatus(), search()
  ├── StudentRepository     → findByRegistrationNumber(), findByEmail()
  ├── StaffRepository      → findByUsername()
  ├── BorrowRepository      → findActiveByStudent(), findActiveByBook()
  ├── ReservationRepository → findPendingByBook(), findByStudent()
  ├── FineRepository        → findPendingByStudent()
  ├── AuditLogRepository    → findByActor(), findByDateRange()
  ├── LibraryConfigRepository → singleton config access
  └── CountersRepository    → ID counter persistence
```

Each repository maintains an in-memory cache loaded lazily on first access
and flushed atomically on every write.

### 4.2 Builder Pattern

Every model class has a static `Builder` inner class. Builders validate
required fields and invariants before constructing the object:

```
Book.builder()
    .isbn("9780132350884")
    .title("Clean Code")
    .author("Robert C. Martin")
    .totalCopies(5)
    .build();
```

Models are mutable (copy counts change during circulation) but construction
is controlled through the builder, preventing invalid states.

### 4.3 Factory Pattern

`EntityFactory` centralizes entity creation with automatic ID generation via
`IdGenerator` (prefix + monotonic counter). All services use the factory
rather than calling builders directly, ensuring consistent ID assignment:

```
EntityFactory.createBook(isbn, title, author, ...)    → BK-000001
EntityFactory.createStudent(name, email, ...)         → STU-000001
EntityFactory.createBorrowRecord(studentId, bookId)  → BRW-000001
EntityFactory.createFine(studentId, borrowId, amount) → FIN-000001
```

### 4.4 Strategy Pattern

**Search:** `BookSearchStrategy` interface with 11 implementations in
`BookSearchStrategies` (by title, author, ISBN, genre, publisher, language,
status, year range, keyword, availability, composite). `BookSearchEngine`
composes and executes strategies.

**Reports:** `ReportStrategy` interface with 10 implementations in
`ReportStrategies`. `ReportService` selects and runs strategies by name.

### 4.5 Observer Pattern

`NotificationPublisher` maintains a list of `NotificationListener`
registrations. When a reservation becomes ready or a fine is recorded, the
publisher fires a `NotificationEvent` to all registered listeners.
`NotificationService` is the primary listener, storing notifications for
student retrieval.

### 4.6 Command Pattern

`LibraryCommand` interface with `IssueBookCommand` implementation. Commands
encapsulate circulation operations, enabling future undo/redo support. The
command carries all parameters needed to execute and reverse the operation.

### 4.7 Facade Pattern

`LibraryFacade` is the composition root. It instantiates all repositories,
services, and controllers, wires dependencies through constructor injection,
and exposes a single entry point to the presentation layer. No other class
directly constructs service or repository instances.

### 4.8 Singleton Pattern

`LibraryConfigRepository` and `CountersRepository` use singleton semantics
for configuration and ID counter access. `SessionManager` is a singleton
managing active sessions. These are intentionally single-instance because
they represent shared application state.

---

## 5. Class Diagram (Text Description)

```
                    ┌──────────────┐
                    │   LibraryFacade   │
                    │  (composition root)│
                    └───────┬──────┘
                            │ wires
          ┌─────────────────┼──────────────────┐
          ▼                 ▼                    ▼
   ┌─────────────┐  ┌─────────────┐     ┌──────────────┐
   │ Controllers │  │  Services   │     │ Repositories │
   │             │  │             │     │              │
   │ AuthCtrl    │→ │ AuthService│ →  │ StaffRepo    │
   │ BookCtrl    │→ │ BookService │ →  │ BookRepo     │
   │ StudentCtrl │→ │ StudentSvc  │ →  │ StudentRepo  │
   │ CircCtrl    │→ │ BorrowSvc   │ →  │ BorrowRepo   │
   │ AdminCtrl   │→ │ FineSvc     │ →  │ FineRepo     │
   │             │  │ ReserveSvc  │ →  │ ReserveRepo  │
   │             │  │ ConfigSvc   │ →  │ ConfigRepo   │
   └─────────────┘  └─────────────┘     └──────────────┘
                            │                    │
                            ▼                    ▼
                   ┌──────────────┐    ┌──────────────┐
                   │  Validators  │    │  Mappers     │
                   │  RBAC        │    │  (entity↔JSON)│
                   │  AuditSvc    │    └──────────────┘
                   │  Analytics   │            │
                   │  Reports     │            ▼
                   │  Backup      │    ┌──────────────┐
                   └──────────────┘    │  Json codec  │
                                       │  FileUtils   │
                                       └──────────────┘
```

---

## 6. Sequence Diagram: Book Issue Flow

```
User          Menu       Controller      Service         Repository      Persistence
 │             │             │              │                │               │
 │──select────▶│             │              │                │               │
 │             │──issue()───▶│              │                │               │
 │             │             │──issue()────▶│                │               │
 │             │             │              │──check RBAC───▶│               │
 │             │             │              │──validate─────▶│               │
 │             │             │              │──find book────▶│──load JSON───▶│
 │             │             │              │──find student─▶│──load JSON───▶│
 │             │             │              │──check limits──│               │
 │             │             │              │──create record─│──save JSON───▶│
 │             │             │              │──update book───│──save JSON───▶│
 │             │             │              │──audit log─────│──save JSON───▶│
 │             │             │◀──result─────│                │               │
 │             │◀──output─────│              │                │               │
 │◀──display───│             │              │                │               │
```

---

## 7. Security Architecture

### 7.1 Authentication

```
Login Flow:
  username + password
    → StaffRepository.findByUsername()
    → PasswordHasher.verify(password, storedHash, salt)
    → SessionManager.createSession(user)
    → Session{token, userId, role, createdAt, expiresAt}
```

Passwords are hashed with PBKDF2-HMAC-SHA256, 10,000 iterations, 16-byte
random salt per password. The salt and hash are stored as hex strings in
`staff.json`.

### 7.2 RBAC

`Permissions` defines 50+ permission constants. `RbacService` maps roles to
permission sets and checks `hasPermission(session, permission)` before every
privileged operation:

```
Role → Permissions mapping:
  ADMIN     → all permissions
  LIBRARIAN → book_*, student_*, circulation_*, reservation_*, fine_*, report_read
  STUDENT   → book_search, book_view, self_borrow_view, self_reserve, self_fine_pay
```

Controllers call `rbac.check(session, Permissions.BOOK_ADD)` before
delegating to the service. A `UnauthorizedAccessException` is thrown if the
check fails.

### 7.3 Session Management

`SessionManager` (singleton) maintains active sessions keyed by token.
Sessions expire after a configurable timeout. On expiry, the session is
invalidated and the user must re-authenticate.

---

## 8. Persistence Architecture

### 8.1 JSON Codec

A hand-written JSON parser (`JsonParser`) and serializer (`JsonSerializer`)
with no external dependencies. The `Json` facade provides typed accessors:

```
Json.parse(string)     → JsonValue
Json.object(...)       → JsonObject
jsonValue.asString()   → String
jsonValue.asInt()      → int
jsonValue.asLong()     → long
jsonValue.asArray()    → List<JsonValue>
```

### 8.2 Atomic File Writes

`FileUtils.writeAtomic(path, content)`:
1. Write content to `path + ".tmp"`.
2. `Files.move(tmp, path, ATOMIC_MOVE)`.

This prevents data corruption if the process crashes mid-write.

### 8.3 Mapper Layer

Each entity has a mapper implementing `JsonMappable<T>`:
`toMap(T entity) → JsonObject` and `fromJson(JsonObject) → T`. Mappers
isolate the entity model from the JSON structure, allowing either to change
without affecting the other.

### 8.4 Repository Cache

`JsonRepository` loads the JSON file into an in-memory `LinkedHashMap` on
first access. All reads come from the cache. All writes update the cache
and then flush atomically to disk. This gives O(1) lookups by ID and O(n)
scans for queries, which is sufficient for the target scale (≤5,000 books).

---

## 9. Error Handling Strategy

### 9.1 Exception Hierarchy

```
LibraryException (root, extends RuntimeException)
  ├── AuthenticationException
  ├── UnauthorizedAccessException
  ├── ValidationException
  │     ├── InvalidEmailException
  │     ├── InvalidPhoneException
  │     ├── InvalidRegistrationException
  │     └── InvalidMenuChoiceException
  ├── BookNotFoundException
  ├── BookUnavailableException
  ├── DuplicateBookException
  │     └── DuplicateISBNException
  ├── DuplicateRegistrationException
  │     └── DuplicateLibraryCardException
  ├── BorrowLimitExceededException
  ├── FinePendingException
  ├── MembershipExpiredException
  ├── ReservationException
  ├── ConfigurationException
  └── DataPersistenceException
```

### 9.2 Handling Layers

- **Persistence:** Throws `DataPersistenceException` on I/O failures.
- **Service:** Catches repository exceptions, wraps in domain exceptions,
  logs via `AppLogger`.
- **Controller:** Catches domain exceptions, maps to user-friendly messages.
- **Menu:** Top-level catch-all for unexpected errors, prints and continues.

---

## 10. Testing Strategy

### 10.1 Test Isolation

Tests use JUnit 5 `@TempDir` and the `setOverrideFile()` method on
repositories to redirect file I/O to a temporary directory. This ensures
tests never touch production data files and run in parallel without
interference:

```java
((JsonRepository<?, ?>) bookRepo).setOverrideFile(tempDir.resolve("books.json"));
```

### 10.2 Test Coverage

| Test Class                    | Tests | Area                              |
|-------------------------------|-------|-----------------------------------|
| `JsonTest`                    | 12    | JSON parser + serializer          |
| `PasswordHasherTest`          | 7     | PBKDF2 hashing + verification     |
| `FormatValidatorsTest`        | 22    | ISBN, email, phone, name, year     |
| `BusinessValidatorsTest`      | 14    | Borrow, renew, reserve, password  |
| `BookTest`                    | 10    | Model invariants                  |
| `JsonRepositoryTest`          | 4     | Repository CRUD                   |
| `AuthenticationServiceTest`   | 7     | Login, logout, password change    |
| `BookServiceTest`             | 9     | Add, search, update, delete       |
| `BookCsvTest`                 | 4     | CSV import/export                 |
| `BorrowServiceTest`           | 10    | Issue, return, renew, fines       |
| **Total**                     | **102** |                                 |

---

## 11. Key Design Decisions

### 11.1 Why Hand-Written JSON?

Using Jackson or Gson would add a runtime dependency. The requirement was
zero external libraries. The hand-written codec is ~500 lines, handles all
JSON types, escapes, and edge cases, and is fully tested with 12 unit tests.

### 11.2 Why Integer Paise for Fines?

Floating-point arithmetic introduces rounding errors (e.g., `0.1 + 0.2 !=
0.3`). Fines are stored as integer paise (1 rupee = 100 paise), making all
arithmetic exact. Display logic converts to rupees only at the presentation
layer.

### 11.3 Why In-Memory Cache in Repositories?

For a single-library system with ≤5,000 books, loading the entire dataset
into memory is fast (<50 ms) and gives O(1) ID lookups. The cache is
flushed atomically on every write, so data is never lost. This avoids the
complexity of query planning while meeting performance targets.

### 11.4 Why Builder Pattern on Models?

Models have 5-15 fields. Constructors with that many parameters are
error-prone (positional arguments get swapped). Builders make construction
self-documenting and allow validation of required fields before object
creation.

### 11.5 Why Facade as Composition Root?

Without a DI framework, manual wiring is necessary. Centralizing it in
`LibraryFacade` keeps construction logic in one place, makes dependencies
explicit, and prevents services from instantiating their own dependencies
(which would create hidden coupling).

---

## 12. File Layout

```
project/
├── pom.xml
├── README.md
├── docs/
│   ├── SRS.md
│   └── ARCHITECTURE.md
├── data/
│   └── admins.json              (seed data)
├── logs/
│   └── application.log
└── src/
    ├── main/java/com/librarymanagement/
    │   ├── LibraryManagementApplication.java
    │   ├── config/
    │   ├── constants/
    │   ├── controller/
    │   ├── command/
    │   ├── enums/
    │   ├── exception/
    │   ├── facade/
    │   ├── factory/
    │   ├── interfaces/
    │   ├── mapper/
    │   ├── menu/
    │   ├── model/
    │   ├── notification/
    │   ├── observer/
    │   ├── repository/
    │   ├── security/
    │   ├── service/
    │   ├── strategy/
    │   ├── util/
    │   ├── audit/
    │   ├── analytics/
    │   ├── backup/
    │   └── reports/
    └── test/java/com/librarymanagement/
        ├── model/
        ├── repository/
        ├── util/
        ├── validator/
        └── service/
```
