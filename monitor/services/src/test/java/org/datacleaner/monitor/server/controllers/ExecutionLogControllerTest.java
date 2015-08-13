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

import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.monitor.configuration.TenantContextFactoryImpl;
import org.datacleaner.monitor.server.job.MockJobEngineManager;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.file.FileRepository;
import org.easymock.EasyMock;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class ExecutionLogControllerTest extends TestCase {

    private ExecutionLogController executionLogController;
    private Repository repository;

    protected void setUp() throws Exception {
        File targetDir = new File("target/repo_result_modification");
        FileUtils.deleteDirectory(targetDir);
        FileUtils.copyDirectory(new File("src/test/resources/example_repo"), targetDir);
        repository = new FileRepository(targetDir);

        TenantContextFactoryImpl tenantContextFactory = new TenantContextFactoryImpl(repository,
                new DataCleanerEnvironmentImpl(), new MockJobEngineManager());

        executionLogController = new ExecutionLogController();
        executionLogController._contextFactory = tenantContextFactory;
    }

    public void testExecutionLogXml() throws Exception {
        final HttpServletResponse response = EasyMock.createMock(HttpServletResponse.class);
        response.setContentType("application/xml");
        response.setCharacterEncoding("UTF-8");

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ServletOutputStream out = new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                baos.write(b);
            }
        };
        EasyMock.expect(response.getOutputStream()).andReturn(out);

        EasyMock.replay(response);

        executionLogController.executionLogXml("tenant1", "product_profiling-3", response);

        EasyMock.verify(response);

        String str = new String(baos.toByteArray());
        assertTrue("Got: " + str, str.indexOf("<ns3:execution-status>SUCCESS</ns3:execution-status>") != -1);
    }
}
