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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.configuration.TenantContextFactoryImpl;
import org.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.datacleaner.monitor.scheduling.model.ExecutionStatus;
import org.datacleaner.monitor.server.SchedulingServiceImpl;
import org.datacleaner.monitor.server.job.DefaultJobEngineManager;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.file.FileRepository;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.web.MockMultipartFile;

public class JobTriggeringControllerIntegrationTest {
    /**
     * Tests if a job is trigger to run as expected and the override properties
     * are applied. The job can't run successful if the override properties
     * aren't applied, because the standard conf.xml refers to a non-existing
     * dictionary, which is corrected by the override properties. 
     */
    @Test
    public void testPostWithOverrideProperties() throws Throwable {
        final String repositoryName = "example_repo";
        final String tenantName = "tenant5";
        final String jobName = "countries";
        
        final File targetDir = new File("target/" + repositoryName);
        
        FileUtils.deleteDirectory(targetDir);
        FileUtils.copyDirectory(new File("src/test/resources/" + repositoryName), targetDir);

        final ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                "context/application-context.xml");
        final Repository repository = new FileRepository(targetDir);
        final TenantContextFactory tenantContextFactory = new TenantContextFactoryImpl(repository,
                new DataCleanerEnvironmentImpl(), new DefaultJobEngineManager(applicationContext));

        final SchedulingServiceImpl schedulingService = new SchedulingServiceImpl(repository, tenantContextFactory);
        schedulingService.setApplicationContext(applicationContext);
        schedulingService.initialize();

        final JobTriggeringController jobTriggeringController = new JobTriggeringController();
        jobTriggeringController._schedulingService = schedulingService;

        final String propertiesFileName = repositoryName + "/" + tenantName + "/override.properties";
        final InputStream overrideProperties = getClass().getClassLoader().getResourceAsStream(propertiesFileName);

        jobTriggeringController.handleMultipartFormData(tenantName, jobName, null, null, new MockMultipartFile(
                propertiesFileName, overrideProperties));

        // Spend a maximum of 10 seconds waiting for the execution to finish.
        final TenantIdentifier tenantIdentifier = new TenantIdentifier(tenantName);
        final JobIdentifier jobIdentifier = new JobIdentifier(jobName);

        ExecutionLog executionLog = null;
        for (int i = 0; i < 100; i++) {
            executionLog = schedulingService.getLatestExecution(tenantIdentifier, jobIdentifier);

            if (executionLog.getExecutionStatus() == ExecutionStatus.RUNNING || executionLog
                    .getExecutionStatus() == ExecutionStatus.PENDING) {
                Thread.sleep(100);
            }
        }

        assertEquals(ExecutionStatus.SUCCESS, executionLog.getExecutionStatus());
    }
}
