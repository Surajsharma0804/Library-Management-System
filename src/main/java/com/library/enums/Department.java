package com.library.enums;

/**
 * Academic departments for student registration.
 */
public enum Department {
    COMPUTER_SCIENCE("Computer Science"),
    INFORMATION_TECHNOLOGY("Information Technology"),
    ELECTRONICS("Electronics"),
    MECHANICAL("Mechanical"),
    CIVIL("Civil"),
    ELECTRICAL("Electrical"),
    CHEMICAL("Chemical"),
    BIOTECHNOLOGY("Biotechnology"),
    MATHEMATICS("Mathematics"),
    PHYSICS("Physics"),
    CHEMISTRY("Chemistry"),
    BUSINESS_ADMINISTRATION("Business Administration"),
    HUMANITIES("Humanities"),
    LIBRARY_SCIENCE("Library Science"),
    OTHER("Other");

    private final String displayName;

    Department(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Department fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Department cannot be null");
        }
        String normalized = value.trim().toUpperCase().replace(' ', '_').replace('-', '_');
        for (Department d : values()) {
            if (d.name().equals(normalized) || d.displayName.equalsIgnoreCase(value.trim())) {
                return d;
            }
        }
        return OTHER;
    }
}
