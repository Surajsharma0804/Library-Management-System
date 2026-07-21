package com.library.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * File read/write helpers.
 */
public final class FileUtils {

    private FileUtils() {}

    public static String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            return "";
        }
    }

    public static void write(Path path, String content) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write " + path, e);
        }
    }

    public static boolean exists(Path path) {
        return Files.exists(path);
    }
}
