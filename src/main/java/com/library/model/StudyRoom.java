package com.library.model;

import java.util.Objects;

/**
 * A physical study space that can be reserved by students.
 */
public class StudyRoom {

    private final String id;
    private String roomName;
    private int capacity;
    private String branchId;
    private boolean active;

    public StudyRoom(Builder b) {
        this.id = Objects.requireNonNull(b.id, "study room id");
        this.roomName = b.roomName;
        this.capacity = b.capacity;
        this.branchId = b.branchId;
        this.active = b.active;
    }

    public String getId() { return id; }
    public String getRoomName() { return roomName; }
    public int getCapacity() { return capacity; }
    public String getBranchId() { return branchId; }
    public boolean isActive() { return active; }

    public void setRoomName(String roomName) { this.roomName = roomName; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setBranchId(String branchId) { this.branchId = branchId; }
    public void setActive(boolean active) { this.active = active; }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String roomName;
        private int capacity;
        private String branchId;
        private boolean active = true;

        public Builder id(String v) { this.id = v; return this; }
        public Builder roomName(String v) { this.roomName = v; return this; }
        public Builder capacity(int v) { this.capacity = v; return this; }
        public Builder branchId(String v) { this.branchId = v; return this; }
        public Builder active(boolean v) { this.active = v; return this; }

        public StudyRoom build() {
            return new StudyRoom(this);
        }
    }
}
