package com.library.search;

import com.library.model.Book;
import java.util.List;
import java.util.Locale;

public final class BookSearchStrategies {
    private BookSearchStrategies() {}
    public static SearchStrategy<Book> byTitle() { return new TitleSearchStrategy(); }
    public static SearchStrategy<Book> byAuthor() { return new AuthorSearchStrategy(); }
    public static SearchStrategy<Book> byIsbn() { return new ISBNSearchStrategy(); }
    public static SearchStrategy<Book> byCategory() { return new CategorySearchStrategy(); }
    public static SearchStrategy<Book> byKeyword() { return new KeywordSearchStrategy(); }
    public static SearchStrategy<Book> byPublisher() {
        return new SearchStrategy<>() {
            public String label() { return "Publisher"; }
            public List<Book> search(List<Book> books, String query) {
                String q = query == null ? "" : query.toLowerCase(Locale.ROOT);
                return books.stream().filter(b -> b.getPublisher() != null && b.getPublisher().toLowerCase(Locale.ROOT).contains(q)).toList();
            }
        };
    }
    public static SearchStrategy<Book> byBarcode() {
        return new SearchStrategy<>() {
            public String label() { return "Barcode"; }
            public List<Book> search(List<Book> books, String query) {
                String q = query == null ? "" : query.toLowerCase(Locale.ROOT);
                return books.stream().filter(b -> b.getBarcode() != null && b.getBarcode().toLowerCase(Locale.ROOT).contains(q)).toList();
            }
        };
    }
    public static SearchStrategy<Book> byRack() {
        return new SearchStrategy<>() {
            public String label() { return "Rack"; }
            public List<Book> search(List<Book> books, String query) {
                String q = query == null ? "" : query.toLowerCase(Locale.ROOT);
                return books.stream().filter(b -> b.getRack() != null && b.getRack().toLowerCase(Locale.ROOT).contains(q)).toList();
            }
        };
    }
    public static SearchStrategy<Book> byShelf() {
        return new SearchStrategy<>() {
            public String label() { return "Shelf"; }
            public List<Book> search(List<Book> books, String query) {
                String q = query == null ? "" : query.toLowerCase(Locale.ROOT);
                return books.stream().filter(b -> b.getShelf() != null && b.getShelf().toLowerCase(Locale.ROOT).contains(q)).toList();
            }
        };
    }
    public static SearchStrategy<Book> byLanguage() {
        return new SearchStrategy<>() {
            public String label() { return "Language"; }
            public List<Book> search(List<Book> books, String query) {
                String q = query == null ? "" : query.toLowerCase(Locale.ROOT);
                return books.stream().filter(b -> b.getLanguage() != null && b.getLanguage().toLowerCase(Locale.ROOT).contains(q)).toList();
            }
        };
    }
    public static SearchStrategy<Book> bySubject() {
        return new SearchStrategy<>() {
            public String label() { return "Subject"; }
            public List<Book> search(List<Book> books, String query) {
                String q = query == null ? "" : query.toLowerCase(Locale.ROOT);
                return books.stream().filter(b -> b.getSubject() != null && b.getSubject().toLowerCase(Locale.ROOT).contains(q)).toList();
            }
        };
    }
}
