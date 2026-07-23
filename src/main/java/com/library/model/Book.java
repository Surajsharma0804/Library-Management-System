package com.library.model;

import com.library.enums.BookStatus;
import com.library.util.DateUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A single catalogued library acquisition. Carries bibliographic,
 * inventory, and physical-location fields. Quantity counters are
 * mutated only through the guarded mutator methods so invariants
 * (available + reserved + borrowed <= total) stay consistent.
 */
public class Book {

    private final String id;
    private String isbn;
    private String barcode;
    private String title;
    private String subtitle;
    private String author;
    private final List<String> coAuthors = new ArrayList<>();
    private String publisher;
    private String edition;
    private String language;
    private String category;
    private String subject;
    private final List<String> keywords = new ArrayList<>();
    private int publicationYear;
    private int totalPages;
    private String rack;
    private String shelf;
    private long purchasePricePaise;
    private LocalDate purchaseDate;
    private int totalQuantity;
    private int availableQuantity;
    private int reservedQuantity;
    private BookStatus status;
    private String description;
    private String coverImagePath;
    private String deweyDecimal;
    private String branchId;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Book(Builder b) {
        this.id = Objects.requireNonNull(b.id, "book id");
        this.isbn = Objects.requireNonNull(b.isbn, "isbn");
        this.barcode = b.barcode;
        this.title = Objects.requireNonNull(b.title, "title");
        this.subtitle = b.subtitle;
        this.author = Objects.requireNonNull(b.author, "author");
        if (b.coAuthors != null) this.coAuthors.addAll(b.coAuthors);
        this.publisher = b.publisher;
        this.edition = b.edition;
        this.language = b.language;
        this.category = b.category;
        this.subject = b.subject;
        if (b.keywords != null) this.keywords.addAll(b.keywords);
        this.publicationYear = b.publicationYear;
        this.totalPages = b.totalPages;
        this.rack = b.rack;
        this.shelf = b.shelf;
        this.purchasePricePaise = b.purchasePricePaise;
        this.purchaseDate = b.purchaseDate;
        this.totalQuantity = b.totalQuantity;
        this.availableQuantity = b.availableQuantity;
        this.reservedQuantity = b.reservedQuantity;
        this.status = b.status == null ? BookStatus.AVAILABLE : b.status;
        this.description = b.description;
        this.coverImagePath = b.coverImagePath;
        this.deweyDecimal = b.deweyDecimal;
        this.branchId = b.branchId;
        this.createdAt = b.createdAt == null ? DateUtils.now() : b.createdAt;
        this.updatedAt = b.updatedAt == null ? this.createdAt : b.updatedAt;
    }

    public String getId() { return id; }
    public String getIsbn() { return isbn; }
    public String getBarcode() { return barcode; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getAuthor() { return author; }
    public List<String> getCoAuthors() { return List.copyOf(coAuthors); }
    public String getPublisher() { return publisher; }
    public String getEdition() { return edition; }
    public String getLanguage() { return language; }
    public String getCategory() { return category; }
    public String getSubject() { return subject; }
    public List<String> getKeywords() { return List.copyOf(keywords); }
    public int getPublicationYear() { return publicationYear; }
    public int getTotalPages() { return totalPages; }
    public String getRack() { return rack; }
    public String getShelf() { return shelf; }
    public long getPurchasePricePaise() { return purchasePricePaise; }
    public double getPurchasePrice() { return purchasePricePaise / 100.0; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public int getTotalQuantity() { return totalQuantity; }
    public int getAvailableQuantity() { return availableQuantity; }
    public int getReservedQuantity() { return reservedQuantity; }
    public int getBorrowedQuantity() { return totalQuantity - availableQuantity - reservedQuantity; }
    public BookStatus getStatus() { return status; }
    public String getDescription() { return description; }
    public String getCoverImagePath() { return coverImagePath; }
    public String getDeweyDecimal() { return deweyDecimal; }
    public String getBranchId() { return branchId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setIsbn(String isbn) { this.isbn = isbn; touch(); }
    public void setBarcode(String barcode) { this.barcode = barcode; touch(); }
    public void setTitle(String title) { this.title = title; touch(); }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; touch(); }
    public void setAuthor(String author) { this.author = author; touch(); }
    public void setCoAuthors(List<String> coAuthors) { this.coAuthors.clear(); if (coAuthors != null) this.coAuthors.addAll(coAuthors); touch(); }
    public void setPublisher(String publisher) { this.publisher = publisher; touch(); }
    public void setEdition(String edition) { this.edition = edition; touch(); }
    public void setLanguage(String language) { this.language = language; touch(); }
    public void setCategory(String category) { this.category = category; touch(); }
    public void setSubject(String subject) { this.subject = subject; touch(); }
    public void setKeywords(List<String> keywords) { this.keywords.clear(); if (keywords != null) this.keywords.addAll(keywords); touch(); }
    public void setPublicationYear(int publicationYear) { this.publicationYear = publicationYear; touch(); }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; touch(); }
    public void setRack(String rack) { this.rack = rack; touch(); }
    public void setShelf(String shelf) { this.shelf = shelf; touch(); }
    public void setPurchasePricePaise(long purchasePricePaise) { this.purchasePricePaise = purchasePricePaise; touch(); }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; touch(); }
    public void setDescription(String description) { this.description = description; touch(); }
    public void setCoverImagePath(String coverImagePath) { this.coverImagePath = coverImagePath; touch(); }
    public void setDeweyDecimal(String deweyDecimal) {
        if (deweyDecimal != null && deweyDecimal.length() > 20) {
            throw new IllegalArgumentException("deweyDecimal must not exceed 20 characters");
        }
        this.deweyDecimal = deweyDecimal;
        touch();
    }
    public void setBranchId(String branchId) { this.branchId = branchId; touch(); }

