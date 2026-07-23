package com.library.service;

import com.library.factory.EntityFactory;
import com.library.model.Student;
import com.library.repository.UserRepository;
import com.library.security.AuthorizationManager;
import com.library.security.Permissions;
import com.library.security.Session;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Service for bulk-importing students from a CSV file.
 *
 * <p>The CSV must have a header row with the columns:
 * {@code firstName,lastName,email,phone,registrationNumber,department,program}
 * (case-insensitive). Each subsequent row is parsed independently; rows that
 * fail validation are skipped and the reason is recorded in the result.
 *
 * <p>Requirements: 28.1
 */
public final class StudentImportService {

    private final UserRepository userRepo;
    private final EntityFactory factory;
    private final AuthorizationManager rbac;
    private final AuditService auditService;

    /**
     * Result of a bulk student import operation.
     *
     * @param imported    number of students successfully imported
     * @param skipped     number of rows skipped due to validation errors or duplicates
     * @param skipReasons human-readable description of each skip event
     */
    public record ImportResult(int imported, int skipped, List<String> skipReasons) {}

    /**
     * Constructs a {@code StudentImportService} with all required dependencies.
     *
     * @param userRepo     repository for user persistence
     * @param factory      factory for creating student entities
     * @param rbac         authorization manager for permission enforcement
     * @param auditService service for recording audit trail entries
     */
    public StudentImportService(UserRepository userRepo,
                                 EntityFactory factory,
                                 AuthorizationManager rbac,
                                 AuditService auditService) {
        this.userRepo     = Objects.requireNonNull(userRepo,     "userRepo must not be null");
        this.factory      = Objects.requireNonNull(factory,      "factory must not be null");
        this.rbac         = Objects.requireNonNull(rbac,         "rbac must not be null");
        this.auditService = Objects.requireNonNull(auditService, "auditService must not be null");
    }

    /**
     * Reads a CSV file and imports each row as a new student account.
     *
     * <p>Expected CSV columns (header must be present, case-insensitive):
     * <pre>firstName,lastName,email,phone,registrationNumber,department,program</pre>
     *
     * <p>Each row is processed independently. A row is skipped when:
     * <ul>
     *   <li>it has fewer than 5 fields</li>
     *   <li>{@code registrationNumber} (column 5) is blank</li>
     *   <li>a student with that registration number already exists</li>
     *   <li>{@code firstName} or {@code lastName} is blank</li>
     *   <li>any unexpected exception occurs during processing</li>
     * </ul>
     *
     * @param session the authenticated session; must have
     *                {@link Permissions#STUDENT_IMPORT}
     * @param csvFile path to the CSV file; must exist and be readable
     * @return {@link ImportResult} with counts of imported and skipped rows
     * @throws UncheckedIOException if the file cannot be read
     * @throws com.library.exception.UnauthorizedAccessException if the session lacks
     *         {@link Permissions#STUDENT_IMPORT}
     */
    public ImportResult importFromCsv(Session session, Path csvFile) {
        Objects.requireNonNull(session, "session must not be null");
        Objects.requireNonNull(csvFile, "csvFile must not be null");
        rbac.require(session, Permissions.STUDENT_IMPORT);

        List<String> lines;
        try {
            lines = Files.readAllLines(csvFile);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read CSV file: " + csvFile, e);
        }

        if (lines.isEmpty()) {
            return new ImportResult(0, 0, List.of());
        }

        // Skip header row (first line)
        int imported = 0;
        int skipped  = 0;
        List<String> skipReasons = new ArrayList<>();

        for (int lineIndex = 1; lineIndex < lines.size(); lineIndex++) {
            String line = lines.get(lineIndex);
            try {
                String[] fields = line.split(",", -1);

                if (fields.length < 5) {
                    skipped++;
                    skipReasons.add("Line " + (lineIndex + 1) + ": too few fields (" + fields.length + ")");
                    continue;
                }

                String firstName          = fields[0].trim();
                String lastName           = fields[1].trim();
                String email              = fields[2].trim();
                String phone              = fields[3].trim();
                String registrationNumber = fields[4].trim();
                String department         = fields.length > 5 ? fields[5].trim() : "";
                String program            = fields.length > 6 ? fields[6].trim() : "";

                if (registrationNumber.isEmpty()) {
                    skipped++;
                    skipReasons.add("Line " + (lineIndex + 1) + ": registrationNumber is blank");
                    continue;
                }

                if (firstName.isEmpty() || lastName.isEmpty()) {
                    skipped++;
                    skipReasons.add("Line " + (lineIndex + 1) + ": firstName or lastName is blank");
                    continue;
                }

                // Duplicate check
                if (userRepo.findStudentByRegistrationNumber(registrationNumber) != null) {
                    skipped++;
                    skipReasons.add("Line " + (lineIndex + 1) + ": duplicate registrationNumber '"
                            + registrationNumber + "'");
                    continue;
                }

                Student student = factory.createStudent(firstName, lastName, email, phone,
                        department, "", 1, "");

                if (program != null && !program.isEmpty()) {
                    student.setProgram(program);
                }

                userRepo.save(student);
                imported++;

            } catch (Exception ex) {
                skipped++;
                skipReasons.add("Line " + (lineIndex + 1) + ": " + ex.getMessage());
            }
        }

        auditService.record(session, "STUDENT_IMPORT", "Student", null,
                "Imported " + imported + " students, skipped " + skipped);

        return new ImportResult(imported, skipped, skipReasons);
    }
}
