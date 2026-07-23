package com.library.repository;

import com.library.config.Constants;
import com.library.mapper.ILLMapper;
import com.library.model.InterLibraryLoan;

/**
 * JSON-backed repository for {@link InterLibraryLoan} entities.
 * No secondary indexes are required; all lookups use the primary ID index.
 */
public final class ILLRepository extends IndexedRepository<InterLibraryLoan, String> {

    public ILLRepository() {
        super(Constants.ILL_RECORDS_FILE, new ILLMapper(), InterLibraryLoan::getId);
    }

    @Override
    protected String secondaryKey(String indexName, InterLibraryLoan entity) {
        return null;
    }
}
