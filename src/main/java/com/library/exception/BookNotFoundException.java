package com.library.exception;

public class BookNotFoundException extends LibraryException {
    private static final long serialVersionUID = 1L;
    public BookNotFoundException(String message) { super(message); }
}
