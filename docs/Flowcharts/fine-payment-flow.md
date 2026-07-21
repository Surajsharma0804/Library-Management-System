# Flowchart: Fine Payment Flow

```
START
  │
  ▼
┌───────────────┐
│ Read fineId   │
│ + amountPaise │
└───────┬───────┘
        │
        ▼
┌───────────────┐     NOT FOUND     ┌───────────────┐
│ Find fine by │─────────────────▶│ Fine not found │
│ fineId       │                  │ error          │
└───────┬───────┘                  └───────┬───────┘
        │ FOUND                            │
        ▼                                  │
┌───────────────┐     PAID/WAIVED  ┌───────────────┐
│ Fine status   │─────────────────▶│ "Fine already │
│ == PENDING?  │                  │  resolved"    │
└───────┬───────┘                  └───────┬───────┘
        │ PENDING                          │
        ▼                                  │
┌───────────────┐                         │
│ Find student  │                         │
│ by studentId  │                         │
└───────┬───────┘                         │
        │                                   │
        ▼                                   │
┌───────────────┐     EXCEEDS       ┌───────────────┐
│ Amount <=    │─────────────────▶│ "Payment      │
│ fine amount? │                  │  exceeds fine"│
└───────┬───────┘                  └───────┬───────┘
        │ OK                                │
        ▼                                  │
┌───────────────┐                         │
│ Reduce student│                         │
│ fineBalance   │                         │
│ by amount     │                         │
└───────┬───────┘                         │
        │                                   │
        ▼                                   │
┌───────────────┐                         │
│ If fully paid:│                         │
│ status = PAID │                         │
│ Else: partial │                         │
│ payment noted │                         │
└───────┬───────┘                         │
        │                                   │
        ▼                                   │
┌───────────────┐                         │
│ Save fine     │                         │
│ (atomic)      │                         │
└───────┬───────┘                         │
        │                                   │
        ▼                                   │
┌───────────────┐                         │
│ Save student  │                         │
│ (atomic)      │                         │
└───────┬───────┘                         │
        │                                   │
        ▼                                   │
┌───────────────┐                         │
│ Audit:        │                         │
│ FINE_PAID     │                         │
└───────┬───────┘                         │
        │                                   │
        ▼                                   │
       END ────────────────────────────────┘
```
