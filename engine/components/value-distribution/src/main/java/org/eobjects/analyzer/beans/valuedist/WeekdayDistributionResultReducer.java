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
package org.eobjects.analyzer.beans.valuedist;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eobjects.analyzer.result.AnalyzerResultReducer;

/**
 * {@link AnalyzerResultReducer} for results of the date-part distribution
 * analyzers, for instance {@link YearDistributionAnalyzer},
 * {@link MonthDistributionAnalyzer}, {@link WeekdayDistributionAnalyzer} and
 * {@link WeekNumberDistributionAnalyzer}.
 */
public class WeekdayDistributionResultReducer extends DatePartDistributionResultReducer {

    @Override
    protected Set<String> createMeasureDimensionCategorySet() {
        // retain the order of the measures (weekday names)
        return new LinkedHashSet<String>();
    }
}
