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
package org.datacleaner.monitor.job;

import java.util.Map;

import org.apache.metamodel.util.HasName;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.repository.RepositoryFile;

/**
 * Defines a context around an {@link AnalysisJob}.
 */
public interface JobContext extends HasName {

    /**
     * Gets the name of the job
     * 
     * @return
     */
    @Override
    public String getName();

    /**
     * Gets the tenant of this job's context.
     * 
     * @return
     */
    public TenantContext getTenantContext();

    /**
     * Gets the {@link JobEngine} responsible for running this type of job
     * 
     * @return
     */
    public JobEngine<?> getJobEngine();

    /**
     * Gets the file in the repository which holds the file, or null if the job
     * is not contained within a repository file.
     * 
     * @return
     */
    public RepositoryFile getJobFile();

    /**
     * Gets a name of a group of jobs. Usually this group will say something
     * about the datastore that the job pertains to, or will be another similar
     * category of jobs.
     * 
     * @return
     */
    public String getGroupName();

    /**
     * Gets any known variables of the job, optionally mapped to their default
     * values.
     * 
     * @return
     */
    public Map<String, String> getVariables();

    /**
     * Gets any metadata properties that the job may hold.
     * 
     * @return
     */
    public Map<String, String> getMetadataProperties();
}
