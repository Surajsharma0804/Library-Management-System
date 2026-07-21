package com.library.search;

import com.library.model.Student;
import java.util.List;
import java.util.Locale;

public final class RegistrationSearchStrategy implements SearchStrategy<Student> {
    @Override
    public String label() { return "Registration"; }

    @Override
    public List<Student> search(List<Student> students, String query) {
        String q = query == null ? "" : query.toLowerCase(Locale.ROOT);
        if (q.isEmpty()) return students;
        return students.stream().filter(s ->
            (s.getRegistrationNumber() != null && s.getRegistrationNumber().toLowerCase(Locale.ROOT).contains(q)) ||
            (s.getLibraryCardNumber() != null && s.getLibraryCardNumber().toLowerCase(Locale.ROOT).contains(q))
        ).toList();
    }
}
