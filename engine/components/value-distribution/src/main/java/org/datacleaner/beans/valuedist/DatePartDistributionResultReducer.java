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

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.datacleaner.result.AbstractCrosstabResultReducer;
import org.datacleaner.result.AnalyzerResultReducer;
import org.datacleaner.result.Crosstab;
import org.datacleaner.result.CrosstabDimension;
import org.datacleaner.result.CrosstabResult;

/**
 * {@link AnalyzerResultReducer} for results of the date-part distribution
 * analyzers, for instance {@link YearDistributionAnalyzer},
 * {@link MonthDistributionAnalyzer}, {@link WeekdayDistributionAnalyzer} and
 * {@link WeekNumberDistributionAnalyzer}.
 */
public class DatePartDistributionResultReducer extends AbstractCrosstabResultReducer<CrosstabResult> {

    @Override
    protected Serializable reduceValues(List<Object> values, String arg1, String arg2,
            Collection<? extends CrosstabResult> arg3, Class<?> arg4) {
        // all measures in these crosstabs are summable.
        return sum(values);
    }

    @Override
    protected CrosstabResult buildResult(Crosstab<?> crosstab, Collection<? extends CrosstabResult> results) {
        return new CrosstabResult(crosstab);
    }

    @Override
    protected Crosstab<Serializable> createMasterCrosstab(Collection<? extends CrosstabResult> results) {
        final CrosstabResult firstResult = results.iterator().next();
        final Class<?> valueClass = firstResult.getCrosstab().getValueClass();
        final Set<String> categories1 = createColumnDimensionCategorySet();
        final Set<String> categories2 = createMeasureDimensionCategorySet();

        for (CrosstabResult crosstabResult : results) {
            Crosstab<?> crosstab = crosstabResult.getCrosstab();
            categories1.addAll(crosstab.getDimension(0).getCategories());
            categories2.addAll(crosstab.getDimension(1).getCategories());
        }

        final CrosstabDimension dimension1 = new CrosstabDimension(firstResult.getCrosstab().getDimension(0).getName());
        dimension1.addCategories(categories1);

        final CrosstabDimension dimension2 = new CrosstabDimension(firstResult.getCrosstab().getDimension(1).getName());
        dimension2.addCategories(categories2);

        @SuppressWarnings({ "unchecked", "rawtypes" })
        final Crosstab<Serializable> crosstab = new Crosstab(valueClass, dimension1, dimension2);
        return crosstab;
    }
    

    protected Set<String> createMeasureDimensionCategorySet() {
        return new TreeSet<String>();
    }

    protected Set<String> createColumnDimensionCategorySet() {
        return new TreeSet<String>();
    }
}
