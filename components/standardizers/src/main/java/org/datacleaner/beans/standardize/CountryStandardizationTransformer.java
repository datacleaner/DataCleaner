/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.datacleaner.beans.standardize;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.HasAnalyzerResult;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Provided;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.MatchingAndStandardizationCategory;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;
import org.datacleaner.util.LabelUtils;

@Named("Country standardizer")
@Description("Allows you to standardize the country names and codes used throughout your database")
@Categorized(MatchingAndStandardizationCategory.class)
public class CountryStandardizationTransformer implements Transformer, HasAnalyzerResult<CountryStandardizationResult> {

    public static final String PROPERTY_COUNTRY_COLUMN = "Country column";
    public static final String PROPERTY_OUTPUT_FORMAT = "Output format";
    public static final String PROPERTY_DEFAULT_COUNTRY = "Default country";

    public final Map<String, RowAnnotation> countryCountMap = new HashMap<>();
    @Configured(PROPERTY_COUNTRY_COLUMN)
    InputColumn<String> countryColumn;
    @Configured(PROPERTY_OUTPUT_FORMAT)
    OutputFormat outputFormat = OutputFormat.ISO2;
    @Configured(value = PROPERTY_DEFAULT_COUNTRY, required = false)
    Country defaultCountry = null;
    @Provided
    @Inject
    RowAnnotationFactory _rowAnnotationFactory;
    AtomicInteger _unrecognizedCountries = new AtomicInteger(0);

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(String.class, countryColumn.getName() + " (standardized)");
    }

    @Override
    public String[] transform(InputRow inputRow) {
        final String value = inputRow.getValue(countryColumn);
        Country country = Country.find(value);

        if (country == null) {
            _unrecognizedCountries.incrementAndGet();
            country = defaultCountry;
        }

        final String countryName;
        if (country == null) {
            countryName = null;
        } else {
            switch (outputFormat) {
            case ISO2:
                countryName = country.getTwoLetterISOCode();
                break;
            case ISO3:
                countryName = country.getThreeLetterISOCode();
                break;
            case NAME:
                countryName = country.getCountryName();
                break;
            default:
                throw new IllegalStateException("Unexpected output format: " + outputFormat);
            }
        }

        final String correctedCountryName;

        if (countryName != null) {
            correctedCountryName = countryName;
        } else {
            correctedCountryName = LabelUtils.UNEXPECTED_LABEL;
        }

        final RowAnnotation annotation;
        // ConcurrentHashMap does not support null keys
        synchronized (this) {
            if (!countryCountMap.containsKey(correctedCountryName)) {
                countryCountMap.put(correctedCountryName, _rowAnnotationFactory.createAnnotation());
            }
            annotation = countryCountMap.get(correctedCountryName);
        }
        _rowAnnotationFactory.annotate(inputRow, 1, annotation);

        return new String[]{countryName};
    }

    @Override
    public CountryStandardizationResult getResult() {
        return new CountryStandardizationResult(_rowAnnotationFactory, countryCountMap, _unrecognizedCountries.intValue());
    }

    public static enum OutputFormat {
        ISO2, ISO3, NAME
    }

}
