package com.library.controller;

import com.library.dto.DashboardDTO;
import com.library.facade.LibraryFacade;
import com.library.security.Permissions;
import com.library.security.Session;

public final class DashboardController extends BaseController {

    private final com.library.service.DashboardService dashboardService;

    public DashboardController(LibraryFacade facade,
                               com.library.service.DashboardService dashboardService) {
        super(facade);
        this.dashboardService = dashboardService;
    }

    public DashboardDTO getDashboardSummary(Session session) {
        require(session, Permissions.ANALYTICS_VIEW);
        return dashboardService.getDashboardSummary(session);
    }
}