    /** Decreases available count when a copy is issued. */
    public void markIssued() {
        if (availableQuantity <= 0) {
            throw new IllegalStateException("No available copies to issue for book " + id);
        }
        this.availableQuantity--;
        recomputeStatus();
        touch();
    }

    /** Increases available count when a copy is returned. */
    public void markReturned() {
        if (availableQuantity + reservedQuantity >= totalQuantity) {
            throw new IllegalStateException("Cannot return more copies than total for book " + id);
        }
        this.availableQuantity++;
        recomputeStatus();
        touch();
    }

    public void markReserved() {
        if (availableQuantity <= 0) {
            throw new IllegalStateException("No available copies to reserve for book " + id);
        }
        this.availableQuantity--;
        this.reservedQuantity++;
        recomputeStatus();
        touch();
    }

    public void releaseReservation() {
        if (reservedQuantity <= 0) {
            throw new IllegalStateException("No reserved copies to release for book " + id);
        }
        this.reservedQuantity--;
        this.availableQuantity++;
        recomputeStatus();
        touch();
    }

    public void markLost() {
        if (totalQuantity <= 0) {
            throw new IllegalStateException("No copies to mark lost for book " + id);
        }
        this.totalQuantity--;
        this.availableQuantity = Math.min(availableQuantity, totalQuantity);
        recomputeStatus();
        touch();
    }

    public void markDamaged() {
        this.status = BookStatus.DAMAGED;
        touch();
    }

    public void markUnderRepair() {
        this.status = BookStatus.UNDER_REPAIR;
        touch();
    }

    public void markAvailable() {
        this.status = BookStatus.AVAILABLE;
        touch();
    }

    public void archive() {
        this.status = BookStatus.ARCHIVED;
        touch();
    }

    public void restore() {
        this.status = availableQuantity > 0 ? BookStatus.AVAILABLE : BookStatus.BORROWED;
        touch();
    }

    private void recomputeStatus() {
        if (status == BookStatus.LOST || status == BookStatus.DAMAGED
                || status == BookStatus.UNDER_REPAIR || status == BookStatus.ARCHIVED) {
            return;
        }
        if (availableQuantity > 0) {
            this.status = BookStatus.AVAILABLE;
        } else if (reservedQuantity > 0) {
            this.status = BookStatus.RESERVED;
        } else {
            this.status = BookStatus.BORROWED;
        }
    }

    private void touch() {
        this.updatedAt = DateUtils.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String isbn;
        private String barcode;
        private String title;
        private String subtitle;
        private String author;
        private List<String> coAuthors;
        private String publisher;
        private String edition;
        private String language;
        private String category;
        private String subject;
        private List<String> keywords;
        private int publicationYear;
        private int totalPages;
        private String rack;
        private String shelf;
        private long purchasePricePaise;
        private LocalDate purchaseDate;
        private int totalQuantity;
        private int availableQuantity;
        private int reservedQuantity;
        private BookStatus status;
        private String description;
        private String coverImagePath;
        private String deweyDecimal;
        private String branchId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(String v) { this.id = v; return this; }
        public Builder isbn(String v) { this.isbn = v; return this; }
        public Builder barcode(String v) { this.barcode = v; return this; }
        public Builder title(String v) { this.title = v; return this; }
        public Builder subtitle(String v) { this.subtitle = v; return this; }
        public Builder author(String v) { this.author = v; return this; }
        public Builder coAuthors(List<String> v) { this.coAuthors = v; return this; }
        public Builder publisher(String v) { this.publisher = v; return this; }
        public Builder edition(String v) { this.edition = v; return this; }
        public Builder language(String v) { this.language = v; return this; }
        public Builder category(String v) { this.category = v; return this; }
        public Builder subject(String v) { this.subject = v; return this; }
        public Builder keywords(List<String> v) { this.keywords = v; return this; }
        public Builder publicationYear(int v) { this.publicationYear = v; return this; }
        public Builder totalPages(int v) { this.totalPages = v; return this; }
        public Builder rack(String v) { this.rack = v; return this; }
        public Builder shelf(String v) { this.shelf = v; return this; }
        public Builder purchasePricePaise(long v) { this.purchasePricePaise = v; return this; }
        public Builder purchaseDate(LocalDate v) { this.purchaseDate = v; return this; }
        public Builder totalQuantity(int v) { this.totalQuantity = v; return this; }
        public Builder availableQuantity(int v) { this.availableQuantity = v; return this; }
        public Builder reservedQuantity(int v) { this.reservedQuantity = v; return this; }
        public Builder status(BookStatus v) { this.status = v; return this; }
        public Builder description(String v) { this.description = v; return this; }
        public Builder coverImagePath(String v) { this.coverImagePath = v; return this; }
        public Builder deweyDecimal(String v) { this.deweyDecimal = v; return this; }
        public Builder branchId(String v) { this.branchId = v; return this; }
        public Builder createdAt(LocalDateTime v) { this.createdAt = v; return this; }
        public Builder updatedAt(LocalDateTime v) { this.updatedAt = v; return this; }

        public Book build() {
            return new Book(this);
        }
    }
}
