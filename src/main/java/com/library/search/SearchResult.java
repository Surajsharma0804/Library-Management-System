package com.library.search;

import java.util.List;

public record SearchResult<T>(List<T> results, int totalCount, long timeTakenMs) {}
