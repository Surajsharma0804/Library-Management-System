package com.library.controller;

import com.library.facade.LibraryFacade;
import com.library.security.Session;

/**
 * Controller for authentication operations: login, logout, and session lookup.
 */
public final class AuthController {
    private final LibraryFacade facade;
    public AuthController(LibraryFacade facade) { this.facade = facade; }
    public String login(String username, String password) { return facade.auth().login(username, password); }
    public void logout(String token) { facade.auth().logout(token); }
    public Session currentSession(String token) { return facade.auth().currentSession(token); }
}
