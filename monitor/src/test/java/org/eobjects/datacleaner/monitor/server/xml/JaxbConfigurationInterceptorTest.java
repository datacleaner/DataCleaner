/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
import java.util.Arrays;
import java.util.Date;

import junit.framework.TestCase;

import org.eobjects.datacleaner.monitor.configuration.ConfigurationCache;
import org.eobjects.datacleaner.monitor.configuration.ConfigurationFactory;
import org.eobjects.datacleaner.monitor.configuration.JobContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactoryImpl;
import org.eobjects.datacleaner.monitor.server.jaxb.JaxbConfigurationInterceptor;
import org.eobjects.datacleaner.repository.Repository;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.file.FileRepository;
import org.eobjects.metamodel.util.DateUtils;
import org.eobjects.metamodel.util.FileHelper;
import org.eobjects.metamodel.util.Month;
import org.eobjects.metamodel.util.Ref;

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
        final ConfigurationCache configurationCache = new ConfigurationCache(repository);

        _contextFactory = new TenantContextFactoryImpl(repository, configurationCache);
    }

    public void testGenerateGenericConfiguration() throws Exception {
        String actual = generationConf(null);

        String expected = FileHelper.readFileAsString(new File("src/test/resources/expected_conf_file_generic.xml"), "UTF-8");
        expected = expected.replaceAll("\r\n", "\n").trim();

        assertEquals(expected, actual);
    }

    public void testGenerateJobSpecificConfigurationSourceOnly() throws Exception {
        JobContext job = _contextFactory.getContext("tenant1").getJob("email_standardizer");
        String actual = generationConf(job);
      
        String expected = FileHelper.readFileAsString(new File("src/test/resources/expected_conf_file_specific_source_only.xml"), "UTF-8");
        expected = expected.replaceAll("\r\n", "\n").trim();

        assertEquals(expected, actual);
    }
    
    public void testGenerateJobSpecificConfigurationTableLookupAnotherDatastore() throws Exception {
        JobContext job = _contextFactory.getContext("tenant1").getJob("lookup_vendor");
        String actual = generationConf(job);
      
        String expected = FileHelper.readFileAsString(new File("src/test/resources/expected_conf_file_specific_lookup_another_ds.xml"), "UTF-8");
        expected = expected.replaceAll("\r\n", "\n").trim();

        assertEquals(expected, actual);
    }

    private String generationConf(JobContext job) throws Exception {

        final Ref<Date> dateRef = new Ref<Date>() {
            @Override
            public Date get() {
                return DateUtils.get(2012, Month.JUNE, 26);
            }
        };

        final JaxbConfigurationInterceptor interceptor = new JaxbConfigurationInterceptor(_contextFactory,
                _configurationFactory, true, dateRef);

        final FileRepository repo = new FileRepository("src/test/resources/example_repo");
        final RepositoryFile file = (RepositoryFile) repo.getRepositoryNode("/tenant1/conf.xml");
        final InputStream in = file.readFile();

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        interceptor.intercept("tenant1", job, in, out);

        final String actual = new String(out.toByteArray(), "UTF-8").trim();
        return actual;
    }
}
