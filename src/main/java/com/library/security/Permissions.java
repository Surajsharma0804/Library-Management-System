package com.library.security;

import com.library.enums.UserRole;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Permission constants and role-mapping for RBAC.
 *
 * <p>All feature permissions are declared here as String constants and
 * registered in {@link #ROLE_MAP} with the set of roles that may exercise them.
 * Controllers call {@link com.library.security.AuthorizationManager#require} to
 * enforce them at runtime.
 */
public final class Permissions {

    private Permissions() {}

    // ------------------------------------------------------------------ Config
    public static final String CONFIG_VIEW   = "CONFIG_VIEW";
    public static final String CONFIG_UPDATE = "CONFIG_UPDATE";

    // --------------------------------------------------------------- Librarian
    public static final String LIBRARIAN_ADD                = "LIBRARIAN_ADD";
    public static final String LIBRARIAN_UPDATE             = "LIBRARIAN_UPDATE";
    public static final String LIBRARIAN_REMOVE             = "LIBRARIAN_REMOVE";
    public static final String LIBRARIAN_RESET_PASSWORD     = "LIBRARIAN_RESET_PASSWORD";
    public static final String LIBRARIAN_ASSIGN_PERMISSIONS = "LIBRARIAN_ASSIGN_PERMISSIONS";
    public static final String LIBRARIAN_VIEW               = "LIBRARIAN_VIEW";

    // ------------------------------------------------------------------ Backup
    public static final String BACKUP_CREATE  = "BACKUP_CREATE";
    public static final String BACKUP_RESTORE = "BACKUP_RESTORE";

    // ---------------------------------------------------------------- Analytics / Reports / Audit
    public static final String ANALYTICS_VIEW   = "ANALYTICS_VIEW";
    public static final String REPORT_GENERATE  = "REPORT_GENERATE";
    public static final String REPORT_VIEW      = "REPORT_VIEW";
    public static final String AUDIT_VIEW       = "AUDIT_VIEW";

    // ------------------------------------------------------------------- Books
    public static final String BOOK_ADD          = "BOOK_ADD";
    public static final String BOOK_UPDATE       = "BOOK_UPDATE";
    public static final String BOOK_DELETE       = "BOOK_DELETE";
    public static final String BOOK_ARCHIVE      = "BOOK_ARCHIVE";
    public static final String BOOK_RESTORE      = "BOOK_RESTORE";
    public static final String BOOK_MARK_LOST    = "BOOK_MARK_LOST";
    public static final String BOOK_MARK_DAMAGED = "BOOK_MARK_DAMAGED";
    public static final String BOOK_REPAIR       = "BOOK_REPAIR";
    public static final String BOOK_VIEW         = "BOOK_VIEW";

    // --------------------------------------------------------------- Borrowing
    public static final String BORROW_ISSUE    = "BORROW_ISSUE";
    public static final String BORROW_RETURN   = "BORROW_RETURN";
    public static final String BORROW_RENEW    = "BORROW_RENEW";
    public static final String BORROW_VIEW_ALL = "BORROW_VIEW_ALL";
    public static final String BORROW_VIEW_OWN = "BORROW_VIEW_OWN";

    // ---------------------------------------------------------------- Students
    public static final String STUDENT_ADD            = "STUDENT_ADD";
    public static final String STUDENT_UPDATE         = "STUDENT_UPDATE";
    /** Restricted to ADMIN and LIBRARIAN only — students cannot list other students. */
    public static final String STUDENT_VIEW           = "STUDENT_VIEW";
    public static final String STUDENT_REMOVE         = "STUDENT_REMOVE";
    public static final String STUDENT_DELETE         = "STUDENT_DELETE";
    public static final String STUDENT_ACTIVATE       = "STUDENT_ACTIVATE";
    public static final String STUDENT_SUSPEND        = "STUDENT_SUSPEND";
    public static final String STUDENT_RESET_PASSWORD = "STUDENT_RESET_PASSWORD";
    public static final String STUDENT_GENERATE_CARD  = "STUDENT_GENERATE_CARD";
    public static final String STUDENT_IMPORT         = "STUDENT_IMPORT";

    // ------------------------------------------------------------------- Users
    public static final String USER_VIEW   = "USER_VIEW";
    public static final String USER_UPDATE = "USER_UPDATE";
    public static final String USER_ADD    = "USER_ADD";
    public static final String USER_DELETE = "USER_DELETE";

    // ------------------------------------------------------------------- Fines
    /**
     * Kept for backward-compatibility with legacy callers; not registered in
     * {@link #ROLE_MAP} — use {@link #FINE_VIEW_ALL} or {@link #FINE_VIEW_OWN}.
     *
     * @deprecated Use {@link #FINE_VIEW_ALL} or {@link #FINE_VIEW_OWN}.
     */
    @Deprecated
    public static final String FINE_VIEW     = "FINE_VIEW";
    /** View all fines — restricted to ADMIN and LIBRARIAN. */
    public static final String FINE_VIEW_ALL = "FINE_VIEW_ALL";
    /** View own fines — ADMIN, LIBRARIAN, and STUDENT. */
    public static final String FINE_VIEW_OWN = "FINE_VIEW_OWN";
    public static final String FINE_PAY      = "FINE_PAY";
    public static final String FINE_WAIVE    = "FINE_WAIVE";
    public static final String FINE_COLLECT  = "FINE_COLLECT";

    // --------------------------------------------------------------- Reservations
    public static final String RESERVATION_CREATE   = "RESERVATION_CREATE";
    public static final String RESERVATION_CANCEL   = "RESERVATION_CANCEL";
    public static final String RESERVATION_VIEW     = "RESERVATION_VIEW";
    public static final String RESERVATION_VIEW_ALL = "RESERVATION_VIEW_ALL";
    public static final String RESERVATION_VIEW_OWN = "RESERVATION_VIEW_OWN";

    // ----------------------------------------------------------------- Profile
    public static final String PROFILE_VIEW = "PROFILE_VIEW";

    // ------------------------------------------------------------- Acquisitions
    public static final String ACQUISITION_REQUEST = "ACQUISITION_REQUEST";
    public static final String ACQUISITION_APPROVE = "ACQUISITION_APPROVE";

    // -------------------------------------------------------------------- ILL
    public static final String ILL_MANAGE = "ILL_MANAGE";

    // ---------------------------------------------------------- Study Rooms
    public static final String ROOM_RESERVATION_CREATE = "ROOM_RESERVATION_CREATE";

    // --------------------------------------------------------- Notifications
    public static final String NOTIFICATION_VIEW = "NOTIFICATION_VIEW";

    // -----------------------------------------------------------------------

    private static final Map<String, Set<UserRole>> ROLE_MAP = Map.ofEntries(
            // Config — admin only
            Map.entry(CONFIG_VIEW,   EnumSet.of(UserRole.ADMIN)),
            Map.entry(CONFIG_UPDATE, EnumSet.of(UserRole.ADMIN)),

            // Librarian management — admin only
            Map.entry(LIBRARIAN_ADD,                EnumSet.of(UserRole.ADMIN)),
            Map.entry(LIBRARIAN_UPDATE,             EnumSet.of(UserRole.ADMIN)),
            Map.entry(LIBRARIAN_REMOVE,             EnumSet.of(UserRole.ADMIN)),
            Map.entry(LIBRARIAN_VIEW,               EnumSet.of(UserRole.ADMIN)),
            Map.entry(LIBRARIAN_RESET_PASSWORD,     EnumSet.of(UserRole.ADMIN)),
            Map.entry(LIBRARIAN_ASSIGN_PERMISSIONS, EnumSet.of(UserRole.ADMIN)),

            // Backup — admin only
            Map.entry(BACKUP_CREATE,  EnumSet.of(UserRole.ADMIN)),
            Map.entry(BACKUP_RESTORE, EnumSet.of(UserRole.ADMIN)),

            // Analytics / Reports / Audit
            Map.entry(ANALYTICS_VIEW,  EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),
            Map.entry(REPORT_GENERATE, EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),
            Map.entry(REPORT_VIEW,     EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),
            Map.entry(AUDIT_VIEW,      EnumSet.of(UserRole.ADMIN)),

            // Books
            Map.entry(BOOK_ADD,          EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),
            Map.entry(BOOK_UPDATE,       EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),
            Map.entry(BOOK_DELETE,       EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),
            Map.entry(BOOK_ARCHIVE,      EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),
            Map.entry(BOOK_RESTORE,      EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),
            Map.entry(BOOK_MARK_LOST,    EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),
            Map.entry(BOOK_MARK_DAMAGED, EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),
            Map.entry(BOOK_REPAIR,       EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),
            Map.entry(BOOK_VIEW,         EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN, UserRole.STUDENT)),

            // Borrowing
            Map.entry(BORROW_ISSUE,    EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),
            Map.entry(BORROW_RETURN,   EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),
            Map.entry(BORROW_RENEW,    EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),
            Map.entry(BORROW_VIEW_ALL, EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),
            Map.entry(BORROW_VIEW_OWN, EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN, UserRole.STUDENT)),

            // Students — STUDENT_VIEW is admin/librarian only (students cannot list peers)
            Map.entry(STUDENT_ADD,            EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),
            Map.entry(STUDENT_UPDATE,         EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),
            Map.entry(STUDENT_VIEW,           EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),
            Map.entry(STUDENT_REMOVE,         EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),
            Map.entry(STUDENT_DELETE,         EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),
            Map.entry(STUDENT_ACTIVATE,       EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),
            Map.entry(STUDENT_SUSPEND,        EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),
            Map.entry(STUDENT_RESET_PASSWORD, EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),
            Map.entry(STUDENT_GENERATE_CARD,  EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),
            Map.entry(STUDENT_IMPORT,         EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),

            // Users
            Map.entry(USER_VIEW,   EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),
            Map.entry(USER_UPDATE, EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),
            Map.entry(USER_ADD,    EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),
            Map.entry(USER_DELETE, EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),

            // Fines — FINE_VIEW removed; use FINE_VIEW_ALL / FINE_VIEW_OWN
            Map.entry(FINE_VIEW_ALL, EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),
            Map.entry(FINE_VIEW_OWN, EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN, UserRole.STUDENT)),
            Map.entry(FINE_PAY,      EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN, UserRole.STUDENT)),
            Map.entry(FINE_WAIVE,    EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),
            Map.entry(FINE_COLLECT,  EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),

            // Reservations
            Map.entry(RESERVATION_CREATE,   EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN, UserRole.STUDENT)),
            Map.entry(RESERVATION_CANCEL,   EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN, UserRole.STUDENT)),
            Map.entry(RESERVATION_VIEW,     EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN, UserRole.STUDENT)),
            Map.entry(RESERVATION_VIEW_ALL, EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),
            Map.entry(RESERVATION_VIEW_OWN, EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN, UserRole.STUDENT)),

            // Profile
            Map.entry(PROFILE_VIEW, EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN, UserRole.STUDENT)),

            // Acquisitions
            Map.entry(ACQUISITION_REQUEST, EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),
            Map.entry(ACQUISITION_APPROVE, EnumSet.of(UserRole.ADMIN)),

            // Inter-Library Loan
            Map.entry(ILL_MANAGE, EnumSet.of(UserRole.ADMIN, UserRole.LIBRARIAN)),

            // Study room reservations — students create them
            Map.entry(ROOM_RESERVATION_CREATE, EnumSet.of(UserRole.STUDENT)),

            // Notifications — students view their own notifications
            Map.entry(NOTIFICATION_VIEW, EnumSet.of(UserRole.STUDENT))
    );

    public static Set<UserRole> rolesFor(String permission) { return ROLE_MAP.get(permission); }
    public static boolean isKnown(String permission) { return ROLE_MAP.containsKey(permission); }
    public static Set<String> all() { return ROLE_MAP.keySet(); }
}
