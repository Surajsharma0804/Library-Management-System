package com.library.exception;

public class InvalidRegistrationException extends LibraryException {
    private static final long serialVersionUID = 1L;
    public InvalidRegistrationException(String message) { super(message); }
}
