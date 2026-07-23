package com.library.facade;

import com.library.config.Constants;
import com.library.mapper.*;
import com.library.model.*;
import com.library.persistence.JsonStorageBackend;
import com.library.persistence.SqliteStorageBackend;
import com.library.repository.*;
import com.library.security.*;
import com.library.factory.EntityFactory;
import com.library.service.*;
import com.library.notification.NotificationPublisher;

/**
 * Central facade that wires all repositories and services together.
 * GUI controllers obtain service references exclusively via this class.
 */
public final class LibraryFacade {

    // ---------------------------------------------------------------- Repositories
    private final UserRepository userRepo;
    private final StaffRepository staffRepo;
    private final BookRepository bookRepo;
    private final BorrowRepository borrowRepo;
    private final ReservationRepository reservationRepo;
    private final FineRepository fineRepo;
    private final NotificationRepository notificationRepo;
    private final LibraryConfigRepository configRepo;
    private final CountersRepository countersRepo;
    private final AuditRepository auditRepo;
    private final MembershipTierRepository membershipTierRepo;
    private final LostBookRepository lostBookRepo;
    private final ReadingListRepository readingListRepo;
    private final AcquisitionRepository acquisitionRepo;
    private final BranchRepository branchRepo;
    private final StudyRoomRepository studyRoomRepo;
    private final RoomReservationRepository roomReservationRepo;
    private final ILLRepository illRepo;

    // ----------------------------------------------------------------- Services
    private final EntityFactory factory;
    private final AuthenticationService authService;
    private final UserService userService;
    private final BookService bookService;
    private final BorrowService borrowService;
    private final ReservationService reservationService;
    private final FineService fineService;
    private final SearchService searchService;
    private final NotificationService notificationService;
    private final AnalyticsService analyticsService;
    private final ReportService reportService;
    private final BackupService backupService;
    private final AuditService auditService;
    private final DashboardService dashboardService;
    private final ConfigService configService;
    private final MembershipTierService membershipTierService;
    private final LostBookService lostBookService;
    private final ReadingListService readingListService;
    private final RecommendationEngine recommendationEngine;
    private final AcquisitionService acquisitionService;
    private final BranchService branchService;
    private final RoomReservationService roomReservationService;
    private final ILLService illService;
    private final StudentImportService studentImportService;
    private final ISBNEnricher isbnEnricher;
    private final SqliteMigrationService sqliteMigrationService;

    // --------------------------------------------------------------- Security
    private final SessionManager sessionManager;
    private final AuthenticationManager authManager;
    private final AuthorizationManager rbac;
    private final NotificationPublisher notificationPublisher;

    public LibraryFacade() {
        // Repositories
        this.userRepo = new UserRepository();
        this.staffRepo = new StaffRepository();
        this.bookRepo = new BookRepository();
        this.borrowRepo = new BorrowRepository();
        this.reservationRepo = new ReservationRepository();
        this.fineRepo = new FineRepository();
        this.notificationRepo = new NotificationRepository();
        this.configRepo = new LibraryConfigRepository();
        this.countersRepo = new CountersRepository();
        this.auditRepo = new AuditRepository();
        this.membershipTierRepo = new MembershipTierRepository();
        this.lostBookRepo = new LostBookRepository();
        this.readingListRepo = new ReadingListRepository();
        this.acquisitionRepo = new AcquisitionRepository();
        this.branchRepo = new BranchRepository();
        this.studyRoomRepo = new StudyRoomRepository();
        this.roomReservationRepo = new RoomReservationRepository();
        this.illRepo = new ILLRepository();

        // Security
        this.sessionManager = new SessionManager();
        this.rbac = new AuthorizationManager();
        this.notificationPublisher = new NotificationPublisher(notificationRepo);

        // Factory
        this.factory = new EntityFactory(countersRepo);

        // Services (order matters — dependencies first)
        this.auditService = new AuditService(auditRepo, factory);
        this.authService = new AuthenticationService(staffRepo, userRepo, sessionManager);
        this.userService = new UserService(staffRepo, sessionManager, rbac, auditService);
        this.bookService = new BookService(bookRepo, factory, auditService);
        this.fineService = new FineService(fineRepo, userRepo, factory, auditService, notificationPublisher);
        this.membershipTierService = new MembershipTierService(membershipTierRepo, userRepo, rbac, auditService);
        this.lostBookService = new LostBookService(lostBookRepo, borrowRepo, bookRepo, fineService, auditService);
        this.borrowService = new BorrowService(bookRepo, userRepo, borrowRepo, reservationRepo,
                configRepo, factory, auditService, notificationPublisher, fineService,
                membershipTierService, lostBookService);
        this.reservationService = new ReservationService(reservationRepo, bookRepo, userRepo,
                configRepo, factory, auditService, notificationPublisher);
        this.searchService = new SearchService(bookRepo);
        this.notificationService = new NotificationService(notificationPublisher);
        this.analyticsService = new AnalyticsService(bookRepo, userRepo, borrowRepo, fineRepo);
        this.reportService = new ReportService(auditService);
        this.backupService = new BackupService(auditService);
        this.dashboardService = new DashboardService(bookRepo, userRepo, staffRepo, borrowRepo,
                reservationRepo, fineRepo);
        this.configService = new ConfigService(configRepo);

        this.authManager = new AuthenticationManager(sessionManager);

        // New enterprise services
        this.readingListService = new ReadingListService(readingListRepo, bookRepo);
        this.recommendationEngine = new RecommendationEngine(borrowRepo, bookRepo);
        this.acquisitionService = new AcquisitionService(acquisitionRepo, rbac, auditService);
        this.branchService = new BranchService(branchRepo, rbac, auditService);
        this.roomReservationService = new RoomReservationService(studyRoomRepo, roomReservationRepo, rbac);
        this.illService = new ILLService(illRepo, rbac, auditService);
        this.studentImportService = new StudentImportService(userRepo, factory, rbac, auditService);
        this.isbnEnricher = new ISBNEnricher();
        this.sqliteMigrationService = new SqliteMigrationService(
                new JsonStorageBackend(),
                new SqliteStorageBackend(Constants.SQLITE_DB_FILE),
                configRepo,
                auditService);
    }

