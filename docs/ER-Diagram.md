# ER Diagram Description
## Library Management System

**Note:** This is a text description of the Entity-Relationship diagram.
Render as a visual diagram using tools like draw.io, PlantUML, or dbdiagram.io.

---

## Entities and Relationships

```
┌─────────────────┐         ┌──────────────────┐         ┌─────────────────┐
│      USER        │         │      BOOK        │         │   BORROW_RECORD  │
│─────────────────│         │──────────────────│         │─────────────────│
│ id (PK)          │         │ id (PK)          │         │ id (PK)          │
│ username         │         │ isbn (UQ)        │         │ bookId (FK)      │───▶ BOOK
│ passwordHash     │         │ title            │         │ studentId (FK)   │───▶ STUDENT
│ salt             │         │ author           │         │ issueDate        │
│ role (ENUM)      │         │ publisher        │         │ dueDate          │
│ active           │         │ edition          │         │ returnDate       │
│ createdAt        │         │ publicationYear  │         │ status (ENUM)    │
│ lastLogin        │         │ genre            │         │ renewCount       │
└────────┬────────┘         │ language         │         │ fineAmountPaise  │
         │                   │ totalCopies      │         └─────────────────┘
         │                   │ availableCopies  │
         │                   │ borrowedCopies   │         ┌─────────────────┐
         │                   │ reservedCopies   │         │   RESERVATION    │
         │                   │ status (ENUM)    │         │─────────────────│
         │                   │ location         │         │ id (PK)          │
         │                   │ archived         │         │ bookId (FK)      │───▶ BOOK
         │                   └──────────────────┘         │ studentId (FK)  │───▶ STUDENT
         │                                                  │ reservedAt      │
         │                   ┌──────────────────┐         │ expiresAt        │
         │                   │     STUDENT       │         │ status (ENUM)    │
         │                   │──────────────────│         └─────────────────┘
         ├──────────────────▶│ id (PK)          │
         │                   │ userId (FK)       │         ┌─────────────────┐
         │                   │ name             │         │      FINE        │
         │                   │ email (UQ)       │         │─────────────────│
         │                   │ phone            │         │ id (PK)          │
         │                   │ registrationNo   │         │ studentId (FK)  │───▶ STUDENT
         │                   │ department       │         │ borrowId (FK)   │───▶ BORROW_RECORD
         │                   │ program          │         │ bookId (FK)     │───▶ BOOK
         │                   │ membershipStatus  │         │ amountPaise      │
         │                   │ fineBalancePaise  │         │ reason           │
         │                   │ membershipExpiry  │         │ status (ENUM)    │
         │                   └──────────────────┘         │ createdAt        │
         │                                                  └─────────────────┘
         │                   ┌──────────────────┐
         │                   │    AUDIT_LOG      │         ┌─────────────────┐
         │                   │──────────────────│         │  NOTIFICATION   │
         │                   │ id (PK)          │         │─────────────────│
         │                   │ actorId           │         │ id (PK)          │
         │                   │ action (ENUM)     │         │ studentId (FK)  │───▶ STUDENT
         │                   │ entityType        │         │ type (ENUM)      │
         │                   │ entityId           │         │ message          │
         │                   │ timestamp         │         │ read             │
         │                   │ details           │         │ createdAt        │
         │                   └──────────────────┘         └─────────────────┘
         │
         │                   ┌──────────────────┐
         └──────────────────▶│    LIBRARIAN      │
                             │──────────────────│
                             │ id (PK)          │
                             │ userId (FK)      │
                             │ name             │
                             │ email            │
                             │ phone            │
                             │ employeeId       │
                             │ active           │
                             └──────────────────┘
```

## Relationships

| From | To | Type | Description |
|------|----|----|-------------|
| USER | STUDENT | 1:1 | Each student has one user account |
| USER | LIBRARIAN | 1:1 | Each librarian has one user account |
| STUDENT | BORROW_RECORD | 1:N | A student can have many borrow records |
| BOOK | BORROW_RECORD | 1:N | A book can be borrowed many times |
| STUDENT | RESERVATION | 1:N | A student can have many reservations |
| BOOK | RESERVATION | 1:N | A book can have many reservations |
| STUDENT | FINE | 1:N | A student can have many fines |
| BORROW_RECORD | FINE | 1:1 | Each overdue borrow can generate one fine |
| STUDENT | NOTIFICATION | 1:N | A student can have many notifications |
| USER | AUDIT_LOG | 1:N | Each audit log entry records the acting user |

## Cardinality Notes

- A STUDENT can have at most `maxBorrows` (default 3) active BORROW_RECORDs at a time.
- A BOOK has `totalCopies` copies; `availableCopies + borrowedCopies + reservedCopies = totalCopies`.
- A FINE is linked to exactly one BORROW_RECORD (the overdue return that generated it).
- RESERVATION is only created when all copies of a BOOK are borrowed.
