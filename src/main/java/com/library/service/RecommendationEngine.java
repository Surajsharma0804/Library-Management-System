package com.library.service;

import com.library.enums.BookStatus;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Content-based book recommendation engine.
 *
 * <p>Produces a personalised list of up to 5 available books for a given member
 * using a two-phase algorithm:
 * <ol>
 *   <li><b>Content path</b> — scores candidate books by matching categories (+2)
 *       and subjects (+1) against the member's five most-recent loans.</li>
 *   <li><b>Popularity fallback</b> — fills remaining slots with the globally
 *       most-borrowed available books the member has not already borrowed.</li>
 * </ol>
 *
 * <p>Requirements: 27.1
 */
public final class RecommendationEngine {

    private static final int MAX_RECOMMENDATIONS = 5;
    private static final int HISTORY_WINDOW = 5;
    private static final int CATEGORY_SCORE = 2;
    private static final int SUBJECT_SCORE = 1;

    private final BorrowRepository borrowRepo;
    private final BookRepository bookRepo;

    /**
     * Constructs a {@code RecommendationEngine} with all required dependencies.
     *
     * @param borrowRepo repository for loading borrow records
     * @param bookRepo   repository for loading and scoring book candidates
     */
    public RecommendationEngine(BorrowRepository borrowRepo,
                                 BookRepository bookRepo) {
        this.borrowRepo = Objects.requireNonNull(borrowRepo, "borrowRepo must not be null");
        this.bookRepo = Objects.requireNonNull(bookRepo, "bookRepo must not be null");
    }

    /**
     * Produces a personalised list of up to 5 recommended available books for
     * the given member.
     *
     * <p>Algorithm:
     * <ol>
     *   <li>Fetch the five most-recent loans (sorted by issue date descending).</li>
     *   <li>Build the full set of book IDs ever borrowed by this member.</li>
     *   <li>Extract distinct categories and subjects from the five recent books
     *       (null values are skipped).</li>
     *   <li>Score every available book not yet borrowed: +2 for a category match,
     *       +1 for a subject match. Take the top 5 by score.</li>
     *   <li>If the history is empty <em>or</em> fewer than 5 content results were
     *       found, fill remaining slots with the globally most-borrowed available
     *       books not already in the result set and not previously borrowed by
     *       this member.</li>
     * </ol>
     *
     * @param registrationNumber the member's registration number; must not be {@code null}
     * @return a list of up to {@value #MAX_RECOMMENDATIONS} recommended {@link Book}s
     */
    public List<Book> recommend(String registrationNumber) {
        Objects.requireNonNull(registrationNumber, "registrationNumber must not be null");

        // Step 1 — last 5 borrows sorted by issueDate desc
        List<BorrowRecord> allBorrows = borrowRepo.findByRegistrationNumber(registrationNumber);

        List<BorrowRecord> recentBorrows = allBorrows.stream()
                .filter(r -> r.getIssueDate() != null)
                .sorted(Comparator.comparing(BorrowRecord::getIssueDate).reversed())
                .limit(HISTORY_WINDOW)
                .toList();

        // Step 2 — full set of ever-borrowed book IDs
        Set<String> alreadyBorrowed = new HashSet<>();
        for (BorrowRecord r : allBorrows) {
            alreadyBorrowed.add(r.getBookId());
        }

        // Step 3 — categories + subjects from recent 5 (null-safe)
        Set<String> preferredCategories = new HashSet<>();
        Set<String> preferredSubjects = new HashSet<>();
        for (BorrowRecord r : recentBorrows) {
            bookRepo.findById(r.getBookId()).ifPresent(b -> {
                if (b.getCategory() != null) preferredCategories.add(b.getCategory());
                if (b.getSubject() != null) preferredSubjects.add(b.getSubject());
            });
        }

        // Step 4 — score available books not yet borrowed
        List<Book> allBooks = bookRepo.findAll();
        List<Book> candidates = allBooks.stream()
                .filter(b -> !alreadyBorrowed.contains(b.getId()))
                .filter(b -> b.getStatus() == BookStatus.AVAILABLE)
                .toList();

        // Only apply content scoring if there is history
        List<Book> contentResults = new ArrayList<>();
        if (!recentBorrows.isEmpty()) {
            Map<String, Integer> scores = new HashMap<>();
            for (Book b : candidates) {
                int score = 0;
                if (b.getCategory() != null && preferredCategories.contains(b.getCategory())) {
                    score += CATEGORY_SCORE;
                }
                if (b.getSubject() != null && preferredSubjects.contains(b.getSubject())) {
                    score += SUBJECT_SCORE;
                }
                if (score > 0) {
                    scores.put(b.getId(), score);
                }
            }

            contentResults = candidates.stream()
                    .filter(b -> scores.containsKey(b.getId()))
                    .sorted(Comparator.comparingInt((Book b) -> scores.getOrDefault(b.getId(), 0)).reversed())
                    .limit(MAX_RECOMMENDATIONS)
                    .toList();
        }

        // Step 5 — fallback with globally most-borrowed available books
        List<Book> result = new ArrayList<>(contentResults);
        if (result.size() < MAX_RECOMMENDATIONS) {
            // count global borrow frequency
            Map<String, Long> borrowCounts = new HashMap<>();
            for (BorrowRecord r : borrowRepo.findAll()) {
                borrowCounts.merge(r.getBookId(), 1L, Long::sum);
            }

            Set<String> alreadyInResult = new HashSet<>();
            for (Book b : result) {
                alreadyInResult.add(b.getId());
            }

            List<Book> fallback = candidates.stream()
                    .filter(b -> !alreadyInResult.contains(b.getId()))
                    .sorted(Comparator.comparingLong(
                            (Book b) -> borrowCounts.getOrDefault(b.getId(), 0L)).reversed())
                    .limit(MAX_RECOMMENDATIONS - result.size())
                    .toList();

            result.addAll(fallback);
        }

        return result;
    }

    /**
     * Returns all borrow records from the repository (used internally for global
     * popularity counts).
     */
    private List<BorrowRecord> findAll() {
        return borrowRepo.findAll();
    }
}
