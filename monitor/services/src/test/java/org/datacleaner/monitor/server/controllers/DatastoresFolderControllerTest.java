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

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.metamodel.util.FileHelper;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.configuration.TenantContextFactoryImpl;
import org.datacleaner.monitor.server.dao.DatastoreDaoImpl;
import org.datacleaner.monitor.server.job.MockJobEngineManager;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.repository.file.FileRepository;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import junit.framework.TestCase;

public class DatastoresFolderControllerTest extends TestCase {

    public void testSimpleScenario() throws Exception {
        FileUtils.copyDirectory(new File("src/test/resources/example_repo/tenant3"),
                new File("target/repo_datastore_registration/dc"));

        final FileRepository repository = new FileRepository("target/repo_datastore_registration");
        final TenantContextFactory contextFactory =
                new TenantContextFactoryImpl(repository, new DataCleanerEnvironmentImpl(), new MockJobEngineManager());

        final DatastoresFolderController controller = new DatastoresFolderController();
        controller._contextFactory = contextFactory;
        controller._datastoreDao = new DatastoreDaoImpl();

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContent("<foo><bar>baz</bar></foo>".getBytes());

        final MockHttpServletResponse response = new MockHttpServletResponse();
        controller.registerDatastore("dc", request, response);

        final RepositoryFile file = repository.getFolder("dc").getFile("conf.xml");
        file.readFile(in -> {
            final String str = FileHelper.readInputStreamAsString(in, "UTF8");
            final String errorMsg = "generated: " + str;
            assertTrue(errorMsg, str.indexOf("<foo>") != -1);
            assertTrue(errorMsg, str.indexOf("baz") != -1);
            assertTrue(errorMsg, str.indexOf("</bar>") != -1);
            assertTrue(errorMsg, str.indexOf("<datastore-catalog>") != -1);
            assertTrue(errorMsg, str.indexOf("<csv-datastore name=\"SomeCSV\">") != -1);
        });
    }
}
