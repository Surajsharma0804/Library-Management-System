package com.library.model;

import com.library.util.DateUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A personal reading list belonging to a library member.
 * Books are stored in insertion order; duplicate IDs are rejected.
 */
public class ReadingList {

    private final String id;
    private final String registrationNumber;
    private String listName;
    private String description;           // nullable
    private final List<String> bookIds;   // ordered, mutable internally
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ReadingList(Builder b) {
        this.id = Objects.requireNonNull(b.id, "reading list id");
        this.registrationNumber = Objects.requireNonNull(b.registrationNumber, "registration number");
        this.listName = b.listName;
        this.description = b.description;
        this.bookIds = b.bookIds == null ? new ArrayList<>() : new ArrayList<>(b.bookIds);
        this.createdAt = b.createdAt == null ? DateUtils.now() : b.createdAt;
        this.updatedAt = b.updatedAt == null ? this.createdAt : b.updatedAt;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public String getId() { return id; }
    public String getRegistrationNumber() { return registrationNumber; }
    public String getListName() { return listName; }
    public String getDescription() { return description; }

    /** Returns an unmodifiable snapshot of the book-id list. */
    public List<String> getBookIds() { return List.copyOf(bookIds); }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // -------------------------------------------------------------------------
    // Setters (mutable fields)
    // -------------------------------------------------------------------------

    public void setListName(String listName) { this.listName = listName; touch(); }
    public void setDescription(String description) { this.description = description; touch(); }

    // -------------------------------------------------------------------------
    // Book management
    // -------------------------------------------------------------------------

    /**
     * Adds {@code bookId} to the list if it is not already present.
     * Calls {@link #touch()} on success.
     */
    public void addBook(String bookId) {
        if (bookId != null && !bookIds.contains(bookId)) {
            bookIds.add(bookId);
            touch();
        }
    }

    /**
     * Removes {@code bookId} from the list.
     * Calls {@link #touch()} if the list actually changed.
     */
    public void removeBook(String bookId) {
        if (bookIds.remove(bookId)) {
            touch();
        }
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private void touch() {
        this.updatedAt = DateUtils.now();
    }

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String registrationNumber;
        private String listName;
        private String description;
        private List<String> bookIds;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(String v) { this.id = v; return this; }
        public Builder registrationNumber(String v) { this.registrationNumber = v; return this; }
        public Builder listName(String v) { this.listName = v; return this; }
        public Builder description(String v) { this.description = v; return this; }
        public Builder bookIds(List<String> v) { this.bookIds = v; return this; }
        public Builder createdAt(LocalDateTime v) { this.createdAt = v; return this; }
        public Builder updatedAt(LocalDateTime v) { this.updatedAt = v; return this; }

        public ReadingList build() {
            return new ReadingList(this);
        }
    }
}
