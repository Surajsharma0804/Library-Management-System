package com.library.repository;

import com.library.config.Constants;
import com.library.mapper.UserMapper;
import com.library.model.User;
import com.library.model.Librarian;

import java.nio.file.Path;
import java.util.List;

/**
 * Repository for staff users (admins and librarians).
 */
public final class StaffRepository extends JsonRepository<User, String> {
    public StaffRepository() {
        super(Path.of(Constants.LIBRARIANS_FILE), new UserMapper(), User::getId);
    }

    public User findByUsername(String username) {
        return findAll(u -> u.getUsername().equalsIgnoreCase(username)).stream().findFirst().orElse(null);
    }

    public List<Librarian> findAllLibrarians() {
        return findAll(u -> u instanceof Librarian).stream().map(u -> (Librarian) u).toList();
    }
}
