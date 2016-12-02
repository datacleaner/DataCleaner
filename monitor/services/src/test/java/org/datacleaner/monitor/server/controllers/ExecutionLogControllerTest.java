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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.monitor.configuration.TenantContextFactoryImpl;
import org.datacleaner.monitor.server.job.DefaultJobEngineManager;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.file.FileRepository;
import org.easymock.EasyMock;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.TestCase;

public class ExecutionLogControllerTest extends TestCase {

    private ExecutionLogController executionLogController;

    protected void setUp() throws Exception {
        final ApplicationContext applicationContext =
                new ClassPathXmlApplicationContext("context/application-context.xml");
        final Repository repository = applicationContext.getBean(FileRepository.class);

        final TenantContextFactoryImpl tenantContextFactory =
                new TenantContextFactoryImpl(repository, new DataCleanerEnvironmentImpl(),
                        new DefaultJobEngineManager(applicationContext));

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
            public void write(final int b) throws IOException {
                baos.write(b);
            }
        };
        EasyMock.expect(response.getOutputStream()).andReturn(out);

        EasyMock.replay(response);

        executionLogController.executionLogXml("tenant1", "product_profiling-3", response);

        EasyMock.verify(response);

        final String str = new String(baos.toByteArray());
        assertTrue("Got: " + str, str.indexOf("<ns3:execution-status>SUCCESS</ns3:execution-status>") != -1);
    }
}
