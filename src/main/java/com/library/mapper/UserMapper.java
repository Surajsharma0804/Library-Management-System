package com.library.mapper;

import com.library.enums.MembershipStatus;
import com.library.enums.UserRole;
import com.library.interfaces.JsonMappable;
import com.library.model.Administrator;
import com.library.model.Librarian;
import com.library.model.Student;
import com.library.model.User;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class UserMapper implements JsonMappable<User> {
    @Override
    public Map<String, Object> toMap(User u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId());
        m.put("username", u.getUsername());
        m.put("firstName", u.getFirstName());
        m.put("middleName", u.getMiddleName());
        m.put("lastName", u.getLastName());
        m.put("email", u.getEmail());
        m.put("phone", u.getPhone());
        m.put("passwordHash", u.getPasswordHash());
        m.put("active", u.isActive());
        m.put("role", u.getRole().name());
        if (u instanceof Librarian lib) m.put("permissions", lib.getPermissions());
        if (u instanceof Student s) {
            m.put("registrationNumber", s.getRegistrationNumber());
            m.put("libraryCardNumber", s.getLibraryCardNumber());
            m.put("department", s.getDepartment());
            m.put("course", s.getCourse());
            m.put("semester", s.getSemester());
            m.put("section", s.getSection());
            m.put("borrowLimit", s.getBorrowLimit());
            m.put("membershipStatus", s.getMembershipStatus().name());
            m.put("joiningDate", s.getJoiningDate() != null ? s.getJoiningDate().toString() : null);
            m.put("membershipExpiry", s.getMembershipExpiry() != null ? s.getMembershipExpiry().toString() : null);
            m.put("fineBalancePaise", s.getFineBalancePaise());
            m.put("borrowCount", s.getCurrentBorrowCount());
            m.put("homeBranchId", s.getHomeBranchId());
            m.put("membershipTierId", s.getMembershipTierId());
            m.put("program", s.getProgram());
        }
        return m;
    }

    @Override
    public User fromMap(Map<String, Object> m) {
        UserRole role = UserRole.valueOf((String) m.get("role"));
        String id = (String) m.get("id");
        String username = (String) m.get("username");
        String firstName = (String) m.get("firstName");
        String lastName = (String) m.get("lastName");
        String email = (String) m.get("email");
        String phone = (String) m.get("phone");
        String passwordHash = (String) m.get("passwordHash");
        boolean active = m.get("active") == null || Boolean.TRUE.equals(m.get("active"));
        return switch (role) {
            case ADMIN -> Administrator.builder().id(id).username(username).firstName(firstName)
                    .lastName(lastName).email(email).phone(phone).passwordHash(passwordHash).active(active).build();
            case LIBRARIAN -> {
                Set<String> perms = m.get("permissions") instanceof Set<?> s
                        ? new HashSet<>((Set<String>) s) : new HashSet<>();
                yield Librarian.builder().id(id).username(username).firstName(firstName)
                        .lastName(lastName).email(email).phone(phone).passwordHash(passwordHash)
                        .active(active).permissions(perms).build();
            }
            case STUDENT -> Student.builder().id(id).username(username).firstName(firstName)
                    .lastName(lastName).email(email).phone(phone).passwordHash(passwordHash)
                    .active(active)
                    .registrationNumber((String) m.getOrDefault("registrationNumber", ""))
                    .libraryCardNumber((String) m.getOrDefault("libraryCardNumber", ""))
                    .department((String) m.getOrDefault("department", ""))
                    .course((String) m.getOrDefault("course", ""))
                    .semester(m.get("semester") instanceof Number n ? n.intValue() : 0)
                    .section((String) m.getOrDefault("section", ""))
                    .borrowLimit(m.get("borrowLimit") instanceof Number n ? n.intValue() : 5)
                    .membershipStatus(m.get("membershipStatus") != null
                            ? MembershipStatus.valueOf((String) m.get("membershipStatus"))
                            : MembershipStatus.ACTIVE)
                    .joiningDate(m.get("joiningDate") != null
                            ? LocalDate.parse((String) m.get("joiningDate")) : null)
                    .membershipExpiry(m.get("membershipExpiry") != null
                            ? LocalDate.parse((String) m.get("membershipExpiry")) : null)
                    .fineBalancePaise(m.get("fineBalancePaise") instanceof Number n ? n.longValue() : 0L)
                    .currentBorrowCount(m.get("borrowCount") instanceof Number n ? n.intValue() : 0)
                    .homeBranchId((String) m.getOrDefault("homeBranchId", null))
                    .membershipTierId((String) m.getOrDefault("membershipTierId", null))
                    .program((String) m.getOrDefault("program", null))
                    .build();
        };
    }
}
