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
package org.datacleaner.monitor.server.controllers;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.configuration.TenantContextFactoryImpl;
import org.datacleaner.monitor.server.job.MockJobEngineManager;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.file.FileRepository;

public class JobInvocationControllerTest extends TestCase {

    public void testInvokeDatabaseSchema() throws Throwable {
        final Repository repository = new FileRepository("src/test/resources/example_repo");
        final TenantContextFactory contextFactory = new TenantContextFactoryImpl(repository,
                new DataCleanerEnvironmentImpl(), new MockJobEngineManager());
        final JobInvocationController controller = new JobInvocationController();
        controller._contextFactory = contextFactory;

        final JobInvocationPayload sourceRecords = new JobInvocationPayload();
        sourceRecords.addRow(new Object[] { "kasper@eobjects.dk" });
        sourceRecords.addRow(new Object[] { "kasper.sorensen@humaninference.com" });

        JobInvocationPayload result = controller.invokeJob("tenant1", "email_standardizer", sourceRecords);

        assertEquals("[Username, Domain]", result.getColumns().toString());

        List<JobInvocationRowData> rows = result.getRows();
        assertEquals(2, rows.size());

        assertEquals("[kasper, eobjects.dk]", Arrays.toString(rows.get(0).getValues()));
        assertEquals("[kasper.sorensen, humaninference.com]", Arrays.toString(rows.get(1).getValues()));
    }

    public void testInvokeFileWithExtensionNameSchema() throws Throwable {
        final Repository repository = new FileRepository("src/test/resources/example_repo");
        final TenantContextFactory contextFactory = new TenantContextFactoryImpl(repository,
                new DataCleanerEnvironmentImpl(), new MockJobEngineManager());
        final JobInvocationController controller = new JobInvocationController();
        controller._contextFactory = contextFactory;

        final JobInvocationPayload sourceRecords = new JobInvocationPayload();

        final int input = 123;
        sourceRecords.addRow(new Object[] { input });

        JobInvocationPayload result = controller.invokeJob("tenant1", "random_number_generation", sourceRecords);

        assertEquals("[Random number]", result.getColumns().toString());

        List<JobInvocationRowData> rows = result.getRows();
        assertEquals(1, rows.size());

        final Object[] values = rows.get(0).getValues();
        assertEquals(1, values.length);
        final Number number = (Number) values[0];
        assertTrue(number.doubleValue() >= 0);
        assertTrue(number.doubleValue() <= input);
    }

    public void testInvokeFileWithoutAnalyzers() throws Throwable {
        final Repository repository = new FileRepository("src/test/resources/example_repo");
        final TenantContextFactory contextFactory = new TenantContextFactoryImpl(repository,
                new DataCleanerEnvironmentImpl(), new MockJobEngineManager());
        final JobInvocationController controller = new JobInvocationController();
        controller._contextFactory = contextFactory;

        final JobInvocationPayload sourceRecords = new JobInvocationPayload();

        sourceRecords.addRow(new Object[] { "foo", "bar" });

        final JobInvocationPayload result = controller.invokeJob("tenant1", "concat_job_no_analyzers", sourceRecords);

        final List<JobInvocationRowData> rows = result.getRows();
        assertEquals(1, rows.size());

        final Object[] values = rows.get(0).getValues();
        assertEquals("[bar foo]", Arrays.toString(values));
    }

    public void testInvokeFileWithShortColumnPaths() throws Throwable {
        final Repository repository = new FileRepository("src/test/resources/example_repo");
        final TenantContextFactory contextFactory = new TenantContextFactoryImpl(repository,
                new DataCleanerEnvironmentImpl(), new MockJobEngineManager());
        final JobInvocationController controller = new JobInvocationController();
        controller._contextFactory = contextFactory;

        final JobInvocationPayload sourceRecords = new JobInvocationPayload();

        sourceRecords.addRow(new Object[] { "foo", "bar" });

        final JobInvocationPayload result = controller.invokeJob("tenant1", "concat_job_short_column_paths",
                sourceRecords);

        final List<JobInvocationRowData> rows = result.getRows();
        assertEquals(1, rows.size());

        final Object[] values = rows.get(0).getValues();
        assertEquals("[bar foo]", Arrays.toString(values));
    }

    public void testInvokeFileWithoutDatastoreMatch() throws Throwable {
        final Repository repository = new FileRepository("src/test/resources/example_repo");
        final TenantContextFactory contextFactory = new TenantContextFactoryImpl(repository,
                new DataCleanerEnvironmentImpl(), new MockJobEngineManager());
        final JobInvocationController controller = new JobInvocationController();
        controller._contextFactory = contextFactory;

        final JobInvocationPayload sourceRecords = new JobInvocationPayload();

        sourceRecords.addRow(new Object[] { "foo", "bar" });

        final JobInvocationPayload result = controller.invokeJob("tenant1", "concat_job_no_datastore", sourceRecords);

        final List<JobInvocationRowData> rows = result.getRows();
        assertEquals(1, rows.size());

        final Object[] values = rows.get(0).getValues();
        assertEquals("[bar foo]", Arrays.toString(values));
    }
}
