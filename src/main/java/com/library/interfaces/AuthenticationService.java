package com.library.interfaces;

import com.library.security.Session;

/**
 * Authentication service contract.
 */
public interface AuthenticationService {
    String login(String username, String password);
    void logout(String token);
    Session currentSession(String token);
}
