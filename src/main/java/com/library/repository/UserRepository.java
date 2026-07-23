package com.library.repository;

import com.library.config.Constants;
import com.library.mapper.UserMapper;
import com.library.model.Student;
import com.library.model.User;

import java.nio.file.Path;
import java.util.List;

public final class UserRepository extends IndexedRepository<User, String> {

    public UserRepository() {
        super(Path.of(Constants.USERS_FILE), new UserMapper(), User::getId);
        registerSecondaryIndex("registrationNumber");
        registerSecondaryIndex("email");
    }

    @Override
    protected String secondaryKey(String indexName, User entity) {
        return switch (indexName) {
            case "registrationNumber" -> entity instanceof Student s ? s.getRegistrationNumber() : null;
            case "email"              -> entity.getEmail();
            default                   -> null;
        };
    }

    public User findByUsername(String username) {
        return findAll(u -> u.getUsername().equalsIgnoreCase(username)).stream().findFirst().orElse(null);
    }

    public Student findStudentByUsername(String username) {
        return findAll(u -> u instanceof Student s
                && s.getUsername().equalsIgnoreCase(username)).stream()
                .map(u -> (Student) u).findFirst().orElse(null);
    }

    public Student findStudentByRegistrationNumber(String reg) {
        User found = findBySecondaryKey("registrationNumber", reg).orElse(null);
        return found instanceof Student s ? s : null;
    }

    public List<Student> findAllStudents() {
        return findAll(u -> u instanceof Student).stream()
                .map(u -> (Student) u).toList();
    }

    public User findByRegistrationNumber(String reg) {
        return findStudentByRegistrationNumber(reg);
    }

    public List<User> findByRole(com.library.enums.UserRole role) {
        return findAll(u -> u.getRole() == role);
    }
}
