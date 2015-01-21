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
package org.datacleaner.configuration;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Named;

import junit.framework.TestCase;

import org.datacleaner.api.Analyzer;
import org.datacleaner.api.ColumnProperty;
import org.datacleaner.api.Configured;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.result.NumberResult;
import org.datacleaner.test.TestHelper;
import org.junit.Ignore;

public class InjectionManagerFactoryTest extends TestCase {

    @Ignore
    @Named("Fancy analyzer")
    public static class FancyAnalyzer implements Analyzer<NumberResult> {

        @Inject
        AtomicInteger fancyInjection;

        @Configured
        @ColumnProperty(escalateToMultipleJobs = true)
        InputColumn<Object> col;

        @Override
        public void run(InputRow row, int distinctCount) {
        }

        @Override
        public NumberResult getResult() {
            return new NumberResult(fancyInjection.get());
        }
    }

    public void testInjectCustomClass() throws Exception {
        final AtomicBoolean touched = new AtomicBoolean(false);

        final InjectionManager injectionManager = new InjectionManager() {
            @SuppressWarnings("unchecked")
            @Override
            public <E> E getInstance(InjectionPoint<E> injectionPoint) {
                touched.set(true);
                assertEquals(AtomicInteger.class, injectionPoint.getBaseType());
                return (E) new AtomicInteger(42);
            }
        };

        final InjectionManagerFactory injectionManagerFactory = new InjectionManagerFactory() {
            @Override
            public InjectionManager getInjectionManager(AnalyzerBeansConfiguration conf, AnalysisJob job) {
                return injectionManager;
            }
        };

        final AnalyzerBeansConfigurationImpl conf = new AnalyzerBeansConfigurationImpl().replace(
                new DatastoreCatalogImpl(TestHelper.createSampleDatabaseDatastore("orderdb"))).replace(
                injectionManagerFactory);

        try (final AnalysisJobBuilder ajb = new AnalysisJobBuilder(conf)) {

            ajb.setDatastore("orderdb");
            ajb.addSourceColumns("PUBLIC.EMPLOYEES.EMPLOYEENUMBER");

            final AnalyzerComponentBuilder<FancyAnalyzer> analyzerBuilder = ajb.addAnalyzer(FancyAnalyzer.class);
            analyzerBuilder.addInputColumns(ajb.getSourceColumns());

            final AnalysisResultFuture result = new AnalysisRunnerImpl(conf).run(ajb.toAnalysisJob());
            assertTrue(result.isSuccessful());
            assertTrue(touched.get());
            assertEquals("42", result.getResults().get(0).toString());

        }
    }
}
