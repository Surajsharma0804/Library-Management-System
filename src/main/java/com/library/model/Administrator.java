package com.library.model;

import com.library.enums.UserRole;

/**
 * System administrator - highest authority role.
 */
public class Administrator extends User {

    public Administrator(Builder b) {
        super(b);
    }

    @Override
    public UserRole getRole() {
        return UserRole.ADMIN;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder extends User.Builder<Builder> {
        public Administrator build() {
            return new Administrator(this);
        }
    }
}
