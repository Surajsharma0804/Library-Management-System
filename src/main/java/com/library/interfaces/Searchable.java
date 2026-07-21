package com.library.interfaces;

import java.util.List;
import java.util.function.Predicate;

/**
 * Contract for entities or services that support search operations.
 *
 * @param <T> the searchable entity type
 */
public interface Searchable<T> {
    List<T> search(String query);
    List<T> search(Predicate<T> filter);
}
