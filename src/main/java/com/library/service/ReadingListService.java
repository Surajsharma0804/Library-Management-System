package com.library.service;

import com.library.model.Book;
import com.library.model.ReadingList;
import com.library.repository.BookRepository;
import com.library.repository.ReadingListRepository;
import com.library.security.Session;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

/**
 * Service for managing personal reading lists.
 *
 * <p>Ownership is enforced by comparing the session's username against the
 * stored registration number — no additional RBAC permission checks are needed
 * because a member can only manage their own lists.
 *
 * <p>Requirements: 21.1, 21.2
 */
public final class ReadingListService {

    private final ReadingListRepository readingListRepo;
    private final BookRepository bookRepo;

    /**
     * Constructs a {@code ReadingListService} with all required dependencies.
     *
     * @param readingListRepo repository for reading-list persistence
     * @param bookRepo        repository used to verify book existence and resolve titles
     */
    public ReadingListService(ReadingListRepository readingListRepo,
                               BookRepository bookRepo) {
        this.readingListRepo = Objects.requireNonNull(readingListRepo,
                "readingListRepo must not be null");
        this.bookRepo = Objects.requireNonNull(bookRepo, "bookRepo must not be null");
    }

    /**
     * Creates a new personal reading list for the authenticated user.
     *
     * @param session     the authenticated session; must not be {@code null}
     * @param listName    display name of the list; must not be blank
     * @param description optional description; may be {@code null}
     * @return the newly created and persisted {@link ReadingList}
     * @throws IllegalArgumentException if {@code listName} is blank
     */
    public ReadingList create(Session session, String listName, String description) {
        Objects.requireNonNull(session, "session must not be null");
        Objects.requireNonNull(listName, "listName must not be null");
        if (listName.isBlank()) {
            throw new IllegalArgumentException("listName must not be blank");
        }

        ReadingList list = ReadingList.builder()
                .id(UUID.randomUUID().toString())
                .registrationNumber(ownerKey(session))
                .listName(listName)
                .description(description)
                .build();
        readingListRepo.save(list);
        return list;
    }

    /**
     * Adds a book to the specified reading list.
     *
     * <p>The session user must own the list. If the book is already present,
     * this is a no-op (idempotent).
     *
     * @param session the authenticated session; must not be {@code null}
     * @param listId  the reading list ID; must not be {@code null}
     * @param bookId  the book ID to add; must not be {@code null}
     * @return the updated and persisted {@link ReadingList}
     * @throws NoSuchElementException   if the list or book does not exist
     * @throws IllegalStateException    if the session user does not own the list
     */
    public ReadingList addBook(Session session, String listId, String bookId) {
        Objects.requireNonNull(session, "session must not be null");
        Objects.requireNonNull(listId, "listId must not be null");
        Objects.requireNonNull(bookId, "bookId must not be null");

        ReadingList list = readingListRepo.findById(listId)
                .orElseThrow(() -> new NoSuchElementException("Reading list not found: " + listId));
        checkOwnership(session, list);

        if (!bookRepo.findById(bookId).isPresent()) {
            throw new NoSuchElementException("Book not found: " + bookId);
        }

        list.addBook(bookId);
        readingListRepo.save(list);
        return list;
    }

    /**
     * Removes a book from the specified reading list.
     *
     * <p>The session user must own the list. If the book is not present,
     * this is a no-op (idempotent).
     *
     * @param session the authenticated session; must not be {@code null}
     * @param listId  the reading list ID; must not be {@code null}
     * @param bookId  the book ID to remove; must not be {@code null}
     * @return the updated and persisted {@link ReadingList}
     * @throws NoSuchElementException if the list does not exist
     * @throws IllegalStateException  if the session user does not own the list
     */
    public ReadingList removeBook(Session session, String listId, String bookId) {
        Objects.requireNonNull(session, "session must not be null");
        Objects.requireNonNull(listId, "listId must not be null");
        Objects.requireNonNull(bookId, "bookId must not be null");

        ReadingList list = readingListRepo.findById(listId)
                .orElseThrow(() -> new NoSuchElementException("Reading list not found: " + listId));
        checkOwnership(session, list);

        list.removeBook(bookId);
        readingListRepo.save(list);
        return list;
    }

    /**
     * Returns all reading lists owned by the authenticated user.
     *
     * @param session the authenticated session; must not be {@code null}
     * @return unmodifiable list of {@link ReadingList}s belonging to the user
     */
    public List<ReadingList> findByStudent(Session session) {
        Objects.requireNonNull(session, "session must not be null");
        return readingListRepo.findByRegistrationNumber(ownerKey(session));
    }

    /**
     * Resolves the book IDs in a reading list to their display titles.
     *
     * @param list the reading list whose book IDs should be resolved; must not be {@code null}
     * @return a {@link LinkedHashMap} mapping each book ID to its title (or the ID itself
     *         if the book cannot be found), in insertion order
     */
    public LinkedHashMap<String, String> resolveBookTitles(ReadingList list) {
        Objects.requireNonNull(list, "list must not be null");
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        for (String bookId : list.getBookIds()) {
            String title = bookRepo.findById(bookId)
                    .map(Book::getTitle)
                    .orElse(bookId);
            result.put(bookId, title);
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Returns the owner key for a session. Uses the session's username as the
     * registration-number equivalent stored in the reading list.
     */
    private String ownerKey(Session session) {
        return session.username();
    }

    /**
     * Verifies that the session user owns the given list.
     *
     * @throws IllegalStateException if the session user is not the list owner
     */
    private void checkOwnership(Session session, ReadingList list) {
        if (!ownerKey(session).equals(list.getRegistrationNumber())) {
            throw new IllegalStateException(
                    "Session user does not own reading list: " + list.getId());
        }
    }
}
