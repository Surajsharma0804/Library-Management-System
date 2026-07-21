package com.library.interfaces;

public interface Reportable {
    String reportTitle();
    java.util.List<String> reportHeaders();
    java.util.List<java.util.List<String>> reportRows();
}
