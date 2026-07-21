package com.library.exception;

public class MembershipExpiredException extends LibraryException {
    private static final long serialVersionUID = 1L;
    public MembershipExpiredException(String message) { super(message); }
}
