package com.library.model;

import com.library.enums.UserRole;

import java.util.HashSet;
import java.util.Set;

/**
 * Librarian - performs daily library operations. Carries a set of
 * granted permission strings used by the RBAC layer.
 */
public class Librarian extends User {

    private final Set<String> permissions = new HashSet<>();

    public Librarian(Builder b) {
        super(b);
        if (b.permissions != null) {
            this.permissions.addAll(b.permissions);
        }
    }

    @Override
    public UserRole getRole() {
        return UserRole.LIBRARIAN;
    }

    public Set<String> getPermissions() {
        return Set.copyOf(permissions);
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    public void grantPermission(String permission) {
        permissions.add(permission);
        touch();
    }

    public void revokePermission(String permission) {
        permissions.remove(permission);
        touch();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder extends User.Builder<Builder> {
        private final Set<String> permissions = new HashSet<>();

        public Builder permission(String p) {
            this.permissions.add(p);
            return this;
        }

        public Builder permissions(Set<String> perms) {
            if (perms != null) this.permissions.addAll(perms);
            return this;
        }

        public Librarian build() {
            return new Librarian(this);
        }
    }
}
