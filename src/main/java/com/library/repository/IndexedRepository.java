package com.library.repository;

import com.library.interfaces.JsonMappable;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Abstract repository with a primary {@link ConcurrentHashMap} index (by ID) and
 * configurable secondary indexes. All mutating operations ({@link #save} and
 * {@link #deleteById}) update indexes atomically: either all indexes are
 * updated or none are.
 *
 * <p>Secondary indexes map a string key value to a list of entities, supporting
 * both unique lookups ({@link #findBySecondaryKey}) and list lookups
 * ({@link #findAllBySecondaryKey}).</p>
 *
 * <p>Subclasses register secondary indexes in their constructor via
 * {@link #registerSecondaryIndex(String)}, and implement
 * {@link #secondaryKey(String, Object)} to extract the index key from an
 * entity for each registered index.</p>
 *
 * @param <T> entity type
 * @param <K> primary key type
 */
public abstract class IndexedRepository<T, K> extends JsonRepository<T, K> {

    /** Primary index: entity ID → entity */
    private final ConcurrentHashMap<K, T> primaryIndex = new ConcurrentHashMap<>();

    /**
     * Secondary indexes: index-name → (field-value-string → List&lt;T&gt;).
     * Using LinkedHashMap to preserve registration order (deterministic iteration).
     */
    private final Map<String, Map<String, List<T>>> secondaryIndexes = new LinkedHashMap<>();

    /** Guards lazy loading; volatile ensures visibility across threads. */
    private volatile boolean indexLoaded = false;

    /**
     * Local copy of the id-extractor so we can call it without reflection,
     * since the parent's idExtractor field is private.
     */
    private final Function<T, K> idExtractorRef;

    // -----------------------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------------------

    protected IndexedRepository(String filePath,
                                 JsonMappable<T> mapper,
                                 Function<T, K> idExtractor) {
        super(filePath, mapper, idExtractor);
        this.idExtractorRef = idExtractor;
    }

    protected IndexedRepository(Path filePath,
                                 JsonMappable<T> mapper,
                                 Function<T, K> idExtractor) {
        super(filePath, mapper, idExtractor);
        this.idExtractorRef = idExtractor;
    }

    // -----------------------------------------------------------------------
    // Index registration & secondary key extraction (subclass contract)
    // -----------------------------------------------------------------------

    /**
     * Registers a named secondary index. Must be called from the subclass
     * constructor before any data access.
     *
     * @param name index name, e.g. {@code "isbn"}, {@code "registrationNumber"}
     */
    protected final void registerSecondaryIndex(String name) {
        secondaryIndexes.put(name, new ConcurrentHashMap<>());
    }

    /**
     * Extracts the secondary index key for the named index from the given entity.
     * Return {@code null} if the entity should not be indexed under this name.
     *
     * @param indexName the registered index name
     * @param entity    the entity to extract the key from
     * @return the string key, or {@code null} to skip indexing
     */
    protected abstract String secondaryKey(String indexName, T entity);

    // -----------------------------------------------------------------------
    // Lazy loading
    // -----------------------------------------------------------------------

    /**
     * Ensures the in-memory indexes are populated. Thread-safe: uses
     * double-checked locking with a {@code synchronized} block.
     */
    private synchronized void ensureLoaded() {
        if (indexLoaded) return;

        primaryIndex.clear();
        for (Map<String, List<T>> bucket : secondaryIndexes.values()) {
            bucket.clear();
        }

        List<T> all = super.findAll();
        for (T entity : all) {
            K id = idExtractorRef.apply(entity);
            primaryIndex.put(id, entity);
            indexEntity(entity);
        }
        indexLoaded = true;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /** Adds an entity to all registered secondary indexes. */
    private void indexEntity(T entity) {
        for (Map.Entry<String, Map<String, List<T>>> entry : secondaryIndexes.entrySet()) {
            String indexName = entry.getKey();
            String key = secondaryKey(indexName, entity);
            if (key != null) {
                entry.getValue()
                     .computeIfAbsent(key, k -> new ArrayList<>())
                     .add(entity);
            }
        }
    }

    /** Removes an entity from all registered secondary indexes. */
    private void deindexEntity(T entity) {
        for (Map.Entry<String, Map<String, List<T>>> entry : secondaryIndexes.entrySet()) {
            String indexName = entry.getKey();
            String key = secondaryKey(indexName, entity);
            if (key != null) {
                List<T> bucket = entry.getValue().get(key);
                if (bucket != null) {
                    bucket.remove(entity);
                    if (bucket.isEmpty()) {
                        entry.getValue().remove(key);
                    }
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Public API — read operations (delegate to in-memory index)
    // -----------------------------------------------------------------------

    @Override
    public List<T> findAll() {
        ensureLoaded();
        return new ArrayList<>(primaryIndex.values());
    }

    @Override
    public List<T> findAll(Predicate<T> filter) {
        ensureLoaded();
        List<T> result = new ArrayList<>();
        for (T entity : primaryIndex.values()) {
            if (filter.test(entity)) result.add(entity);
        }
        return result;
    }

    @Override
    public Optional<T> findById(K id) {
        ensureLoaded();
        return Optional.ofNullable(primaryIndex.get(id));
    }

    @Override
    public boolean exists(Predicate<T> filter) {
        ensureLoaded();
        for (T entity : primaryIndex.values()) {
            if (filter.test(entity)) return true;
        }
        return false;
    }

    /** Returns the total number of entities without any disk I/O. */
    @Override
    public long count() {
        ensureLoaded();
        return primaryIndex.size();
    }

    // -----------------------------------------------------------------------
    // Secondary index lookups
    // -----------------------------------------------------------------------

    /**
     * Finds a single entity by a registered secondary index key (for unique indexes).
     *
     * @param indexName the registered index name
     * @param value     the key value to look up
     * @return the first matching entity, or empty if none
     */
    public Optional<T> findBySecondaryKey(String indexName, String value) {
        ensureLoaded();
        Map<String, List<T>> index = secondaryIndexes.get(indexName);
        if (index == null) return Optional.empty();
        List<T> bucket = index.get(value);
        if (bucket == null || bucket.isEmpty()) return Optional.empty();
        return Optional.of(bucket.get(0));
    }

    /**
     * Finds all entities matching a registered secondary index key (for list indexes).
     *
     * @param indexName the registered index name
     * @param value     the key value to look up
     * @return unmodifiable list of matching entities; empty list if none
     */
    public List<T> findAllBySecondaryKey(String indexName, String value) {
        ensureLoaded();
        Map<String, List<T>> index = secondaryIndexes.get(indexName);
        if (index == null) return List.of();
        List<T> bucket = index.get(value);
        if (bucket == null) return List.of();
        return Collections.unmodifiableList(new ArrayList<>(bucket));
    }

    // -----------------------------------------------------------------------
    // Mutating operations with atomic index update + rollback
    // -----------------------------------------------------------------------

    /**
     * Saves (insert or update) an entity with atomic index maintenance.
     *
     * <p>Algorithm:
     * <ol>
     *   <li>Snapshot the old secondary keys for this entity ID.</li>
     *   <li>Write entity to primary index (in-memory).</li>
     *   <li>For each secondary index: remove old key bucket entry, add new key bucket entry.</li>
     *   <li>Persist to storage backend via {@code super.save(entity)}.</li>
     *   <li>If persistence throws: restore primary + all secondary indexes from snapshot,
     *       then re-throw.</li>
     * </ol>
     * </p>
     *
     * @param entity the entity to save
     * @return the saved entity
     */
    @Override
    public synchronized T save(T entity) {
        ensureLoaded();

        K id = idExtractorRef.apply(entity);
        T oldEntity = primaryIndex.get(id);

        // Step 1: snapshot old secondary keys (null if entity is new)
        Map<String, String> oldKeys = new HashMap<>();
        if (oldEntity != null) {
            for (String indexName : secondaryIndexes.keySet()) {
                oldKeys.put(indexName, secondaryKey(indexName, oldEntity));
            }
        }

        // Step 2: write to primary index
        primaryIndex.put(id, entity);

        // Step 3: update secondary indexes
        for (Map.Entry<String, Map<String, List<T>>> entry : secondaryIndexes.entrySet()) {
            String indexName = entry.getKey();
            Map<String, List<T>> index = entry.getValue();

            // Remove from old bucket
            String oldKey = oldKeys.get(indexName);
            if (oldKey != null) {
                List<T> bucket = index.get(oldKey);
                if (bucket != null) {
                    bucket.remove(oldEntity);
                    if (bucket.isEmpty()) {
                        index.remove(oldKey);
                    }
                }
            }

            // Add to new bucket
            String newKey = secondaryKey(indexName, entity);
            if (newKey != null) {
                index.computeIfAbsent(newKey, k -> new ArrayList<>()).add(entity);
            }
        }

        // Step 4: persist to storage backend
        try {
            super.save(entity);
        } catch (Exception e) {
            // Step 5: rollback — restore primary index
            if (oldEntity != null) {
                primaryIndex.put(id, oldEntity);
            } else {
                primaryIndex.remove(id);
            }

            // Rollback secondary indexes
            for (Map.Entry<String, Map<String, List<T>>> entry : secondaryIndexes.entrySet()) {
                String indexName = entry.getKey();
                Map<String, List<T>> index = entry.getValue();

                // Remove the new key entry we just added
                String newKey = secondaryKey(indexName, entity);
                if (newKey != null) {
                    List<T> bucket = index.get(newKey);
                    if (bucket != null) {
                        bucket.remove(entity);
                        if (bucket.isEmpty()) {
                            index.remove(newKey);
                        }
                    }
                }

                // Restore the old key entry
                String oldKey = oldKeys.get(indexName);
                if (oldKey != null && oldEntity != null) {
                    index.computeIfAbsent(oldKey, k -> new ArrayList<>()).add(oldEntity);
                }
            }

            throw e;
        }

        return entity;
    }

    /**
     * Deletes an entity by ID, removing it from the primary index and all
     * secondary indexes before delegating to the storage backend.
     *
     * @param id the primary key of the entity to delete
     * @return {@code true} if the entity was found and deleted, {@code false} otherwise
     */
    @Override
    public synchronized boolean deleteById(K id) {
        ensureLoaded();

        T entity = primaryIndex.get(id);
        if (entity == null) {
            return false;
        }

        // Remove from primary index
        primaryIndex.remove(id);

        // Remove from all secondary indexes
        deindexEntity(entity);

        // Persist deletion
        try {
            return super.deleteById(id);
        } catch (Exception e) {
            // Rollback: restore primary + secondary indexes
            primaryIndex.put(id, entity);
            indexEntity(entity);
            throw e;
        }
    }

    /**
     * Invalidates the in-memory indexes, forcing a reload on the next access.
     * Call this if the underlying JSON file has been modified externally.
     */
    protected synchronized void invalidateIndex() {
        indexLoaded = false;
    }
}
