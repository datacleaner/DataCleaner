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
package org.eobjects.datacleaner.monitor.server.dao;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.eobjects.analyzer.configuration.InjectionManagerFactoryImpl;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactoryImpl;
import org.eobjects.datacleaner.repository.Repository;
import org.eobjects.datacleaner.repository.file.FileRepository;

import junit.framework.TestCase;

public class DatastoreDaoImplTest extends TestCase {

    public void testRemoveDatastore() throws Exception {
        final File targetDir = new File("target/repo_remove_datastore");
        FileUtils.deleteDirectory(targetDir);
        FileUtils.copyDirectory(new File("src/test/resources/example_repo"), targetDir);

        final Repository repository = new FileRepository(targetDir);
        
        final TenantContextFactoryImpl tenantContextFactory = new TenantContextFactoryImpl(repository,
                new InjectionManagerFactoryImpl());
        
        DatastoreDao dao = new DatastoreDaoImpl();
        
        TenantContext tenantContext = tenantContextFactory.getContext("tenant1");
        
        Datastore orderdb = tenantContext.getConfiguration().getDatastoreCatalog().getDatastore("orderdb");
        assertNotNull(orderdb);
        
        dao.removeDatastore(tenantContext, "orderdb");
        
        orderdb = tenantContext.getConfiguration().getDatastoreCatalog().getDatastore("orderdb");
        assertNull(orderdb);
    }
}
