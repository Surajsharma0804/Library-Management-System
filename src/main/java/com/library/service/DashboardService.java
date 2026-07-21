package com.library.service;

import com.library.dto.DashboardDTO;
import com.library.enums.BorrowStatus;
import com.library.enums.FineStatus;
import com.library.enums.MembershipStatus;
import com.library.enums.ReservationStatus;
import com.library.model.Student;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRepository;
import com.library.repository.FineRepository;
import com.library.repository.ReservationRepository;
import com.library.repository.StaffRepository;
import com.library.repository.UserRepository;
import com.library.security.Session;

public final class DashboardService {

    private final BookRepository bookRepo;
    private final UserRepository studentRepo;
    private final StaffRepository staffRepo;
    private final BorrowRepository borrowRepo;
    private final ReservationRepository reservationRepo;
    private final FineRepository fineRepo;

    public DashboardService(BookRepository bookRepo, UserRepository studentRepo,
                            StaffRepository staffRepo, BorrowRepository borrowRepo,
                            ReservationRepository reservationRepo, FineRepository fineRepo) {
        this.bookRepo = bookRepo;
        this.studentRepo = studentRepo;
        this.staffRepo = staffRepo;
        this.borrowRepo = borrowRepo;
        this.reservationRepo = reservationRepo;
        this.fineRepo = fineRepo;
    }

    public DashboardDTO studentDashboard(Session session) {
        DashboardDTO dto = baseSummary();
        Student student = studentRepo.findStudentByUsername(session.username());
        if (student != null) {
            int activeBorrows = borrowRepo.findActiveByRegistrationNumber(student.getRegistrationNumber()).size();
            dto.setBooksBorrowedByCurrentUser(activeBorrows);
            dto.setRemainingBorrowLimit(student.remainingBorrowSlots());
            dto.setCurrentUserFinePaise(student.getFineBalancePaise());
        }
        return dto;
    }

    public DashboardDTO librarianDashboard(Session session) {
        DashboardDTO dto = baseSummary();
        dto.setOverdueBooks(borrowRepo.findAllOverdue().size());
        dto.setPendingReservations(reservationRepo.findAll(r ->
                r.getStatus() == ReservationStatus.PENDING).size());
        return dto;
    }

    public DashboardDTO adminDashboard(Session session) {
        DashboardDTO dto = baseSummary();
        dto.setOverdueBooks(borrowRepo.findAllOverdue().size());
        dto.setPendingReservations(reservationRepo.findAll(r ->
                r.getStatus() == ReservationStatus.PENDING).size());
        dto.setTotalStudents(studentRepo.findAllStudents().size());
        dto.setPendingFines(fineRepo.findAll(f -> f.getStatus() == FineStatus.PENDING).size());
        dto.setActiveStudents(studentRepo.findAllStudents().stream()
                .filter(s -> s.getMembershipStatus() == MembershipStatus.ACTIVE).toList().size());
        dto.setTotalFineAmountPaise(fineRepo.findAll(f ->
                f.getStatus() == FineStatus.PENDING)
                .stream().mapToLong(f -> f.getAmountPaise()).sum());
        return dto;
    }

    public DashboardDTO getDashboardSummary(Session session) {
        return switch (session.role()) {
            case STUDENT -> studentDashboard(session);
            case LIBRARIAN -> librarianDashboard(session);
            case ADMIN -> adminDashboard(session);
        };
    }

    private DashboardDTO baseSummary() {
        DashboardDTO dto = new DashboardDTO();
        dto.setTotalBooks((int) bookRepo.count());
        dto.setAvailableBooks(bookRepo.findAll(b -> b.getAvailableQuantity() > 0).size());
        dto.setBorrowedBooks(borrowRepo.findAllActive().size());
        return dto;
    }
}
