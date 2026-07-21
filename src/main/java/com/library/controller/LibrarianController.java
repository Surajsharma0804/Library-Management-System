package com.library.controller;

import com.library.facade.LibraryFacade;
import com.library.model.BorrowRecord;
import com.library.model.Reservation;
import com.library.model.Fine;
import com.library.security.Permissions;
import com.library.security.Session;

import java.util.List;

public final class LibrarianController extends BaseController {

    private final com.library.service.BorrowService borrows;
    private final com.library.service.ReservationService reservations;
    private final com.library.service.FineService fines;

    public LibrarianController(LibraryFacade facade) {
        super(facade);
        this.borrows = facade.borrows();
        this.reservations = facade.reservations();
        this.fines = facade.fines();
    }

    public BorrowRecord issueBook(Session session, String bookId, String registrationNumber) {
        require(session, Permissions.BORROW_ISSUE);
        return borrows.issueBook(session, bookId, registrationNumber);
    }

    public BorrowRecord returnBook(Session session, String borrowId) {
        require(session, Permissions.BORROW_RETURN);
        return borrows.returnBook(session, borrowId);
    }

    public BorrowRecord renewBook(Session session, String borrowId) {
        require(session, Permissions.BORROW_RENEW);
        return borrows.renewBook(session, borrowId);
    }

    public List<BorrowRecord> viewAllActive(Session session) {
        require(session, Permissions.BORROW_VIEW_ALL);
        return borrows.findAllActive();
    }

    public List<BorrowRecord> viewAllOverdue(Session session) {
        require(session, Permissions.BORROW_VIEW_ALL);
        return borrows.findAllOverdue();
    }

    public List<Reservation> viewAllReservations(Session session) {
        require(session, Permissions.RESERVATION_VIEW);
        return reservations.findAll();
    }

    public Fine collectFine(Session session, String fineId) {
        require(session, Permissions.FINE_COLLECT);
        return fines.collectFine(session, fineId);
    }

    public Fine waiveFine(Session session, String fineId, String reason) {
        require(session, Permissions.FINE_WAIVE);
        return fines.waiveFine(session, fineId, reason);
    }


    public List<BorrowRecord> viewOwnActive(Session session, String registrationNumber) {
        require(session, Permissions.BORROW_VIEW_OWN);
        return borrows.findActiveByStudent(registrationNumber);
    }

    public List<BorrowRecord> viewOwnHistory(Session session, String registrationNumber) {
        require(session, Permissions.BORROW_VIEW_OWN);
        return borrows.findHistoryByStudent(registrationNumber);
    }

    public List<Reservation> viewOwnReservations(Session session, String registrationNumber) {
        require(session, Permissions.RESERVATION_VIEW_OWN);
        return reservations.findByStudent(registrationNumber);
    }

    public List<Fine> viewOwnFines(Session session, String registrationNumber) {
        require(session, Permissions.FINE_VIEW);
        return fines.findByStudent(registrationNumber);
    }

    public List<Fine> viewOwnPendingFines(Session session, String registrationNumber) {
        require(session, Permissions.FINE_VIEW);
        return fines.findPendingByStudent(registrationNumber);
    }

    public List<Fine> viewAllPendingFines(Session session) {
        require(session, Permissions.FINE_VIEW);
        return fines.findAllPending();
    }
}
