# User Manual

## University Central Library Management System

Version 1.0 — Core Java Implementation

---

## Table of Contents

1. [Getting Started](#getting-started)
2. [System Requirements](#system-requirements)
3. [Installation](#installation)
4. [Default Credentials](#default-credentials)
5. [Roles Overview](#roles-overview)
6. [Administrator Guide](#administrator-guide)
7. [Librarian Guide](#librarian-guide)
8. [Student Guide](#student-guide)
9. [Troubleshooting](#troubleshooting)
10. [Data and Backup](#data-and-backup)

---

## Getting Started

The Library Management System is a console-based application built in pure Core Java. It manages books, users, borrowing, reservations, fines, and notifications for a university library.

## System Requirements

- **Java 21 LTS** or later
- **Maven 3.9+**
- ~50 MB disk space for data, logs, and backups

## Installation

```bash
# Build the project
mvn clean package

# Run the application
java -cp target/classes com.library.Main
```

## Default Credentials

| Username | Password   | Role          |
|----------|------------|---------------|
| admin    | admin@123  | Administrator |

**Change the admin password immediately after first login.**

---

## Roles Overview

The system implements three roles with strict role-based access control:

| Role          | Scope                                                         |
|---------------|---------------------------------------------------------------|
| Administrator | Full system: config, staff, inventory, analytics, backups     |
| Librarian     | Daily ops: issue/return/renew, reservations, fines, search    |
| Student       | Self-service: dashboard, search, borrow history, fines, profile|

---

## Administrator Guide

### 1. Login
Enter username `admin` and password `admin@123` at the main menu.

### 2. Manage Librarians
- Add a new librarian (name, email, phone, password)
- View all librarians
- Activate/deactivate librarian accounts

### 3. Manage Books
- Add new book (title, author, ISBN, category, publisher, year, copies)
- Update book metadata
- Remove books from inventory
- View all books

### 4. Manage Students
- Register new students (with department and registration number)
- View all students
- View student details and borrow history

### 5. Configuration
- Set loan period (days)
- Set borrow limit per student
- Set fine rate per overdue day
- Set reservation hold period
- Set membership duration

### 6. Reports
- Inventory report
- Borrow activity report
- Overdue report
- Fine collection report
- Lost/damaged books report
- Popular books report
- Inactive members report
- Monthly and yearly reports
- Export any report to CSV

### 7. Backup and Restore
- Create manual backup
- View backup history
- Restore from a backup snapshot

### 8. Audit Logs
- View all audit log entries
- Filter by action type or date range

---

## Librarian Guide

### 1. Issue a Book
1. Select "Issue Book" from the menu
2. Enter the student's registration number
3. Enter the book's ISBN
4. The system validates membership, borrow limit, pending fines, and availability
5. On success, a borrow record is created with the due date

### 2. Return a Book
1. Select "Return Book"
2. Enter the borrow record ID
3. The system calculates any overdue fine automatically
4. If overdue, a fine record is created for the student

### 3. Renew a Book
1. Select "Renew Book"
2. Enter the borrow record ID
3. The due date is extended by the loan period (up to max renewals)

### 4. Manage Reservations
- View pending reservations
- Cancel a reservation
- Process a reservation when the book becomes available

### 5. Manage Fines
- View all pending fines
- Collect a fine payment
- Waive a fine (with reason)

### 6. Mark Book Status
- Mark a book as Lost, Damaged, or Under Repair
- Update status back to Available when repaired

### 7. Search
- Search books by title, author, ISBN, category, or keyword
- Search students by name or registration number

---

## Student Guide

### 1. Dashboard
After login, the dashboard shows:
- Current membership status and expiry date
- Books currently borrowed (with due dates)
- Pending fines total
- Active reservations
- Unread notifications

### 2. Search Books
- Search by title, author, ISBN, category, or keyword
- View availability status

### 3. Borrow History
- View all past and current borrow records
- See due dates, return dates, and status

### 4. Reservations
- View active reservations and their status
- Cancel a reservation

### 5. Fines
- View all fines (pending and paid)
- Pay a pending fine

### 6. Notifications
- View notifications (due date reminders, fine notices, reservation updates)
- Mark notifications as read

### 7. Profile
- View profile details (name, email, phone, department, registration number)
- View library card with membership status
- Change password

---

## Troubleshooting

### "Invalid credentials"
- Verify username and password are correct
- Contact administrator if password reset is needed

### "Membership expired"
- Contact librarian or administrator to renew membership

### "Borrow limit exceeded"
- Return a book before borrowing more
- Default limit is 5 books (configurable by admin)

### "Fine pending"
- Pay all pending fines before borrowing new books

### "Book unavailable"
- Reserve the book; you'll be notified when it becomes available

### Application won't start
- Verify Java 21 is installed: `java -version`
- Verify data files exist in `data/` directory
- Check `logs/error.log` for details

---

## Data and Backup

### Data Location
All data is stored as JSON in the `data/` directory:

| File                  | Contents                                    |
|-----------------------|---------------------------------------------|
| books.json            | Book catalog                                |
| users.json            | All users (admins, librarians, students)    |
| borrow_records.json   | Borrow transactions                         |
| reservations.json     | Book reservations                           |
| fines.json            | Fine records                                |
| notifications.json    | Student notification inboxes               |
| audit_logs.json       | Security audit trail                        |
| library_config.json   | Library configuration settings              |
| counters.json         | ID counters for entity generation           |

### Backups
- Manual backups: `backups/manual/`
- Daily backups: `backups/daily/`
- Weekly backups: `backups/weekly/`

Each backup is a timestamped snapshot of the entire `data/` directory.

### Logs
- `logs/application.log` — General application events
- `logs/audit.log` — Security-relevant actions
- `logs/error.log` — Errors and exceptions
