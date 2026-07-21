package com.library.menu;

import com.library.controller.AdminController;
import com.library.controller.BookController;
import com.library.controller.LibrarianController;
import com.library.controller.StudentController;
import com.library.enums.BookStatus;
import com.library.enums.MembershipStatus;
import com.library.facade.LibraryFacade;
import com.library.model.AuditLog;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.Librarian;
import com.library.model.LibraryConfig;
import com.library.model.Student;
import com.library.security.Permissions;
import com.library.security.Session;
import com.library.util.ConsoleInput;
import com.library.util.DateUtils;
import com.library.util.StringUtils;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Administrator-facing menu: system configuration, librarian management,
 * inventory, student management, analytics, reports, backup, and audit.
 */
final class AdminMenu extends AbstractMenu {

    private final AdminController admin;
    private final BookController books;
    private final StudentController students;
    private final LibrarianController circulation;
    private final LibraryFacade facade;
    private final Session session;

    AdminMenu(ConsoleInput in, AdminController admin, BookController books,
              StudentController students, LibrarianController circulation,
              LibraryFacade facade, Session session) {
        super(in);
        this.admin = admin;
        this.books = books;
        this.students = students;
        this.circulation = circulation;
        this.facade = facade;
        this.session = session;
    }

    @Override
    protected String title() {
        return "Administrator Console - " + session.username();
    }

    @Override
    protected List<String> options() {
        return List.of(
                "System Dashboard",
                "Library Configuration",
                "Manage Librarians",
                "Book Inventory Management",
                "Student Management",
                "Circulation Operations",
                "Analytics",
                "Reports",
                "Audit Logs",
                "Backup & Restore",
                "Change Password");
    }

    @Override
    protected boolean handle(int choice) {
        switch (choice) {
            case 1 -> dashboard();
            case 2 -> configuration();
            case 3 -> manageLibrarians();
            case 4 -> bookInventory();
            case 5 -> studentManagement();
            case 6 -> circulationOps();
            case 7 -> analytics();
            case 8 -> reports();
            case 9 -> auditLogs();
            case 10 -> backupRestore();
            case 11 -> changePassword();
            default -> System.out.println("Invalid choice.");
        }
        return true;
    }

    private void dashboard() {
        printSection("System Dashboard");
        var a = admin.analytics(session);
        System.out.println("Total Books:         " + a.totalBooks());
        System.out.println("Total Students:      " + a.totalStudents());
        System.out.println("Active Borrows:      " + a.totalActiveBorrows());
        System.out.println("Overdue Borrows:     " + a.totalOverdueBorrows());
        System.out.println("Pending Fines:       " + a.totalPendingFines());
        System.out.println("Pending Fine Amount: " + (a.totalPendingFineAmountPaise() / 100.0));
        System.out.println();
        System.out.println("Books by Status:");
        a.booksByStatus().forEach((status, count) ->
                System.out.printf("  %-12s %d%n", status, count));
        System.out.println();
        System.out.println("Students by Status:");
        a.studentsByStatus().forEach((status, count) ->
                System.out.printf("  %-12s %d%n", status, count));
        pause();
    }

