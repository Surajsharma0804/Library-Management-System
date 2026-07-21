package com.library.reports;

import com.library.repository.ReservationRepository;
import java.util.List;

public final class ReservationReport implements ReportStrategy {
    private final com.library.repository.ReservationRepository repo;
    public ReservationReport(com.library.repository.ReservationRepository repo) { this.repo = repo; }
    @Override public String id() { return "reservations"; }
    @Override public String title() { return "Reservations Report"; }
    @Override public ReportData generate() {
        var all = repo.findAll();
        var rows = all.stream().map(r -> List.of(r.getId(), r.getBookId(), r.getRegistrationNumber(),
                String.valueOf(r.getQueuePosition()), r.getStatus().name())).toList();
        return new ReportData(title(), List.of("ID", "Book ID", "Student", "Queue Pos", "Status"), rows, "Total reservations: " + all.size());
    }
}
