package com.library.menu;

import com.library.controller.BookController;
import com.library.controller.LibrarianController;
import com.library.controller.StudentController;
import com.library.facade.LibraryFacade;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.Fine;
import com.library.model.Student;
import com.library.security.Session;
import com.library.util.ConsoleInput;
import com.library.util.StringUtils;

import java.util.List;

/**
 * Librarian-facing menu for daily library operations: circulation,
 * book search, member lookup, and fine management.
 */
final class LibrarianMenu extends AbstractMenu {

    private final BookController books;
    private final StudentController students;
    private final LibrarianController circulation;
    private final LibraryFacade facade;
    private final Session session;

    LibrarianMenu(ConsoleInput in, BookController books, StudentController students,
                  LibrarianController circulation, LibraryFacade facade, Session session) {
        super(in);
        this.books = books;
        this.students = students;
        this.circulation = circulation;
        this.facade = facade;
        this.session = session;
    }

    @Override
    protected String title() {
        return "Librarian Console - " + session.username();
    }

    @Override
    protected List<String> options() {
        return List.of(
                "Issue Book",
                "Return Book",
                "Renew Book",
                "Search Books",
                "View All Books",
                "Search Members",
                "View Member History",
                "View All Active Borrows",
                "View Overdue Books",
                "Manage Reservations",
                "Collect Fine",
                "Waive Fine",
                "View All Pending Fines",
                "Change Password");
    }

    @Override
    protected boolean handle(int choice) {
        switch (choice) {
            case 1 -> issueBook();
            case 2 -> returnBook();
            case 3 -> renewBook();
            case 4 -> searchBooks();
            case 5 -> viewAllBooks();
            case 6 -> searchMembers();
            case 7 -> viewMemberHistory();
            case 8 -> viewActiveBorrows();
            case 9 -> viewOverdue();
            case 10 -> manageReservations();
            case 11 -> collectFine();
            case 12 -> waiveFine();
            case 13 -> viewPendingFines();
            case 14 -> changePassword();
            default -> System.out.println("Invalid choice.");
        }
        return true;
    }

    private void issueBook() {
        printSection("Issue Book");
        String bookId = in.readLine("Book ID: ");
        String reg = in.readLine("Student Registration Number: ");
        BorrowRecord record = circulation.issueBook(session, bookId, reg);
        System.out.println("Book issued. Borrow ID: " + record.getId() + ", Due: " + record.getDueDate());
        pause();
    }

    private void returnBook() {
        printSection("Return Book");
        String borrowId = in.readLine("Borrow ID: ");
        BorrowRecord record = circulation.returnBook(session, borrowId);
        System.out.println("Book returned. Status: " + record.getStatus()
                + (record.getFine() > 0 ? ", Fine: " + record.getFine() : ""));
        pause();
    }

    private void renewBook() {
        printSection("Renew Book");
        String borrowId = in.readLine("Borrow ID: ");
        BorrowRecord record = circulation.renewBook(session, borrowId);
        System.out.println("Book renewed. New due date: " + record.getDueDate());
        pause();
    }

    private void searchBooks() {
        printSection("Search Books");
        List<String> fields = List.of("Title", "Author", "ISBN", "Barcode", "Publisher",
                "Category", "Subject", "Keyword", "Rack", "Shelf", "Language");
        String field = in.readChoice("Select search field: ", fields);
        String query = in.readLine("Enter search text: ");
        List<Book> results = books.search(session, field.toLowerCase(), query);
        printBookTable(results);
        pause();
    }

