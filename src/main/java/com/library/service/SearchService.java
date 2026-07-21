package com.library.service;

import com.library.model.Book;
import com.library.repository.BookRepository;
import com.library.search.BookSearchEngine;
import com.library.search.BookSearchStrategies;
import com.library.search.SearchStrategy;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public final class SearchService {

    private final BookRepository bookRepo;

    public SearchService(BookRepository bookRepo) {
        this.bookRepo = bookRepo;
    }

    private BookSearchEngine engine() {
        return new BookSearchEngine(bookRepo.findAll());
    }

    public List<Book> searchByTitle(String title) {
        return engine().search(BookSearchStrategies.byTitle(), title);
    }

    public List<Book> searchByAuthor(String author) {
        return engine().search(BookSearchStrategies.byAuthor(), author);
    }

    public List<Book> searchByIsbn(String isbn) {
        return engine().search(BookSearchStrategies.byIsbn(), isbn);
    }

    public List<Book> searchByCategory(String category) {
        return engine().search(BookSearchStrategies.byCategory(), category);
    }

    public List<Book> searchByPublisher(String publisher) {
        return engine().search(BookSearchStrategies.byPublisher(), publisher);
    }

    public List<Book> searchByLanguage(String language) {
        return engine().search(BookSearchStrategies.byLanguage(), language);
    }

    public List<Book> searchByKeyword(String keyword) {
        return engine().search(BookSearchStrategies.byKeyword(), keyword);
    }

    public List<Book> searchWithSort(SearchStrategy strategy, String query, Comparator<Book> sorter) {
        return engine().searchAndSort(strategy, query, sorter);
    }

    public List<Book> filter(Predicate<Book> predicate) {
        return bookRepo.findAll(predicate);
    }

    public List<SearchStrategy> availableStrategies() {
        return List.of(
                BookSearchStrategies.byTitle(),
                BookSearchStrategies.byAuthor(),
                BookSearchStrategies.byIsbn(),
                BookSearchStrategies.byCategory(),
                BookSearchStrategies.byPublisher(),
                BookSearchStrategies.byKeyword()
        );
    }
}
