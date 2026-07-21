package com.library.repository;

import com.library.config.Constants;
import com.library.mapper.UserMapper;
import com.library.model.User;

import java.nio.file.Path;
import java.util.List;

public final class UserRepository extends JsonRepository<User, String> {
    public UserRepository() {
        super(Path.of(Constants.USERS_FILE), new UserMapper(), User::getId);
    }

    public User findByUsername(String username) {
        return findAll(u -> u.getUsername().equalsIgnoreCase(username)).stream().findFirst().orElse(null);
    }

    public com.library.model.Student findStudentByUsername(String username) {
        return findAll(u -> u instanceof com.library.model.Student s
                && s.getUsername().equalsIgnoreCase(username)).stream()
                .map(u -> (com.library.model.Student) u).findFirst().orElse(null);
    }

    public com.library.model.Student findStudentByRegistrationNumber(String reg) {
        return findAll(u -> u instanceof com.library.model.Student s
                && s.getRegistrationNumber().equalsIgnoreCase(reg)).stream()
                .map(u -> (com.library.model.Student) u).findFirst().orElse(null);
    }

    public java.util.List<com.library.model.Student> findAllStudents() {
        return findAll(u -> u instanceof com.library.model.Student).stream()
                .map(u -> (com.library.model.Student) u).toList();
    }

    public User findByRegistrationNumber(String reg) {
        return findStudentByRegistrationNumber(reg);
    }

    public List<User> findByRole(com.library.enums.UserRole role) {
        return findAll(u -> u.getRole() == role);
    }
}
