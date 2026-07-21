package com.library.factory;

import com.library.model.Book;
import com.library.repository.CountersRepository;

/**
 * Factory for creating book entities with auto-generated IDs.
 */
public final class BookFactory {

    private final EntityFactory entityFactory;

    public BookFactory(CountersRepository countersRepo) {
        this.entityFactory = new EntityFactory(countersRepo);
    }

    public Book createBook(String isbn, String title, String author, int totalCopies) {
        return entityFactory.createBook(isbn, title, author, totalCopies);
    }
}