    private void configuration() {
        LibraryConfig config = admin.viewConfig(session);
        printSection("Library Configuration");
        System.out.println("1. Loan Period:        " + config.getLoanPeriodDays() + " days");
        System.out.println("2. Max Renewals:       " + config.getMaxRenewals());
        System.out.println("3. Borrow Limit:       " + config.getDefaultBorrowLimit());
        System.out.println("4. Max Reservations:   " + config.getMaxReservations());
        System.out.println("5. Fine per Day:       " + config.getFinePerDay());
        System.out.println("6. Reservation Hold:   " + config.getReservationHoldDays() + " days");
        System.out.println("7. Membership Months:  " + config.getMembershipMonths());
        System.out.println("8. Holidays:           " + config.getHolidays().size());
        System.out.println("9. Add Holiday");
        System.out.println("10. Remove Holiday");
        int choice = in.readInt("Select to update (0 to cancel): ", 0, 10);
        switch (choice) {
            case 0 -> {}
            case 1 -> admin.updateLoanPeriod(session, in.readInt("New loan period (days): ", 1, 90));
            case 2 -> admin.updateMaxRenewals(session, in.readInt("New max renewals: ", 0, 10));
            case 3 -> admin.updateBorrowLimit(session, in.readInt("New borrow limit: ", 1, 50));
            case 4 -> admin.updateMaxReservations(session, in.readInt("New max reservations: ", 1, 20));
            case 5 -> admin.updateFinePerDay(session, in.readLong("New fine per day (paise): ", 0, Long.MAX_VALUE));
            case 6 -> admin.updateReservationHoldDays(session, in.readInt("New hold days: ", 1, 14));
            case 7 -> admin.updateMembershipMonths(session, in.readInt("New membership months: ", 1, 120));
            case 9 -> admin.addHoliday(session, in.readDate("Holiday date"));
            case 10 -> admin.removeHoliday(session, in.readDate("Holiday date to remove"));
            default -> System.out.println("Invalid choice.");
        }
        if (choice > 0 && choice <= 10) {
            System.out.println("Configuration updated.");
        }
        pause();
    }

    private void manageLibrarians() {
        printSection("Librarian Management");
        System.out.println("1. Add Librarian");
        System.out.println("2. View All Librarians");
        System.out.println("3. Update Librarian Permissions");
        System.out.println("4. Remove Librarian");
        System.out.println("5. Reset Librarian Password");
        int choice = in.readInt("Choose: ", 0, 5);
        switch (choice) {
            case 0 -> {}
            case 1 -> addLibrarian();
            case 2 -> viewLibrarians();
            case 3 -> assignPermissions();
            case 4 -> removeLibrarian();
            case 5 -> resetLibrarianPassword();
        }
        pause();
    }

    private void addLibrarian() {
        printSection("Add Librarian");
        String firstName = in.readLine("First name: ");
        String lastName = in.readLine("Last name: ");
        String email = in.readLine("Email: ");
        String phone = in.readLine("Phone: ");
        String username = in.readLine("Username: ");
        String password = in.readPassword("Password: ");
        Set<String> perms = selectPermissions();
        Librarian lib = admin.addLibrarian(session, firstName, lastName, email, phone,
                username, password, perms);
        System.out.println("Librarian added. ID: " + lib.getId());
    }

    private void viewLibrarians() {
        printSection("All Librarians");
        List<Librarian> librarians = admin.findAllLibrarians(session);
        if (librarians.isEmpty()) {
            System.out.println("No librarians.");
            return;
        }
        for (Librarian lib : librarians) {
            System.out.printf("%-12s %-20s %-15s %-5s %s%n",
                    lib.getId(), StringUtils.pad(lib.fullName(), 20),
                    StringUtils.pad(lib.getUsername(), 15),
                    lib.isActive() ? "Yes" : "No",
                    lib.getPermissions().size() + " permissions");
        }
    }

    private void assignPermissions() {
        printSection("Assign Permissions");
        String libId = in.readLine("Librarian ID: ");
        Set<String> perms = selectPermissions();
        admin.assignPermissions(session, libId, perms);
        System.out.println("Permissions updated.");
    }

    private void removeLibrarian() {
        String libId = in.readLine("Librarian ID to remove: ");
        if (admin.removeLibrarian(session, libId)) {
            System.out.println("Librarian removed.");
        } else {
            System.out.println("Librarian not found.");
        }
    }

    private void resetLibrarianPassword() {
        String libId = in.readLine("Librarian ID: ");
        String pwd = in.readPassword("New temporary password: ");
        admin.resetLibrarianPassword(session, libId, pwd);
        System.out.println("Password reset.");
    }

