package com.library.model;

import com.library.enums.UserRole;
import com.library.util.DateUtils;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Abstract base for all system users (admins, librarians, students).
 * Holds the identity and credential fields common to every role.
 */
public abstract class User {

    protected final String id;
    protected String username;
    protected String firstName;
    protected String middleName;
    protected String lastName;
    protected String email;
    protected String phone;
    protected String passwordHash;
    protected boolean active;
    protected final LocalDateTime createdAt;
    protected LocalDateTime updatedAt;

    protected User(Builder<?> b) {
        this.id = Objects.requireNonNull(b.id, "user id");
        this.username = Objects.requireNonNull(b.username, "username");
        this.firstName = Objects.requireNonNull(b.firstName, "first name");
        this.middleName = b.middleName;
        this.lastName = b.lastName;
        this.email = b.email;
        this.phone = b.phone;
        this.passwordHash = Objects.requireNonNull(b.passwordHash, "password hash");
        this.active = b.active;
        this.createdAt = b.createdAt == null ? DateUtils.now() : b.createdAt;
        this.updatedAt = b.updatedAt == null ? this.createdAt : b.updatedAt;
    }

    public abstract UserRole getRole();

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getFirstName() { return firstName; }
    public String getMiddleName() { return middleName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getPasswordHash() { return passwordHash; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setUsername(String username) { this.username = username; touch(); }
    public void setFirstName(String firstName) { this.firstName = firstName; touch(); }
    public void setMiddleName(String middleName) { this.middleName = middleName; touch(); }
    public void setLastName(String lastName) { this.lastName = lastName; touch(); }
    public void setEmail(String email) { this.email = email; touch(); }
    public void setPhone(String phone) { this.phone = phone; touch(); }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; touch(); }
    public void setActive(boolean active) { this.active = active; touch(); }

    public String fullName() {
        StringBuilder sb = new StringBuilder(firstName);
        if (middleName != null && !middleName.isBlank()) {
            sb.append(' ').append(middleName);
        }
        if (lastName != null && !lastName.isBlank()) {
            sb.append(' ').append(lastName);
        }
        return sb.toString();
    }

    protected void touch() {
        this.updatedAt = DateUtils.now();
    }

    @SuppressWarnings("unchecked")
    public abstract static class Builder<B extends Builder<B>> {
        protected String id;
        protected String username;
        protected String firstName;
        protected String middleName;
        protected String lastName;
        protected String email;
        protected String phone;
        protected String passwordHash;
        protected boolean active = true;
        protected LocalDateTime createdAt;
        protected LocalDateTime updatedAt;

        public B id(String v) { this.id = v; return (B) this; }
        public B username(String v) { this.username = v; return (B) this; }
        public B firstName(String v) { this.firstName = v; return (B) this; }
        public B middleName(String v) { this.middleName = v; return (B) this; }
        public B lastName(String v) { this.lastName = v; return (B) this; }
        public B email(String v) { this.email = v; return (B) this; }
        public B phone(String v) { this.phone = v; return (B) this; }
        public B passwordHash(String v) { this.passwordHash = v; return (B) this; }
        public B active(boolean v) { this.active = v; return (B) this; }
        public B createdAt(LocalDateTime v) { this.createdAt = v; return (B) this; }
        public B updatedAt(LocalDateTime v) { this.updatedAt = v; return (B) this; }
    }
}
