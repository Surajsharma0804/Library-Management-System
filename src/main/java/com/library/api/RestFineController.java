package com.library.api;

import com.library.enums.FineStatus;
import com.library.enums.UserRole;
import com.library.facade.LibraryFacade;
import com.library.model.Fine;
import com.library.model.Student;
import com.library.security.Session;
import io.javalin.http.Context;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST endpoints for fine management.
 * Students view their own fines; admins view all outstanding fines.
 */
public final class RestFineController extends BaseRestController {

    public RestFineController(LibraryFacade facade) {
        super(facade);
    }

    /** GET /api/fines — Lists fines. Students see own; staff see all pending. */
    public void list(Context ctx) {
        Session session = requireSession(ctx);
        List<Fine> fines;

        if (session.role() == UserRole.STUDENT) {
            Student student = facade.userRepo().findStudentByUsername(session.username());
            if (student == null) {
                ctx.json(List.of());
                return;
            }
            String regNo = student.getRegistrationNumber();
            fines = facade.fineRepo().findAll(f ->
                    f.getRegistrationNumber().equals(regNo));
        } else {
            fines = facade.fineRepo().findAll(f -> f.getStatus() == FineStatus.PENDING);
        }

        ctx.json(fines.stream().map(this::toMap).toList());
    }

    /** POST /api/fines/{id}/pay — Marks a fine as paid. */
    public void pay(Context ctx) {
        Session session = requireSession(ctx);
        String fineId = ctx.pathParam("id");
        facade.fines().collectFine(session, fineId);
        ctx.json(Map.of("message", "Fine paid"));
    }

    // ── Serialization ───────────────────────────────────────────────
    private Map<String, Object> toMap(Fine f) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", f.getId());
        m.put("registrationNumber", f.getRegistrationNumber());
        m.put("amount", f.getAmountPaise() / 100.0);
        m.put("reason", f.getReason());
        m.put("status", f.getStatus() != null ? f.getStatus().name() : null);
        m.put("createdAt", f.getCreatedAt() != null ? f.getCreatedAt().toString() : null);
        m.put("settledAt", f.getSettledAt() != null ? f.getSettledAt().toString() : null);
        return m;
    }
}
