package com.library.builder;

import com.library.model.Administrator;
import com.library.model.Librarian;
import com.library.model.Student;

public final class UserBuilder {
    public static Student.Builder student() { return Student.builder(); }
    public static Librarian.Builder librarian() { return Librarian.builder(); }
    public static Administrator.Builder administrator() { return Administrator.builder(); }
}
