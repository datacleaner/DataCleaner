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
package org.datacleaner.job.runner;

import junit.framework.TestCase;

import org.datacleaner.beans.NumberAnalyzer;
import org.datacleaner.beans.filter.EqualsFilter;
import org.datacleaner.beans.filter.NullCheckFilter;
import org.datacleaner.beans.filter.ValidationCategory;
import org.datacleaner.components.maxrows.MaxRowsFilter;
import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.configuration.AnalyzerBeansConfigurationImpl;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.job.concurrent.TaskListener;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.job.tasks.Task;
import org.datacleaner.lifecycle.LifeCycleHelper;
import org.datacleaner.test.TestHelper;
import org.datacleaner.util.SourceColumnFinder;
import org.junit.Assert;

public class RowProcessingMetricsImplTest extends TestCase {

    private Datastore datastore = TestHelper.createSampleDatabaseDatastore("orderdb");
    private AnalyzerBeansConfiguration configuration = new AnalyzerBeansConfigurationImpl()
            .replace(new DatastoreCatalogImpl(datastore));
    private AnalysisJob job;

    public void testGetExpectedRowCountNoFilter() throws Exception {
        AnalysisJobBuilder ajb = createAnalysisJobBuilder();

        job = ajb.toAnalysisJob();

        assertEquals(23, getExpectedRowCount());
    }

    private AnalysisJobBuilder createAnalysisJobBuilder() {
        AnalysisJobBuilder ajb = new AnalysisJobBuilder(configuration);
        ajb.setDatastore(datastore);
        ajb.addSourceColumns("PUBLIC.EMPLOYEES.EMPLOYEENUMBER");
        ajb.addAnalyzer(NumberAnalyzer.class).addInputColumns(ajb.getSourceColumns());
        return ajb;
    }

    public void testGetExpectedRowCountMaxRows() throws Exception {
        AnalysisJobBuilder ajb = createAnalysisJobBuilder();

        FilterComponentBuilder<MaxRowsFilter, MaxRowsFilter.Category> filter = ajb.addFilter(MaxRowsFilter.class);
        filter.getComponentInstance().setMaxRows(10);
        ajb.setDefaultRequirement(filter.getFilterOutcome(MaxRowsFilter.Category.VALID));

        job = ajb.toAnalysisJob();

        assertEquals(10, getExpectedRowCount());
    }

    public void testGetExpectedRowCountEquals() throws Exception {
        AnalysisJobBuilder ajb = createAnalysisJobBuilder();

        FilterComponentBuilder<EqualsFilter, ValidationCategory> filter = ajb.addFilter(EqualsFilter.class);
        filter.addInputColumns(ajb.getSourceColumns());
        filter.getComponentInstance().setValues(new String[] { "1002", "1165" });

        ajb.setDefaultRequirement(filter.getFilterOutcome(ValidationCategory.VALID));

        job = ajb.toAnalysisJob();

        assertEquals(2, getExpectedRowCount());
    }

    public void testGetExpectedRowCountMultipleFilters() throws Exception {
        AnalysisJobBuilder ajb = createAnalysisJobBuilder();

        // there's 21 records that are not 1056 or 1165
        FilterComponentBuilder<EqualsFilter, ValidationCategory> filter1 = ajb.addFilter(EqualsFilter.class);
        filter1.addInputColumns(ajb.getSourceColumns());
        filter1.getComponentInstance().setValues(new String[] { "1056", "1165" });

        // there's 1 record which has a reportsto value of null.
        FilterComponentBuilder<NullCheckFilter, NullCheckFilter.NullCheckCategory> filter2 = ajb
                .addFilter(NullCheckFilter.class);
        ajb.addSourceColumns("PUBLIC.EMPLOYEES.REPORTSTO");
        filter2.addInputColumn(ajb.getSourceColumnByName("reportsto"));
        filter2.getComponentInstance().setConsiderEmptyStringAsNull(true);
        filter2.setRequirement(filter1.getFilterOutcome(ValidationCategory.INVALID));

        ajb.getAnalyzerComponentBuilders().get(0)
                .setRequirement(filter2.getFilterOutcome(NullCheckFilter.NullCheckCategory.NOT_NULL));

        job = ajb.toAnalysisJob();

        assertEquals(21 - 1, getExpectedRowCount());
    }

    private int getExpectedRowCount() {
        final AnalysisListener analysisListener = new InfoLoggingAnalysisListener();
        final TaskRunner taskRunner = configuration.getTaskRunner();

        final LifeCycleHelper lifeCycleHelper = new LifeCycleHelper(configuration.getInjectionManager(job), null, true);
        SourceColumnFinder sourceColumnFinder = new SourceColumnFinder();
        sourceColumnFinder.addSources(job);

        final RowProcessingPublishers publishers = new RowProcessingPublishers(job, analysisListener, taskRunner,
                lifeCycleHelper, sourceColumnFinder);
        final RowProcessingPublisher publisher = publishers.getRowProcessingPublisher(publishers.getTables()[0]);
        publisher.initializeConsumers(new TaskListener() {
            @Override
            public void onError(Task arg0, Throwable t) {
                Assert.fail(t.getMessage());
            }
            
            @Override
            public void onComplete(Task arg0) {
            }
            
            @Override
            public void onBegin(Task arg0) {
            }
        });

        final RowProcessingMetricsImpl metrics = new RowProcessingMetricsImpl(publishers, publisher);

        return metrics.getExpectedRows();
    }
}
