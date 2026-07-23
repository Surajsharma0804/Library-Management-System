package com.library.pbt;

import com.library.enums.BookStatus;
import com.library.mapper.BookMapper;
import com.library.model.Book;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.StringLength;

import java.time.LocalDate;
import java.util.Map;

/**
 * Property-based test: Book mapper round-trip preservation.
 * **Validates: Requirements 1.3, 2.1** (data integrity in JSON codec)
 */
class JsonCodecRoundTripTest {

    private final BookMapper mapper = new BookMapper();

    @Property
    @Report(Reporting.GENERATED)
    void bookRoundTripPreservesIdentity(
            @ForAll @StringLength(min = 3, max = 20) String id,
            @ForAll @StringLength(min = 10, max = 17) String isbn,
            @ForAll @StringLength(min = 5, max = 100) String title,
            @ForAll @StringLength(min = 3, max = 60) String author,
            @ForAll @IntRange(min = 1, max = 100) int totalQuantity
    ) {
        Book original = Book.builder()
                .id(id)
                .isbn(isbn)
                .title(title)
                .author(author)
                .totalQuantity(totalQuantity)
                .availableQuantity(totalQuantity)
                .reservedQuantity(0)
                .status(BookStatus.AVAILABLE)
                .publicationYear(2000)
                .build();

        Map<String, Object> map = mapper.toMap(original);
        Book decoded = mapper.fromMap(map);

        assert decoded.getId().equals(original.getId()) :
                "ID not preserved: expected " + original.getId() + ", got " + decoded.getId();
        assert decoded.getIsbn().equals(original.getIsbn()) :
                "ISBN not preserved";
        assert decoded.getTitle().equals(original.getTitle()) :
                "Title not preserved";
        assert decoded.getAuthor().equals(original.getAuthor()) :
                "Author not preserved";
        assert decoded.getTotalQuantity() == original.getTotalQuantity() :
                "TotalQuantity not preserved";
    }
}
