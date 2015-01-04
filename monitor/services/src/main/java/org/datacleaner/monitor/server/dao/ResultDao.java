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
package org.datacleaner.monitor.server.dao;

import java.util.Date;
import java.util.List;

import org.datacleaner.monitor.configuration.ResultContext;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.datacleaner.repository.RepositoryFile;

/**
 * DAO (Data Access Object) interface for interactions with analysis results in
 * the repository.
 */
public interface ResultDao {

    /**
     * Gets all results (as a list of {@link RepositoryFile}s) for a particular
     * job.
     * 
     * Use {@link #getResult(TenantIdentifier, RepositoryFile)} on relevant
     * elements of the list, if needed.
     * 
     * @param tenantIdentifier
     * @param job
     * @return
     */
    public List<RepositoryFile> getResultsForJob(TenantIdentifier tenantIdentifier, JobIdentifier job);

    /**
     * Gets a {@link ResultContext} of a particular {@link RepositoryFile}.
     * 
     * @param tenant
     * @param resultFile
     * @return
     */
    public ResultContext getResult(TenantIdentifier tenant, RepositoryFile resultFile);

    /**
     * Gets the latest result of a particular job
     * 
     * @param tenant
     * @param job
     * @return
     */
    public ResultContext getLatestResult(TenantIdentifier tenant, JobIdentifier job);

    /**
     * Updates an existing result with a new job and/or new timestamp
     * 
     * @param tenantIdentifier
     * @param resultFile
     * @param newJob
     *            the new job of the result, or null if unchanged
     * @param newTimestamp
     *            the new timestamp of the result, or null if unchanged
     * @return
     */
    public ResultContext updateResult(TenantIdentifier tenantIdentifier, RepositoryFile resultFile,
            JobIdentifier newJob, Date newTimestamp);

    /**
     * Updates an existing result with a new job and/or new timestamp
     * 
     * @param tenantIdentifier
     * @param repositoryFile
     * @param newJob
     *            the new job of the result, or null if unchanged
     * @param newTimestamp
     *            the new timestamp of the result, or null if unchanged
     * @return
     */
    public ResultContext updateResult(TenantIdentifier tenantIdentifier, ResultContext result, JobIdentifier newJob,
            Date newTimestamp);
}
