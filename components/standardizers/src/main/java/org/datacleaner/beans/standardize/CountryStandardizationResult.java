package org.datacleaner.beans.standardize;

import java.util.Map;

import org.datacleaner.result.CategorizationResult;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;

public class CountryStandardizationResult extends CategorizationResult {
    private static final long serialVersionUID = 1L;

    public CountryStandardizationResult(RowAnnotationFactory rowAnnotationFactory, Map<String, RowAnnotation> countryCountMap) {
        super(rowAnnotationFactory, countryCountMap);
    }
}
