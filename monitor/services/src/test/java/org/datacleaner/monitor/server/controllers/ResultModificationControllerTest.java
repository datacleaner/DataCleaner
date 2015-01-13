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
import java.io.InputStream;
import java.util.Date;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.datacleaner.components.convert.ConvertToDateTransformer;
import org.datacleaner.configuration.InjectionManagerFactoryImpl;
import org.datacleaner.monitor.configuration.TenantContextFactoryImpl;
import org.datacleaner.monitor.events.ResultModificationEvent;
import org.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.datacleaner.monitor.server.dao.ResultDaoImpl;
import org.datacleaner.monitor.server.jaxb.JaxbExecutionLogReader;
import org.datacleaner.monitor.server.job.MockJobEngineManager;
import org.datacleaner.monitor.server.listeners.ResultModificationEventExecutionLogListener;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.repository.RepositoryNode;
import org.datacleaner.repository.file.FileRepository;
import org.apache.metamodel.util.Action;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

public class ResultModificationControllerTest extends TestCase {

    private ResultModificationController resultModificationController;
    private ResultModificationEventExecutionLogListener resultModificationListener;
    private Repository repository;

    protected void setUp() throws Exception {
        File targetDir = new File("target/repo_result_modification");
        FileUtils.deleteDirectory(targetDir);
        FileUtils.copyDirectory(new File("src/test/resources/example_repo"), targetDir);
        repository = new FileRepository(targetDir);

        TenantContextFactoryImpl tenantContextFactory = new TenantContextFactoryImpl(repository,
                new InjectionManagerFactoryImpl(), new MockJobEngineManager());

        resultModificationController = new ResultModificationController();
        resultModificationListener = new ResultModificationEventExecutionLogListener(tenantContextFactory);

        final ApplicationEventPublisher applicationEventPublisher = new ApplicationEventPublisher() {
            @Override
            public void publishEvent(ApplicationEvent event) {
                resultModificationListener.onApplicationEvent((ResultModificationEvent) event);
            }
        };
        
        resultModificationController._contextFactory = tenantContextFactory;
        resultModificationController._resultDao = new ResultDaoImpl(tenantContextFactory, applicationEventPublisher);
    }

    public void testModifyJob() throws Exception {
        ResultModificationPayload input = new ResultModificationPayload();
        input.setJob("email_standardizer");

        Map<String, String> response = resultModificationController.modifyResult("tenant1", "product_profiling-3",
                input);
        assertEquals("{new_result_name=email_standardizer-1338990580902.analysis.result.dat, "
                + "old_result_name=product_profiling-3.analysis.result.dat, "
                + "repository_url=/tenant1/results/email_standardizer-1338990580902.analysis.result.dat}",
                response.toString());
    }

    public void testModifyDate() throws Exception {
        ResultModificationPayload input = new ResultModificationPayload();
        input.setDate("2012-12-17");

        // reproduce the date, to make unittest locale-independent
        Date date = ConvertToDateTransformer.getInternalInstance().transformValue("2012-12-17");

        Map<String, String> response = resultModificationController.modifyResult("tenant1", "product_profiling-3",
                input);
        assertEquals("{new_result_name=product_profiling-" + date.getTime() + ".analysis.result.dat, "
                + "old_result_name=product_profiling-3.analysis.result.dat, "
                + "repository_url=/tenant1/results/product_profiling-" + date.getTime() + ".analysis.result.dat}",
                response.toString());

        RepositoryNode executionLogFile = repository.getRepositoryNode("/tenant1/results/product_profiling-"
                + date.getTime() + ".analysis.execution.log.xml");
        assertNotNull(executionLogFile);
    }

    public void testModifyBothDateAndJob() throws Exception {
        ResultModificationPayload input = new ResultModificationPayload();
        input.setJob("email_standardizer");
        input.setDate("1355698800000");

        Map<String, String> response = resultModificationController.modifyResult("tenant1", "product_profiling-3",
                input);
        assertEquals("{new_result_name=email_standardizer-1355698800000.analysis.result.dat, "
                + "old_result_name=product_profiling-3.analysis.result.dat, "
                + "repository_url=/tenant1/results/email_standardizer-1355698800000.analysis.result.dat}",
                response.toString());

        assertNotNull(repository
                .getRepositoryNode("/tenant1/results/email_standardizer-1355698800000.analysis.result.dat"));

        RepositoryFile executionLogFile = (RepositoryFile) repository
                .getRepositoryNode("/tenant1/results/email_standardizer-1355698800000.analysis.execution.log.xml");
        assertNotNull(executionLogFile);

        executionLogFile.readFile(new Action<InputStream>() {
            @Override
            public void run(InputStream inputStream) throws Exception {
                ExecutionLog executionLog = new JaxbExecutionLogReader().read(inputStream, new JobIdentifier(
                        "email_standardizer"), new TenantIdentifier("tenant1"));
                assertEquals("email_standardizer-1355698800000", executionLog.getResultId());
            }
        });

    }
}
