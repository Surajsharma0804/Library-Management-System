# Class Diagram Description
## Library Management System

**Note:** This is a text description of the class diagram.
Render using PlantUML or draw.io.

---

## Class Hierarchy

```
                    ┌──────────────────────┐
                    │    LibraryFacade      │
                    │  (composition root)   │
                    └──────────┬───────────┘
                               │ wires
          ┌────────────────────┼─────────────────────┐
          ▼                    ▼                       ▼
   ┌──────────────┐  ┌──────────────┐        ┌──────────────┐
   │  Controllers │  │   Services   │        │ Repositories │
   └──────────────┘  └──────────────┘        └──────────────┘
```

## Model Classes

```
┌──────────────────┐     ┌──────────────────┐     ┌──────────────────┐
│      User         │     │     Student       │     │    Librarian     │
│──────────────────│     │──────────────────│     │──────────────────│
│ - id: String      │     │ - id: String      │     │ - id: String      │
│ - username        │     │ - userId: String   │     │ - userId: String   │
│ - passwordHash    │     │ - name: String     │     │ - name: String     │
│ - salt: String    │     │ - email: String    │     │ - email: String    │
│ - role: UserRole  │     │ - phone: String    │     │ - phone: String    │
│ - active: boolean │     │ - registrationNo  │     │ - employeeId      │
│ - createdAt       │     │ - department      │     │ - active: boolean  │
│ - lastLogin       │     │ - program         │     └──────────────────┘
└──────────────────┘     │ - membershipStatus │
                          │ - fineBalancePaise │     ┌──────────────────┐
                          │ - membershipExpiry │     │     Admin        │
                          └──────────────────┘     │──────────────────│
                                                    │ - id: String      │
┌──────────────────┐                                │ - userId: String   │
│      Book        │                                │ - name: String     │
│──────────────────│                                │ - email: String    │
│ - id: String      │                                │ - active: boolean  │
│ - isbn: String    │                                └──────────────────┘
│ - title: String   │
│ - author: String  │     ┌──────────────────┐     ┌──────────────────┐
│ - publisher       │     │  BorrowRecord    │     │  Reservation     │
│ - edition        │     │──────────────────│     │──────────────────│
│ - publicationYear│     │ - id: String      │     │ - id: String      │
│ - genre          │     │ - bookId: String   │     │ - bookId: String   │
│ - language       │     │ - studentId       │     │ - studentId       │
│ - totalCopies    │     │ - issueDate       │     │ - reservedAt      │
│ - availableCopies│     │ - dueDate         │     │ - expiresAt       │
│ - borrowedCopies │     │ - returnDate      │     │ - status          │
│ - reservedCopies │     │ - status: BorrowStatus│  └──────────────────┘
│ - status         │     │ - renewCount      │
│ - location       │     │ - fineAmountPaise │     ┌──────────────────┐
│ - archived       │     └──────────────────┘     │      Fine         │
└──────────────────┘                                │──────────────────│
                                                    │ - id: String      │
┌──────────────────┐                                │ - studentId       │
│    AuditLog      │                                │ - borrowId        │
│──────────────────│                                │ - bookId          │
│ - id: String      │                                │ - amountPaise      │
│ - actorId         │                                │ - reason: String   │
│ - action: String  │                                │ - status: FineStatus│
│ - entityType      │                                │ - createdAt        │
│ - entityId        │                                └──────────────────┘
│ - timestamp       │
│ - details         │     ┌──────────────────┐     ┌──────────────────┐
└──────────────────┘     │  LibraryConfig   │     │   Counters       │
                          │──────────────────│     │──────────────────│
                          │ - loanPeriodDays │     │ - bookCounter    │
                          │ - maxBorrows     │     │ - studentCounter  │
                          │ - maxRenewals    │     │ - borrowCounter   │
                          │ - fineRatePaise  │     │ - reservationCntr │
                          │ - reservationHold│     │ - fineCounter     │
                          └──────────────────┘     └──────────────────┘
```

## Service Classes

