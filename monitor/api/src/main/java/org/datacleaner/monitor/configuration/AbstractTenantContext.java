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
package org.datacleaner.monitor.configuration;

import java.util.List;

import org.apache.metamodel.util.CollectionUtils;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.monitor.job.JobContext;
import org.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.repository.RepositoryFolder;
import org.datacleaner.util.FileFilters;
import org.datacleaner.util.StringUtils;

/**
 * Abstract helper-implementation of {@link TenantContext}. Provides the methods
 * which have a trivial implementation.
 */
public abstract class AbstractTenantContext implements TenantContext {

    protected static final String PATH_TIMELINES = "timelines";
    protected static final String PATH_JOBS = "jobs";
    protected static final String PATH_RESULTS = "results";

    protected static final String EXTENSION_RESULT = FileFilters.ANALYSIS_RESULT_SER.getExtension();

    protected abstract ResultContext getResult(RepositoryFile resultFile);

    @Override
    public final JobContext getJob(final String jobName) {
        return getJob(new JobIdentifier(jobName));
    }

    @Override
    public final ResultContext getLatestResult(final JobContext job) {
        final String jobName = job.getName();
        final RepositoryFolder resultFolder = getResultFolder();
        final RepositoryFile resultFile = resultFolder.getLatestFile(jobName, EXTENSION_RESULT);
        return getResult(resultFile);
    }

    @Override
    public final ResultContext getResult(String resultFilename) {
        if (StringUtils.isNullOrEmpty(resultFilename)) {
            return null;
        }
        if (!resultFilename.endsWith(EXTENSION_RESULT)) {
            resultFilename = resultFilename + EXTENSION_RESULT;
        }

        final RepositoryFolder resultFolder = getResultFolder();

        final RepositoryFile resultFile;
        if (resultFilename.endsWith("-latest" + EXTENSION_RESULT)) {
            final String jobName =
                    resultFilename.substring(0, resultFilename.length() - ("-latest" + EXTENSION_RESULT).length());
            resultFile = resultFolder.getLatestFile(jobName, EXTENSION_RESULT);
        } else {
            resultFile = resultFolder.getFile(resultFilename);
        }

        return getResult(resultFile);
    }

    @Override
    public final RepositoryFolder getJobFolder() {
        final RepositoryFolder tenantFolder = getTenantRootFolder();
        return tenantFolder.getOrCreateFolder(PATH_JOBS);
    }

    @Override
    public final RepositoryFolder getResultFolder() {
        final RepositoryFolder tenantFolder = getTenantRootFolder();
        return tenantFolder.getOrCreateFolder(PATH_RESULTS);
    }

    @Override
    public final RepositoryFolder getTimelineFolder() {
        final RepositoryFolder tenantFolder = getTenantRootFolder();
        return tenantFolder.getOrCreateFolder(PATH_TIMELINES);
    }

    @Override
    public final boolean containsJob(final String jobName) {
        try {
            final JobContext job = getJob(jobName);
            return job != null;
        } catch (final IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public final Datastore getDatastore(final DatastoreIdentifier datastoreIdentifier) {
        if (datastoreIdentifier == null) {
            return null;
        }
        final String name = datastoreIdentifier.getName();
        if (name == null) {
            return null;
        }
        final DatastoreCatalog datastoreCatalog = getConfiguration().getDatastoreCatalog();
        return datastoreCatalog.getDatastore(name);
    }

    @Override
    public final List<DatastoreIdentifier> getDatastores() {
        final DatastoreCatalog datastoreCatalog = getConfiguration().getDatastoreCatalog();
        final String[] datastoreNames = datastoreCatalog.getDatastoreNames();

        return CollectionUtils.map(datastoreNames, DatastoreIdentifier::new);
    }
}
