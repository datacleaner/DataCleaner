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
package org.datacleaner.beans.filter;

import java.util.Date;
import java.util.List;

import org.apache.metamodel.util.DateUtils;
import org.apache.metamodel.util.Month;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.connection.Datastore;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.result.ListResult;
import org.datacleaner.test.MockAnalyzer;
import org.datacleaner.test.TestHelper;

import junit.framework.TestCase;

public class DateRangeFilterTest extends TestCase {

    public void testFilter() throws Exception {
        final DateRangeFilter filter =
                new DateRangeFilter(DateUtils.get(2010, Month.JANUARY, 1), DateUtils.get(2011, Month.JANUARY, 1));
        filter.validate();
        assertEquals(RangeFilterCategory.LOWER, filter.categorize((Date) null));
        assertEquals(RangeFilterCategory.LOWER, filter.categorize(DateUtils.get(2009, Month.JANUARY, 1)));
        assertEquals(RangeFilterCategory.LOWER, filter.categorize(DateUtils.get(0, Month.JANUARY, 1)));
        assertEquals(RangeFilterCategory.VALID, filter.categorize(DateUtils.get(2010, Month.FEBRUARY, 1)));
        assertEquals(RangeFilterCategory.VALID, filter.categorize(DateUtils.get(2010, Month.JANUARY, 1)));
        assertEquals(RangeFilterCategory.VALID, filter.categorize(DateUtils.get(2011, Month.JANUARY, 1)));
        assertEquals(RangeFilterCategory.HIGHER, filter.categorize(DateUtils.get(2012, Month.JANUARY, 1)));
    }

    public void testSameMaxAndMin() throws Exception {
        final DateRangeFilter filter =
                new DateRangeFilter(DateUtils.get(2010, Month.JANUARY, 1), DateUtils.get(2010, Month.JANUARY, 1));
        filter.validate();
        assertEquals(RangeFilterCategory.VALID, filter.categorize(DateUtils.get(2010, Month.JANUARY, 1)));
        assertEquals(RangeFilterCategory.LOWER, filter.categorize(DateUtils.get(2009, Month.JANUARY, 1)));
        assertEquals(RangeFilterCategory.HIGHER, filter.categorize(DateUtils.get(2012, Month.JANUARY, 1)));
    }

    @SuppressWarnings("resource")
    public void testQueryOptimize() throws Throwable {
        final Datastore ds = TestHelper.createSampleDatabaseDatastore("orderdb");
        final DataCleanerConfiguration conf = new DataCleanerConfigurationImpl().withDatastores(ds);
        final AnalysisJobBuilder ajb = new AnalysisJobBuilder(conf);
        ajb.setDatastore(ds);
        ajb.addSourceColumns("orders.orderdate");
        final FilterComponentBuilder<DateRangeFilter, RangeFilterCategory> fjb = ajb.addFilter(DateRangeFilter.class);
        final InputColumn<?> column = ajb.getSourceColumnByName("orderdate");
        fjb.addInputColumn(column);
        fjb.setConfiguredProperty("Lowest value", DateUtils.get(2003, Month.MARCH, 10));
        fjb.setConfiguredProperty("Highest value", DateUtils.get(2003, Month.MARCH, 24));

        assertTrue(fjb.isConfigured());

        ajb.addAnalyzer(MockAnalyzer.class).addInputColumn(column).setRequirement(fjb, RangeFilterCategory.VALID);

        final AnalysisRunnerImpl runner = new AnalysisRunnerImpl(conf);
        final AnalysisJob job = ajb.toAnalysisJob();
        final AnalysisResultFuture resultFuture = runner.run(job);
        if (resultFuture.isErrornous()) {
            throw resultFuture.getErrors().get(0);
        }

        final AnalyzerResult analyzerResult = resultFuture.getResults().get(0);
        assertEquals(ListResult.class, analyzerResult.getClass());

        @SuppressWarnings("unchecked") final ListResult<InputRow> listResult = (ListResult<InputRow>) analyzerResult;
        final List<InputRow> values = listResult.getValues();

        assertEquals("[MetaModelInputRow[Row[values=[2003-03-10 00:00:00.0]]], "
                + "MetaModelInputRow[Row[values=[2003-03-18 00:00:00.0]]], "
                + "MetaModelInputRow[Row[values=[2003-03-24 00:00:00.0]]]]", values.toString());

        ajb.close();
    }
}
