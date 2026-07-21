package com.library.reports;

import com.library.model.Book;
import com.library.repository.BookRepository;

import java.util.List;

/**
 * Report strategy for inventory summary.
 */
public final class InventoryReport implements ReportStrategy {

    private final BookRepository bookRepo;

    public InventoryReport(BookRepository bookRepo) {
        this.bookRepo = bookRepo;
    }

    @Override
    public String id() {
        return "inventory";
    }

    @Override
    public String title() {
        return "Book Inventory Report";
    }

    @Override
    public ReportData generate() {
        var books = bookRepo.findAll();
        int total = books.size();
        int available = books.stream().mapToInt(Book::getAvailableQuantity).sum();
        int borrowed = books.stream().mapToInt(Book::getBorrowedQuantity).sum();
        int reserved = books.stream().mapToInt(Book::getReservedQuantity).sum();

        return new ReportData(
                title(),
                List.of("Metric", "Count"),
                List.of(
                        List.of("Total Books", String.valueOf(total)),
                        List.of("Available Copies", String.valueOf(available)),
                        List.of("Borrowed Copies", String.valueOf(borrowed)),
                        List.of("Reserved Copies", String.valueOf(reserved))
                ),
                "Total titles: " + total + " | Total copies: " + (available + borrowed + reserved)
        );
    }
}
