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
package org.datacleaner.monitor.scheduling.quartz;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.configuration.TenantContextFactoryImpl;
import org.datacleaner.monitor.job.JobEngine;
import org.datacleaner.monitor.job.JobEngineManager;
import org.datacleaner.monitor.scheduling.SchedulingService;
import org.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.datacleaner.monitor.scheduling.model.TriggerType;
import org.datacleaner.monitor.server.SchedulingServiceImpl;
import org.datacleaner.monitor.server.job.DataCleanerJobContext;
import org.datacleaner.monitor.server.job.DataCleanerJobEngine;
import org.datacleaner.monitor.server.job.DefaultJobEngineManager;
import org.datacleaner.monitor.server.job.MockJobEngineManager;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.RepositoryNode;
import org.datacleaner.repository.file.FileRepository;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.io.Files;

import junit.framework.TestCase;

public class ExecuteJobTest extends TestCase {

    public void testAssumptionsAboutDisallowConcurrentExecution() throws Exception {
        final JobDetail jobDetail = JobBuilder.newJob(ExecuteJob.class).withIdentity("job1", "tenant2").storeDurably()
                .build();
        assertTrue(jobDetail.isConcurrentExectionDisallowed());
    }

    public void testFileNotFound() throws Exception {
        final Repository repo = new FileRepository("src/test/resources/example_repo");
        final TenantContextFactory tenantContextFactory = new TenantContextFactoryImpl(repo,
                new DataCleanerEnvironmentImpl(), new MockJobEngineManager());

        TenantContext tenantContext = tenantContextFactory.getContext("tenant3");
        JobIdentifier job = new JobIdentifier("some_csv_profiling");
        TenantIdentifier tenantIdentifier = new TenantIdentifier("tenant3");
        ScheduleDefinition schedule = new ScheduleDefinition(tenantIdentifier, job, "SomeCSV");
        ExecutionLog execution = new ExecutionLog(schedule, TriggerType.MANUAL);

        String executionId = new ExecuteJob().executeJob(tenantContext, execution, null, new MockJobEngineManager());

        assertNotNull(executionId);
        try {
            SchedulingService schedulingService = new SchedulingServiceImpl(repo, tenantContextFactory);

            ExecutionLog log = schedulingService.getExecution(tenantIdentifier, execution);
            String logOutput = log.getLogOutput();
            assertTrue(
                    logOutput,
                    logOutput.indexOf("Resource does not exist: FileResource["
                            + new File("src/test/resources/example_repo/tenant3/foo/bar.csv").getPath()
                            + "] (ResourceException)") != -1);
            assertTrue(logOutput, logOutput.indexOf("org.apache.metamodel.util.ResourceException: ") != -1);
        } finally {
            RepositoryNode logNode = repo.getRepositoryNode("/tenant3/results/" + executionId
                    + ".analysis.execution.log.xml");
            assertNotNull(logNode);

            // cleanup
            logNode.delete();
        }
    }

    public void testInvalidDatastoreInJob() throws Exception {
        final Repository repo = new FileRepository("src/test/resources/example_repo");
        final TenantContextFactory tenantContextFactory = new TenantContextFactoryImpl(repo,
                new DataCleanerEnvironmentImpl(), new MockJobEngineManager());

        TenantContext tenantContext = tenantContextFactory.getContext("tenant3");
        JobIdentifier job = new JobIdentifier("product_profiling");
        TenantIdentifier tenantIdentifier = new TenantIdentifier("tenant3");
        ScheduleDefinition schedule = new ScheduleDefinition(tenantIdentifier, job, "orderdb");
        ExecutionLog execution = new ExecutionLog(schedule, TriggerType.MANUAL);

        String executionId = new ExecuteJob().executeJob(tenantContext, execution, null, new MockJobEngineManager());
        assertNotNull(executionId);
        try {
            SchedulingService schedulingService = new SchedulingServiceImpl(repo, tenantContextFactory);

            ExecutionLog log = schedulingService.getExecution(tenantIdentifier, execution);
            String logOutput = log.getLogOutput();
            assertTrue(logOutput, logOutput.indexOf("- No such datastore: orderdb (NoSuchDatastoreException)") != -1);
            assertTrue(logOutput,
                    logOutput.indexOf("org.datacleaner.job.NoSuchDatastoreException: No such datastore: orderdb") != -1);
        } finally {
            RepositoryNode logNode = repo.getRepositoryNode("/tenant3/results/" + executionId
                    + ".analysis.execution.log.xml");
            assertNotNull(logNode);

            // cleanup
            logNode.delete();
        }
    }
    /**
     * Testing Hadoop execution in debug mode. The example job from example_hadoop_repo should be modified accordingly. 
     */
    public void ignoreTestHadoopExecution() throws Exception {

        final File targetDir = Files.createTempDir();
        targetDir.deleteOnExit();
        FileUtils.copyDirectory(new File("src/test/resources/example_hadoop_repo"), targetDir);

        final Repository repository = new FileRepository(targetDir); 
        final ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                 "context/application-context.xml");

        final TenantContextFactory tenantContextFactory = new TenantContextFactoryImpl(repository, new DataCleanerEnvironmentImpl(),
                 new DefaultJobEngineManager(applicationContext));

        final TenantIdentifier tenantIdentifier = new TenantIdentifier("tenant");
        final TenantContext tenantContext = tenantContextFactory.getContext(tenantIdentifier);
        final JobIdentifier jobIdentifier = new JobIdentifier("hadoop_job");
        
        final ScheduleDefinition schedule = new ScheduleDefinition(tenantIdentifier, jobIdentifier, "Hadoop");
        schedule.setRunOnHadoop(true);
        assertTrue(schedule.isRunOnHadoop()); 
        final ExecutionLog execution = new ExecutionLog(schedule, TriggerType.MANUAL);

        final JobEngineManager manager = applicationContext.getBean(JobEngineManager.class);
        
        assertTrue(manager instanceof DefaultJobEngineManager);

        final JobEngine<?> engine;
        
        engine = manager.getJobEngine(DataCleanerJobContext.class);
        assertEquals(DataCleanerJobEngine.class, engine.getClass());
        
        final String executionId = new ExecuteJob().executeJob(tenantContext, execution, null, manager);
        assertNotNull(executionId);
        try {
            final SchedulingService schedulingService = new SchedulingServiceImpl(repository, tenantContextFactory);

            final ExecutionLog log = schedulingService.getExecution(tenantIdentifier, execution);
            assertEquals("SUCCESS", log.getExecutionStatus().toString()); 

        } finally {
            final RepositoryNode logNode = repository.getRepositoryNode("/tenant/results/" + executionId
                    + ".analysis.execution.log.xml");
            assertNotNull(logNode);

            // cleanup
            logNode.delete();
        }
        
        
    }
}
