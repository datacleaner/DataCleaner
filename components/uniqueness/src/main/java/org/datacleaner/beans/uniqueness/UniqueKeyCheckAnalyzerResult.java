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
package org.datacleaner.beans.uniqueness;

import java.util.Map;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.Metric;

/**
 * {@link AnalyzerResult} class for {@link UniqueKeyCheckAnalyzer}.
 */
public class UniqueKeyCheckAnalyzerResult implements AnalyzerResult {

    private static final long serialVersionUID = 1L;

    private final int _nonUniqueCount;
    private final int _rowCount;
    private final int _uniqueCount;
    private final int _nullCount;
    private final Map<String, Integer> _nonUniqueSamples;

    public UniqueKeyCheckAnalyzerResult(final int rowCount, final int uniqueCount, final int nonUniqueCount,
            final int nullCount, final Map<String, Integer> samples) {
        _rowCount = rowCount;
        _uniqueCount = uniqueCount;
        _nonUniqueCount = nonUniqueCount;
        _nullCount = nullCount;
        _nonUniqueSamples = samples;
    }

    @Metric("Row count")
    public int getRowCount() {
        return _rowCount;
    }

    @Metric("Null count")
    public int getNullCount() {
        return _nullCount;
    }

    @Metric("Unique count")
    public int getUniqueCount() {
        return _uniqueCount;
    }

    @Metric("Non-unique count")
    public int getNonUniqueCount() {
        return _nonUniqueCount;
    }

    /**
     * Gets samples of the non-unique values
     *
     * @return
     */
    public Map<String, Integer> getNonUniqueSamples() {
        return _nonUniqueSamples;
    }

    @Override
    public String toString() {
        return "Unique key check result:" + "\n - Row count: " + getRowCount() + "\n - Null count: " + getNullCount()
                + "" + "\n - Unique count: " + getUniqueCount() + "\n - Non-unique count: " + getNonUniqueCount();
    }

}
