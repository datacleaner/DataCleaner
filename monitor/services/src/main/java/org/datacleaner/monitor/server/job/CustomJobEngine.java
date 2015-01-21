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
package org.datacleaner.monitor.server.job;

import java.io.Serializable;
import java.util.Map;

import org.datacleaner.configuration.InjectionManager;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.job.ComponentConfiguration;
import org.datacleaner.lifecycle.LifeCycleHelper;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.job.ExecutionLogger;
import org.datacleaner.monitor.job.JobEngine;
import org.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.repository.RepositoryFile;
import org.springframework.stereotype.Component;

/**
 * A {@link JobEngine} implementation for running custom Java-class based jobs
 */
@Component
public class CustomJobEngine extends AbstractJobEngine<CustomJobContext> {

    public static final String EXTENSION = ".custom.job.xml";

    public CustomJobEngine() {
        super(EXTENSION);
    }

    @Override
    public String getJobType() {
        return "CustomJob";
    }

    @Override
    protected CustomJobContext getJobContext(TenantContext context, RepositoryFile file) {
        final InjectionManager injectionManager = context.getConfiguration().getInjectionManager(null);

        return new CustomJobContext(context, this, file, injectionManager);
    }

    @Override
    public void executeJob(TenantContext context, ExecutionLog execution, ExecutionLogger executionLogger,
            Map<String, String> variables) throws Exception {
        executionLogger.setStatusRunning();

        final JobIdentifier jobIdentifier = execution.getJob();
        final CustomJobContext jobContext = getJobContext(context, jobIdentifier);

        final CustomJob customJob;
        final ComponentDescriptor<?> descriptor = jobContext.getDescriptor();
        try {
            customJob = (CustomJob) descriptor.newInstance();
        } catch (Exception e) {
            executionLogger.setStatusFailed(this, descriptor, e);
            return;
        }

        executionLogger.log("Succesfully loaded a job instance of type: " + descriptor.getComponentClass().getName());

        final Serializable result;
        try {
            final ComponentConfiguration beanConfiguration = jobContext.getComponentConfiguration(customJob);

            final InjectionManager injectionManager = context.getConfiguration().getInjectionManager(null);
            final LifeCycleHelper lifeCycleHelper = new LifeCycleHelper(injectionManager, true);

            lifeCycleHelper.assignProvidedProperties(descriptor, customJob);
            lifeCycleHelper.assignConfiguredProperties(descriptor, customJob, beanConfiguration);
            lifeCycleHelper.initialize(descriptor, customJob);
            executionLogger.log("Succesfully initialized job instance, executing");

            try {
                final CustomJobCallback callback = new CustomJobCallbackImpl(context, executionLogger);
                result = customJob.execute(callback);
                executionLogger.log("Succesfully executed job instance, closing");
                lifeCycleHelper.close(descriptor, customJob, true);
            } catch (Exception e) {
                lifeCycleHelper.close(descriptor, customJob, false);
                throw e;
            }

        } catch (Exception e) {
            executionLogger.setStatusFailed(this, null, e);
            return;
        }

        final Boolean persistResult = jobContext.getCustomJavaComponentJob().getPersistResult();
        if (persistResult == null || persistResult.booleanValue()) {
            executionLogger.setStatusSuccess(result);
        } else {
            executionLogger.setStatusSuccess(null);
        }
    }

    @Override
    public boolean cancelJob(TenantContext tenantContext, ExecutionLog executionLog) {
        // always return false - cannot stop a custom method from running
        return false;
    }
}
