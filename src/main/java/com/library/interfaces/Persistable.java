package com.library.interfaces;

/**
 * Contract for entities that can be persisted to a storage medium.
 */
public interface Persistable {
    String getId();
    boolean isPersistent();
}
