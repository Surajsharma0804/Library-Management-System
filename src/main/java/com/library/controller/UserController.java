package com.library.controller;

import com.library.facade.LibraryFacade;
import com.library.security.Permissions;
import com.library.security.Session;
import com.library.model.User;

import java.util.List;

public final class UserController extends BaseController {
    public UserController(LibraryFacade facade) { super(facade); }

    public List<User> listUsers(Session session) {
        require(session, Permissions.USER_VIEW);
        return facade.userRepo().findAll();
    }

    public User getUser(Session session, String id) {
        require(session, Permissions.USER_VIEW);
        return facade.userRepo().findById(id).orElse(null);
    }

    public void activateUser(Session session, String userId) {
        require(session, Permissions.USER_UPDATE);
        User u = facade.userRepo().findById(userId).orElse(null);
        if (u != null) { u.setActive(true); facade.userRepo().save(u); }
    }

    public void deactivateUser(Session session, String userId) {
        require(session, Permissions.USER_UPDATE);
        User u = facade.userRepo().findById(userId).orElse(null);
        if (u != null) { u.setActive(false); facade.userRepo().save(u); }
    }
}
