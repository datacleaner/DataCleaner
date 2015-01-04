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
package org.datacleaner.monitor.wizard.job;

import java.io.OutputStream;

import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.JaxbJobWriter;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.util.StringUtils;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.shared.model.DCUserInputException;
import org.datacleaner.repository.RepositoryFolder;
import org.datacleaner.util.FileFilters;
import org.apache.metamodel.util.Action;

/**
 * Represents a typically used abstract class of {@link JobWizardSession}, which
 * is applicable for every job wizard that produces DataCleaner jobs.
 */
public abstract class DataCleanerJobWizardSession extends AbstractJobWizardSession implements JobWizardSession {

    private String _jobName;

    public DataCleanerJobWizardSession(JobWizardContext context) {
        super(context);
    }
    
    public String getJobName() {
        return _jobName;
    }
    
    public void setJobName(String jobName) {
        _jobName = jobName;
    }

    @Override
    public final String finished() {
        final TenantContext tenantContext = getWizardContext().getTenantContext();
        final RepositoryFolder jobFolder = tenantContext.getJobFolder();
        final String jobName = getJobName();
        if (StringUtils.isNullOrEmpty(jobName)) {
            throw new DCUserInputException("No job name provided");
        }
        jobFolder.createFile(jobName + FileFilters.ANALYSIS_XML.getExtension(), new Action<OutputStream>() {
            @Override
            public void run(OutputStream out) throws Exception {

                final AnalysisJobBuilder jobBuilder = createJob();
                final AnalysisJob analysisJob = jobBuilder.toAnalysisJob();

                final AnalyzerBeansConfiguration configuration = tenantContext.getConfiguration();
                final JaxbJobWriter writer = new JaxbJobWriter(configuration);
                writer.write(analysisJob, out);
            }
        });
        return jobName;
    }

    /**
     * Creates the final analysis job as prescribed by the wizard. This method
     * will be invoked when no more pages are available and the wizard has
     * ended.
     * 
     * @return
     */
    public abstract AnalysisJobBuilder createJob();
}
