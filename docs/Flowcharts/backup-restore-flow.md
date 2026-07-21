# Flowchart: Backup and Restore Flow

## Backup

```
START
  │
  ▼
┌───────────────┐
│ Admin selects │
│ "Backup"      │
└───────┬───────┘
        │
        ▼
┌───────────────┐
│ RBAC check:   │─── DENIED ──▶ UnauthorizedException
│ BACKUP_CREATE │
└───────┬───────┘
        │ ALLOWED
        ▼
┌───────────────┐
│ Create backup │
│ directory:    │
│ backups/manual/
│ yyyy-MM-dd_HH │
│ -mm-ss/       │
└───────┬───────┘
        │
        ▼
┌───────────────┐
│ For each JSON │◄──────────────┐
│ file in data/ │               │
└───────┬───────┘               │
        │                        │
        ▼                        │
┌───────────────┐              │
│ Copy file to  │              │
│ backup dir    │              │
│ (atomic)      │              │
└───────┬───────┘              │
        │                        │
        ▼                        │
┌───────────────┐              │
│ More files?   │── YES ────────┘
└───────┬───────┘
        │ NO
        ▼
┌───────────────┐
│ Audit:        │
│ BACKUP_CREATE │
└───────┬───────┘
        │
        ▼
┌───────────────┐
│ Return backup │
│ path to admin │
└───────┬───────┘
        │
        ▼
       END
```

## Restore

```
START
  │
  ▼
┌───────────────┐
│ Admin selects │
│ "Restore"     │
│ + backup path │
└───────┬───────┘
        │
        ▼
┌───────────────┐
│ RBAC check:   │─── DENIED ──▶ UnauthorizedException
│ BACKUP_RESTORE│
└───────┬───────┘
        │ ALLOWED
        ▼
┌───────────────┐     NOT FOUND     ┌───────────────┐
│ Verify backup │─────────────────▶│ "Backup dir    │
│ dir exists    │                  │  not found"    │
└───────┬───────┘                  └───────┬───────┘
        │ EXISTS                           │
        ▼                                  │
┌───────────────┐                         │
│ For each file │◄──────────────┐        │
│ in backup dir │               │        │
└───────┬───────┘               │        │
        │                        │        │
        ▼                        │        │
┌───────────────┐              │        │
│ Copy backup   │              │        │
│ file to data/ │              │        │
│ (atomic)      │              │        │
└───────┬───────┘              │        │
        │                        │        │
        ▼                        │        │
┌───────────────┐              │        │
│ More files?   │── YES ────────┘        │
└───────┬───────┘                        │
        │ NO                              │
        ▼                                  │
┌───────────────┐                      │
│ Reload all    │                      │
│ repository    │                      │
│ caches        │                      │
└───────┬───────┘                      │
        │                                  │
        ▼                                  │
┌───────────────┐                      │
│ Audit:        │                      │
│ BACKUP_RESTORE│                      │
└───────┬───────┘                      │
        │                                  │
        ▼                                  │
       END ──────────────────────────────┘
```
