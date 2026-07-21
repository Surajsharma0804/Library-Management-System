package com.library.controller;

import com.library.facade.LibraryFacade;
import com.library.model.Fine;
import com.library.security.Permissions;
import com.library.security.Session;

import java.util.List;

/**
 * Controller dedicated to fine management operations.
 */
public final class FineController extends BaseController {

    public FineController(LibraryFacade facade) {
        super(facade);
    }

    public Fine collectFine(Session session, String fineId) {
        require(session, Permissions.FINE_COLLECT);
        return facade.fines().collectFine(session, fineId);
    }

    public Fine waiveFine(Session session, String fineId, String reason) {
        require(session, Permissions.FINE_WAIVE);
        return facade.fines().waiveFine(session, fineId, reason);
    }

    public List<Fine> viewAllPendingFines(Session session) {
        require(session, Permissions.FINE_VIEW);
        return facade.fines().findAllPending();
    }

    public List<Fine> viewOwnFines(Session session, String registrationNumber) {
        require(session, Permissions.FINE_VIEW);
        return facade.fines().findByStudent(registrationNumber);
    }

    public List<Fine> viewOwnPendingFines(Session session, String registrationNumber) {
        require(session, Permissions.FINE_VIEW);
        return facade.fines().findPendingByStudent(registrationNumber);
    }
}
