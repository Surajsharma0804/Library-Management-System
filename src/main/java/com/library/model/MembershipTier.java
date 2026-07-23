package com.library.model;

import com.library.util.DateUtils;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a membership tier that defines borrowing privileges for library members.
 * Each tier specifies limits on borrows, loan periods, renewals, and reservations.
 */
public class MembershipTier {

    private final String id;
    private String tierName;
    private int borrowLimit;
    private int loanPeriodDays;
    private int renewalLimit;
    private int maxActiveReservations;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MembershipTier(Builder b) {
        this.id = Objects.requireNonNull(b.id, "membershipTier id");
        this.tierName = b.tierName;
        this.borrowLimit = b.borrowLimit;
        this.loanPeriodDays = b.loanPeriodDays;
        this.renewalLimit = b.renewalLimit;
        this.maxActiveReservations = b.maxActiveReservations;
        this.createdAt = b.createdAt == null ? DateUtils.now() : b.createdAt;
        this.updatedAt = b.updatedAt == null ? this.createdAt : b.updatedAt;
    }

    // --- Getters ---

    public String getId() { return id; }
    public String getTierName() { return tierName; }
    public int getBorrowLimit() { return borrowLimit; }
    public int getLoanPeriodDays() { return loanPeriodDays; }
    public int getRenewalLimit() { return renewalLimit; }
    public int getMaxActiveReservations() { return maxActiveReservations; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // --- Mutable setters (update updatedAt via touch()) ---

    public void setTierName(String tierName) { this.tierName = tierName; touch(); }
    public void setBorrowLimit(int borrowLimit) { this.borrowLimit = borrowLimit; touch(); }
    public void setLoanPeriodDays(int loanPeriodDays) { this.loanPeriodDays = loanPeriodDays; touch(); }
    public void setRenewalLimit(int renewalLimit) { this.renewalLimit = renewalLimit; touch(); }
    public void setMaxActiveReservations(int maxActiveReservations) { this.maxActiveReservations = maxActiveReservations; touch(); }

    private void touch() {
        this.updatedAt = DateUtils.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String tierName;
        private int borrowLimit;
        private int loanPeriodDays;
        private int renewalLimit;
        private int maxActiveReservations;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(String v) { this.id = v; return this; }
        public Builder tierName(String v) { this.tierName = v; return this; }
        public Builder borrowLimit(int v) { this.borrowLimit = v; return this; }
        public Builder loanPeriodDays(int v) { this.loanPeriodDays = v; return this; }
        public Builder renewalLimit(int v) { this.renewalLimit = v; return this; }
        public Builder maxActiveReservations(int v) { this.maxActiveReservations = v; return this; }
        public Builder createdAt(LocalDateTime v) { this.createdAt = v; return this; }
        public Builder updatedAt(LocalDateTime v) { this.updatedAt = v; return this; }

        public MembershipTier build() {
            return new MembershipTier(this);
        }
    }
}
