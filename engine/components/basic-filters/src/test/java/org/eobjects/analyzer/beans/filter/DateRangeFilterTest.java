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
package org.eobjects.analyzer.beans.filter;

import java.util.Date;
import java.util.List;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.ListResult;
import org.eobjects.analyzer.test.MockAnalyzer;
import org.eobjects.analyzer.test.TestHelper;
import org.apache.metamodel.util.DateUtils;
import org.apache.metamodel.util.Month;

import junit.framework.TestCase;

public class DateRangeFilterTest extends TestCase {

    public void testFilter() throws Exception {
        DateRangeFilter filter = new DateRangeFilter(DateUtils.get(2010, Month.JANUARY, 1), DateUtils.get(2011,
                Month.JANUARY, 1));
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
        DateRangeFilter filter = new DateRangeFilter(DateUtils.get(2010, Month.JANUARY, 1), DateUtils.get(2010,
                Month.JANUARY, 1));
        filter.validate();
        assertEquals(RangeFilterCategory.VALID, filter.categorize(DateUtils.get(2010, Month.JANUARY, 1)));
        assertEquals(RangeFilterCategory.LOWER, filter.categorize(DateUtils.get(2009, Month.JANUARY, 1)));
        assertEquals(RangeFilterCategory.HIGHER, filter.categorize(DateUtils.get(2012, Month.JANUARY, 1)));
    }

    @SuppressWarnings("resource")
    public void testQueryOptimize() throws Throwable {
        Datastore ds = TestHelper.createSampleDatabaseDatastore("orderdb");
        AnalyzerBeansConfiguration conf = new AnalyzerBeansConfigurationImpl().replace(new DatastoreCatalogImpl(ds));
        AnalysisJobBuilder ajb = new AnalysisJobBuilder(conf);
        ajb.setDatastore(ds);
        ajb.addSourceColumns("orders.orderdate");
        FilterJobBuilder<DateRangeFilter, RangeFilterCategory> fjb = ajb.addFilter(DateRangeFilter.class);
        InputColumn<?> column = ajb.getSourceColumnByName("orderdate");
        fjb.addInputColumn(column);
        fjb.setConfiguredProperty("Lowest value", DateUtils.get(2003, Month.MARCH, 10));
        fjb.setConfiguredProperty("Highest value", DateUtils.get(2003, Month.MARCH, 24));

        assertTrue(fjb.isConfigured());

        ajb.addAnalyzer(MockAnalyzer.class).addInputColumn(column).setRequirement(fjb, RangeFilterCategory.VALID);

        AnalysisRunnerImpl runner = new AnalysisRunnerImpl(conf);
        AnalysisJob job = ajb.toAnalysisJob();
        AnalysisResultFuture resultFuture = runner.run(job);
        if (resultFuture.isErrornous()) {
            throw resultFuture.getErrors().get(0);
        }

        AnalyzerResult analyzerResult = resultFuture.getResults().get(0);
        assertEquals(ListResult.class, analyzerResult.getClass());

        @SuppressWarnings("unchecked")
        ListResult<InputRow> listResult = (ListResult<InputRow>) analyzerResult;
        List<InputRow> values = listResult.getValues();

        assertEquals("[MetaModelInputRow[Row[values=[2003-03-10 00:00:00.0]]], "
                + "MetaModelInputRow[Row[values=[2003-03-18 00:00:00.0]]], "
                + "MetaModelInputRow[Row[values=[2003-03-24 00:00:00.0]]]]", values.toString());
        
        ajb.close();
    }
}
