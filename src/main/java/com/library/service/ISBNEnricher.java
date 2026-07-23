package com.library.service;

import com.library.dto.BookDTO;
import com.library.util.JsonUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Enriches book metadata from the Open Library Books API using an ISBN.
 *
 * <p>Both synchronous ({@link #enrich(String)}) and asynchronous
 * ({@link #enrichAsync(String)}) variants are provided. Neither method
 * ever throws a checked or unchecked exception; failures are logged to
 * {@code System.err} and an empty {@link BookDTO} is returned instead.
 *
 * <p>Requirements: 29.1
 */
public final class ISBNEnricher {

    private static final String OPEN_LIBRARY_URL =
            "https://openlibrary.org/api/books?bibkeys=ISBN:%s&format=json&jscmd=data";

    private final HttpClient httpClient;

    /**
     * Creates an {@code ISBNEnricher} with a default 5-second connect timeout.
     */
    public ISBNEnricher() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * Asynchronously fetches and parses book metadata for the given ISBN.
     *
     * <p>The returned {@link CompletableFuture} never completes exceptionally;
     * any error returns an empty {@link BookDTO}.
     *
     * @param isbn the ISBN-10 or ISBN-13 to look up; {@code null} or blank returns empty
     * @return a future that resolves to a populated or empty {@link BookDTO}
     */
    public CompletableFuture<BookDTO> enrichAsync(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            return CompletableFuture.completedFuture(emptyBookDTO());
        }
        String url = String.format(OPEN_LIBRARY_URL, isbn.trim());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(resp -> parseResponse(isbn, resp.body()))
                .exceptionally(ex -> {
                    logError(isbn, ex);
                    return emptyBookDTO();
                });
    }

    /**
     * Synchronously fetches and parses book metadata for the given ISBN,
     * waiting up to 5 seconds for a response.
     *
     * <p>Never throws; returns an empty {@link BookDTO} on any error.
     *
     * @param isbn the ISBN-10 or ISBN-13 to look up
     * @return a populated or empty {@link BookDTO}
     */
    public BookDTO enrich(String isbn) {
        try {
            return enrichAsync(isbn).get(5, TimeUnit.SECONDS);
        } catch (Exception ex) {
            logError(isbn, ex);
            return emptyBookDTO();
        }
    }

    // -------------------------------------------------------------------------
    // Internals
    // -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private BookDTO parseResponse(String isbn, String body) {
        try {
            // Open Library returns: {"ISBN:XXXXXXX": { "title": "...", "authors": [...], ... }}
            if (body == null || body.isBlank() || body.equals("{}")) {
                return emptyBookDTO();
            }
            Object parsed = JsonUtils.parse(body);
            if (!(parsed instanceof Map<?, ?> rawOuter) || rawOuter.isEmpty()) {
                return emptyBookDTO();
            }
            Map<String, Object> outer = (Map<String, Object>) rawOuter;
            Object firstValue = outer.values().iterator().next();
            if (!(firstValue instanceof Map<?, ?> rawData)) {
                return emptyBookDTO();
            }
            Map<String, Object> data = (Map<String, Object>) rawData;

            String title = Objects.toString(data.getOrDefault("title", ""), "");

            // Authors: List<Map> with "name" key
            String author = "";
            Object authorsObj = data.get("authors");
            if (authorsObj instanceof List<?> authorsList && !authorsList.isEmpty()) {
                Object first = authorsList.get(0);
                if (first instanceof Map<?, ?> authorMap) {
                    Object nameVal = authorMap.get("name");
                    author = nameVal != null ? String.valueOf(nameVal) : "";
                }
            }

            // Publishers: List<Map> with "name" key
            String publisher = "";
            Object pubObj = data.get("publishers");
            if (pubObj instanceof List<?> pubs && !pubs.isEmpty()) {
                Object first = pubs.get(0);
                if (first instanceof Map<?, ?> pubMap) {
                    Object nameVal = pubMap.get("name");
                    publisher = nameVal != null ? String.valueOf(nameVal) : "";
                }
            }

            // Publish date — last 4 chars treated as year
            int year = 0;
            Object pubDate = data.get("publish_date");
            if (pubDate instanceof String s && s.length() >= 4) {
                try {
                    year = Integer.parseInt(s.substring(s.length() - 4));
                } catch (NumberFormatException ignored) {
                    // leave year as 0
                }
            }

            BookDTO dto = new BookDTO();
            dto.setIsbn(isbn);
            dto.setTitle(title);
            dto.setAuthor(author);
            dto.setPublisher(publisher);
            dto.setPublicationYear(year);
            return dto;

        } catch (Exception ex) {
            logError(isbn, ex);
            return emptyBookDTO();
        }
    }

    /**
     * Returns a {@link BookDTO} with all fields at their default values.
     */
    private BookDTO emptyBookDTO() {
        return new BookDTO();
    }

    private void logError(String isbn, Throwable ex) {
        System.err.println("[ISBNEnricher] Failed for ISBN " + isbn + ": " + ex.getMessage());
    }
}
