package com.library.repository;

import com.library.config.Constants;
import com.library.util.JsonUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Generic JSON-backed repository. Reads/writes a list of entities
 * from/to a JSON file, using a mapper function and an id-extractor.
 */
public abstract class JsonRepository<T, ID> {

    private final Path filePath;
    private Path overrideFile;
    private final com.library.interfaces.JsonMappable<T> mapper;
    private final Function<T, ID> idExtractor;

    protected JsonRepository(String filePath, com.library.interfaces.JsonMappable<T> mapper,
                             Function<T, ID> idExtractor) {
        this.filePath = Path.of(filePath);
        this.mapper = mapper;
        this.idExtractor = idExtractor;
    }

    protected JsonRepository(Path filePath, com.library.interfaces.JsonMappable<T> mapper,
                             Function<T, ID> idExtractor) {
        this.filePath = filePath;
        this.mapper = mapper;
        this.idExtractor = idExtractor;
    }

    public void setOverrideFile(Path file) { this.overrideFile = file; }
    private Path effectivePath() { return overrideFile != null ? overrideFile : filePath; }

    public List<T> findAll() { return loadAll(); }

    public List<T> findAll(Predicate<T> filter) {
        List<T> result = new ArrayList<>();
        for (T item : loadAll()) { if (filter.test(item)) result.add(item); }
        return result;
    }

    public Optional<T> findById(ID id) {
        for (T item : loadAll()) { if (idExtractor.apply(item).equals(id)) return Optional.of(item); }
        return Optional.empty();
    }

    public boolean exists(Predicate<T> filter) {
        for (T item : loadAll()) { if (filter.test(item)) return true; }
        return false;
    }

    public T save(T entity) {
        List<T> all = loadAll();
        ID id = idExtractor.apply(entity);
        boolean found = false;
        List<T> updated = new ArrayList<>();
        for (T item : all) {
            if (idExtractor.apply(item).equals(id)) { updated.add(entity); found = true; }
            else { updated.add(item); }
        }
        if (!found) updated.add(entity);
        writeAll(updated);
        return entity;
    }

    public boolean deleteById(ID id) {
        List<T> all = loadAll();
        List<T> updated = new ArrayList<>();
        boolean removed = false;
        for (T item : all) {
            if (idExtractor.apply(item).equals(id)) { removed = true; }
            else { updated.add(item); }
        }
        if (removed) writeAll(updated);
        return removed;
    }

    public long count() { return loadAll().size(); }

    @SuppressWarnings("unchecked")
    private List<T> loadAll() {
        Path path = effectivePath();
        if (!Files.exists(path)) return new ArrayList<>();
        try {
            String content = Files.readString(path);
            if (content.isBlank()) return new ArrayList<>();
            List<Map<String, Object>> list = JsonUtils.parseArray(content);
            List<T> result = new ArrayList<>();
            for (Map<String, Object> m : list) result.add(mapper.fromMap(m));
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read " + path + ": " + e.getMessage(), e);
        }
    }

    private void writeAll(List<T> entities) {
        try {
            Path path = effectivePath();
            Files.createDirectories(path.getParent());
            List<Map<String, Object>> list = new ArrayList<>();
            for (T e : entities) list.add(mapper.toMap(e));
            Files.writeString(path, JsonUtils.prettyPrint(list));
        } catch (IOException e) {
            throw new RuntimeException("Failed to write " + effectivePath(), e);
        }
    }
}
