/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.connection;

/**
 * Simple implementation of the performance characteristics interface.
 */
public final class PerformanceCharacteristicsImpl implements PerformanceCharacteristics {

    private final boolean _queryOptimizationPreferred;
    private final boolean _naturalRecordOrderConsistent;

    /**
     * 
     * @param queryOptimizationPreferred
     * 
     * @deprecated use {@link #PerformanceCharacteristicsImpl(boolean, boolean)}
     *             instead.
     */
    @Deprecated
    public PerformanceCharacteristicsImpl(boolean queryOptimizationPreferred) {
        this(queryOptimizationPreferred, true);
    }

    public PerformanceCharacteristicsImpl(boolean queryOptimizationPreferred, boolean naturalRecordOrderConsistent) {
        _queryOptimizationPreferred = queryOptimizationPreferred;
        _naturalRecordOrderConsistent = naturalRecordOrderConsistent;
    }

    @Override
    public boolean isQueryOptimizationPreferred() {
        return _queryOptimizationPreferred;
    }

    @Override
    public boolean isNaturalRecordOrderConsistent() {
        return _naturalRecordOrderConsistent;
    }

}
