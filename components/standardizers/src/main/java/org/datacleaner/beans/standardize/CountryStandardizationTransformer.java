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

import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.MatchingAndStandardizationCategory;

@Named("Country standardizer")
@Description("Allows you to standardize the country names and codes used throughout your database")
@Categorized(MatchingAndStandardizationCategory.class)
public class CountryStandardizationTransformer implements Transformer {

    public static final String PROPERTY_COUNTRY_COLUMN = "Country column";
    public static final String PROPERTY_OUTPUT_FORMAT = "Output format";
    public static final String PROPERTY_DEFAULT_COUNTRY = "Default country";

    public static enum OutputFormat {
        ISO2, ISO3, NAME
    }

    @Configured(PROPERTY_COUNTRY_COLUMN)
    InputColumn<String> countryColumn;

    @Configured(PROPERTY_OUTPUT_FORMAT)
    OutputFormat outputFormat = OutputFormat.ISO2;

    @Configured(value = PROPERTY_DEFAULT_COUNTRY, required = false)
    Country defaultCountry = null;

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(String.class, countryColumn.getName() + " (standardized)");
    }

    @Override
    public String[] transform(InputRow inputRow) {
        final String value = inputRow.getValue(countryColumn);
        final Country country = Country.find(value, defaultCountry);
        if (country == null) {
            return new String[1];
        }
        switch (outputFormat) {
        case ISO2:
            return new String[] { country.getTwoLetterISOCode() };
        case ISO3:
            return new String[] { country.getThreeLetterISOCode() };
        case NAME:
            return new String[] { country.getCountryName() };
        default:
            throw new IllegalStateException("Unexpected output format: " + outputFormat);
        }
    }

}
