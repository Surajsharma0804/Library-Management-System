package com.library.controller;

import com.library.enums.MembershipStatus;
import com.library.exception.ValidationException;
import com.library.facade.LibraryFacade;
import com.library.model.Student;
import com.library.repository.UserRepository;
import com.library.security.PasswordHasher;
import com.library.security.Permissions;
import com.library.security.Session;

import java.util.List;

public final class StudentController extends BaseController {

    private final UserRepository userRepo;

    public StudentController(LibraryFacade facade) {
        super(facade);
        this.userRepo = facade.userRepo();
    }

    public Student viewOwnProfile(Session session) { return getProfile(session); }

    public Student getProfile(Session session) {
        return userRepo.findStudentByUsername(session.username());
    }

    public List<Student> findAll(Session session) {
        require(session, Permissions.STUDENT_VIEW);
        return userRepo.findAllStudents();
    }

    public List<Student> search(Session session, String query) {
        require(session, Permissions.STUDENT_VIEW);
        String q = query == null ? "" : query.toLowerCase();
        return findAll(session).stream()
                .filter(s -> s.fullName().toLowerCase().contains(q)
                        || s.getRegistrationNumber().toLowerCase().contains(q)
                        || (s.getEmail() != null && s.getEmail().toLowerCase().contains(q)))
                .toList();
    }

    public Student register(Session session, String firstName, String lastName, String email,
                            String phone, String department, String course, int semester,
                            String section) {
        require(session, Permissions.STUDENT_ADD);
        Student student = facade.factory().createStudent(firstName, lastName, email, phone,
                department, course, semester, section);
        userRepo.save(student);
        facade.audit().record(session, "STUDENT_ADD", "Student", student.getId(),
                "Registered " + student.fullName() + " (" + student.getRegistrationNumber() + ")");
        return student;
    }

    public void suspend(Session session, String studentId) {
        require(session, Permissions.STUDENT_SUSPEND);
        Student s = findStudent(studentId);
        s.setMembershipStatus(MembershipStatus.INACTIVE);
        userRepo.save(s);
        facade.audit().record(session, "STUDENT_SUSPEND", "Student", studentId, "Suspended");
    }

    public void activate(Session session, String studentId) {
        require(session, Permissions.STUDENT_ACTIVATE);
        Student s = findStudent(studentId);
        s.setMembershipStatus(MembershipStatus.ACTIVE);
        s.setActive(true);
        userRepo.save(s);
        facade.audit().record(session, "STUDENT_ACTIVATE", "Student", studentId, "Activated");
    }

    public void resetPassword(Session session, String registrationNumber, String newPassword) {
        require(session, Permissions.STUDENT_RESET_PASSWORD);
        Student s = userRepo.findStudentByRegistrationNumber(registrationNumber);
        if (s == null) {
            throw new ValidationException("No student with registration number: " + registrationNumber);
        }
        s.setPasswordHash(PasswordHasher.hash(newPassword));
        userRepo.save(s);
        facade.audit().record(session, "STUDENT_RESET_PASSWORD", "Student", s.getId(),
                "Reset password for " + registrationNumber);
    }

    public Student regenerateCard(Session session, String studentId) {
        require(session, Permissions.STUDENT_GENERATE_CARD);
        Student s = findStudent(studentId);
        String newCard = "LIB" + System.currentTimeMillis();
        s.setMembershipStatus(s.getMembershipStatus());
        userRepo.save(s);
        facade.audit().record(session, "STUDENT_GENERATE_CARD", "Student", studentId,
                "Regenerated library card");
        return s;
    }

    public boolean delete(Session session, String studentId) {
        require(session, Permissions.STUDENT_DELETE);
        boolean removed = userRepo.deleteById(studentId);
        if (removed) {
            facade.audit().record(session, "STUDENT_DELETE", "Student", studentId, "Deleted");
        }
        return removed;
    }

    public List<com.library.model.BorrowRecord> myBorrows(Session session) {
        Student s = getProfile(session);
        if (s == null) return List.of();
        return facade.borrowRepo().findActiveByRegistrationNumber(s.getRegistrationNumber());
    }

    public List<com.library.model.BorrowRecord> myHistory(Session session) {
        Student s = getProfile(session);
        if (s == null) return List.of();
        return facade.borrowRepo().findByRegistrationNumber(s.getRegistrationNumber());
    }

    public List<com.library.model.Reservation> myReservations(Session session) {
        Student s = getProfile(session);
        if (s == null) return List.of();
        return facade.reservations().findByStudent(s.getRegistrationNumber());
    }

    public List<com.library.model.Fine> myFines(Session session) {
        Student s = getProfile(session);
        if (s == null) return List.of();
        return facade.fines().findByStudent(s.getRegistrationNumber());
    }

    private Student findStudent(String studentId) {
        return userRepo.findStudentByRegistrationNumber(studentId);
    }
}
