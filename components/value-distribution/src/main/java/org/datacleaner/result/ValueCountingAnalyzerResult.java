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
package org.datacleaner.result;

import java.util.Collection;

import org.datacleaner.api.AnalyzerResult;

/**
 * Interface for analyzer results that generally work as "value counters". Such
 * results have counted the occurrence of certain values, as well as some
 * (optional) value types like unique values, unexpected values etc.
 */
public interface ValueCountingAnalyzerResult extends AnalyzerResult {

    String getName();

    Collection<ValueFrequency> getValueCounts();

    /**
     * Gets a {@link ValueFrequency} list similar to that of
     * {@link #getValueCounts()}, but allowing the underlying implementation to
     * reduce the list by building {@link CompositeValueFrequency} objects if
     * necesary to reach the preferred maximum number of elements.
     *
     * @param preferredMaximum
     *            the preferred maximum number of elements. Note that this is a
     *            hint, but depending on the implementation and the data, it
     *            might not always be possible to reduce to this preferred
     *            maximum.
     * @return
     */
    Collection<ValueFrequency> getReducedValueFrequencies(int preferredMaximum);

    int getNullCount();

    int getTotalCount();

    Integer getCount(String value);

    Integer getDistinctCount();

    Integer getUniqueCount();

    Integer getUnexpectedValueCount();

    boolean hasAnnotatedRows(String value);

    AnnotatedRowsResult getAnnotatedRowsForValue(String value);

    AnnotatedRowsResult getAnnotatedRowsForNull();

    AnnotatedRowsResult getAnnotatedRowsForUnexpectedValues();

    Collection<String> getUniqueValues();

}
