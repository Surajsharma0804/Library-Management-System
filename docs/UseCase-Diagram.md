# Use Case Diagram Description
## Library Management System

**Note:** This is a text description of the use case diagram.
Render using PlantUML or draw.io.

---

## Actors

| Actor | Description |
|-------|-------------|
| Admin | System administrator with full access |
| Librarian | Library staff managing books and circulation |
| Student | Library member borrowing and reserving books |

---

## Use Cases by Actor

### Admin Use Cases

```
Admin
 ├─── Manage Staff (add/remove librarians)
 ├─── View All Users
 ├─── Manage Books (add, update, delete, archive, restore)
 ├─── Mark Book Lost/Damaged
 ├─── Import Books from CSV
 ├─── Export Books to CSV
 ├─── Register Student
 ├─── Update Student
 ├─── Activate/Deactivate Student
 ├─── Issue Book
 ├─── Process Return
 ├─── Renew Book
 ├─── Manage Reservations (approve/reject)
 ├─── Record Fine
 ├─── Waive Fine
 ├─── View Analytics Dashboard
 ├─── Generate Reports
 ├─── Create Backup
 ├─── Restore Backup
 ├─── View Audit Logs
 ├─── Update Configuration
 ├─── Change Own Password
 └─── Logout
```

### Librarian Use Cases

```
Librarian
 ├─── Manage Books (add, update)
 ├─── Search Books
 ├─── Register Student
 ├─── Update Student
 ├─── Issue Book
 ├─── Process Return
 ├─── Renew Book
 ├─── Manage Reservations (approve/reject)
 ├─── Record Fine
 ├─── Collect Fine Payment
 ├─── View Circulation Reports
 ├─── View Overdue Books
 ├─── Change Own Password
 └─── Logout
```

### Student Use Cases

```
Student
 ├─── Search Books
 ├─── View Book Details
 ├─── View Own Borrow History
 ├─── View Currently Borrowed Books
 ├─── Reserve Book
 ├─── Cancel Own Reservation
 ├─── View Own Reservations
 ├─── Pay Fine
 ├─── View Own Fines
 ├─── View Own Notifications
 ├─── View Own Dashboard
 ├─── Change Own Password
 └─── Logout
```

---

## Use Case Diagram (Text)

```
                    ┌─────────────────────────────────────────────────┐
                    │              Library Management System            │
                    │                                                   │
  ┌──────┐         │   ┌──────────┐  ┌──────────┐  ┌──────────┐      │
  │      │──login──▶│   │  Login    │  │ Search   │  │ View     │      │
  │      │         │   │           │  │  Books   │  │ Dashboard│      │
  │ Admin│         │   └──────────┘  └──────────┘  └──────────┘      │
  │      │         │                                                   │
  │      │──manage─▶│   ┌──────────┐  ┌──────────┐  ┌──────────┐      │
  │      │  staff   │   │ Manage   │  │ Issue    │  │ Process  │      │
  └──────┘         │   │ Books    │  │ Book     │  │ Return   │      │
                    │   └──────────┘  └──────────┘  └──────────┘      │
                    │                                                   │
  ┌──────┐         │   ┌──────────┐  ┌──────────┐  ┌──────────┐      │
  │      │──issue──▶│   │ Renew    │  │ Reserve  │  │ Manage   │      │
  │Libr- │  book   │   │ Book     │  │ Book     │  │ Reservat.│      │
  │arian │         │   └──────────┘  └──────────┘  └──────────┘      │
  │      │──report─▶│   ┌──────────┐  ┌──────────┐                    │
  └──────┘         │   │ Record   │  │ Collect  │                    │
                    │   │ Fine     │  │ Payment  │                    │
                    │   └──────────┘  └──────────┘                    │
                    │                                                   │
  ┌──────┐         │   ┌──────────┐  ┌──────────┐  ┌──────────┐      │
  │      │──search─▶│   │ View     │  │ Reserve  │  │ Pay      │      │
  │      │  books  │   │ History  │  │ Book     │  │ Fine     │      │
  │Stud- │         │   └──────────┘  └──────────┘  └──────────┘      │
  │ ent  │         │                                                   │
  │      │──view───▶│   ┌──────────┐  ┌──────────┐                    │
  │      │  notif  │   │ Cancel   │  │ Change   │                    │
  └──────┘         │   │ Reservat.│  │ Password │                    │
                    │   └──────────┘  └──────────┘                    │
                    │                                                   │
                    │   ┌──────────┐  ┌──────────┐  ┌──────────┐      │
                    │   │ Backup   │  │ Restore  │  │ View     │      │
                    │   │          │  │          │  │ Audit Log│      │
                    │   └──────────┘  └──────────┘  └──────────┘      │
                    │                                                   │
                    │   ┌──────────┐  ┌──────────┐                    │
                    │   │ Analytics│  │ Config   │                    │
                    │   └──────────┘  └──────────┘                    │
                    └─────────────────────────────────────────────────┘
```

## Include/Extend Relationships

| Base Use Case | Relationship | Included/Extended Use Case |
|---------------|-------------|---------------------------|
| Login | include | Authenticate (RBAC check) |
| Issue Book | include | Check Borrow Limit |
| Issue Book | include | Check Membership Status |
| Issue Book | include | Check Pending Fines |
| Process Return | extend | Record Fine (if overdue) |
| Reserve Book | include | Check Book Availability |
| Renew Book | include | Check Pending Reservations |
| Generate Report | include | Check RBAC Permission |
| Create Backup | include | Check RBAC Permission |
