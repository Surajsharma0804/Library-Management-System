package com.library.menu;

import com.library.controller.BookController;
import com.library.controller.LibrarianController;
import com.library.controller.StudentController;
import com.library.enums.BookStatus;
import com.library.facade.LibraryFacade;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.Fine;
import com.library.model.LibraryConfig;
import com.library.model.Reservation;
import com.library.model.Student;
import com.library.service.NotificationService;
import com.library.security.Permissions;
import com.library.security.Session;
import com.library.util.ConsoleInput;
import com.library.util.DateUtils;
import com.library.util.StringUtils;

import java.util.List;

/**
 * Student-facing menu: dashboard, profile, search, borrow history,
 * reservations, fines, notifications, and password changes.
 */
final class StudentMenu extends AbstractMenu {

    private final BookController books;
    private final LibrarianController circulation;
    private final StudentController students;
    private final LibraryFacade facade;
    private final Session session;
    private final Student self;

    StudentMenu(ConsoleInput in, BookController books, LibrarianController circulation,
                StudentController students, LibraryFacade facade, Session session) {
        super(in);
        this.books = books;
        this.circulation = circulation;
        this.students = students;
        this.facade = facade;
        this.session = session;
        this.self = facade.userRepo().findStudentByUsername(session.username());
    }

    @Override
    protected String title() {
        return "Student Dashboard - " + self.fullName();
    }

    @Override
    protected List<String> options() {
        return List.of(
                "View Dashboard",
                "View Profile",
                "View Library Card",
                "Search Books",
                "View Current Borrowed Books",
                "View Borrow History",
                "View Reservations",
                "View Fines",
                "View Notifications",
                "Change Password");
    }

    @Override
    protected boolean handle(int choice) {
        switch (choice) {
            case 1 -> viewDashboard();
            case 2 -> viewProfile();
            case 3 -> viewLibraryCard();
            case 4 -> searchBooks();
            case 5 -> viewCurrentBorrows();
            case 6 -> viewBorrowHistory();
            case 7 -> viewReservations();
            case 8 -> viewFines();
            case 9 -> viewNotifications();
            case 10 -> changePassword();
            default -> System.out.println("Invalid choice.");
        }
        return true;
    }

    private void viewDashboard() {
        LibraryConfig config = facade.config().get();
        List<BorrowRecord> active = circulation.viewOwnActive(session, self.getRegistrationNumber());
        List<Reservation> reservations = circulation.viewOwnReservations(session, self.getRegistrationNumber());
        List<Fine> pendingFines = circulation.viewOwnPendingFines(session, self.getRegistrationNumber());
        long unread = facade.notifications().unreadCount(self.getRegistrationNumber());

        printSection("Dashboard Summary");
        System.out.println("Name:              " + self.fullName());
        System.out.println("Registration No:   " + self.getRegistrationNumber());
        System.out.println("Library Card:      " + self.getLibraryCardNumber());
        System.out.println("Department:        " + self.getDepartment());
        System.out.println("Membership Status: " + self.getMembershipStatus());
        System.out.println("Membership Expiry: " + self.getMembershipExpiry());
        System.out.println("Borrow Limit:      " + self.getBorrowLimit());
        System.out.println("Current Borrows:   " + self.getCurrentBorrowCount());
        System.out.println("Remaining Slots:   " + self.remainingBorrowSlots());
        System.out.println("Fine Balance:      " + self.getFineBalance());
        System.out.println("Active Reservations: " + reservations.size());
        System.out.println("Unread Notifications: " + unread);

        if (!active.isEmpty()) {
            printSection("Current Borrowed Books");
            System.out.printf("%-14s %-30s %-12s %-12s %-10s %-8s %-8s%n",
                    "Borrow ID", "Book Title", "Issue Date", "Due Date", "Remaining", "Late", "Fine");
            System.out.println("-".repeat(100));
            for (BorrowRecord r : active) {
                Book book = books.findById(session, r.getBookId());
                long remaining = r.remainingDays();
                long late = r.overdueDays();
                String title = book == null ? r.getBookId() : StringUtils.pad(book.getTitle(), 30);
                System.out.printf("%-14s %-30s %-12s %-12s %-10d %-8d %-8.2f%n",
                        r.getId(), title, r.getIssueDate(), r.getDueDate(),
                        remaining, late, r.getFine());
            }
        }
        pause();
    }

    private void viewProfile() {
        printSection("My Profile");
        Student s = students.viewOwnProfile(session);
        System.out.println("Student ID:        " + s.getId());
        System.out.println("Registration No:   " + s.getRegistrationNumber());
        System.out.println("Library Card:      " + s.getLibraryCardNumber());
        System.out.println("Name:              " + s.fullName());
        System.out.println("Email:             " + s.getEmail());
        System.out.println("Phone:             " + s.getPhone());
        System.out.println("Department:        " + s.getDepartment());
        System.out.println("Course:            " + s.getCourse());
        System.out.println("Semester:          " + s.getSemester());
        System.out.println("Section:           " + s.getSection());
        System.out.println("Address:           " + s.getAddress());
        System.out.println("Joining Date:      " + s.getJoiningDate());
        System.out.println("Membership Expiry: " + s.getMembershipExpiry());
        System.out.println("Status:            " + s.getMembershipStatus());
        pause();
    }

