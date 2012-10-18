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
package org.eobjects.datacleaner.monitor.configuration;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalysisJobMetadata;
import org.eobjects.analyzer.job.JaxbJobReader;
import org.eobjects.datacleaner.monitor.server.MonitorJobReader;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.metamodel.util.Action;
import org.eobjects.metamodel.util.FileHelper;
import org.eobjects.metamodel.util.Func;

/**
 * Default {@link JobContext} implementation. This implementation caches the
 * read analysis jobs for quick access.
 */
class DefaultJobContext implements JobContext {

    private final RepositoryFile _file;
    private final TenantContext _context;

    private volatile long _lastModifiedCache;
    private volatile AnalysisJob _job;
    private volatile String _sourceDatastoreName;
    private volatile List<String> _sourceColumnPaths;
    private volatile Map<String, String> _variables;

    public DefaultJobContext(TenantContext context, RepositoryFile file) {
        _context = context;
        _file = file;

        _lastModifiedCache = -1;
        _sourceDatastoreName = null;
        _variables = null;
        _job = null;
    }

    @Override
    public AnalysisJob getAnalysisJob(Map<String, String> variableOverrides) {
        if (variableOverrides == null || variableOverrides.isEmpty()) {
            // use cached job definitions
            long lastModified = _file.getLastModified();
            if (_job == null || lastModified != _lastModifiedCache) {
                synchronized (this) {
                    lastModified = _file.getLastModified();
                    if (_job == null || lastModified != _lastModifiedCache) {
                        _lastModifiedCache = lastModified;
                        final AnalyzerBeansConfiguration configuration = _context.getConfiguration();
                        final MonitorJobReader reader = new MonitorJobReader(configuration, _file);
                        _job = reader.readJob();
                        _sourceDatastoreName = _job.getDatastore().getName();
                    }
                }
            }
            return _job;
        }

        final AnalyzerBeansConfiguration configuration = _context.getConfiguration();
        final MonitorJobReader reader = new MonitorJobReader(configuration, _file);
        final AnalysisJob job = reader.readJob(variableOverrides);
        _sourceDatastoreName = _job.getDatastore().getName();
        return job;
    }

    @Override
    public AnalysisJob getAnalysisJob() {
        return getAnalysisJob(null);
    }

    @Override
    public String getSourceDatastoreName() {
        verifyJobMetadataCurrent(_sourceDatastoreName);
        return _sourceDatastoreName;
    }

    @Override
    public List<String> getSourceColumnPaths() {
        verifyJobMetadataCurrent(_sourceColumnPaths);
        return _sourceColumnPaths;
    }

    @Override
    public Map<String, String> getVariables() {
        verifyJobMetadataCurrent(_variables);
        return _variables;
    }

    /**
     * Checks if the current copy of the metadata object is recent.
     * 
     * @param metadataObject
     *            the object to look for.
     */
    private void verifyJobMetadataCurrent(final Object metadataObject) {
        long lastModified = _file.getLastModified();
        if (metadataObject == null || lastModified != _lastModifiedCache) {
            synchronized (this) {
                lastModified = _file.getLastModified();
                if (_sourceDatastoreName == null || lastModified != _lastModifiedCache) {
                    final AnalyzerBeansConfiguration configuration = _context.getConfiguration();
                    final AnalysisJobMetadata metadata = _file.readFile(new Func<InputStream, AnalysisJobMetadata>() {
                        @Override
                        public AnalysisJobMetadata eval(InputStream in) {
                            final JaxbJobReader jobReader = new JaxbJobReader(configuration);
                            AnalysisJobMetadata metadata = jobReader.readMetadata(in);
                            return metadata;
                        }
                    });
                    _sourceDatastoreName = metadata.getDatastoreName();
                    _sourceColumnPaths = metadata.getSourceColumnPaths();
                    _variables = metadata.getVariables();
                }
            }
        }
    }

    @Override
    public void toXml(final OutputStream out) {
        _file.readFile(new Action<InputStream>() {
            @Override
            public void run(InputStream in) throws Exception {
                FileHelper.copy(in, out);
            }
        });
    }

    @Override
    public RepositoryFile getJobFile() {
        return _file;
    }
}
