package com.library.api;

import com.library.enums.BorrowStatus;
import com.library.facade.LibraryFacade;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.Student;
import com.library.security.Session;
import io.javalin.http.Context;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST endpoints for borrow operations — issue, return, and list.
 * Students see their own borrows; staff see all active borrows.
 */
public final class RestBorrowController extends BaseRestController {

    public RestBorrowController(LibraryFacade facade) {
        super(facade);
    }

    /** GET /api/borrows — Lists borrows. Students see own; staff see all active. */
    public void list(Context ctx) {
        Session session = requireSession(ctx);
        List<BorrowRecord> records;

        if (session.role() == com.library.enums.UserRole.STUDENT) {
            Student student = facade.userRepo().findStudentByUsername(session.username());
            if (student == null) {
                ctx.json(List.of());
                return;
            }
            records = facade.borrowRepo().findByRegistrationNumber(student.getRegistrationNumber());
        } else {
            records = facade.borrowRepo().findAllActive();
        }

        ctx.json(records.stream().map(this::toMap).toList());
    }

    /** POST /api/borrows/issue — Issues a book to a student. */
    public void issue(Context ctx) {
        Session session = requireSession(ctx);
        var body = ctx.bodyAsClass(IssueRequest.class);
        BorrowRecord record = facade.borrows().issueBook(session, body.bookId,
                body.registrationNumber);
        ctx.status(201).json(toMap(record));
    }

    /** POST /api/borrows/return — Returns a borrowed book. */
    public void returnBook(Context ctx) {
        Session session = requireSession(ctx);
        var body = ctx.bodyAsClass(ReturnRequest.class);
        facade.borrows().returnBook(session, body.borrowId);
        ctx.json(Map.of("message", "Book returned successfully"));
    }

    // ── Serialization ───────────────────────────────────────────────
    private Map<String, Object> toMap(BorrowRecord r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("bookId", r.getBookId());
        m.put("registrationNumber", r.getRegistrationNumber());
        m.put("issueDate", r.getIssueDate() != null ? r.getIssueDate().toString() : null);
        m.put("dueDate", r.getDueDate() != null ? r.getDueDate().toString() : null);
        m.put("returnDate", r.getReturnDate() != null ? r.getReturnDate().toString() : null);
        m.put("status", r.getStatus() != null ? r.getStatus().name() : null);
        m.put("issuedBy", r.getIssuedBy());

        // Enrich with book title for display
        try {
            Book book = facade.bookRepo().findById(r.getBookId()).orElse(null);
            m.put("bookTitle", book != null ? book.getTitle() : "Unknown");
            m.put("bookAuthor", book != null ? book.getAuthor() : "");
        } catch (Exception e) {
            m.put("bookTitle", "Unknown");
            m.put("bookAuthor", "");
        }
        return m;
    }

    public static class IssueRequest {
        public String bookId;
        public String registrationNumber;
        public int loanDays;
    }

    public static class ReturnRequest {
        public String borrowId;
    }
}
