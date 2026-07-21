package com.library.reports;

import com.library.service.AnalyticsService;
import java.util.List;

public final class InactiveMembersReport implements ReportStrategy {
    private final AnalyticsService analytics;
    public InactiveMembersReport(AnalyticsService analytics) { this.analytics = analytics; }
    @Override public String id() { return "inactive-members"; }
    @Override public String title() { return "Inactive Members Report"; }
    @Override public ReportData generate() {
        var inactive = analytics.inactiveMembers();
        var rows = inactive.stream().map(s -> List.of(s.getRegistrationNumber(), s.fullName(),
                s.getDepartment() != null ? s.getDepartment() : "")).toList();
        return new ReportData(title(), List.of("Reg No", "Name", "Department"), rows, "Inactive members: " + inactive.size());
    }
}
