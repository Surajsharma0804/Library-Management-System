package com.library;

import com.library.enums.MembershipStatus;
import com.library.model.Student;
import com.library.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Student service tests")
class StudentServiceTest {

    @TempDir
    Path tempDir;

    private UserRepository createRepo() {
        UserRepository repo = new UserRepository();
        repo.setOverrideFile(tempDir.resolve("students.json"));
        return repo;
    }

    @Test
    @DisplayName("Save and find student by id")
    void saveAndFindById() {
        UserRepository repo = createRepo();
        Student student = Student.builder().id("ST-1").firstName("John").lastName("Doe")
                .email("john@test.com").phone("1234567890")
                .registrationNumber("REG-001").libraryCardNumber("LIB-001")
                .membershipStatus(MembershipStatus.ACTIVE)
                .joiningDate(LocalDate.now())
                .membershipExpiry(LocalDate.now().plusMonths(12))
                .username("student1").passwordHash("hash").build();
        repo.save(student);
        Optional<com.library.model.User> found = repo.findById("ST-1");
        assertTrue(found.isPresent());
        assertEquals("John", found.get().getFirstName());
    }

    @Test
    @DisplayName("Find by registration number")
    void findByRegistrationNumber() {
        UserRepository repo = createRepo();
        Student student = Student.builder().id("ST-1").firstName("Jane").lastName("Smith")
                .email("jane@test.com").phone("1234567890")
                .registrationNumber("REG-002").libraryCardNumber("LIB-002")
                .membershipStatus(MembershipStatus.ACTIVE)
                .joiningDate(LocalDate.now())
                .membershipExpiry(LocalDate.now().plusMonths(12))
                .username("student1").passwordHash("hash").build();
        repo.save(student);
        assertNotNull(repo.findByRegistrationNumber("REG-002"));
    }
}
