package com.library.model;

import com.library.enums.MembershipStatus;
import com.library.enums.UserRole;
import com.library.util.DateUtils;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Student / library member. Carries membership, department, and
 * borrowing-state fields in addition to the common user fields.
 */
public class Student extends User {

    private final String registrationNumber;
    private final String libraryCardNumber;
    private String department;
    private String course;
    private int semester;
    private String section;
    private String address;
    private LocalDate joiningDate;
    private LocalDate membershipExpiry;
    private int borrowLimit;
    private int currentBorrowCount;
    private long fineBalancePaise;
    private MembershipStatus membershipStatus;
    private String homeBranchId;
    private String membershipTierId;
    private String program;

    public Student(Builder b) {
        super(b);
        this.registrationNumber = Objects.requireNonNull(b.registrationNumber, "registration number");
        this.libraryCardNumber = Objects.requireNonNull(b.libraryCardNumber, "library card number");
        this.department = b.department;
        this.course = b.course;
        this.semester = b.semester;
        this.section = b.section;
        this.address = b.address;
        this.joiningDate = b.joiningDate;
        this.membershipExpiry = b.membershipExpiry;
        this.borrowLimit = b.borrowLimit;
        this.currentBorrowCount = b.currentBorrowCount;
        this.fineBalancePaise = b.fineBalancePaise;
        this.membershipStatus = b.membershipStatus == null ? MembershipStatus.ACTIVE : b.membershipStatus;
        this.homeBranchId = b.homeBranchId;
        this.membershipTierId = b.membershipTierId;
        this.program = b.program;
    }

    @Override
    public UserRole getRole() {
        return UserRole.STUDENT;
    }

    public String getRegistrationNumber() { return registrationNumber; }
    public String getLibraryCardNumber() { return libraryCardNumber; }
    public String getDepartment() { return department; }
    public String getCourse() { return course; }
    public int getSemester() { return semester; }
    public String getSection() { return section; }
    public String getAddress() { return address; }
    public LocalDate getJoiningDate() { return joiningDate; }
    public LocalDate getMembershipExpiry() { return membershipExpiry; }
    public int getBorrowLimit() { return borrowLimit; }
    public int getCurrentBorrowCount() { return currentBorrowCount; }
    public long getFineBalancePaise() { return fineBalancePaise; }
    public double getFineBalance() { return fineBalancePaise / 100.0; }
    public MembershipStatus getMembershipStatus() { return membershipStatus; }
    public String getHomeBranchId() { return homeBranchId; }
    public String getMembershipTierId() { return membershipTierId; }
    public String getProgram() { return program; }

    public void setDepartment(String department) { this.department = department; touch(); }
    public void setCourse(String course) { this.course = course; touch(); }
    public void setSemester(int semester) { this.semester = semester; touch(); }
    public void setSection(String section) { this.section = section; touch(); }
    public void setAddress(String address) { this.address = address; touch(); }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; touch(); }
    public void setMembershipExpiry(LocalDate membershipExpiry) { this.membershipExpiry = membershipExpiry; touch(); }
    public void setBorrowLimit(int borrowLimit) { this.borrowLimit = borrowLimit; touch(); }
    public void setMembershipStatus(MembershipStatus membershipStatus) { this.membershipStatus = membershipStatus; touch(); }
    public void setHomeBranchId(String homeBranchId) { this.homeBranchId = homeBranchId; touch(); }
    public void setMembershipTierId(String membershipTierId) { this.membershipTierId = membershipTierId; touch(); }
    public void setProgram(String program) { this.program = program; touch(); }

    public void incrementBorrowCount() { this.currentBorrowCount++; touch(); }
    public void decrementBorrowCount() { this.currentBorrowCount = Math.max(0, this.currentBorrowCount - 1); touch(); }
    public void addFine(long paise) { this.fineBalancePaise += paise; touch(); }
    public void subtractFine(long paise) { this.fineBalancePaise = Math.max(0, this.fineBalancePaise - paise); touch(); }
    public void clearFine() { this.fineBalancePaise = 0; touch(); }

    public int remainingBorrowSlots() {
        return Math.max(0, borrowLimit - currentBorrowCount);
    }

    public boolean membershipExpired() {
        return membershipExpiry != null && membershipExpiry.isBefore(DateUtils.today());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder extends User.Builder<Builder> {
        private String registrationNumber;
        private String libraryCardNumber;
        private String department;
        private String course;
        private int semester;
        private String section;
        private String address;
        private LocalDate joiningDate;
        private LocalDate membershipExpiry;
        private int borrowLimit;
        private int currentBorrowCount;
        private long fineBalancePaise;
        private MembershipStatus membershipStatus;
        private String homeBranchId;
        private String membershipTierId;
        private String program;

        public Builder registrationNumber(String v) { this.registrationNumber = v; return this; }
        public Builder libraryCardNumber(String v) { this.libraryCardNumber = v; return this; }
        public Builder department(String v) { this.department = v; return this; }
        public Builder course(String v) { this.course = v; return this; }
        public Builder semester(int v) { this.semester = v; return this; }
        public Builder section(String v) { this.section = v; return this; }
        public Builder address(String v) { this.address = v; return this; }
        public Builder joiningDate(LocalDate v) { this.joiningDate = v; return this; }
        public Builder membershipExpiry(LocalDate v) { this.membershipExpiry = v; return this; }
        public Builder borrowLimit(int v) { this.borrowLimit = v; return this; }
        public Builder currentBorrowCount(int v) { this.currentBorrowCount = v; return this; }
        public Builder fineBalancePaise(long v) { this.fineBalancePaise = v; return this; }
        public Builder membershipStatus(MembershipStatus v) { this.membershipStatus = v; return this; }
        public Builder homeBranchId(String v) { this.homeBranchId = v; return this; }
        public Builder membershipTierId(String v) { this.membershipTierId = v; return this; }
        public Builder program(String v) { this.program = v; return this; }

        public Student build() {
            return new Student(this);
        }
    }
}
