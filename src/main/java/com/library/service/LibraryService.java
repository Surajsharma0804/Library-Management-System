package com.library.service;

import com.library.config.LibraryConfiguration;
import com.library.model.LibraryConfig;

/**
 * High-level facade for library operations combining multiple services.
 */
public final class LibraryService {
    private final BookService bookService;
    private final BorrowService borrowService;
    private final ReservationService reservationService;
    private final FineService fineService;
    private final ConfigService configService;

    public LibraryService(BookService bookService, BorrowService borrowService,
                           ReservationService reservationService, FineService fineService,
                           ConfigService configService) {
        this.bookService = bookService;
        this.borrowService = borrowService;
        this.reservationService = reservationService;
        this.fineService = fineService;
        this.configService = configService;
    }

    public LibraryConfig getConfig() { return configService.get(); }
    public String systemName() { return LibraryConfiguration.appName(); }
}
