package com.library.service;

import com.library.service.AuditService;
import com.library.config.Constants;
import com.library.exception.PersistenceException;
import com.library.security.Session;
import com.library.util.AppLogger;
import com.library.util.DateUtils;
import com.library.util.FileUtils;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Creates and restores full-system backups by copying every JSON data
 * file into a timestamped snapshot directory. Restore copies the
 * snapshot back over the live files and signals that repositories
 * should reload.
 */
public final class BackupService {

    private static final String LOG = "BackupService";
    private final AuditService auditService;

    public BackupService(AuditService auditService) {
        this.auditService = auditService;
    }

    private static final List<Path> DATA_FILES = List.of(
            Path.of(Constants.BOOKS_FILE), Path.of(Constants.STUDENTS_FILE),
            Path.of(Constants.USERS_FILE), Path.of(Constants.LIBRARIANS_FILE),
            Path.of(Constants.BORROW_RECORDS_FILE), Path.of(Constants.RESERVATIONS_FILE),
            Path.of(Constants.FINES_FILE), Path.of(Constants.AUDIT_LOG_FILE),
            Path.of(Constants.SETTINGS_FILE), Path.of(Constants.COUNTERS_FILE));

    public String createBackup(Session session) {
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Path backupDir = Path.of(Constants.BACKUP_DIR).resolve("backup-" + stamp);
        try { java.nio.file.Files.createDirectories(backupDir); } catch (java.io.IOException e) {
            throw new PersistenceException("Failed to create backup directory: " + e.getMessage());
        }
        for (Path file : DATA_FILES) {
            try {
                if (java.nio.file.Files.exists(file) && java.nio.file.Files.size(file) > 0) {
                    java.nio.file.Files.copy(file, backupDir.resolve(file.getFileName()));
                }
            } catch (java.io.IOException e) {
                throw new PersistenceException("Backup failed for " + file + ": " + e.getMessage());
            }
        }
        AppLogger.info(LOG, "Backup created at " + backupDir);
        auditService.record(session, "BACKUP_CREATE", "Backup", backupDir.toString(),
                "Created backup " + stamp);
        return backupDir.toString();
    }

    public String restoreBackup(Session session, String backupDirPath) {
        Path backupDir = Path.of(backupDirPath);
        if (!backupDir.toFile().isDirectory()) {
            throw new PersistenceException("Backup directory not found: " + backupDirPath);
        }
        for (Path file : DATA_FILES) {
            try {
                Path backupFile = backupDir.resolve(file.getFileName());
                if (java.nio.file.Files.exists(backupFile) && java.nio.file.Files.size(backupFile) > 0) {
                    java.nio.file.Files.copy(backupFile, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (java.io.IOException e) {
                throw new PersistenceException("Restore failed for " + file + ": " + e.getMessage());
            }
        }
        AppLogger.info(LOG, "Restored backup from " + backupDir);
        auditService.record(session, "BACKUP_RESTORE", "Backup", backupDir.toString(),
                "Restored backup from " + backupDirPath);
        return backupDirPath;
    }

    public List<Path> listBackups() {
        Path dir = Path.of(Constants.BACKUP_DIR);
        if (!java.nio.file.Files.exists(dir)) return List.of();
        try (var stream = java.nio.file.Files.list(dir)) {
            return stream.filter(java.nio.file.Files::isDirectory)
                    .sorted(java.util.Comparator.reverseOrder())
                    .toList();
        } catch (java.io.IOException e) {
            return List.of();
        }
    }
}
