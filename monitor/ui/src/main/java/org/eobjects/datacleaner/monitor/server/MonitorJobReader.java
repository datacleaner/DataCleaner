/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.monitor.server;

import java.io.InputStream;
import java.util.List;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.SourceColumnMapping;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalysisJobMetadata;
import org.eobjects.analyzer.job.JaxbJobReader;
import org.eobjects.analyzer.job.JobReader;
import org.eobjects.datacleaner.monitor.configuration.PlaceholderDatastore;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.metamodel.schema.ColumnType;
import org.eobjects.metamodel.util.FileHelper;

/**
 * A component that reads jobs for the monitor web app without starting a live
 * connection to the source. Since the datastore may not be reachable from the
 * monitor application, a {@link PlaceholderDatastore} will be created and used
 * for reading the job.
 */
public class MonitorJobReader {

    private final AnalyzerBeansConfiguration _configuration;
    private final RepositoryFile _jobFile;

    public MonitorJobReader(AnalyzerBeansConfiguration configuration, RepositoryFile jobFile) {
        _configuration = configuration;
        _jobFile = jobFile;
    }

    public AnalysisJob readJob() {
        final JobReader<InputStream> jobReader = new JaxbJobReader(_configuration);

        // read metadata
        final AnalysisJobMetadata metadata;
        {
            final InputStream inputStream = _jobFile.readFile();
            try {
                metadata = jobReader.readMetadata(inputStream);
            } finally {
                FileHelper.safeClose(inputStream);
            }
        }

        final String datastoreName = metadata.getDatastoreName();
        final Datastore datastore = _configuration.getDatastoreCatalog().getDatastore(datastoreName);

        // read job
        final AnalysisJob job;
        {
            final InputStream inputStream = _jobFile.readFile();
            try {
                if (datastore == null) {
                    final List<String> sourceColumnPaths = metadata.getSourceColumnPaths();
                    final List<ColumnType> sourceColumnTypes = metadata.getSourceColumnTypes();
                    final PlaceholderDatastore placeholderDatastore = new PlaceholderDatastore(datastoreName,
                            sourceColumnPaths, sourceColumnTypes);

                    final SourceColumnMapping sourceColumnMapping = new SourceColumnMapping(sourceColumnPaths);
                    sourceColumnMapping.setDatastore(placeholderDatastore);
                    sourceColumnMapping.autoMap(placeholderDatastore);

                    if (!sourceColumnMapping.isSatisfied()) {
                        throw new IllegalStateException("Not all column mapping satisfied. Missing: "
                                + sourceColumnMapping.getUnmappedPaths());
                    }
                    job = jobReader.read(inputStream, sourceColumnMapping);
                } else {
                    job = jobReader.read(inputStream);
                }
            } finally {
                FileHelper.safeClose(inputStream);
            }
        }

        return job;
    }
}
