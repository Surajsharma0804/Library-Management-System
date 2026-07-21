package com.library.builder;

import com.library.model.Reservation;

public final class ReservationBuilder {
    public static Reservation.Builder create() { return Reservation.builder(); }
}
