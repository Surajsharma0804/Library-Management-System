package com.library.search;

import com.library.model.Book;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class BookSearchEngine {
    private final List<Book> books;

    public BookSearchEngine(List<Book> books) {
        this.books = books;
    }

    public List<Book> searchAndSort(SearchStrategy<Book> strategy, String query, Comparator<Book> sorter) {
        List<Book> result = new ArrayList<>(strategy.search(books, query));
        result.sort(sorter);
        return result;
    }

    public List<Book> search(SearchStrategy<Book> strategy, String query) {
        return new ArrayList<>(strategy.search(books, query));
    }

    public SearchResult<Book> search(SearchCriteria criteria) {
        long start = System.nanoTime();
        SearchStrategy<Book> strategy = strategyFor(criteria.field());
        List<Book> results = new ArrayList<>(strategy.search(books, criteria.query()));
        if (criteria.sortBy() != null && !criteria.sortBy().isBlank()) {
            results.sort(comparatorFor(criteria.sortBy(), criteria.ascending()));
        }
        long elapsed = (System.nanoTime() - start) / 1_000_000;
        return new SearchResult<>(results, results.size(), elapsed);
    }

    private SearchStrategy<Book> strategyFor(String field) {
        if (field == null) return new KeywordSearchStrategy();
        return switch (field.toLowerCase()) {
            case "title" -> new TitleSearchStrategy();
            case "author" -> new AuthorSearchStrategy();
            case "isbn" -> new ISBNSearchStrategy();
            case "category" -> new CategorySearchStrategy();
            case "keyword" -> new KeywordSearchStrategy();
            default -> new KeywordSearchStrategy();
        };
    }

    private Comparator<Book> comparatorFor(String sortBy, boolean ascending) {
        Comparator<Book> cmp = switch (sortBy.toLowerCase()) {
            case "title" -> Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER);
            case "author" -> Comparator.comparing(Book::getAuthor, String.CASE_INSENSITIVE_ORDER);
            case "year" -> Comparator.comparingInt(Book::getPublicationYear);
            default -> Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER);
        };
        return ascending ? cmp : cmp.reversed();
    }
}
