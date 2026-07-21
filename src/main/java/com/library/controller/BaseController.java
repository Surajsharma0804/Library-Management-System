package com.library.controller;

import com.library.facade.LibraryFacade;
import com.library.security.AuthorizationManager;
import com.library.security.Session;

/**
 * Base controller providing shared facade access and RBAC enforcement.
 */
public abstract class BaseController {
    protected final LibraryFacade facade;
    private final AuthorizationManager rbac = new AuthorizationManager();
    protected BaseController(LibraryFacade facade) { this.facade = facade; }
    protected void require(Session session, String permission) { rbac.require(session, permission); }
}
