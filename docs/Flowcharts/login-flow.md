# Flowchart: Login Flow

```
START
  │
  ▼
┌───────────────┐
│ Display Main  │
│ Menu          │
└───────┬───────┘
        │
        ▼
┌───────────────┐
│ Read username │
│ + password    │
└───────┬───────┘
        │
        ▼
┌───────────────┐     NOT FOUND     ┌───────────────┐
│ Find user by  │─────────────────▶│ "User not     │
│ username      │                  │  found"       │
└───────┬───────┘                  └───────┬───────┘
        │ FOUND                            │
        ▼                                  │
┌───────────────┐                         │
│ Is user       │─── NO ──────────────▶ │ "Account     │
│ active?       │                        │  inactive"   │
└───────┬───────┘                  ┌───────┬───────┘
        │ YES                       │
        ▼                           │
┌───────────────┐                  │
│ Verify        │                  │
│ password with │                  │
│ PBKDF2        │                  │
└───────┬───────┘                  │
        │                           │
        ▼                           │
┌───────────────┐   MISMATCH      │
│ Password      │────────────────▶│ "Invalid      │
│ matches?      │                  │  credentials" │
└───────┬───────┘                  └───────┬───────┘
        │ MATCH                            │
        ▼                                  │
┌───────────────┐                         │
│ Create session│                         │
│ (UUID token)  │                         │
└───────┬───────┘                         │
        │                                   │
        ▼                                   │
┌───────────────┐                         │
│ Log login     │                         │
│ (audit)       │                         │
└───────┬───────┘                         │
        │                                   │
        ▼                                   │
┌───────────────┐                         │
│ Route to role │                         │
│ menu (Admin/  │                         │
│ Lib/Student)  │                         │
└───────┬───────┘                         │
        │                                   │
        ▼                                   │
       END ────────────────────────────────┘
```
