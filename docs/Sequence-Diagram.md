# Sequence Diagram Descriptions
## Library Management System

**Note:** These are text descriptions of sequence diagrams.
Render using PlantUML or draw.io.

---

## 1. Login Sequence

```
User        MainMenu    AuthController    AuthService    StaffRepo    SessionMgr
  │            │             │                │             │             │
  │──enter────▶│             │                │             │             │
  │  username  │             │                │             │             │
  │  +password │──login()───▶│                │             │             │
  │            │             │──login()──────▶│             │             │
  │            │             │                │──findByUsername()──▶│      │
  │            │             │                │◀──User object──────│      │
  │            │             │                │                          │
  │            │             │                │──verify password───       │
  │            │             │                │  (PBKDF2)                 │
  │            │             │                │                          │
  │            │             │                │──createSession()─────────▶│
  │            │             │                │◀──Session token──────────│
  │            │             │◀──Session──────│                          │
  │            │◀──redirect──│                │                          │
  │            │  to role    │                │                          │
  │            │  menu       │                │                          │
  │◀──show menu│             │                │                          │
```

---

## 2. Issue Book Sequence

```
Librarian   LibrarianMenu   BookController   BorrowService   BookRepo   StudentRepo   FineService
  │              │                │                │              │           │             │
  │──select──────▶│                │                │              │           │             │
  │  issue book   │──issue()──────▶│                │              │           │             │
  │               │                │──issue()──────▶│              │           │             │
  │               │                │                │──RBAC check─│           │             │
  │               │                │                │──find book───▶│           │             │
  │               │                │                │◀──Book────────│           │             │
  │               │                │                │──find student─────────────▶│           │
  │               │                │                │◀──Student────────────────│           │
  │               │                │                │                                          │
  │               │                │                │──validate:                               │
  │               │                │                │  · copies available?                    │
  │               │                │                │  · borrow limit?                        │
  │               │                │                │  · membership active?                   │
  │               │                │                │  · pending fines?                      │
  │               │                │                │                                          │
  │               │                │                │──create BorrowRecord                    │
  │               │                │                │──save record─────────────▶│           │
  │               │                │                │──update book copies──────▶│           │
  │               │                │                │──audit log───────────────▶│           │
  │               │                │                │  (BORROW_ISSUE)                        │
  │               │                │◀──BorrowRecord─│              │           │             │
  │               │◀──success──────│                │              │           │             │
  │◀──display─────│                │                │              │           │             │
```

---

## 3. Return Book Sequence (with overdue fine)

```
Librarian   LibrarianMenu   CirculationController   BorrowService   FineService   AuditService
  │              │                  │                     │               │             │
  │──select──────▶│                  │                     │               │             │
  │  return book  │──return()───────▶│                     │               │             │
  │               │                  │──returnBook()──────▶│               │             │
  │               │                  │                     │──find record─▶│             │
  │               │                  │                     │◀──BorrowRecord│             │
  │               │                  │                     │                             │
  │               │                  │                     │──check overdue:             │
  │               │                  │                     │  dueDate < now?             │
  │               │                  │                     │                             │
  │               │                  │                     │ [IF OVERDUE]                │
  │               │                  │                     │──calculate fine:            │
  │               │                  │                     │  days × ratePaise           │
  │               │                  │                     │──recordFine()──────────────▶│
  │               │                  │                     │                             │──log
  │               │                  │                     │                             │  FINE_RECORD
  │               │                  │                     │                             │
  │               │                  │                     │──update book copies         │
  │               │                  │                     │──save record                │
  │               │                  │                     │──audit: BORROW_RETURN───────▶│
  │               │                  │◀──result────────────│               │             │
  │               │◀──success─────────│                     │               │             │
  │◀──display─────│                  │                     │               │             │
```

---

## 4. Reserve Book Sequence

```
Student    StudentMenu   StudentController   ReservationService   BookRepo   NotificationPub
  │            │                │                    │                 │             │
  │──select────▶│                │                    │                 │             │
  │  reserve    │──reserve()────▶│                    │                 │             │
  │             │                │──reserve()───────▶│                 │             │
  │             │                │                    │──find book──────▶│             │
  │             │                │                    │◀──Book──────────│             │
  │             │                │                    │                               │
  │             │                │                    │──check: all copies borrowed?  │
  │             │                │                    │  (availableCopies == 0)       │
  │             │                │                    │                               │
  │             │                │                    │──create Reservation           │
  │             │                │                    │──save reservation              │
  │             │                │                    │──publish event────────────────▶│
  │             │                │                    │                               │
  │             │                │◀──Reservation─────│                 │             │
  │             │◀──success──────│                    │                 │             │
  │◀──display───│                │                    │                 │             │
```

---

## 5. Search Books Sequence

```
User       Menu          Controller      BookService     SearchEngine     Strategy
  │          │                │               │               │              │
  │──enter──▶│                │               │               │              │
  │  query   │──search()──────▶│               │               │              │
  │          │                │──search()────▶│               │              │
  │          │                │               │──get all books│              │
  │          │                │               │──search()───────────────────▶│
  │          │                │               │               │──select strategy
  │          │                │               │               │──execute()──────────▶│
  │          │                │               │               │◀──filtered books───│
  │          │                │               │◀──List<Book>──│              │
  │          │                │◀──results─────│               │              │
  │          │◀──display──────│               │               │              │
  │◀──show───│                │               │               │              │
```

---

## 6. Backup Sequence

```
Admin      AdminMenu      AdminController    BackupService     FileUtils
  │           │                │                  │               │
  │──select──▶│                │                  │               │
  │  backup   │──backup()─────▶│                  │               │
  │           │                │──createBackup()─▶│               │
  │           │                │                  │──create dir   │
  │           │                │                  │  backups/manual/│
  │           │                │                  │  timestamp/    │
  │           │                │                  │               │
  │           │                │                  │──copy each    │
  │           │                │                  │  data file────▶│
  │           │                │                  │  (atomic copy) │
  │           │                │                  │               │
  │           │                │                  │──audit log    │
  │           │                │◀──backup path───│               │
  │           │◀──success──────│                  │               │
  │◀──display─│                │                  │               │
```
