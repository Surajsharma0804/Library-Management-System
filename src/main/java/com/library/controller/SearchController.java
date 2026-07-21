package com.library.controller;

import com.library.facade.LibraryFacade;
import com.library.model.Book;
import com.library.security.Session;
import com.library.search.BookSearchEngine;
import com.library.search.SearchCriteria;
import com.library.search.SearchResult;

import java.util.List;

public final class SearchController extends BaseController {
    public SearchController(LibraryFacade facade) { super(facade); }

    public SearchResult<Book> search(Session session, SearchCriteria criteria) {
        List<Book> books = facade.bookRepo().findAll();
        BookSearchEngine engine = new BookSearchEngine(books);
        return engine.search(criteria);
    }
}
