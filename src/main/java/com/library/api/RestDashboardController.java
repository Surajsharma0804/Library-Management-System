package com.library.api;

import com.library.dto.DashboardDTO;
import com.library.facade.LibraryFacade;
import com.library.security.Session;
import io.javalin.http.Context;

/**
 * Role-aware dashboard statistics endpoint.
 * Returns different metrics based on the authenticated user's role.
 */
public final class RestDashboardController extends BaseRestController {

    public RestDashboardController(LibraryFacade facade) {
        super(facade);
    }

    /** GET /api/dashboard — Returns role-specific dashboard summary. */
    public void summary(Context ctx) {
        Session session = requireSession(ctx);
        DashboardDTO dto = facade.dashboard().getDashboardSummary(session);
        ctx.json(dto);
    }
}
