package com.library.mapper;

import com.library.enums.BookStatus;
import com.library.interfaces.JsonMappable;
import com.library.model.Book;
import com.library.util.DateUtils;
import com.library.util.JsonUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps {@link Book} to and from JSON-ready maps.
 */
public final class BookMapper implements JsonMappable<Book> {

    @Override
    public Map<String, Object> toMap(Book b) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", b.getId());
        m.put("isbn", b.getIsbn());
        m.put("barcode", b.getBarcode());
        m.put("title", b.getTitle());
        m.put("subtitle", b.getSubtitle());
        m.put("author", b.getAuthor());
        m.put("coAuthors", b.getCoAuthors());
        m.put("publisher", b.getPublisher());
        m.put("edition", b.getEdition());
        m.put("language", b.getLanguage());
        m.put("category", b.getCategory());
        m.put("subject", b.getSubject());
        m.put("keywords", b.getKeywords());
        m.put("publicationYear", b.getPublicationYear());
        m.put("totalPages", b.getTotalPages());
        m.put("rack", b.getRack());
        m.put("shelf", b.getShelf());
        m.put("purchasePricePaise", b.getPurchasePricePaise());
        m.put("purchaseDate", DateUtils.format(b.getPurchaseDate()));
        m.put("totalQuantity", b.getTotalQuantity());
        m.put("availableQuantity", b.getAvailableQuantity());
        m.put("reservedQuantity", b.getReservedQuantity());
        m.put("status", b.getStatus().name());
        m.put("description", b.getDescription());
        m.put("coverImagePath", b.getCoverImagePath());
        m.put("deweyDecimal", b.getDeweyDecimal());
        m.put("branchId", b.getBranchId());
        m.put("createdAt", DateUtils.formatDateTime(b.getCreatedAt()));
        m.put("updatedAt", DateUtils.formatDateTime(b.getUpdatedAt()));
        return m;
    }

    @Override
    public Book fromMap(Map<String, Object> m) {
        Book.Builder b = Book.builder()
                .id(JsonUtils.requireString(m, "id"))
                .isbn(JsonUtils.requireString(m, "isbn"))
                .barcode(JsonUtils.getString(m, "barcode"))
                .title(JsonUtils.requireString(m, "title"))
                .subtitle(JsonUtils.getString(m, "subtitle"))
                .author(JsonUtils.requireString(m, "author"))
                .coAuthors(toStringList(JsonUtils.getArray(m, "coAuthors")))
                .publisher(JsonUtils.getString(m, "publisher"))
                .edition(JsonUtils.getString(m, "edition"))
                .language(JsonUtils.getString(m, "language"))
                .category(JsonUtils.getString(m, "category"))
                .subject(JsonUtils.getString(m, "subject"))
                .keywords(toStringList(JsonUtils.getArray(m, "keywords")))
                .publicationYear(JsonUtils.requireInt(m, "publicationYear"))
                .totalPages(JsonUtils.getInt(m, "totalPages") == null ? 0 : JsonUtils.getInt(m, "totalPages"))
                .rack(JsonUtils.getString(m, "rack"))
                .shelf(JsonUtils.getString(m, "shelf"))
                .purchasePricePaise(JsonUtils.getLong(m, "purchasePricePaise") == null ? 0L : JsonUtils.getLong(m, "purchasePricePaise"))
                .purchaseDate(DateUtils.parseDate(JsonUtils.getString(m, "purchaseDate")))
                .totalQuantity(JsonUtils.requireInt(m, "totalQuantity"))
                .availableQuantity(JsonUtils.requireInt(m, "availableQuantity"))
                .reservedQuantity(JsonUtils.getInt(m, "reservedQuantity") == null ? 0 : JsonUtils.getInt(m, "reservedQuantity"))
                .status(BookStatus.fromString(JsonUtils.requireString(m, "status")))
                .description(JsonUtils.getString(m, "description"))
                .coverImagePath(JsonUtils.getString(m, "coverImagePath"))
                .deweyDecimal(JsonUtils.getString(m, "deweyDecimal"))
                .branchId(JsonUtils.getString(m, "branchId"))
                .createdAt(DateUtils.parseDateTime(JsonUtils.getString(m, "createdAt")))
                .updatedAt(DateUtils.parseDateTime(JsonUtils.getString(m, "updatedAt")));
        return b.build();
    }

    private List<String> toStringList(List<Object> list) {
        List<String> result = new ArrayList<>();
        for (Object o : list) {
            if (o != null) result.add(String.valueOf(o));
        }
        return result;
    }
}
