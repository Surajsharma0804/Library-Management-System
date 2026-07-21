package com.library.factory;

import com.library.model.Student;
import com.library.repository.CountersRepository;

import java.util.Set;

/**
 * Factory for creating user entities (students, librarians, admins)
 * with auto-generated IDs.
 */
public final class UserFactory {

    private final EntityFactory entityFactory;

    public UserFactory(CountersRepository countersRepo) {
        this.entityFactory = new EntityFactory(countersRepo);
    }

    public Student createStudent(String firstName, String lastName, String email,
                                  String phone, String department, String course,
                                  int semester, String section) {
        return entityFactory.createStudent(firstName, lastName, email, phone,
                department, course, semester, section);
    }

    public com.library.model.Librarian createLibrarian(String firstName, String lastName,
                                                                  String email, String phone,
                                                                  String username, String password,
                                                                  Set<String> permissions) {
        return entityFactory.createLibrarian(firstName, lastName, email, phone,
                username, password, permissions);
    }

    public com.library.model.Administrator createAdmin(String firstName, String lastName,
                                                           String email, String phone,
                                                           String username, String password) {
        return entityFactory.createAdmin(firstName, lastName, email, phone,
                username, password);
    }
}
