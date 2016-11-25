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
package org.datacleaner.monitor.server;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.util.Func;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.SourceColumnMapping;
import org.datacleaner.connection.Datastore;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.AnalysisJobMetadata;
import org.datacleaner.job.JaxbJobReader;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.monitor.configuration.PlaceholderDatastore;
import org.datacleaner.repository.RepositoryFile;

/**
 * A component that reads jobs for the monitor web app without starting a live
 * connection to the source. Since the datastore may not be reachable from the
 * monitor application, a {@link PlaceholderDatastore} will be created and used
 * for reading the job.
 */
public class MonitorJobReader {

    private final DataCleanerConfiguration _configuration;
    private final RepositoryFile _jobFile;

    public MonitorJobReader(final DataCleanerConfiguration configuration, final RepositoryFile jobFile) {
        _configuration = configuration;
        _jobFile = jobFile;
    }

    public AnalysisJob readJob() {
        return readJob(null);
    }

    public AnalysisJob readJob(final Map<String, String> variableOverrides) {
        final JaxbJobReader jobReader = new JaxbJobReader(_configuration);

        // read metadata
        final AnalysisJobMetadata metadata =
                _jobFile.readFile((Func<InputStream, AnalysisJobMetadata>) jobReader::readMetadata);

        final String datastoreName = metadata.getDatastoreName();
        final Datastore datastore = _configuration.getDatastoreCatalog().getDatastore(datastoreName);

        // read job
        final Func<InputStream, AnalysisJobBuilder> readCallback = inputStream -> {
            if (datastore == null) {
                final List<String> sourceColumnPaths = metadata.getSourceColumnPaths();
                final List<ColumnType> sourceColumnTypes = metadata.getSourceColumnTypes();
                final PlaceholderDatastore placeholderDatastore =
                        new PlaceholderDatastore(datastoreName, sourceColumnPaths, sourceColumnTypes);

                final SourceColumnMapping sourceColumnMapping = new SourceColumnMapping(sourceColumnPaths);
                sourceColumnMapping.setDatastore(placeholderDatastore);
                sourceColumnMapping.autoMap(placeholderDatastore);

                if (!sourceColumnMapping.isSatisfied()) {
                    throw new IllegalStateException(
                            "Not all column mapping satisfied. Missing: " + sourceColumnMapping.getUnmappedPaths());
                }

                return jobReader.create(inputStream, sourceColumnMapping, variableOverrides);
            } else {
                return jobReader.create(inputStream, variableOverrides);
            }
        };

        try (AnalysisJobBuilder jobBuilder = _jobFile.readFile(readCallback)) {
            return jobBuilder.toAnalysisJob(false);
        }
    }
}
