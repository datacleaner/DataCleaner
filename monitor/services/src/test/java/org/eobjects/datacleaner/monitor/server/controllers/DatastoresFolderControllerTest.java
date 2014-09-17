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
package org.eobjects.datacleaner.monitor.server.controllers;

import java.io.File;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.eobjects.analyzer.configuration.InjectionManagerFactoryImpl;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactoryImpl;
import org.eobjects.datacleaner.monitor.server.dao.DatastoreDaoImpl;
import org.eobjects.datacleaner.monitor.server.job.MockJobEngineManager;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.file.FileRepository;
import org.apache.metamodel.util.Action;
import org.apache.metamodel.util.FileHelper;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class DatastoresFolderControllerTest extends TestCase {

    public void testSimpleScenario() throws Exception {
        FileUtils.copyDirectory(new File("src/test/resources/example_repo/tenant3"), new File(
                "target/repo_datastore_registration/dc"));

        final FileRepository repository = new FileRepository("target/repo_datastore_registration");
        final TenantContextFactory contextFactory = new TenantContextFactoryImpl(repository,
                new InjectionManagerFactoryImpl(), new MockJobEngineManager());

        DatastoresFolderController controller = new DatastoresFolderController();
        controller._contextFactory = contextFactory;
        controller._datastoreDao = new DatastoreDaoImpl();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContent("<foo><bar>baz</bar></foo>".getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.registerDatastore("dc", request, response);

        RepositoryFile file = repository.getFolder("dc").getFile("conf.xml");
        file.readFile(new Action<InputStream>() {
            @Override
            public void run(InputStream in) throws Exception {
                String str = FileHelper.readInputStreamAsString(in, "UTF8");
                String errorMsg = "generated: " + str;
                assertTrue(errorMsg, str.indexOf("<foo>") != -1);
                assertTrue(errorMsg, str.indexOf("baz") != -1);
                assertTrue(errorMsg, str.indexOf("</bar>") != -1);
                assertTrue(errorMsg, str.indexOf("<datastore-catalog>") != -1);
                assertTrue(errorMsg, str.indexOf("<csv-datastore name=\"SomeCSV\">") != -1);
            }
        });
    }
}
