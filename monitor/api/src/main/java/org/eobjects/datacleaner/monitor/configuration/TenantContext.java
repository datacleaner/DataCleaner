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
package org.eobjects.datacleaner.monitor.configuration;

import java.util.List;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.datacleaner.monitor.job.JobContext;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFolder;

/**
 * Defines a context for a <i>single</i> tenant in which access to shared
 * entries in the repository can be reached.
 */
public interface TenantContext {

    public String getTenantId();

    public List<JobIdentifier> getJobs();

    public JobContext getJob(String jobName);

    public JobContext getJob(JobIdentifier jobIdentifier);

    public ResultContext getLatestResult(JobContext job);

    public ResultContext getResult(String resultFileName);

    public RepositoryFolder getTenantRootFolder();

    public RepositoryFolder getJobFolder();

    public RepositoryFolder getResultFolder();

    public RepositoryFolder getTimelineFolder();

    public RepositoryFile getConfigurationFile();

    public AnalyzerBeansConfiguration getConfiguration();

    public boolean containsJob(String jobName);

    /**
     * Notification method callable by external components if a circumstance in the configuration changes.
     */
    public void onConfigurationChanged();
}
