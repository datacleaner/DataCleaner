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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalysisJobMetadata;
import org.eobjects.analyzer.job.JaxbJobReader;
import org.eobjects.datacleaner.monitor.server.MonitorJobReader;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.metamodel.util.FileHelper;

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

    public DefaultJobContext(TenantContext context, RepositoryFile file) {
        _context = context;
        _file = file;

        _lastModifiedCache = -1;
        _sourceDatastoreName = null;
        _job = null;
    }

    @Override
    public AnalysisJob getAnalysisJob() {
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

    @Override
    public String getSourceDatastoreName() {
        long lastModified = _file.getLastModified();
        if (_sourceDatastoreName == null || lastModified != _lastModifiedCache) {
            synchronized (this) {
                lastModified = _file.getLastModified();
                if (_sourceDatastoreName == null || lastModified != _lastModifiedCache) {
                    final AnalyzerBeansConfiguration configuration = _context.getConfiguration();
                    final JaxbJobReader jobReader = new JaxbJobReader(configuration);
                    final InputStream in = _file.readFile();
                    try {
                        AnalysisJobMetadata metadata = jobReader.readMetadata(in);
                        _sourceDatastoreName = metadata.getDatastoreName();
                    } finally {
                        FileHelper.safeClose(in);
                    }
                }
            }
        }
        return _sourceDatastoreName;
    }

    @Override
    public void toXml(OutputStream out) {
        final InputStream in = _file.readFile();
        try {
            FileHelper.copy(in, out);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            FileHelper.safeClose(in);
        }
    }
}
