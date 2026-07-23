package com.library.service;

import com.library.config.Constants;
import com.library.enums.NotificationType;
import com.library.model.BorrowRecord;
import com.library.notification.NotificationEvent;
import com.library.notification.NotificationPublisher;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRepository;
import com.library.util.DateUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Background job that scans for overdue borrow records and sends
 * reminder notifications to members.
 *
 * <p>Runs on a single daemon thread at a fixed interval defined by
 * {@link Constants#OVERDUE_JOB_INTERVAL_HOURS}. A per-member 24-hour
 * cooldown prevents duplicate reminders within the same day.
 *
 * <p>Requirements: 13.5
 */
public final class OverdueJob {

    private final ScheduledExecutorService scheduler;
    private final BorrowRepository borrowRepo;
    private final BookRepository bookRepo;
    private final NotificationPublisher notificationPublisher;
    private final ConcurrentHashMap<String, Instant> lastNotified = new ConcurrentHashMap<>();

    /**
     * Constructs an {@code OverdueJob} with all required dependencies.
     *
     * @param borrowRepo          repository for querying overdue borrow records
     * @param bookRepo            repository for resolving book titles
     * @param notificationPublisher publisher used to dispatch overdue reminder events
     */
    public OverdueJob(BorrowRepository borrowRepo,
                      BookRepository bookRepo,
                      NotificationPublisher notificationPublisher) {
        this.borrowRepo = Objects.requireNonNull(borrowRepo, "borrowRepo must not be null");
        this.bookRepo = Objects.requireNonNull(bookRepo, "bookRepo must not be null");
        this.notificationPublisher = Objects.requireNonNull(notificationPublisher,
                "notificationPublisher must not be null");
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "overdue-job");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Starts the scheduled job. The first run happens immediately (zero delay),
     * subsequent runs at every {@link Constants#OVERDUE_JOB_INTERVAL_HOURS} hours.
     */
    public void start() {
        scheduler.scheduleAtFixedRate(
                this::runCycle,
                0,
                Constants.OVERDUE_JOB_INTERVAL_HOURS,
                TimeUnit.HOURS);
    }

    /**
     * Stops the scheduled job by shutting down the executor immediately.
     * Safe to call multiple times.
     */
    public void stop() {
        scheduler.shutdownNow();
    }

    /**
     * One execution cycle: scans all overdue records, enforces a 24-hour per-member
     * cooldown, then publishes an {@link NotificationType#OVERDUE_REMINDER} for each
     * eligible member.
     *
     * <p>Any exception thrown inside the cycle is caught and logged to stderr so the
     * scheduled thread is never killed by application errors.
     */
    void runCycle() {
        try {
            Instant cutoff = Instant.now().minus(24, ChronoUnit.HOURS);
            for (BorrowRecord record : borrowRepo.findAllOverdue()) {
                String reg = record.getRegistrationNumber();
                Instant last = lastNotified.get(reg);
                if (last != null && last.isAfter(cutoff)) {
                    continue;
                }
                String title = bookRepo.findById(record.getBookId())
                        .map(com.library.model.Book::getTitle)
                        .orElse("a book");
                notificationPublisher.publish(new NotificationEvent(
                        reg,
                        NotificationType.OVERDUE_ALERT,
                        "Overdue Book",
                        "You have an overdue book: '" + title + "'. Please return it.",
                        DateUtils.now()));
                lastNotified.put(reg, Instant.now());
            }
        } catch (Throwable t) {
            // log but never kill the scheduled thread
            System.err.println("[OverdueJob] Error in runCycle: " + t.getMessage());
        }
    }
}