    private void viewAllBooks() {
        printSection("All Books");
        printBookTable(books.findAll(session));
        pause();
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

    private void searchMembers() {
        printSection("Search Members");
        String query = in.readLine("Enter name or registration number: ");
        List<Student> results = students.search(session, query);
        if (results.isEmpty()) {
            System.out.println("No members found.");
            pause();
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
        pause();
    }

    private void viewMemberHistory() {
        printSection("Member Borrow History");
        String reg = in.readLine("Student Registration Number: ");
        List<BorrowRecord> history = circulation.viewOwnHistory(session, reg);
        if (history.isEmpty()) {
            System.out.println("No borrow history for " + reg);
            pause();
            return;
        }
        System.out.printf("%-14s %-30s %-12s %-12s %-12s %-12s%n",
                "Borrow ID", "Book ID", "Issue Date", "Due Date", "Return Date", "Status");
        System.out.println("-".repeat(95));
        for (BorrowRecord r : history) {
            String ret = r.getReturnDate() == null ? "-" : String.valueOf(r.getReturnDate());
            System.out.printf("%-14s %-30s %-12s %-12s %-12s %-12s%n",
                    r.getId(), StringUtils.pad(r.getBookId(), 30),
                    r.getIssueDate(), r.getDueDate(), ret, r.getStatus());
        }
        pause();
    }

    private void viewActiveBorrows() {
        printSection("All Active Borrows");
        List<BorrowRecord> active = circulation.viewAllActive(session);
        if (active.isEmpty()) {
            System.out.println("No active borrows.");
            pause();
            return;
        }
        System.out.printf("%-14s %-30s %-16s %-12s %-12s %-10s%n",
                "Borrow ID", "Book ID", "Student Reg", "Issue Date", "Due Date", "Remaining");
        System.out.println("-".repeat(95));
        for (BorrowRecord r : active) {
            System.out.printf("%-14s %-30s %-16s %-12s %-12s %-10d%n",
                    r.getId(), StringUtils.pad(r.getBookId(), 30), r.getRegistrationNumber(),
                    r.getIssueDate(), r.getDueDate(), r.remainingDays());
        }
        pause();
    }

    private void viewOverdue() {
        printSection("Overdue Books");
        List<BorrowRecord> overdue = circulation.viewAllOverdue(session);
        if (overdue.isEmpty()) {
            System.out.println("No overdue books.");
            pause();
            return;
        }
        System.out.printf("%-14s %-30s %-16s %-12s %-10s %-8s%n",
                "Borrow ID", "Book ID", "Student Reg", "Due Date", "Late Days", "Fine");
        System.out.println("-".repeat(95));
        for (BorrowRecord r : overdue) {
            System.out.printf("%-14s %-30s %-16s %-12s %-10d %-8.2f%n",
                    r.getId(), StringUtils.pad(r.getBookId(), 30), r.getRegistrationNumber(),
                    r.getDueDate(), r.overdueDays(), r.overdueDays() * facade.config().get().getFinePerDay());
        }
        pause();
    }

    private void manageReservations() {
        printSection("All Reservations");
        var reservations = circulation.viewAllReservations(session);
        if (reservations.isEmpty()) {
            System.out.println("No reservations.");
            pause();
            return;
        }
        System.out.printf("%-14s %-30s %-16s %-12s %-6s %-12s%n",
                "Res ID", "Book ID", "Student Reg", "Date", "Queue", "Status");
        System.out.println("-".repeat(95));
        for (var r : reservations) {
            System.out.printf("%-14s %-30s %-16s %-12s %-6d %-12s%n",
                    r.getId(), StringUtils.pad(r.getBookId(), 30), r.getRegistrationNumber(),
                    r.getReservationDate(), r.getQueuePosition(), r.getStatus());
        }
        pause();
    }

    private void collectFine() {
        printSection("Collect Fine");
        String fineId = in.readLine("Fine ID: ");
        Fine fine = circulation.collectFine(session, fineId);
        System.out.println("Fine collected: " + fine.getAmount() + " from " + fine.getRegistrationNumber());
        pause();
    }

    private void waiveFine() {
        printSection("Waive Fine");
        String fineId = in.readLine("Fine ID: ");
        String reason = in.readLine("Reason: ");
        Fine fine = circulation.waiveFine(session, fineId, reason);
        System.out.println("Fine waived: " + fine.getAmount());
        pause();
    }

    private void viewPendingFines() {
        printSection("Pending Fines");
        List<Fine> fines = circulation.viewAllPendingFines(session);
        if (fines.isEmpty()) {
            System.out.println("No pending fines.");
            pause();
            return;
        }
        System.out.printf("%-14s %-16s %-10s %-30s%n",
                "Fine ID", "Student Reg", "Amount", "Reason");
        System.out.println("-".repeat(75));
        for (Fine f : fines) {
            System.out.printf("%-14s %-16s %-10.2f %-30s%n",
                    f.getId(), f.getRegistrationNumber(), f.getAmount(),
                    StringUtils.pad(f.getReason() == null ? "" : f.getReason(), 30));
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
