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
package org.datacleaner.test.full.scenarios;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Named;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.metamodel.schema.Column;
import org.datacleaner.api.Analyzer;
import org.datacleaner.api.Close;
import org.datacleaner.api.Configured;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.configuration.AnalyzerBeansConfigurationImpl;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.datacleaner.connection.SchemaNavigator;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.concurrent.PreviousErrorsExistException;
import org.datacleaner.job.runner.AnalysisJobFailedException;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.result.NumberResult;
import org.datacleaner.test.ActivityAwareMultiThreadedTaskRunner;
import org.datacleaner.test.TestHelper;
import org.datacleaner.util.CollectionUtils2;

/**
 * Tests that a job where one of the row processing consumers fail is gracefully
 * error handled.
 * 
 * 
 */
public class ErrorInRowProcessingConsumerTest extends TestCase {

    private static final AtomicBoolean closed = new AtomicBoolean();

    @Named("Errornous analyzer")
    public static class ErrornousAnalyzer implements Analyzer<NumberResult> {

        private final AtomicInteger counter = new AtomicInteger(0);

        @Configured
        InputColumn<String> inputColumn;

        @Override
        public NumberResult getResult() {
            return new NumberResult(counter.get());
        }

        @Override
        public void run(InputRow row, int distinctCount) {
            assertNotNull(inputColumn);
            assertNotNull(row);
            assertEquals(1, distinctCount);
            String value = row.getValue(inputColumn);
            assertNotNull(value);
            int count = counter.incrementAndGet();
            if (count == 3) {
                throw new IllegalStateException("This analyzer can only analyze two rows!");
            }
        }

        @Close
        public void close() {
            closed.set(true);
        }

    }

    public void testScenario() throws Exception {
        closed.set(false);

        ActivityAwareMultiThreadedTaskRunner taskRunner = new ActivityAwareMultiThreadedTaskRunner();

        Datastore datastore = TestHelper.createSampleDatabaseDatastore("my db");
        AnalyzerBeansConfiguration conf = new AnalyzerBeansConfigurationImpl().replace(taskRunner).replace(
                new DatastoreCatalogImpl(datastore));

        AnalysisJob job;
        try (AnalysisJobBuilder ajb = new AnalysisJobBuilder(conf)) {
            ajb.setDatastore(datastore);
            
            SchemaNavigator schemaNavigator = datastore.openConnection().getSchemaNavigator();
            Column column = schemaNavigator.convertToColumn("PUBLIC.EMPLOYEES.EMAIL");
            assertNotNull(column);
            
            ajb.addSourceColumn(column);
            ajb.addAnalyzer(ErrornousAnalyzer.class).addInputColumn(ajb.getSourceColumns().get(0));
            
            job = ajb.toAnalysisJob();
        }

        AnalysisResultFuture resultFuture = new AnalysisRunnerImpl(conf).run(job);

        assertTrue(resultFuture.isErrornous());

        // isErrornous should be blocking
        assertTrue(resultFuture.isDone());

        try {
            resultFuture.getResults();
            fail("Exception expected");
        } catch (AnalysisJobFailedException e) {
            String message = e.getMessage();
            assertEquals("The analysis ended with 2 errors: ["
                    + "IllegalStateException: This analyzer can only analyze two rows!,"
                    + "PreviousErrorsExistException: A previous exception has occurred]", message);
        }

        List<Throwable> errors = resultFuture.getErrors();

        // the amount of errors may vary depending on the thread scheduling
        int numErrors = errors.size();
        assertTrue(numErrors == 2 || numErrors == 3);

        // sort the errors to make the order deterministic
        errors = CollectionUtils2.sorted(errors, new Comparator<Throwable>() {
            @Override
            public int compare(Throwable o1, Throwable o2) {
                return o1.getClass().getName().compareTo(o2.getClass().getName());
            }
        });

        assertEquals(IllegalStateException.class, errors.get(0).getClass());
        assertEquals("This analyzer can only analyze two rows!", errors.get(0).getMessage());

        assertTrue(numErrors + " errors found, 2 or 3 expected!", numErrors == 2 || numErrors == 3);

        if (numErrors == 3) {
            // this is caused by the assertion
            // ("assertEquals(1, distinctCount);")
            // above
            assertEquals(AssertionFailedError.class, errors.get(1).getClass());
            assertEquals("expected:<1> but was:<2>", errors.get(1).getMessage());
            assertEquals(PreviousErrorsExistException.class, errors.get(2).getClass());
            assertEquals("A previous exception has occurred", errors.get(2).getMessage());
        } else {
            assertEquals(PreviousErrorsExistException.class, errors.get(1).getClass());
            assertEquals("A previous exception has occurred", errors.get(1).getMessage());
        }

        int taskCount = taskRunner.assertAllBegunTasksFinished(500);
        assertTrue("taskCount was: " + taskCount, taskCount > 4);

        assertTrue(closed.get());
    }
}