    private void viewLibraryCard() {
        printSection("Library Card");
        System.out.println("Card Number:       " + self.getLibraryCardNumber());
        System.out.println("Name:              " + self.fullName());
        System.out.println("Department:        " + self.getDepartment());
        System.out.println("Valid Until:       " + self.getMembershipExpiry());
        System.out.println("Status:            " + self.getMembershipStatus());
        pause();
    }

    private void searchBooks() {
        printSection("Search Books");
        List<String> fields = List.of("Title", "Author", "ISBN", "Barcode", "Publisher",
                "Category", "Subject", "Keyword", "Rack", "Shelf", "Language");
        String field = in.readChoice("Select search field: ", fields);
        String query = in.readLine("Enter search text: ");
        List<Book> results = books.search(session, field.toLowerCase(), query);
        if (results.isEmpty()) {
            System.out.println("No books found.");
        } else {
            System.out.printf("%-12s %-35s %-20s %-10s %-8s %-12s%n",
                    "ID", "Title", "Author", "ISBN", "Avail.", "Status");
            System.out.println("-".repeat(100));
            for (Book b : results) {
                System.out.printf("%-12s %-35s %-20s %-10s %-8d %-12s%n",
                        b.getId(), StringUtils.pad(b.getTitle(), 35), StringUtils.pad(b.getAuthor(), 20),
                        b.getIsbn(), b.getAvailableQuantity(), b.getStatus());
            }
            System.out.println("\n" + results.size() + " book(s) found.");
        }
        pause();
    }

    private void viewCurrentBorrows() {
        printSection("Current Borrowed Books");
        List<BorrowRecord> active = circulation.viewOwnActive(session, self.getRegistrationNumber());
        if (active.isEmpty()) {
            System.out.println("You have no active borrows.");
            pause();
            return;
        }
        printBorrowTable(active);
        pause();
    }

    private void viewBorrowHistory() {
        printSection("Borrow History");
        List<BorrowRecord> history = circulation.viewOwnHistory(session, self.getRegistrationNumber());
        if (history.isEmpty()) {
            System.out.println("No borrow history.");
            pause();
            return;
        }
        printBorrowTable(history);
        pause();
    }

    private void printBorrowTable(List<BorrowRecord> records) {
        System.out.printf("%-14s %-30s %-12s %-12s %-12s %-10s %-6s %-8s%n",
                "Borrow ID", "Book Title", "Issue Date", "Due Date", "Return Date", "Remaining", "Renew", "Fine");
        System.out.println("-".repeat(110));
        for (BorrowRecord r : records) {
            Book book = books.findById(session, r.getBookId());
            String title = book == null ? r.getBookId() : StringUtils.pad(book.getTitle(), 30);
            String returnDate = r.getReturnDate() == null ? "-" : String.valueOf(r.getReturnDate());
            System.out.printf("%-14s %-30s %-12s %-12s %-12s %-10d %-6d %-8.2f%n",
                    r.getId(), title, r.getIssueDate(), r.getDueDate(),
                    returnDate, r.remainingDays(), r.getRenewCount(), r.getFine());
        }
    }

    private void viewReservations() {
        printSection("My Reservations");
        List<Reservation> reservations = circulation.viewOwnReservations(session, self.getRegistrationNumber());
        if (reservations.isEmpty()) {
            System.out.println("You have no active reservations.");
            pause();
            return;
        }
        System.out.printf("%-14s %-30s %-12s %-12s %-6s %-12s%n",
                "Res ID", "Book Title", "Res Date", "Expiry", "Queue", "Status");
        System.out.println("-".repeat(90));
        for (Reservation r : reservations) {
            Book book = books.findById(session, r.getBookId());
            String title = book == null ? r.getBookId() : StringUtils.pad(book.getTitle(), 30);
            String expiry = r.getExpiryDate() == null ? "-" : String.valueOf(r.getExpiryDate());
            System.out.printf("%-14s %-30s %-12s %-12s %-6d %-12s%n",
                    r.getId(), title, r.getReservationDate(), expiry,
                    r.getQueuePosition(), r.getStatus());
        }
        pause();
    }

    private void viewFines() {
        printSection("My Fines");
        List<Fine> fines = circulation.viewOwnFines(session, self.getRegistrationNumber());
        if (fines.isEmpty()) {
            System.out.println("You have no fines.");
            pause();
            return;
        }
        System.out.printf("%-14s %-10s %-10s %-10s %-30s%n",
                "Fine ID", "Amount", "Status", "Created", "Reason");
        System.out.println("-".repeat(80));
        for (Fine f : fines) {
            System.out.printf("%-14s %-10.2f %-10s %-10s %-30s%n",
                    f.getId(), f.getAmount(), f.getStatus(),
                    DateUtils.formatDateTime(f.getCreatedAt()),
                    StringUtils.pad(f.getReason() == null ? "" : f.getReason(), 30));
        }
        System.out.println("\nTotal outstanding: " + self.getFineBalance());
        pause();
    }

    private void viewNotifications() {
        printSection("Notifications");
        NotificationService ns = facade.notifications();
        var notifications = ns.inboxFor(self.getRegistrationNumber());
        if (notifications.isEmpty()) {
            System.out.println("No notifications.");
            pause();
            return;
        }
        for (var n : notifications) {
            String marker = n.read() ? "  " : "NEW";
            System.out.printf("[%s] %s - %s%n  %s%n  %s%n%n",
                    marker, n.category(), n.subject(), n.message(),
                    DateUtils.formatDateTime(n.timestamp()));
        }
        ns.markAllRead(self.getRegistrationNumber());
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
