package com.library.reports;

import com.library.enums.FineStatus;
import com.library.model.Fine;
import com.library.repository.FineRepository;
import com.library.repository.UserRepository;

import java.util.List;

/**
 * Report strategy for fine summary.
 */
public final class FineReport implements ReportStrategy {

    private final FineRepository fineRepo;
    private final UserRepository studentRepo;

    public FineReport(FineRepository fineRepo, UserRepository studentRepo) {
        this.fineRepo = fineRepo;
        this.studentRepo = studentRepo;
    }

    @Override
    public String id() {
        return "fines";
    }

    @Override
    public String title() {
        return "Fine Report";
    }

    @Override
    public ReportData generate() {
        var fines = fineRepo.findAll();
        var rows = fines.stream()
                .map(f -> List.of(
                        f.getId(),
                        f.getRegistrationNumber(),
                        String.valueOf(f.getAmountPaise() / 100.0),
                        f.getReason() != null ? f.getReason() : "",
                        f.getStatus().name()
                ))
                .toList();

        long totalPending = fines.stream()
                .filter(f -> f.getStatus() == FineStatus.PENDING)
                .mapToLong(Fine::getAmountPaise)
                .sum();

        return new ReportData(
                title(),
                List.of("Fine ID", "Student Reg No", "Amount (Rs)", "Reason", "Status"),
                rows,
                "Total fines: " + fines.size() + " | Pending amount: Rs." + (totalPending / 100.0)
        );
    }
}
