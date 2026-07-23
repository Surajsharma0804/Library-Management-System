package com.library.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a library branch location.
 */
public class Branch {

    private final String id;
    private String branchName;
    private String location;
    private String phone;
    private final LocalDateTime createdAt;

    public Branch(Builder b) {
        this.id = Objects.requireNonNull(b.id, "branch id");
        this.branchName = Objects.requireNonNull(b.branchName, "branchName");
        this.location = b.location;
        this.phone = b.phone;
        this.createdAt = b.createdAt == null ? LocalDateTime.now() : b.createdAt;
    }

    // --- Getters ---

    public String getId() { return id; }
    public String getBranchName() { return branchName; }
    public String getLocation() { return location; }
    public String getPhone() { return phone; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // --- Setters for mutable fields ---

    public void setBranchName(String branchName) { this.branchName = branchName; }
    public void setLocation(String location) { this.location = location; }
    public void setPhone(String phone) { this.phone = phone; }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String branchName;
        private String location;
        private String phone;
        private LocalDateTime createdAt;

        public Builder id(String v) { this.id = v; return this; }
        public Builder branchName(String v) { this.branchName = v; return this; }
        public Builder location(String v) { this.location = v; return this; }
        public Builder phone(String v) { this.phone = v; return this; }
        public Builder createdAt(LocalDateTime v) { this.createdAt = v; return this; }

        public Branch build() {
            return new Branch(this);
        }
    }
}
