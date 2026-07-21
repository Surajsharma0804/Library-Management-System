package com.library.exception;

public class PersistenceException extends LibraryException {
    private static final long serialVersionUID = 1L;
    public PersistenceException(String message) { super(message); }
    public PersistenceException(String message, Throwable cause) { super(message, cause); }
}
