package com.library.api;

import com.library.facade.LibraryFacade;
import com.library.security.Session;
import io.javalin.http.Context;

import java.util.Map;

/**
 * Handles authentication lifecycle — login, logout, and password management.
 * Issues Bearer tokens for session-based API access.
 */
public final class RestAuthController extends BaseRestController {

    public RestAuthController(LibraryFacade facade) {
        super(facade);
    }

    /** POST /api/login — Authenticates user and returns a Bearer token. */
    public void login(Context ctx) {
        var body = ctx.bodyAsClass(LoginRequest.class);
        if (body.username == null || body.password == null) {
            ctx.status(400).json(Map.of("error", "Username and password are required"));
            return;
        }
        String token = facade.auth().login(body.username, body.password);
        Session session = facade.sessions().require(token);
        ctx.json(Map.of(
                "token", token,
                "username", session.username(),
                "role", session.role().name(),
                "userId", session.userId()
        ));
    }

    /** POST /api/logout — Invalidates the current session. */
    public void logout(Context ctx) {
        String token = requireToken(ctx);
        facade.auth().logout(token);
        ctx.json(Map.of("message", "Logged out"));
    }

    /** POST /api/change-password — Changes the authenticated user's password. */
    public void changePassword(Context ctx) {
        String token = requireToken(ctx);
        var body = ctx.bodyAsClass(PasswordChangeRequest.class);
        if (body.oldPassword == null || body.newPassword == null) {
            ctx.status(400).json(Map.of("error", "Both old and new passwords are required"));
            return;
        }
        facade.auth().changePassword(token, body.oldPassword, body.newPassword);
        ctx.json(Map.of("message", "Password changed successfully"));
    }

    // ── Request DTOs ────────────────────────────────────────────────
    public static class LoginRequest {
        public String username;
        public String password;
    }

    public static class PasswordChangeRequest {
        public String oldPassword;
        public String newPassword;
    }
}
