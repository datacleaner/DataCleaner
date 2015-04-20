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
package org.datacleaner.monitor.configuration;

import junit.framework.TestCase;

import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.monitor.job.JobContext;
import org.datacleaner.monitor.job.JobEngineManager;
import org.datacleaner.monitor.server.job.MockJobEngineManager;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.file.FileRepository;

public class TenantContextImplTest extends TestCase {

    private final Repository repository = new FileRepository("src/test/resources/example_repo");
    private final DataCleanerEnvironment environment = new DataCleanerEnvironmentImpl();
    private final JobEngineManager jobEngineManager = new MockJobEngineManager();
    private TenantContext tenantContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tenantContext = new TenantContextImpl("tenant4", repository, environment, jobEngineManager);
    }

    public void testJobNameWithSignificantSpace() throws Exception {

        JobContext job1 = tenantContext.getJob("my job");
        JobContext job2 = tenantContext.getJob("my job ");
        JobContext job3 = tenantContext.getJob("my job");
        JobContext job4 = tenantContext.getJob("my job ");

        assertNotNull(job1);
        assertNotNull(job2);
        assertNotNull(job3);
        assertNotNull(job4);

        assertNotSame(job1, job2);

        assertSame(job1, job3);
        assertSame(job2, job4);

        assertEquals("my job.analysis.xml", job1.getJobFile().getName());
        assertEquals("my job .analysis.xml", job2.getJobFile().getName());
    }

    public void testGetConfiguration() throws Exception {
        DataCleanerConfiguration configuration = tenantContext.getConfiguration();
        assertNotNull(configuration);
    }
}
