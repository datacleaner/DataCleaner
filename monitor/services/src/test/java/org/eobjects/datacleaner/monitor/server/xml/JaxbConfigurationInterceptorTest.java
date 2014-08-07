/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.monitor.server.xml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.eobjects.analyzer.configuration.InjectionManagerFactoryImpl;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.datacleaner.monitor.configuration.ConfigurationFactory;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactoryImpl;
import org.eobjects.datacleaner.monitor.job.JobContext;
import org.eobjects.datacleaner.monitor.server.jaxb.JaxbConfigurationInterceptor;
import org.eobjects.datacleaner.monitor.server.job.DataCleanerJobContext;
import org.eobjects.datacleaner.monitor.server.job.MockJobEngineManager;
import org.eobjects.datacleaner.repository.Repository;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.file.FileRepository;
import org.apache.metamodel.util.Action;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.Ref;

public class JaxbConfigurationInterceptorTest extends TestCase {

    private TenantContextFactoryImpl _contextFactory;
    private ConfigurationFactory _configurationFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        _configurationFactory = new ConfigurationFactory();
        _configurationFactory.setNumThreads(10);
        _configurationFactory.setScannedPackages(Arrays.asList("org.eobjects", "com.hi"));

        final Repository repository = new FileRepository("src/test/resources/example_repo");

        _contextFactory = new TenantContextFactoryImpl(repository, new InjectionManagerFactoryImpl(),
                new MockJobEngineManager());
    }

    public void testGenerateGenericConfiguration() throws Exception {
        String actual = generationConf(null, null);

        String expected = FileHelper.readFileAsString(new File("src/test/resources/expected_conf_file_generic.xml"),
                "UTF-8");
        expected = expected.replaceAll("\r\n", "\n").trim();

        if (!expected.equals(actual)) {
            final URL orderDbScript = ClassLoader.getSystemResource("orderdb.script");
            System.out.println("!! Seems there is some issue in resolving the correct orderdb file: " + orderDbScript);

            assertEquals(expected, actual);
        }
    }

    public void testGenerateDatastoreSpecificConfiguration() throws Exception {
        String actual = generationConf(null, "orderdb");

        String expected = FileHelper.readFileAsString(new File(
                "src/test/resources/expected_conf_file_specific_datastore.xml"), "UTF-8");
        expected = expected.replaceAll("\r\n", "\n").trim();

        assertEquals(expected, actual);
    }

    public void testGenerateJobSpecificConfigurationSourceOnly() throws Exception {
        JobContext job = _contextFactory.getContext("tenant1").getJob("email_standardizer");
        String actual = generationConf(job, null);

        String expected = FileHelper.readFileAsString(new File(
                "src/test/resources/expected_conf_file_specific_source_only.xml"), "UTF-8");
        expected = expected.replaceAll("\r\n", "\n").trim();

        assertEquals(expected, actual);
    }

    public void testGenerateJobSpecificConfigurationTableLookupAnotherDatastore() throws Exception {
        JobContext job = _contextFactory.getContext("tenant1").getJob("lookup_vendor");
        String actual = generationConf(job, null);

        String expected = FileHelper.readFileAsString(new File(
                "src/test/resources/expected_conf_file_specific_lookup_another_ds.xml"), "UTF-8");
        expected = expected.replaceAll("\r\n", "\n").trim();

        assertEquals(expected, actual);
    }

    public void testGenerateWithLookupInsertUpdate() throws Exception {
        final TenantContext tenantContext = _contextFactory.getContext("tenant1");
        final Datastore ds = tenantContext.getConfiguration().getDatastoreCatalog().getDatastore("orderdb");

        try (final DatastoreConnection con = ds.openConnection()) {
            JobContext job = tenantContext.getJob("Move employees to customers");
            String actual = generationConf(job, null);

            String expected = FileHelper.readFileAsString(new File(
                    "src/test/resources/expected_conf_file_lookup_insert_update.xml"), "UTF-8");

            expected = expected.replaceAll("\r\n", "\n").trim();
            assertEquals(expected, actual);
        }
    }

    private String generationConf(final JobContext jobContext, final String datastoreName) throws Exception {
        final DataCleanerJobContext job = (DataCleanerJobContext) jobContext;

        final Ref<Calendar> dateRef = new Ref<Calendar>() {
            @Override
            public Calendar get() {
                Calendar cal = Calendar.getInstance();
                cal.setTimeZone(TimeZone.getTimeZone("GMT"));
                cal.set(Calendar.YEAR, 2012);
                cal.set(Calendar.MONTH, Calendar.JUNE);
                cal.set(Calendar.DAY_OF_MONTH, 26);
                cal.set(Calendar.HOUR, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                return cal;
            }
        };

        final JaxbConfigurationInterceptor interceptor = new JaxbConfigurationInterceptor(_contextFactory,
                _configurationFactory, true, dateRef);

        final FileRepository repo = new FileRepository("src/test/resources/example_repo");
        final RepositoryFile file = (RepositoryFile) repo.getRepositoryNode("/tenant1/conf.xml");

        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        file.readFile(new Action<InputStream>() {
            @Override
            public void run(InputStream in) throws Exception {
                interceptor.intercept("tenant1", job, datastoreName, in, out);
            }
        });

        final String actual = new String(out.toByteArray(), "UTF-8").trim();
        return actual;
    }
}