    // --------------------------------------------------------------- Service accessors
    public AuthenticationService auth() { return authService; }
    public UserService users() { return userService; }
    public UserService librarians() { return userService; }
    public BookService books() { return bookService; }
    public BorrowService borrows() { return borrowService; }
    public ReservationService reservations() { return reservationService; }
    public FineService fines() { return fineService; }
    public SearchService search() { return searchService; }
    public NotificationService notifications() { return notificationService; }
    public AnalyticsService analytics() { return analyticsService; }
    public ReportService reports() { return reportService; }
    public BackupService backup() { return backupService; }
    public AuditService audit() { return auditService; }
    public DashboardService dashboard() { return dashboardService; }
    public ConfigService config() { return configService; }
    public MembershipTierService membershipTiers() { return membershipTierService; }
    public LostBookService lostBooks() { return lostBookService; }
    public ReadingListService readingLists() { return readingListService; }
    public RecommendationEngine recommendations() { return recommendationEngine; }
    public AcquisitionService acquisitions() { return acquisitionService; }
    public BranchService branches() { return branchService; }
    public RoomReservationService roomReservations() { return roomReservationService; }
    public ILLService ill() { return illService; }
    public StudentImportService studentImport() { return studentImportService; }
    public ISBNEnricher isbnEnricher() { return isbnEnricher; }
    public SqliteMigrationService sqliteMigration() { return sqliteMigrationService; }

    // ------------------------------------------------------------- Security accessors
    public SessionManager sessions() { return sessionManager; }
    public AuthenticationManager authManager() { return authManager; }
    public AuthorizationManager rbac() { return rbac; }
    public NotificationPublisher notificationPublisher() { return notificationPublisher; }

    // ---------------------------------------------------------- Repository accessors
    public UserRepository userRepo() { return userRepo; }
    public StaffRepository staffRepo() { return staffRepo; }
    public BookRepository bookRepo() { return bookRepo; }
    public BorrowRepository borrowRepo() { return borrowRepo; }
    public ReservationRepository reservationRepo() { return reservationRepo; }
    public FineRepository fineRepo() { return fineRepo; }
    public NotificationRepository notificationRepo() { return notificationRepo; }
    public LibraryConfigRepository configRepo() { return configRepo; }
    public CountersRepository countersRepo() { return countersRepo; }
    public AuditRepository auditRepo() { return auditRepo; }
    public EntityFactory factory() { return factory; }
    public MembershipTierRepository membershipTierRepo() { return membershipTierRepo; }
    public LostBookRepository lostBookRepo() { return lostBookRepo; }
    public ReadingListRepository readingListRepo() { return readingListRepo; }
    public AcquisitionRepository acquisitionRepo() { return acquisitionRepo; }
    public BranchRepository branchRepo() { return branchRepo; }
    public StudyRoomRepository studyRoomRepo() { return studyRoomRepo; }
    public RoomReservationRepository roomReservationRepo() { return roomReservationRepo; }
    public ILLRepository illRepo() { return illRepo; }
}
