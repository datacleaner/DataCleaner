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
package org.datacleaner.monitor.server.xml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.Ref;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.monitor.configuration.ConfigurationFactory;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.configuration.TenantContextFactoryImpl;
import org.datacleaner.monitor.job.JobContext;
import org.datacleaner.monitor.server.jaxb.JaxbConfigurationInterceptor;
import org.datacleaner.monitor.server.job.DataCleanerJobContext;
import org.datacleaner.monitor.server.job.MockJobEngineManager;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.repository.file.FileRepository;

import junit.framework.TestCase;

public class JaxbConfigurationInterceptorTest extends TestCase {

    private TenantContextFactoryImpl _contextFactory;
    private ConfigurationFactory _configurationFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        _configurationFactory = new ConfigurationFactory();
        _configurationFactory.setNumThreads(10);
        _configurationFactory.setScannedPackages(Arrays.asList("org.datacleaner", "com.hi"));

        final Repository repository = new FileRepository("src/test/resources/example_repo");

        _contextFactory =
                new TenantContextFactoryImpl(repository, new DataCleanerEnvironmentImpl(), new MockJobEngineManager());
    }

    public void testGenerateGenericConfiguration() throws Exception {
        final String actual = generationConf(null, null);

        String expected =
                FileHelper.readFileAsString(new File("src/test/resources/expected_conf_file_generic.xml"), "UTF-8");
        expected = expected.replaceAll("\r\n", "\n").trim();

        if (!expected.equals(actual)) {
            final URL orderDbScript = ClassLoader.getSystemResource("orderdb.script");
            System.out.println("!! Seems there is some issue in resolving the correct orderdb file: " + orderDbScript);

            assertEquals(expected, actual);
        }
    }

    public void testGenerateDatastoreSpecificConfiguration() throws Exception {
        final String actual = generationConf(null, "orderdb");

        String expected = FileHelper
                .readFileAsString(new File("src/test/resources/expected_conf_file_specific_datastore.xml"), "UTF-8");
        expected = expected.replaceAll("\r\n", "\n").trim();

        assertEquals(expected, actual);
    }

    public void testGenerateJobSpecificConfigurationSourceOnly() throws Exception {
        final JobContext job = _contextFactory.getContext("tenant1").getJob("email_standardizer");
        final String actual = generationConf(job, null);

        String expected = FileHelper
                .readFileAsString(new File("src/test/resources/expected_conf_file_specific_source_only.xml"), "UTF-8");
        expected = expected.replaceAll("\r\n", "\n").trim();

        assertEquals(expected, actual);
    }

    public void testGenerateJobSpecificConfigurationTableLookupAnotherDatastore() throws Exception {
        final JobContext job = _contextFactory.getContext("tenant1").getJob("lookup_vendor");
        final String actual = generationConf(job, null);

        String expected = FileHelper
                .readFileAsString(new File("src/test/resources/expected_conf_file_specific_lookup_another_ds.xml"),
                        "UTF-8");
        expected = expected.replaceAll("\r\n", "\n").trim();

        assertEquals(expected, actual);
    }

    public void testGenerateWithLookupInsertUpdate() throws Exception {
        final TenantContext tenantContext = _contextFactory.getContext("tenant1");
        final Datastore ds = tenantContext.getConfiguration().getDatastoreCatalog().getDatastore("orderdb");

        try (DatastoreConnection con = ds.openConnection()) {
            final JobContext job = tenantContext.getJob("Move employees to customers");
            final String actual = generationConf(job, null);

            String expected = FileHelper
                    .readFileAsString(new File("src/test/resources/expected_conf_file_lookup_insert_update.xml"),
                            "UTF-8");

            expected = expected.replaceAll("\r\n", "\n").trim();
            assertEquals(expected, actual);
        }
    }

    private String generationConf(final JobContext jobContext, final String datastoreName) throws Exception {
        final DataCleanerJobContext job = (DataCleanerJobContext) jobContext;

        final Ref<Calendar> dateRef = () -> {
            final Calendar cal = Calendar.getInstance();
            cal.setTimeZone(TimeZone.getTimeZone("GMT"));
            cal.set(Calendar.YEAR, 2012);
            cal.set(Calendar.MONTH, Calendar.JUNE);
            cal.set(Calendar.DAY_OF_MONTH, 26);
            cal.set(Calendar.HOUR, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal;
        };

        final JaxbConfigurationInterceptor interceptor =
                new JaxbConfigurationInterceptor(_contextFactory, _configurationFactory, true, dateRef);

        final FileRepository repo = new FileRepository("src/test/resources/example_repo");
        final RepositoryFile file = (RepositoryFile) repo.getRepositoryNode("/tenant1/conf.xml");

        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        file.readFile(in -> {
            interceptor.intercept("tenant1", job, datastoreName, in, out);
        });

        return new String(out.toByteArray(), "UTF-8").trim();
    }
}
