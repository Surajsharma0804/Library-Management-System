# Flowchart: Book Issue Flow

```
START
  │
  ▼
┌───────────────┐
│ Read studentId│
│ + bookId      │
└───────┬───────┘
        │
        ▼
┌───────────────┐     NOT FOUND     ┌───────────────┐
│ Find book by  │─────────────────▶│ BookNotFound   │
│ bookId        │                  │ Exception      │
└───────┬───────┘                  └───────┬───────┘
        │ FOUND                            │
        ▼                                  │
┌───────────────┐                         │
│ Find student  │                         │
│ by studentId  │                         │
└───────┬───────┘                         │
        │                                  │
        ▼                                  │
┌───────────────┐     EXPIRED       ┌───────────────┐
│ Membership   │─────────────────▶│ Membership     │
│ active?       │                  │ Expired        │
└───────┬───────┘                  └───────┬───────┘
        │ ACTIVE                            │
        ▼                                  │
┌───────────────┐     PENDING       ┌───────────────┐
│ Pending      │─────────────────▶│ FinePending    │
│ fines?        │                  │ Exception      │
└───────┬───────┘                  └───────┬───────┘
        │ NONE                              │
        ▼                                  │
┌───────────────┐     EXCEEDED      ┌───────────────┐
│ Borrow limit │─────────────────▶│ BorrowLimit    │
│ reached?     │                  │ Exceeded       │
└───────┬───────┘                  └───────┬───────┘
        │ OK                                │
        ▼                                  │
┌───────────────┐     ZERO         ┌───────────────┐
│ Available    │─────────────────▶│ BookUnavail    │
│ copies > 0?  │                  │ ableException  │
└───────┬───────┘                  └───────┬───────┘
        │ YES                              │
        ▼                                  │
┌───────────────┐                         │
│ Create        │                         │
│ BorrowRecord  │                         │
│ (issueDate,   │                         │
│  dueDate)     │                         │
└───────┬───────┘                         │
        │                                   │
        ▼                                   │
┌───────────────┐                         │
│ Update book:  │                         │
│ availCopies-- │                         │
│ borrowCopies++│                         │
└───────┬───────┘                         │
        │                                   │
        ▼                                   │
┌───────────────┐                         │
│ Save borrow   │                         │
│ record to JSON│                         │
└───────┬───────┘                         │
        │                                   │
        ▼                                   │
┌───────────────┐                         │
│ Save book to  │                         │
│ JSON (atomic)│                         │
└───────┬───────┘                         │
        │                                   │
        ▼                                   │
┌───────────────┐                         │
│ Audit log:    │                         │
│ BORROW_ISSUE  │                         │
└───────┬───────┘                         │
        │                                   │
        ▼                                   │
┌───────────────┐                         │
│ Return        │                         │
│ BorrowRecord  │                         │
└───────┬───────┘                         │
        │                                   │
        ▼                                   │
       END ────────────────────────────────┘
```