    private Set<String> selectPermissions() {
        Set<String> selected = new HashSet<>();
        List<String> all = List.copyOf(Permissions.all());
        System.out.println("Available permissions:");
        for (int i = 0; i < all.size(); i++) {
            System.out.printf("%2d. %s%n", i + 1, all.get(i));
        }
        System.out.println("Enter permission numbers (comma-separated), or 'all': ");
        String input = in.readLine("Permissions: ");
        if (input.equalsIgnoreCase("all")) {
            selected.addAll(all);
        } else {
            for (String part : input.split(",")) {
                try {
                    int idx = Integer.parseInt(part.trim()) - 1;
                    if (idx >= 0 && idx < all.size()) {
                        selected.add(all.get(idx));
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return selected;
    }

    private void bookInventory() {
        printSection("Book Inventory");
        System.out.println("1. Add Book");
        System.out.println("2. View All Books");
        System.out.println("3. Search Books");
        System.out.println("4. Archive Book");
        System.out.println("5. Restore Book");
        System.out.println("6. Mark Lost");
        System.out.println("7. Mark Damaged");
        System.out.println("8. Mark Under Repair");
        System.out.println("9. Mark Available");
        System.out.println("10. Delete Book");
        System.out.println("11. Find Duplicates");
        int choice = in.readInt("Choose: ", 0, 11);
        switch (choice) {
            case 0 -> {}
            case 1 -> addBook();
            case 2 -> printBookTable(books.findAll(session));
            case 3 -> searchBooks();
            case 4 -> books.archiveBook(session, in.readLine("Book ID: "));
            case 5 -> books.restoreBook(session, in.readLine("Book ID: "));
            case 6 -> books.markLost(session, in.readLine("Book ID: "), in.readLine("Reason: "));
            case 7 -> books.markDamaged(session, in.readLine("Book ID: "), in.readLine("Reason: "));
            case 8 -> books.markUnderRepair(session, in.readLine("Book ID: "));
            case 9 -> books.markAvailable(session, in.readLine("Book ID: "));
            case 10 -> books.deleteBook(session, in.readLine("Book ID: "));
            case 11 -> printBookTable(books.findDuplicates(session));
        }
        pause();
    }

    private void addBook() {
        printSection("Add Book");
        String isbn = in.readLine("ISBN: ");
        String title = in.readLine("Title: ");
        String author = in.readLine("Author: ");
        int qty = in.readInt("Total quantity: ", 1, 1000);
        Book book = books.addBook(session, isbn, title, author, qty);
        System.out.println("Book added. ID: " + book.getId() + ", Barcode: " + book.getBarcode());
    }

    private void searchBooks() {
        List<String> fields = List.of("Title", "Author", "ISBN", "Barcode", "Publisher",
                "Category", "Subject", "Keyword", "Rack", "Shelf", "Language");
        String field = in.readChoice("Select search field: ", fields);
        String query = in.readLine("Enter search text: ");
        printBookTable(books.search(session, field.toLowerCase(), query));
    }

    private void printBookTable(List<Book> results) {
        if (results.isEmpty()) {
            System.out.println("No books found.");
            return;
        }
        System.out.printf("%-12s %-35s %-20s %-14s %-6s %-6s %-12s%n",
                "ID", "Title", "Author", "ISBN", "Total", "Avail", "Status");
        System.out.println("-".repeat(110));
        for (Book b : results) {
            System.out.printf("%-12s %-35s %-20s %-14s %-6d %-6d %-12s%n",
                    b.getId(), StringUtils.pad(b.getTitle(), 35), StringUtils.pad(b.getAuthor(), 20),
                    b.getIsbn(), b.getTotalQuantity(), b.getAvailableQuantity(), b.getStatus());
        }
        System.out.println("\n" + results.size() + " book(s).");
    }

    private void studentManagement() {
        printSection("Student Management");
        System.out.println("1. Add Student");
        System.out.println("2. View All Students");
        System.out.println("3. Search Students");
        System.out.println("4. Suspend Student");
        System.out.println("5. Activate Student");
        System.out.println("6. Reset Student Password");
        System.out.println("7. Regenerate Library Card");
        System.out.println("8. Delete Student");
        int choice = in.readInt("Choose: ", 0, 8);
        switch (choice) {
            case 0 -> {}
            case 1 -> addStudent();
            case 2 -> printStudentTable(students.findAll(session));
            case 3 -> printStudentTable(students.search(session, in.readLine("Search: ")));
            case 4 -> students.suspend(session, in.readLine("Student ID: "));
            case 5 -> students.activate(session, in.readLine("Student ID: "));
            case 6 -> students.resetPassword(session, in.readLine("Registration number: "),
                    in.readPassword("New password: "));
            case 7 -> students.regenerateCard(session, in.readLine("Student ID: "));
            case 8 -> students.delete(session, in.readLine("Student ID: "));
        }
        pause();
    }

    private void addStudent() {
        printSection("Add Student");
        String firstName = in.readLine("First name: ");
        String lastName = in.readLine("Last name: ");
        String email = in.readLine("Email: ");
        String phone = in.readLine("Phone: ");
        String dept = in.readLine("Department: ");
        String course = in.readLine("Course: ");
        int semester = in.readInt("Semester: ", 1, 12);
        String section = in.readLine("Section: ");
        Student s = students.register(session, firstName, lastName, email, phone,
                dept, course, semester, section);
        System.out.println("Student added. ID: " + s.getId() + ", Reg: " + s.getRegistrationNumber()
                + ", Card: " + s.getLibraryCardNumber());
    }

    private void printStudentTable(List<Student> results) {
        if (results.isEmpty()) {
            System.out.println("No students found.");
            return;
        }
        System.out.printf("%-16s %-25s %-15s %-5s %-12s %-8s %-8s%n",
                "Reg No", "Name", "Department", "Sem", "Status", "Borrowed", "Fine");
        System.out.println("-".repeat(95));
        for (Student s : results) {
            System.out.printf("%-16s %-25s %-15s %-5d %-12s %-8d %-8.2f%n",
                    s.getRegistrationNumber(), StringUtils.pad(s.fullName(), 25),
                    StringUtils.pad(s.getDepartment() == null ? "" : s.getDepartment(), 15),
                    s.getSemester(), s.getMembershipStatus(), s.getCurrentBorrowCount(), s.getFineBalance());
        }
    }

    private void circulationOps() {
        printSection("Circulation");
        System.out.println("1. Issue Book");
        System.out.println("2. Return Book");
        System.out.println("3. Renew Book");
        System.out.println("4. View Active Borrows");
        System.out.println("5. View Overdue Books");
        System.out.println("6. View All Reservations");
        System.out.println("7. Collect Fine");
        System.out.println("8. Waive Fine");
        System.out.println("9. View Pending Fines");
        int choice = in.readInt("Choose: ", 0, 9);
        switch (choice) {
            case 0 -> {}
            case 1 -> {
                BorrowRecord r = circulation.issueBook(session, in.readLine("Book ID: "),
                        in.readLine("Student Reg: "));
                System.out.println("Issued. Borrow ID: " + r.getId());
            }
            case 2 -> {
                BorrowRecord r = circulation.returnBook(session, in.readLine("Borrow ID: "));
                System.out.println("Returned. Status: " + r.getStatus());
            }
            case 3 -> {
                BorrowRecord r = circulation.renewBook(session, in.readLine("Borrow ID: "));
                System.out.println("Renewed. New due: " + r.getDueDate());
            }
            case 4 -> circulation.viewAllActive(session).forEach(r ->
                    System.out.println(r.getId() + " " + r.getBookId() + " " + r.getRegistrationNumber()
                            + " due " + r.getDueDate()));
            case 5 -> circulation.viewAllOverdue(session).forEach(r ->
                    System.out.println(r.getId() + " " + r.getBookId() + " " + r.getRegistrationNumber()
                            + " overdue " + r.overdueDays() + " days"));
            case 6 -> circulation.viewAllReservations(session).forEach(r ->
                    System.out.println(r.getId() + " " + r.getBookId() + " " + r.getRegistrationNumber()
                            + " queue#" + r.getQueuePosition() + " " + r.getStatus()));
            case 7 -> circulation.collectFine(session, in.readLine("Fine ID: "));
            case 8 -> circulation.waiveFine(session, in.readLine("Fine ID: "), in.readLine("Reason: "));
            case 9 -> circulation.viewAllPendingFines(session).forEach(f ->
                    System.out.println(f.getId() + " " + f.getRegistrationNumber() + " " + f.getAmount()));
        }
        pause();
    }

    private void analytics() {
        printSection("Analytics");
        var a = admin.analytics(session);
        System.out.println("Popular Books:");
        a.mostBorrowedBooks(5).forEach(b ->
                System.out.println("  " + b.getTitle() + " - " + b.getAuthor()));
        System.out.println();
        System.out.println("Books by Category:");
        a.booksByCategory().forEach((cat, count) ->
                System.out.printf("  %-20s %d%n", cat, count));
        System.out.println();
        System.out.println("Monthly Borrows (" + LocalDate.now().getYear() + "):");
        a.monthlyBorrowCounts(LocalDate.now().getYear()).forEach((month, count) ->
                System.out.printf("  %-10s %d%n", month, count));
        pause();
    }

    private void reports() {
        printSection("Reports");
        List<String> reportIds = admin.availableReports(session);
        for (int i = 0; i < reportIds.size(); i++) {
            System.out.printf("%2d. %s%n", i + 1, reportIds.get(i));
        }
        System.out.println((reportIds.size() + 1) + ". Export report to CSV");
        int choice = in.readInt("Choose: ", 0, reportIds.size() + 1);
        if (choice == 0) {
            return;
        }
        if (choice == reportIds.size() + 1) {
            String id = in.readLine("Report ID to export: ");
            String path = admin.exportReport(session, id);
            System.out.println("Exported to: " + path);
        } else {
            String reportId = reportIds.get(choice - 1);
            System.out.println(admin.renderReport(session, reportId));
        }
        pause();
    }

    private void auditLogs() {
        printSection("Audit Logs");
        List<AuditLog> logs = admin.viewAuditLogs(session);
        if (logs.isEmpty()) {
            System.out.println("No audit logs.");
            pause();
            return;
        }
        int limit = Math.min(50, logs.size());
        System.out.println("Showing latest " + limit + " of " + logs.size() + " entries:");
        System.out.println("-".repeat(80));
        for (int i = logs.size() - limit; i < logs.size(); i++) {
            AuditLog log = logs.get(i);
            System.out.printf("[%s] %s by %s on %s:%s%n",
                    DateUtils.formatDateTime(log.timestamp()),
                    log.action(), log.actorId(), log.targetType(), log.targetId());
        }
        pause();
    }

    private void backupRestore() {
        printSection("Backup & Restore");
        System.out.println("1. Create Backup");
        System.out.println("2. List Backups");
        System.out.println("3. Restore Backup");
        int choice = in.readInt("Choose: ", 0, 3);
        switch (choice) {
            case 0 -> {}
            case 1 -> System.out.println("Backup created: " + admin.createBackup(session));
            case 2 -> {
                List<Path> backups = admin.listBackups(session);
                if (backups.isEmpty()) {
                    System.out.println("No backups found.");
                } else {
                    backups.forEach(b -> System.out.println("  " + b));
                }
            }
            case 3 -> {
                String dir = in.readLine("Backup directory path: ");
                admin.restoreBackup(session, dir);
                System.out.println("Backup restored. Restart application to reload data.");
            }
        }
        pause();
    }

    private void changePassword() {
        printSection("Change Password");
        String oldPwd = in.readPassword("Current password: ");
        String newPwd = in.readPassword("New password: ");
        facade.auth().changePassword(session.token(), oldPwd, newPwd);
        System.out.println("Password changed successfully.");
        pause();
    }
}
