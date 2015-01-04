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
package org.eobjects.analyzer.test.full.scenarios;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eobjects.analyzer.beans.filter.MaxRowsFilter;
import org.eobjects.analyzer.beans.filter.MaxRowsFilter.Category;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.runner.AnalysisListener;
import org.eobjects.analyzer.job.runner.AnalysisListenerAdaptor;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunner;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.job.runner.RowProcessingMetrics;
import org.eobjects.analyzer.test.MockAnalyzer;
import org.eobjects.analyzer.test.TestHelper;

import junit.framework.TestCase;

public class QueryOptimizationAndAnalysisListeningTest extends TestCase {

    public void testScenario() throws Exception {
        final List<Integer> rowNumbers = new ArrayList<Integer>();
        final AtomicInteger expectedRows = new AtomicInteger();

        final Datastore datastore = TestHelper.createSampleDatabaseDatastore("orderdb");
        final AnalyzerBeansConfiguration configuration = new AnalyzerBeansConfigurationImpl()
                .replace(new DatastoreCatalogImpl(datastore));
        final AnalysisListener analysisListener = new AnalysisListenerAdaptor() {
            @Override
            public void rowProcessingBegin(AnalysisJob job, RowProcessingMetrics metrics) {
                final int expected = metrics.getExpectedRows();
                expectedRows.set(expected);
            }

            @Override
            public void rowProcessingProgress(AnalysisJob job, RowProcessingMetrics metrics, int currentRow) {
                rowNumbers.add(currentRow);
            }
        };

        final AnalysisJob job;
        try (final AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(configuration)) {
            jobBuilder.setDatastore("orderdb");
            jobBuilder.addSourceColumns("customers.contactfirstname", "customers.contactlastname");

            final FilterJobBuilder<MaxRowsFilter, Category> filter = jobBuilder.addFilter(MaxRowsFilter.class);
            filter.getComponentInstance().setFirstRow(42);
            filter.getComponentInstance().setMaxRows(10);
            jobBuilder.setDefaultRequirement(filter, MaxRowsFilter.Category.VALID);

            final AnalyzerJobBuilder<MockAnalyzer> analyzer = jobBuilder.addAnalyzer(MockAnalyzer.class);
            analyzer.addInputColumns(jobBuilder.getSourceColumns());

            job = jobBuilder.toAnalysisJob();
        }

        final AnalysisRunner runner = new AnalysisRunnerImpl(configuration, analysisListener);
        AnalysisResultFuture resultFuture = runner.run(job);

        // task runner is single-threaded, so we expect it to be immediately
        // finished
        assertTrue(resultFuture.isSuccessful());

        assertEquals("10", expectedRows.toString());
        assertEquals("[1, 2, 3, 4, 5, 6, 7, 8, 9, 10]", rowNumbers.toString());
    }
}
