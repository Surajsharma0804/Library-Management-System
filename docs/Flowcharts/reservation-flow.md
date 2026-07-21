# Flowchart: Reservation Flow

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
│ Find book    │─────────────────▶│ BookNotFound   │
│ by bookId     │                  │ Exception      │
└───────┬───────┘                  └───────┬───────┘
        │ FOUND                            │
        ▼                                  │
┌───────────────┐                         │
│ Find student  │                         │
│ by studentId  │                         │
└───────┬───────┘                         │
        │                                   │
        ▼                                   │
┌───────────────┐     EXPIRED       ┌───────────────┐
│ Membership   │─────────────────▶│ Membership     │
│ active?       │                  │ Expired        │
└───────┬───────┘                  └───────┬───────┘
        │ ACTIVE                            │
        ▼                                  │
┌───────────────┐     AVAILABLE     ┌───────────────┐
│ Available    │─────────────────▶│ "Book is       │
│ copies > 0?  │                  │  available -   │
└───────┬───────┘                  │  borrow it"    │
        │ ZERO                      └───────┬───────┘
        ▼                                  │
┌───────────────┐                         │
│ Check existing│                         │
│ pending       │                         │
│ reservation   │                         │
│ by student    │                         │
└───────┬───────┘                         │
        │                                   │
        ├── ALREADY EXISTS ──▶┌───────────────┐
        │                       │ "Already      │
        │                       │  reserved"    │
        │                       └───────┬───────┘
        ▼                               │
┌───────────────┐                      │
│ Create         │                      │
│ Reservation    │                      │
│ (status =      │                      │
│  PENDING)      │                      │
└───────┬───────┘                      │
        │                                │
        ▼                                │
┌───────────────┐                      │
│ Set expiresAt │                      │
│ = now + hold  │                      │
│   period      │                      │
└───────┬───────┘                      │
        │                                │
        ▼                                │
┌───────────────┐                      │
│ Save           │                      │
│ reservation   │                      │
│ (atomic)      │                      │
└───────┬───────┘                      │
        │                                │
        ▼                                │
┌───────────────┐                      │
│ Publish        │                      │
│ Notification  │                      │
│ Event         │                      │
└───────┬───────┘                      │
        │                                │
        ▼                                │
┌───────────────┐                      │
│ Audit:        │                      │
│ RESERVE_CREATE│                      │
└───────┬───────┘                      │
        │                                │
        ▼                                │
       END ─────────────────────────────┘
```
