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
package org.eobjects.datacleaner.monitor.server.job;

import java.io.InputStream;
import java.util.Map;

import org.eobjects.analyzer.configuration.InjectionManager;
import org.eobjects.analyzer.descriptors.ComponentDescriptor;
import org.eobjects.analyzer.descriptors.Descriptors;
import org.eobjects.analyzer.lifecycle.LifeCycleHelper;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.job.ExecutionLogger;
import org.eobjects.datacleaner.monitor.job.JobEngine;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.eobjects.datacleaner.monitor.server.jaxb.JaxbCustomJavaJobAdaptor;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.metamodel.util.Func;
import org.springframework.stereotype.Component;

/**
 * A {@link JobEngine} implementation for running custom Java-based jobs
 */
@Component
public class CustomJavaJobEngine extends AbstractJobEngine<CustomJavaJobContext> {

    public static final String EXTENSION = ".java.job.xml";

    public CustomJavaJobEngine() {
        super(EXTENSION);
    }

    @Override
    public String getJobTypeDisplayName() {
        return "Custom Java job";
    }

    @Override
    protected CustomJavaJobContext getJobContext(TenantContext tenantContext, RepositoryFile file) {
        return new CustomJavaJobContext(file);
    }

    @Override
    public void executeJob(TenantContext context, ExecutionLog execution, ExecutionLogger executionLogger,
            Map<String, String> variables) throws Exception {
        executionLogger.setStatusRunning();

        final String jobName = execution.getJob().getName();
        final CustomJavaJobContext jobContext = getJobContext(context, jobName);
        final RepositoryFile jobFile = jobContext.getJobFile();
        final org.eobjects.datacleaner.monitor.jaxb.CustomJavaJob customJavaJobType = jobFile
                .readFile(new Func<InputStream, org.eobjects.datacleaner.monitor.jaxb.CustomJavaJob>() {
                    @Override
                    public org.eobjects.datacleaner.monitor.jaxb.CustomJavaJob eval(InputStream in) {
                        final JaxbCustomJavaJobAdaptor adaptor = new JaxbCustomJavaJobAdaptor();
                        return adaptor.unmarshal(in);
                    }
                });

        final String className = customJavaJobType.getClassName();
        final CustomJavaJob customJavaJob;
        final ComponentDescriptor<?> descriptor;
        try {
            final Class<?> customJavaClass = Class.forName(className, true, getClass().getClassLoader());
            descriptor = Descriptors.ofComponent(customJavaClass);
            customJavaJob = (CustomJavaJob) ReflectionUtils.newInstance(customJavaClass);
        } catch (Exception e) {
            executionLogger.setStatusFailed(this, className, e);
            return;
        }

        executionLogger.log("Succesfully loaded a job instance of Java class: " + className);

        try {

            final InjectionManager injectionManager = context.getConfiguration().getInjectionManager(null);
            final LifeCycleHelper lifeCycleHelper = new LifeCycleHelper(injectionManager, true);

            lifeCycleHelper.assignProvidedProperties(descriptor, customJavaJob);
            lifeCycleHelper.initialize(descriptor, customJavaJob);
            executionLogger.log("Succesfully initialized job instance, executing");

            try {
                customJavaJob.execute();
                executionLogger.log("Succesfully executed job instance, closing");
            } finally {
                lifeCycleHelper.close(descriptor, customJavaJob);
            }

            executionLogger.setStatusSuccess(null);
        } catch (Exception e) {
            executionLogger.setStatusFailed(this, null, e);
        }
    }
}
