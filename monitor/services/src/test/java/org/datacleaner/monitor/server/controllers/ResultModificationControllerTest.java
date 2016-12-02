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

import java.util.Date;
import java.util.Map;

import org.datacleaner.components.convert.ConvertToDateTransformer;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.monitor.configuration.TenantContextFactoryImpl;
import org.datacleaner.monitor.events.ResultModificationEvent;
import org.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.datacleaner.monitor.server.dao.ResultDaoImpl;
import org.datacleaner.monitor.server.jaxb.JaxbExecutionLogReader;
import org.datacleaner.monitor.server.job.DefaultJobEngineManager;
import org.datacleaner.monitor.server.listeners.ResultModificationEventExecutionLogListener;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.repository.RepositoryNode;
import org.datacleaner.repository.file.FileRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.TestCase;

public class ResultModificationControllerTest extends TestCase {

    private ResultModificationController resultModificationController;
    private ResultModificationEventExecutionLogListener resultModificationListener;
    private Repository repository;

    protected void setUp() throws Exception {
        final ApplicationContext applicationContext =
                new ClassPathXmlApplicationContext("context/application-context.xml");
        repository = applicationContext.getBean(FileRepository.class);

        final TenantContextFactoryImpl tenantContextFactory =
                new TenantContextFactoryImpl(repository, new DataCleanerEnvironmentImpl(),
                        new DefaultJobEngineManager(applicationContext));

        resultModificationController = new ResultModificationController();
        resultModificationListener = new ResultModificationEventExecutionLogListener(tenantContextFactory);

        final ApplicationEventPublisher applicationEventPublisher =
                event -> resultModificationListener.onApplicationEvent((ResultModificationEvent) event);

        resultModificationController._contextFactory = tenantContextFactory;
        resultModificationController._resultDao = new ResultDaoImpl(tenantContextFactory, applicationEventPublisher);
    }

    public void testModifyJob() throws Exception {
        final ResultModificationPayload input = new ResultModificationPayload();
        input.setJob("email_standardizer");

        final Map<String, String> response =
                resultModificationController.modifyResult("tenant1", "product_profiling-3", input);
        assertEquals("{new_result_name=email_standardizer-1338990580902.analysis.result.dat, "
                        + "old_result_name=product_profiling-3.analysis.result.dat, "
                        + "repository_url=/tenant1/results/email_standardizer-1338990580902.analysis.result.dat}",
                response.toString());
    }

    public void testModifyDate() throws Exception {
        final ResultModificationPayload input = new ResultModificationPayload();
        input.setDate("2012-12-17");

        // reproduce the date, to make unittest locale-independent
        final Date date = ConvertToDateTransformer.getInternalInstance().transformValue("2012-12-17");

        final Map<String, String> response =
                resultModificationController.modifyResult("tenant1", "product_profiling-3", input);
        assertEquals("{new_result_name=product_profiling-" + date.getTime() + ".analysis.result.dat, "
                        + "old_result_name=product_profiling-3.analysis.result.dat, "
                        + "repository_url=/tenant1/results/product_profiling-" + date.getTime() + ".analysis.result.dat}",
                response.toString());

        final RepositoryNode executionLogFile = repository.getRepositoryNode(
                "/tenant1/results/product_profiling-" + date.getTime() + ".analysis.execution.log.xml");
        assertNotNull(executionLogFile);
    }

    public void testModifyBothDateAndJob() throws Exception {
        final ResultModificationPayload input = new ResultModificationPayload();
        input.setJob("email_standardizer");
        input.setDate("1355698800000");

        final Map<String, String> response =
                resultModificationController.modifyResult("tenant1", "product_profiling-3", input);
        assertEquals("{new_result_name=email_standardizer-1355698800000.analysis.result.dat, "
                        + "old_result_name=product_profiling-3.analysis.result.dat, "
                        + "repository_url=/tenant1/results/email_standardizer-1355698800000.analysis.result.dat}",
                response.toString());

        assertNotNull(
                repository.getRepositoryNode("/tenant1/results/email_standardizer-1355698800000.analysis.result.dat"));

        final RepositoryFile executionLogFile = (RepositoryFile) repository
                .getRepositoryNode("/tenant1/results/email_standardizer-1355698800000.analysis.execution.log.xml");
        assertNotNull(executionLogFile);

        executionLogFile.readFile(inputStream -> {
            final ExecutionLog executionLog = new JaxbExecutionLogReader()
                    .read(inputStream, new JobIdentifier("email_standardizer"), new TenantIdentifier("tenant1"));
            assertEquals("email_standardizer-1355698800000", executionLog.getResultId());
        });

    }
}
