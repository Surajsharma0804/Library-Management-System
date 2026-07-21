package com.library.config;

/**
 * Centralised library system configuration loaded at startup.
 */
public final class LibraryConfiguration {
    private static final String APP_NAME = "Library Management System";
    private static final String VERSION = "2.0.0";
    private static final int SESSION_TIMEOUT_MINUTES = 30;

    private LibraryConfiguration() {}

    public static String appName() { return APP_NAME; }
    public static String version() { return VERSION; }
    public static int sessionTimeoutMinutes() { return SESSION_TIMEOUT_MINUTES; }
}
