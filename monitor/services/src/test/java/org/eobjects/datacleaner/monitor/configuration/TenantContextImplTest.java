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
package org.eobjects.datacleaner.monitor.configuration;

import junit.framework.TestCase;

import org.eobjects.analyzer.configuration.InjectionManagerFactory;
import org.eobjects.analyzer.configuration.InjectionManagerFactoryImpl;
import org.eobjects.datacleaner.monitor.job.JobContext;
import org.eobjects.datacleaner.monitor.job.JobEngineManager;
import org.eobjects.datacleaner.monitor.server.job.MockJobEngineManager;
import org.eobjects.datacleaner.repository.Repository;
import org.eobjects.datacleaner.repository.file.FileRepository;

public class TenantContextImplTest extends TestCase {

    public void testJobNameWithSignificantSpace() throws Exception {
        Repository repository = new FileRepository("src/test/resources/example_repo");
        InjectionManagerFactory injectionManagerFactory = new InjectionManagerFactoryImpl();
        JobEngineManager jobEngineManager = new MockJobEngineManager();

        TenantContext tenantContext = new TenantContextImpl("tenant4", repository, injectionManagerFactory,
                jobEngineManager);

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
}
