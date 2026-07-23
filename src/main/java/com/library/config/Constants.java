package com.library.config;

/**
 * Centralised application-wide numeric and textual constants.
 * Every magic number lives here so business rules can be tuned in one place.
 */
public final class Constants {

    private Constants() {
        throw new AssertionError("Constants class - do not instantiate");
    }

    /** Days a member may keep a borrowed book before a renewal is required. */
    public static final int DEFAULT_LOAN_PERIOD_DAYS = 14;

    /** Maximum renewals permitted on a single borrow. */
    public static final int DEFAULT_MAX_RENEWALS = 2;

    /** Maximum concurrent active borrows for a default student membership. */
    public static final int DEFAULT_BORROW_LIMIT = 5;

    /** Maximum active reservations per member. */
    public static final int DEFAULT_MAX_RESERVATIONS = 3;

    /** Per-day fine for an overdue book, in the system's minor currency unit (paise). */
    public static final int DEFAULT_FINE_PER_DAY_PAISE = 500;

    /** Number of days a fulfilled reservation is held before it expires. */
    public static final int DEFAULT_RESERVATION_HOLD_DAYS = 2;

    /** Default membership validity in months from the joining date. */
    public static final int DEFAULT_MEMBERSHIP_MONTHS = 48;

    /** ISBN-13 length including check digit. */
    public static final int ISBN_13_LENGTH = 13;

    /** ISBN-10 length including check digit. */
    public static final int ISBN_10_LENGTH = 10;

    /** Minimum plausible publication year for a library acquisition. */
    public static final int MIN_PUBLICATION_YEAR = 1450;

    /** Maximum member name length. */
    public static final int MAX_NAME_LENGTH = 60;

    /** Standard CSV/JSON record field delimiter used in exports. */
    public static final String CSV_DELIMITER = ",";

    /** System-wide date format used across persistence and the console UI. */
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /** System-wide date-time format used for audit logs and timestamps. */
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /** Seed filename for the default administrator account. */
    public static final String DEFAULT_ADMIN_USERNAME = "admin";

    /** Default one-time password the administrator must change on first login. */
    public static final String DEFAULT_ADMIN_PASSWORD = "admin@123";

    /** Library name shown in the console banner. */
    public static final String LIBRARY_NAME = "University Central Library";

    // ---- File paths for JSON persistence ----
    public static final String DATA_DIR = "data";
    public static final String LOG_DIR = "logs";
    public static final String BACKUP_DIR = "backups";
    public static final String EXPORT_DIR = "exports";

    public static final String BOOKS_FILE = DATA_DIR + "/books.json";
    public static final String STUDENTS_FILE = DATA_DIR + "/students.json";
    public static final String USERS_FILE = DATA_DIR + "/users.json";
    public static final String LIBRARY_CONFIG_FILE = DATA_DIR + "/library_config.json";
    public static final String COUNTERS_FILE = DATA_DIR + "/counters.json";
    public static final String LIBRARIANS_FILE = DATA_DIR + "/librarians.json";
    // public static final String ADMINS_FILE = DATA_DIR + "/admins.json";
    public static final String BORROW_RECORDS_FILE = DATA_DIR + "/borrow_records.json";
    public static final String RESERVATIONS_FILE = DATA_DIR + "/reservations.json";
    public static final String FINES_FILE = DATA_DIR + "/fines.json";
    public static final String NOTIFICATIONS_FILE = DATA_DIR + "/notifications.json";
    public static final String SETTINGS_FILE = DATA_DIR + "/settings.json";
    public static final String CONFIG_FILE = SETTINGS_FILE;
    public static final String AUDIT_LOG_FILE = DATA_DIR + "/audit_logs.json";

    // ---- New JSON data file paths (enterprise upgrade) ----
    public static final String MEMBERSHIP_TIERS_FILE = DATA_DIR + "/membership_tiers.json";
    public static final String LOST_BOOKS_FILE = DATA_DIR + "/lost_books.json";
    public static final String READING_LISTS_FILE = DATA_DIR + "/reading_lists.json";
    public static final String ACQUISITIONS_FILE = DATA_DIR + "/acquisitions.json";
    public static final String BRANCHES_FILE = DATA_DIR + "/branches.json";
    public static final String STUDY_ROOMS_FILE = DATA_DIR + "/study_rooms.json";
    public static final String ROOM_RESERVATIONS_FILE = DATA_DIR + "/room_reservations.json";
    public static final String ILL_RECORDS_FILE = DATA_DIR + "/inter_library_loans.json";

    // ---- SQLite persistence ----
    public static final String SQLITE_DB_FILE = DATA_DIR + "/library.db";

    // ---- Export directories ----
    public static final String CARDS_EXPORT_DIR = EXPORT_DIR + "/cards";

    // ---- Account-lockout thresholds ----
    /** Maximum consecutive failed login attempts before the account is temporarily locked. */
    public static final int MAX_LOGIN_ATTEMPTS = 5;

    /** Time window in minutes in which MAX_LOGIN_ATTEMPTS failures trigger a lockout. */
    public static final int LOCKOUT_WINDOW_MINUTES = 15;

    /** Duration in minutes that an account remains locked after exceeding MAX_LOGIN_ATTEMPTS. */
    public static final int LOCKOUT_DURATION_MINUTES = 15;

    // ---- Background job intervals ----
    /** How often the overdue-reminder job runs, in hours. */
    public static final int OVERDUE_JOB_INTERVAL_HOURS = 24;

}
