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
package org.datacleaner.monitor.server.dao;

import junit.framework.TestCase;

import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.connection.Datastore;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.configuration.TenantContextFactoryImpl;
import org.datacleaner.monitor.server.job.DefaultJobEngineManager;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.file.FileRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DatastoreDaoImplTest extends TestCase {

    public void testRemoveDatastore() throws Exception {
        final ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                "context/application-context.xml");
        final Repository repository = applicationContext.getBean(FileRepository.class);

        final TenantContextFactoryImpl tenantContextFactory = new TenantContextFactoryImpl(repository,
                new DataCleanerEnvironmentImpl(), new DefaultJobEngineManager(applicationContext));
        DatastoreDao dao = new DatastoreDaoImpl();

        TenantContext tenantContext = tenantContextFactory.getContext("tenant1");

        Datastore orderdb = tenantContext.getConfiguration().getDatastoreCatalog().getDatastore("orderdb");
        assertNotNull(orderdb);

        dao.removeDatastore(tenantContext, "orderdb");

        orderdb = tenantContext.getConfiguration().getDatastoreCatalog().getDatastore("orderdb");
        assertNull(orderdb);
    }
}