```
┌────────────────────┐  ┌────────────────────┐  ┌────────────────────┐
│ AuthenticationService│  │    BookService     │  │  StudentService    │
│────────────────────│  │────────────────────│  │────────────────────│
│ - staffRepo        │  │ - bookRepo        │  │ - studentRepo      │
│ - sessionMgr       │  │ - searchEngine    │  │ - validators       │
│ - passwordHasher   │  │ - entityFactory   │  │ - entityFactory    │
│ - auditService     │  │ - auditService    │  │ - auditService     │
│ - rbacService      │  │ - rbacService     │  │ - rbacService      │
│────────────────────│  │────────────────────│  │────────────────────│
│ + login()          │  │ + addBook()       │  │ + registerStudent()│
│ + logout()         │  │ + searchBooks()   │  │ + getStudent()     │
│ + changePassword() │  │ + updateBook()    │  │ + updateStudent()  │
└────────────────────┘  │ + deleteBook()    │  │ + deactivate()     │
                        │ + archiveBook()   │  └────────────────────┘
┌────────────────────┐  │ + importFromCsv() │
│   BorrowService    │  │ + exportToCsv()   │  ┌────────────────────┐
│────────────────────│  └────────────────────┘  │ ReservationService │
│ - borrowRepo       │                            │────────────────────│
│ - bookRepo         │  ┌────────────────────┐  │ - reservationRepo  │
│ - studentRepo     │  │    FineService     │  │ - bookRepo         │
│ - fineService     │  │────────────────────│  │ - studentRepo      │
│ - configService   │  │ - fineRepo        │  │ - notificationPub  │
│ - auditService    │  │ - studentRepo     │  │ - auditService     │
│ - rbacService     │  │ - auditService    │  │────────────────────│
│ - entityFactory   │  │ - rbacService     │  │ + reserveBook()    │
│────────────────────│  │────────────────────│  │ + cancelReservation()│
│ + issueBook()      │  │ + recordFine()    │  │ + approveReservation()│
│ + returnBook()     │  │ + payFine()       │  │ + rejectReservation()│
│ + renewBook()      │  │ + waiveFine()     │  └────────────────────┘
└────────────────────┘  └────────────────────┘
```

## Repository Classes

```
┌──────────────────────────────┐
│ JsonRepository<E, ID> (abstract) │
│──────────────────────────────│
│ # file: Path                  │
│ # cache: LinkedHashMap<ID,E> │
│ # mapper: JsonMappable<E>    │
│ # loaded: boolean             │
│──────────────────────────────│
│ + save(E): E                  │
│ + findById(ID): Optional<E>  │
│ + findAll(): List<E>          │
│ + delete(ID): boolean        │
│ + count(): long               │
│ + filter(Predicate): List<E> │
│ # load(): void                │
│ # flush(): void               │
│ + setOverrideFile(Path): void │
└──────────────────────────────┘
        ▲
        │ extends
┌───────┴────────┐  ┌─────────────┐  ┌──────────────┐
│ BookRepository  │  │ StudentRepo │  │ StaffRepo    │
│────────────────│  │─────────────│  │──────────────│
│+findByIsbn()   │  │+findByRegNo│  │+findByUsername│
│+findByStatus() │  │+findByEmail│  └──────────────┘
└────────────────┘  └─────────────┘
┌────────────────┐  ┌─────────────┐  ┌──────────────┐
│ BorrowRepo     │  │ ReserveRepo │  │ FineRepo     │
│────────────────│  │─────────────│  │──────────────│
│+findActiveByStu│  │+findPending │  │+findPending  │
│+findActiveByBk │  │  ByBook()   │  │  ByStudent() │
│+findOverdue()  │  │+findByStudent│  │+findByStudent│
└────────────────┘  └─────────────┘  └──────────────┘
```

## Design Pattern Classes

```
STRATEGY:
┌──────────────────────┐
│ BookSearchStrategy   │ (interface)
│──────────────────────│
│ + search(List<Book>, │
│   String): List<Book> │
└──────────────────────┘
         ▲
         │ implements
┌────────┴───┬──────────┬──────────┐
│TitleStrategy│AuthorStrat│...11 more│
└─────────────┴──────────┴──────────┘
         │ used by
         ▼
┌──────────────────────┐
│  BookSearchEngine    │
│──────────────────────│
│ - strategies: Map    │
│──────────────────────│
│ + search(books,query,│
│   strategyName)      │
└──────────────────────┘

OBSERVER:
┌──────────────────────┐
│ NotificationListener │ (interface)
│──────────────────────│
│ + onNotification(evt)│
└──────────────────────┘
         ▲
         │ implements
┌────────┴───────────┐
│ NotificationService │
└─────────────────────┘
         │ registered with
         ▼
┌──────────────────────┐
│ NotificationPublisher │
│──────────────────────│
│ - listeners: List    │
│──────────────────────│
│ + subscribe()        │
│ + publish(event)     │
└──────────────────────┘

COMMAND:
┌──────────────────────┐
│  LibraryCommand       │ (interface)
│──────────────────────│
│ + execute(): void    │
│ + undo(): void       │
└──────────────────────┘
         ▲
         │ implements
┌────────┴───────────┐
│  IssueBookCommand   │
│─────────────────────│
│ - borrowService     │
│ - session           │
│ - studentId         │
│ - bookId            │
└─────────────────────┘

FACTORY:
┌──────────────────────┐
│   EntityFactory       │
│──────────────────────│
│ - idGenerator        │
│──────────────────────│
│ + createBook()       │
│ + createStudent()    │
│ + createBorrowRecord()│
│ + createFine()       │
│ + createReservation()│
└──────────────────────┘
```
