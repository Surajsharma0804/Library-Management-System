package com.library.reports;

/**
 * Strategy interface for report generation.
 */
public interface ReportStrategy {
    String id();
    String title();
    ReportData generate();
}
