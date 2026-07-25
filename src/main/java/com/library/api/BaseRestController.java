package com.library.api;

import com.library.facade.LibraryFacade;
import com.library.security.Session;
import io.javalin.http.Context;

/**
 * Base class for REST controllers — provides helper methods
 * for extracting the auth token and resolving the session.
 */
public abstract class BaseRestController {

    protected final LibraryFacade facade;

    protected BaseRestController(LibraryFacade facade) {
        this.facade = facade;
    }

    /**
     * Extracts the session from the Authorization header (Bearer token).
     * Throws 401 if the token is missing or invalid.
     */
    protected Session requireSession(Context ctx) {
        String header = ctx.header("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            ctx.status(401);
            throw new com.library.exception.UnauthorizedAccessException("Authentication required");
        }
        String token = header.substring(7);
        return facade.sessions().require(token);
    }

    /** Extracts the raw token string from the Authorization header. */
    protected String requireToken(Context ctx) {
        String header = ctx.header("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            ctx.status(401);
            throw new com.library.exception.UnauthorizedAccessException("Authentication required");
        }
        return header.substring(7);
    }
}
