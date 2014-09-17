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
package org.eobjects.datacleaner.monitor.configuration;

import java.util.List;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.datacleaner.monitor.job.JobContext;
import org.eobjects.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFolder;

/**
 * Defines a context for a <i>single</i> tenant in which access to shared
 * entries in the repository can be reached.
 */
public interface TenantContext {

    /**
     * Gets the ID of the tenant.
     * 
     * @return
     */
    public String getTenantId();

    /**
     * Gets a list of all jobs owned/managed by the tenant.
     * 
     * @return
     */
    public List<JobIdentifier> getJobs();

    /**
     * Gets a particular job by it's name.
     * 
     * @param jobName
     * @return
     */
    public JobContext getJob(String jobName);

    /**
     * Gets a particular job by it's {@link JobIdentifier}.
     * 
     * @param jobIdentifier
     * @return
     */
    public JobContext getJob(JobIdentifier jobIdentifier);

    /**
     * Gets the latest result of a particular job.
     * 
     * @param job
     * @return
     */
    public ResultContext getLatestResult(JobContext job);

    /**
     * Gets a particular result by it's result filename
     * 
     * @param resultFileName
     * @return
     */
    public ResultContext getResult(String resultFileName);

    /**
     * Gets the root folder of this tenant.
     * 
     * @return
     */
    public RepositoryFolder getTenantRootFolder();

    /**
     * Gets the folder used for storing jobs by this tenant.
     * 
     * @return
     */
    public RepositoryFolder getJobFolder();

    /**
     * Gets the folder used for storing results by this tenant.
     * 
     * @return
     */
    public RepositoryFolder getResultFolder();

    /**
     * Gets the folder used for storing timeline definitions by this tenant.
     * 
     * @return
     */
    public RepositoryFolder getTimelineFolder();

    /**
     * Gets the configuration file (conf.xml) used by this tenant. Usually it is
     * preferable to get the configuration object itself instead, see
     * {@link #getConfiguration()}.
     * 
     * @return
     */
    public RepositoryFile getConfigurationFile();

    /**
     * Gets a list of datastores owned/controlled by the tenant.
     * 
     * @return
     */
    public List<DatastoreIdentifier> getDatastores();

    /**
     * Gets a datastore by it's identifier, basically a shortcut for
     * {@link DatastoreCatalog#getDatastore(String)}.
     * 
     * @param datastoreName
     * @return
     */
    public Datastore getDatastore(DatastoreIdentifier datastoreIdentifier);

    /**
     * Gets/loads the {@link AnalyzerBeansConfiguration} for this tenant.
     * 
     * @return
     */
    public AnalyzerBeansConfiguration getConfiguration();

    /**
     * Determines if a particular job exists or not.
     * 
     * @param jobName
     * @return
     */
    public boolean containsJob(String jobName);

    /**
     * Notification method callable by external components if a circumstance in
     * the configuration changes.
     */
    public void onConfigurationChanged();
}
