package com.library.api;

import com.library.enums.UserRole;
import com.library.facade.LibraryFacade;
import com.library.model.Student;
import com.library.model.User;
import com.library.security.Session;
import io.javalin.http.Context;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * REST endpoint for the authenticated user's profile.
 * Returns account details based on the user's role.
 */
public final class RestProfileController extends BaseRestController {

    public RestProfileController(LibraryFacade facade) {
        super(facade);
    }

    /** GET /api/profile — Returns the authenticated user's profile data. */
    public void get(Context ctx) {
        Session session = requireSession(ctx);
        Map<String, Object> profile = new LinkedHashMap<>();

        profile.put("username", session.username());
        profile.put("role", session.role().name());

        if (session.role() == UserRole.STUDENT) {
            Student s = facade.userRepo().findStudentByUsername(session.username());
            if (s != null) {
                profile.put("firstName", s.getFirstName());
                profile.put("lastName", s.getLastName());
                profile.put("email", s.getEmail());
                profile.put("phone", s.getPhone());
                profile.put("registrationNumber", s.getRegistrationNumber());
                profile.put("libraryCardNumber", s.getLibraryCardNumber());
                profile.put("department", s.getDepartment());
                profile.put("course", s.getCourse());
                profile.put("semester", s.getSemester());
                profile.put("section", s.getSection());
                profile.put("membershipStatus", s.getMembershipStatus() != null ? s.getMembershipStatus().name() : null);
                profile.put("membershipExpiry", s.getMembershipExpiry() != null ? s.getMembershipExpiry().toString() : null);
                profile.put("borrowLimit", s.getBorrowLimit());
                profile.put("currentBorrowCount", s.getCurrentBorrowCount());
                profile.put("fineBalance", s.getFineBalancePaise() / 100.0);
            }
        } else {
            User u = facade.staffRepo().findByUsername(session.username());
            if (u != null) {
                profile.put("firstName", u.getFirstName());
                profile.put("lastName", u.getLastName());
                profile.put("email", u.getEmail());
                profile.put("phone", u.getPhone());
                profile.put("staffId", u.getId());
            }
        }

        ctx.json(profile);
    }
}
