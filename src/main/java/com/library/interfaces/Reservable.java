package com.library.interfaces;

/**
 * Contract for entities that can be reserved by students.
 */
public interface Reservable {
    String getId();
    boolean isReservable();
    int getReservedCopies();
    void incrementReserved();
    void decrementReserved();
}
