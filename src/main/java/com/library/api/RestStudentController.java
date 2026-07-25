package com.library.api;

import com.library.controller.StudentController;
import com.library.facade.LibraryFacade;
import com.library.model.Student;
import com.library.security.Session;
import io.javalin.http.Context;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST endpoints for student management (admin-only operations).
 * Lists students, handles registration, suspension, and activation.
 */
public final class RestStudentController extends BaseRestController {

    private final StudentController ctrl;

    public RestStudentController(LibraryFacade facade) {
        super(facade);
        this.ctrl = new StudentController(facade);
    }

    /** GET /api/students — Lists all registered students. */
    public void list(Context ctx) {
        requireSession(ctx);
        List<Student> students = facade.userRepo().findAllStudents();
        ctx.json(students.stream().map(this::toMap).toList());
    }

    /** POST /api/students — Registers a new student (admin-only). */
    public void register(Context ctx) {
        Session session = requireSession(ctx);
        var body = ctx.bodyAsClass(StudentRequest.class);
        Student s = ctrl.register(session, body.firstName, body.lastName, body.email,
                body.phone, body.department, body.course, body.semester, body.section);
        Map<String, Object> result = toMap(s);
        result.put("defaultPassword", "changeme123");
        ctx.status(201).json(result);
    }

    /** PUT /api/students/{id}/suspend — Suspends a student's library access. */
    public void suspend(Context ctx) {
        Session session = requireSession(ctx);
        String studentId = ctx.pathParam("id");
        ctrl.suspend(session, studentId);
        ctx.json(Map.of("message", "Student suspended"));
    }

    /** PUT /api/students/{id}/activate — Reactivates a suspended student. */
    public void activate(Context ctx) {
        Session session = requireSession(ctx);
        String studentId = ctx.pathParam("id");
        ctrl.activate(session, studentId);
        ctx.json(Map.of("message", "Student activated"));
    }

    // ── Serialization ───────────────────────────────────────────────
    private Map<String, Object> toMap(Student s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("username", s.getUsername());
        m.put("firstName", s.getFirstName());
        m.put("lastName", s.getLastName());
        m.put("email", s.getEmail());
        m.put("phone", s.getPhone());
        m.put("registrationNumber", s.getRegistrationNumber());
        m.put("libraryCardNumber", s.getLibraryCardNumber());
        m.put("department", s.getDepartment());
        m.put("course", s.getCourse());
        m.put("semester", s.getSemester());
        m.put("section", s.getSection());
        m.put("membershipStatus", s.getMembershipStatus() != null ? s.getMembershipStatus().name() : null);
        m.put("membershipExpiry", s.getMembershipExpiry() != null ? s.getMembershipExpiry().toString() : null);
        m.put("borrowLimit", s.getBorrowLimit());
        m.put("currentBorrowCount", s.getCurrentBorrowCount());
        m.put("fineBalance", s.getFineBalancePaise() / 100.0);
        return m;
    }

    public static class StudentRequest {
        public String firstName;
        public String lastName;
        public String email;
        public String phone;
        public String department;
        public String course;
        public int semester = 1;
        public String section;
    }
}
