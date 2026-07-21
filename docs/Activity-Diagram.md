# Activity Diagrams

## 1. Login Activity

```
[Start] → Prompt for username/password → [Validate input] →
  └─ Invalid → Show error → [End]
  └─ Valid → Hash password → Compare with stored hash →
       ├─ Mismatch → Show "Invalid credentials" → [End]
       └─ Match → Create session → Load role menu → [End]
```

## 2. Book Issue Activity

```
[Start] → Librarian enters student ID + book ISBN →
  → Check student membership active →
    └─ Expired → Show error → [End]
  → Check student borrow count < limit →
    └─ Exceeded → Show error → [End]
  → Check student has no pending fines →
    └─ Fines pending → Show error → [End]
  → Check book available (copies > 0) →
    └─ Unavailable → Offer reservation → [End]
  → Create borrow record → Decrement available copies →
  → Set due date (today + loanPeriodDays) →
  → Log audit entry → Send notification → [End]
```

## 3. Book Return Activity

```
[Start] → Librarian enters borrow record ID →
  → Fetch borrow record →
    └─ Not found → Show error → [End]
  → Mark record RETURNED → Increment available copies →
  → Calculate days overdue →
    └─ Overdue → Compute fine (days × finePerDay) → Create fine record →
  → Check pending reservations for this book →
    └─ Reservations exist → Notify first in queue →
  → Log audit entry → [End]
```

## 4. Reservation Activity

```
[Start] → Student searches for book → Book unavailable →
  → Student requests reservation →
  → Check student reservation count < limit →
    └─ Exceeded → Show error → [End]
  → Create reservation (status: PENDING) →
  → Log audit entry → Send notification → [End]
```

## 5. Fine Payment Activity

```
[Start] → Student views pending fines → Selects fine to pay →
  → Mark fine PAID → Log audit entry → Send notification → [End]
```

## 6. Backup Activity

```
[Start] → Admin selects Backup →
  → Create timestamped snapshot of data/ →
  → Copy to backups/<type>/ (daily/weekly/manual) →
  → Log audit entry → [End]
```

