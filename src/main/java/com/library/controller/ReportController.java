package com.library.controller;

import com.library.facade.LibraryFacade;
import com.library.reports.ReportData;
import com.library.security.Permissions;
import com.library.security.Session;

import java.util.List;

/**
 * Controller for report generation and viewing.
 */
public final class ReportController extends BaseController {

    public ReportController(LibraryFacade facade) {
        super(facade);
    }

    public ReportData generateReport(Session session, String reportName) {
        require(session, Permissions.REPORT_VIEW);
        return facade.reports().generate(reportName);
    }

    public List<String> listAvailableReports(Session session) {
        require(session, Permissions.REPORT_VIEW);
        return facade.reports().availableReportIds();
    }
}
