package com.library.interfaces;

/**
 * Contract for entities that can be borrowed from the library.
 */
public interface Borrowable {
    String getId();
    boolean isAvailable();
    int getAvailableCopies();
    void incrementBorrowed();
    void decrementBorrowed();
}
