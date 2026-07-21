package com.library.util;

/**
 * Thread-safe sequential ID generator.
 */
public final class IdGenerator {
    private long counter = 0;
    public synchronized String nextId(String prefix) { return prefix + "-" + String.format("%06d", ++counter); }
    public synchronized long nextValue() { return ++counter; }
    public synchronized String generateLibraryCardNumber() { return "LIB-" + String.format("%06d", ++counter); }
}
