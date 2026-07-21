package com.library.exception;

public class DuplicateUserException extends ValidationException {
    private static final long serialVersionUID = 1L;
    public DuplicateUserException(String message) { super(message); }
}
