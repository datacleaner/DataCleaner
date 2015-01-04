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
package org.datacleaner.beans.valuedist;

import org.datacleaner.result.AbstractValueCountingAnalyzerResult;
import org.datacleaner.result.Metric;

/**
 * Defines abstract methods, metrics etc. for analyzer results of the Value
 * Distribution analyzer.
 */
public abstract class ValueDistributionAnalyzerResult extends AbstractValueCountingAnalyzerResult {

    private static final long serialVersionUID = 1L;

    @Metric("Total count")
    @Override
    public abstract int getTotalCount();

    @Metric("Null count")
    @Override
    public abstract int getNullCount();

    @Metric("Unique count")
    @Override
    public abstract Integer getUniqueCount();

    @Metric("Distinct count")
    @Override
    public abstract Integer getDistinctCount();
}
