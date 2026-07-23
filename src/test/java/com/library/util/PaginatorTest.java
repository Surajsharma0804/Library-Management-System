package com.library.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Paginator unit tests")
class PaginatorTest {

    @Test
    @DisplayName("Empty list returns page 1 with 0 items and totalPages=1")
    void emptyList() {
        Page<String> page = Paginator.paginate(List.of(), 1, 10);
        assertEquals(0, page.items().size());
        assertEquals(1, page.totalPages());
        assertEquals(0, page.totalRecords());
        assertEquals(1, page.currentPage());
    }

    @Test
    @DisplayName("Page number beyond totalPages returns empty items")
    void pageNumberBeyondTotal() {
        List<Integer> source = List.of(1, 2, 3, 4, 5);
        Page<Integer> page = Paginator.paginate(source, 10, 2);
        assertTrue(page.items().isEmpty());
        assertEquals(10, page.currentPage());
        assertEquals(3, page.totalPages());
    }

    @Test
    @DisplayName("Union of all pages equals original list (no duplicates, no omissions)")
    void unionOfAllPagesEqualsOriginalList() {
        List<Integer> source = new ArrayList<>();
        for (int i = 0; i < 25; i++) source.add(i);

        int pageSize = 10;
        Page<Integer> firstPage = Paginator.paginate(source, 1, pageSize);
        int totalPages = firstPage.totalPages();

        List<Integer> collected = new ArrayList<>();
        for (int p = 1; p <= totalPages; p++) {
            Page<Integer> page = Paginator.paginate(source, p, pageSize);
            collected.addAll(page.items());
        }

        assertEquals(source.size(), collected.size());
        assertEquals(source, collected);
    }

    @Test
    @DisplayName("pageNumber=0 throws IllegalArgumentException")
    void pageNumberZeroThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> Paginator.paginate(List.of("a", "b"), 0, 10));
    }

    @Test
    @DisplayName("pageSize=0 throws IllegalArgumentException")
    void pageSizeZeroThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> Paginator.paginate(List.of("a", "b"), 1, 0));
    }

    @Test
    @DisplayName("pageSize=501 throws IllegalArgumentException")
    void pageSizeTooLargeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> Paginator.paginate(List.of("a", "b"), 1, 501));
    }

    @Test
    @DisplayName("pageSize=500 is accepted (boundary)")
    void pageSizeMaxAccepted() {
        List<String> source = List.of("x");
        assertDoesNotThrow(() -> Paginator.paginate(source, 1, 500));
    }

    @Test
    @DisplayName("Single element list on page 1 has 1 item")
    void singleElementList() {
        Page<String> page = Paginator.paginate(List.of("only"), 1, 10);
        assertEquals(1, page.items().size());
        assertEquals("only", page.items().get(0));
        assertEquals(1, page.totalPages());
    }
}
