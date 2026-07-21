package com.library.service;

import com.library.enums.BookStatus;
import com.library.enums.BorrowStatus;
import com.library.enums.MembershipStatus;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.Fine;
import com.library.model.Student;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRepository;
import com.library.repository.FineRepository;
import com.library.repository.UserRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Computes aggregate analytics over the entire dataset. Pure read-only
 * queries with no side effects - used by the admin dashboard and the
 * reports module.
 */
public final class AnalyticsService {

    private final BookRepository bookRepo;
    private final UserRepository studentRepo;
    private final BorrowRepository borrowRepo;
    private final FineRepository fineRepo;

    public AnalyticsService(BookRepository bookRepo, UserRepository studentRepo,
                            BorrowRepository borrowRepo, FineRepository fineRepo) {
        this.bookRepo = bookRepo;
        this.studentRepo = studentRepo;
        this.borrowRepo = borrowRepo;
        this.fineRepo = fineRepo;
    }

    public long totalBooks() {
        return bookRepo.count();
    }

    public long totalStudents() {
        return studentRepo.count();
    }

    public long totalActiveBorrows() {
        return borrowRepo.findAllActive().size();
    }

    public long totalOverdueBorrows() {
        return borrowRepo.findAllOverdue().size();
    }

    public long totalPendingFines() {
        return fineRepo.findAllPending().size();
    }

    public long totalPendingFineAmountPaise() {
        return fineRepo.findAllPending().stream().mapToLong(Fine::getAmountPaise).sum();
    }

    public Map<BookStatus, Long> booksByStatus() {
        return bookRepo.findAll().stream()
                .collect(Collectors.groupingBy(Book::getStatus, Collectors.counting()));
    }

    public Map<MembershipStatus, Long> studentsByStatus() {
        return studentRepo.findAllStudents().stream()
                .collect(Collectors.groupingBy(Student::getMembershipStatus, Collectors.counting()));
    }

    public List<Book> mostBorrowedBooks(int limit) {
        Map<String, Long> borrowCounts = borrowRepo.findAll().stream()
                .collect(Collectors.groupingBy(BorrowRecord::getBookId, Collectors.counting()));
        return bookRepo.findAll().stream()
                .sorted((a, b) -> Long.compare(
                        borrowCounts.getOrDefault(b.getId(), 0L),
                        borrowCounts.getOrDefault(a.getId(), 0L)))
                .limit(limit)
                .toList();
    }

    public List<Map.Entry<String, Long>> popularBooks(int limit) {
        Map<String, Long> borrowCounts = borrowRepo.findAll().stream()
                .collect(Collectors.groupingBy(BorrowRecord::getBookId, Collectors.counting()));
        return borrowCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .toList();
    }

    public List<Student> inactiveMembers() {
        return inactiveMembers(90);
    }

    public List<Student> inactiveMembers(int daysInactive) {
        return studentRepo.findAllStudents().stream()
                .filter(s -> borrowRepo.findByRegistrationNumber(s.getRegistrationNumber()).stream()
                        .noneMatch(r -> r.getIssueDate() != null
                                && r.getIssueDate().isAfter(
                                        java.time.LocalDate.now().minusDays(daysInactive))))
                .toList();
    }

    public Map<String, Long> monthlyBorrowCounts(int year) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (int m = 1; m <= 12; m++) {
            int month = m;
            long count = borrowRepo.findAll().stream()
                    .filter(r -> r.getIssueDate() != null && r.getIssueDate().getYear() == year
                            && r.getIssueDate().getMonthValue() == month)
                    .count();
            counts.put(java.time.Month.of(m).toString(), count);
        }
        return counts;
    }

    public Map<String, Long> borrowsByStatus() {
        return borrowRepo.findAll().stream()
                .collect(Collectors.groupingBy(r -> r.getStatus().name(), Collectors.counting()));
    }

    public Map<String, Long> booksByCategory() {
        return bookRepo.findAll().stream()
                .filter(b -> b.getCategory() != null && !b.getCategory().isBlank())
                .collect(Collectors.groupingBy(Book::getCategory, Collectors.counting()));
    }
}
