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

import org.eobjects.analyzer.configuration.InjectionManager;
import org.eobjects.analyzer.configuration.InjectionManagerFactory;
import org.eobjects.analyzer.configuration.InjectionManagerFactoryImpl;
import org.eobjects.analyzer.configuration.SimpleInjectionPoint;
import org.eobjects.analyzer.util.convert.StringConverter;
import org.eobjects.datacleaner.monitor.job.JobEngineManager;
import org.eobjects.datacleaner.monitor.server.job.MockJobEngineManager;
import org.eobjects.datacleaner.repository.Repository;
import org.eobjects.datacleaner.repository.RepositoryFileResource;
import org.eobjects.datacleaner.repository.file.FileRepository;
import org.apache.metamodel.util.Resource;

public class TenantContextFactoryImplTest extends TestCase {

    private Repository repository = new FileRepository("src/test/resources/example_repo");
    private InjectionManagerFactory parentInjectionManagerFactory = new InjectionManagerFactoryImpl();
    private JobEngineManager jobEngineManager = new MockJobEngineManager();
    private TenantContextFactory tenantContextFactory = new TenantContextFactoryImpl(repository,
            parentInjectionManagerFactory, jobEngineManager);

    public void testUseTenantSpecificInjectionManager() throws Exception {
        TenantContext tenantContext = tenantContextFactory.getContext("tenant1");

        InjectionManager injectionManager = tenantContext.getConfiguration().getInjectionManager(null);
        assertTrue(injectionManager instanceof TenantInjectionManager);

        TenantInjectionManager tenantInjectionManager = (TenantInjectionManager) injectionManager;
        assertEquals("tenant1", tenantInjectionManager.getTenantId());

        StringConverter converter = injectionManager.getInstance(SimpleInjectionPoint.of(StringConverter.class));

        Resource resource = converter.deserialize("repo://jobs/email_standardizer.analysis.xml", Resource.class);
        assertTrue(resource.isExists());

        RepositoryFileResource repositoryFileResource = (RepositoryFileResource) resource;

        assertEquals("/tenant1/jobs/email_standardizer.analysis.xml", repositoryFileResource.getRepositoryFile()
                .getQualifiedPath());

        String serializedForm = converter.serialize(resource);
        assertEquals("repo://jobs/email_standardizer.analysis.xml", serializedForm);
    }

    public void testValidateTenantId() throws Exception {
        try {
            tenantContextFactory.getContext("\\//");
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
            assertEquals("Tenant ID contained only invalid characters: \\//", e.getMessage());
        }
    }

    public void testStandardizeTenantIdToCacheTenantContext() throws Exception {
        TenantContext tc1 = tenantContextFactory.getContext("tenant1");
        TenantContext tc2 = tenantContextFactory.getContext("   TENANT1 ");
        TenantContext tc3 = tenantContextFactory.getContext("Tenant\\1");

        assertSame(tc1, tc2);
        assertSame(tc1, tc3);

        assertEquals("tenant1", tc3.getTenantId());

        TenantContext tc4 = tenantContextFactory.getContext("Tenant\\\n2");

        assertNotSame(tc1, tc4);

        assertEquals("tenant2", tc4.getTenantId());
    }
}
