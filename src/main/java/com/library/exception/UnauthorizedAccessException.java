package com.library.exception;

public class UnauthorizedAccessException extends LibraryException {
    private static final long serialVersionUID = 1L;
    public UnauthorizedAccessException(String message) { super(message); }
}
