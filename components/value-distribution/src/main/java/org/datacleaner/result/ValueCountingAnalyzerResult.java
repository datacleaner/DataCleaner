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

    public String getName();

    public Collection<ValueFrequency> getValueCounts();

    public int getNullCount();

    public int getTotalCount();

    public Integer getCount(String value);

    public Integer getDistinctCount();

    public Integer getUniqueCount();

    public Integer getUnexpectedValueCount();

    public boolean hasAnnotatedRows(String value);

    public AnnotatedRowsResult getAnnotatedRowsForValue(String value);

    public AnnotatedRowsResult getAnnotatedRowsForNull();

    public AnnotatedRowsResult getAnnotatedRowsForUnexpectedValues();

    public Collection<String> getUniqueValues();

}
