package com.library.controller;

import com.library.facade.LibraryFacade;
import com.library.model.Reservation;
import com.library.security.Permissions;
import com.library.security.Session;

import java.util.List;

/**
 * Controller dedicated to reservation operations.
 */
public final class ReservationController extends BaseController {

    public ReservationController(LibraryFacade facade) {
        super(facade);
    }

    public Reservation reserveBook(Session session, String bookId, String registrationNumber) {
        require(session, Permissions.RESERVATION_CREATE);
        return facade.reservations().reserve(session, bookId, registrationNumber);
    }

    public Reservation cancelReservation(Session session, String reservationId) {
        require(session, Permissions.RESERVATION_CANCEL);
        return facade.reservations().cancel(session, reservationId);
    }

    public List<Reservation> viewAllReservations(Session session) {
        require(session, Permissions.RESERVATION_VIEW_ALL);
        return facade.reservations().findAll();
    }

    public List<Reservation> viewOwnReservations(Session session, String registrationNumber) {
        require(session, Permissions.RESERVATION_VIEW_OWN);
        return facade.reservations().findByStudent(registrationNumber);
    }
}
