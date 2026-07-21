package com.library.search;

public record SearchCriteria(String field, String query, String sortBy, boolean ascending) {}
