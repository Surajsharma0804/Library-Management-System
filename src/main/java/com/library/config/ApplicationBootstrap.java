package com.library.config;

import com.library.enums.UserRole;
import com.library.facade.LibraryFacade;
import com.library.model.Administrator;
import com.library.model.User;
import com.library.security.PasswordHasher;

/**
 * One-time bootstrap: seeds a default admin on first run.
 */
public final class ApplicationBootstrap {
    private ApplicationBootstrap() {}
    public static void initialise(LibraryFacade facade) {
        for (User u : facade.staffRepo().findAll()) {
            if (u.getRole() == UserRole.ADMIN) return;
        }
        Administrator admin = Administrator.builder()
                .id("admin-000001").username("admin").firstName("System").lastName("Administrator")
                .passwordHash(PasswordHasher.hash("admin@123")).active(true).build();
        facade.staffRepo().save(admin);
    }
}
