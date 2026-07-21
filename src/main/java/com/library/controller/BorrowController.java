package com.library.controller;

import com.library.facade.LibraryFacade;
import com.library.model.BorrowRecord;
import com.library.security.Permissions;
import com.library.security.Session;

import java.util.List;

/**
 * Controller dedicated to borrow operations (issue, return, renew, view).
 */
public final class BorrowController extends BaseController {

    public BorrowController(LibraryFacade facade) {
        super(facade);
    }

    public BorrowRecord issueBook(Session session, String bookId, String registrationNumber) {
        require(session, Permissions.BORROW_ISSUE);
        return facade.borrows().issueBook(session, bookId, registrationNumber);
    }

    public BorrowRecord returnBook(Session session, String borrowId) {
        require(session, Permissions.BORROW_RETURN);
        return facade.borrows().returnBook(session, borrowId);
    }

    public BorrowRecord renewBook(Session session, String borrowId) {
        require(session, Permissions.BORROW_RENEW);
        return facade.borrows().renewBook(session, borrowId);
    }

    public List<BorrowRecord> viewAllActive(Session session) {
        require(session, Permissions.BORROW_VIEW_ALL);
        return facade.borrows().findAllActive();
    }

    public List<BorrowRecord> viewAllOverdue(Session session) {
        require(session, Permissions.BORROW_VIEW_ALL);
        return facade.borrows().findAllOverdue();
    }

    public List<BorrowRecord> viewOwnActive(Session session, String registrationNumber) {
        require(session, Permissions.BORROW_VIEW_OWN);
        return facade.borrows().findActiveByStudent(registrationNumber);
    }

    public List<BorrowRecord> viewOwnHistory(Session session, String registrationNumber) {
        require(session, Permissions.BORROW_VIEW_OWN);
        return facade.borrows().findHistoryByStudent(registrationNumber);
    }

    public BorrowRecord findBorrow(Session session, String borrowId) {
        require(session, Permissions.BORROW_VIEW_ALL);
        return facade.borrows().findById(borrowId);
    }
}
