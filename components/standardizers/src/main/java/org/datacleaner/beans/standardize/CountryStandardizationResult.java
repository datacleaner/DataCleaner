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

import java.util.Map;

import org.datacleaner.result.CategorizationResult;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;

public class CountryStandardizationResult extends CategorizationResult {
    private static final long serialVersionUID = 1L;

    private final int _unrecognizedCountries;

    public CountryStandardizationResult(final RowAnnotationFactory rowAnnotationFactory,
            final Map<String, RowAnnotation> countryCountMap, final int unrecognizedCountries) {
        super(rowAnnotationFactory, countryCountMap);
        _unrecognizedCountries = unrecognizedCountries;
    }

    public int getUnrecognizedCountries() {
        return _unrecognizedCountries;
    }
}
