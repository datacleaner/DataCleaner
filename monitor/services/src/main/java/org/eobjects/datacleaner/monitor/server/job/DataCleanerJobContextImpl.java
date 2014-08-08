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
package org.eobjects.datacleaner.monitor.server.job;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalysisJobMetadata;
import org.eobjects.analyzer.job.JaxbJobReader;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.job.JobContext;
import org.eobjects.datacleaner.monitor.server.MetricValueUtils;
import org.eobjects.datacleaner.monitor.server.MonitorJobReader;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.JobMetrics;
import org.eobjects.datacleaner.monitor.shared.model.MetricGroup;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.metamodel.util.Action;
import org.eobjects.metamodel.util.FileHelper;
import org.eobjects.metamodel.util.Func;

/**
 * The {@link JobContext} implementation for DataCleaner analysis jobs. This
 * implementation caches the read analysis jobs for quick access.
 */
public class DataCleanerJobContextImpl implements DataCleanerJobContext {

    private final RepositoryFile _file;
    private final DataCleanerJobEngine _engine;
    private final TenantContext _tenantContext;

    private volatile long _lastModifiedCache;
    private volatile AnalysisJob _job;
    private volatile String _sourceDatastoreName;
    private volatile List<String> _sourceColumnPaths;
    private volatile Map<String, String> _variables;

    public DataCleanerJobContextImpl(DataCleanerJobEngine engine, TenantContext tenantContext, RepositoryFile file) {
        _tenantContext = tenantContext;
        _engine = engine;
        _file = file;

        _lastModifiedCache = -1;
        _sourceDatastoreName = null;
        _variables = null;
        _job = null;
    }

    @Override
    public TenantContext getTenantContext() {
        return _tenantContext;
    }

    @Override
    public DataCleanerJobEngine getJobEngine() {
        return _engine;
    }

    @Override
    public String getName() {
        final int extensionLength = FileFilters.ANALYSIS_XML.getExtension().length();
        final String filename = _file.getName();
        return filename.substring(0, filename.length() - extensionLength);
    }

    @Override
    public AnalysisJob getAnalysisJob(Map<String, String> variableOverrides) {
        if (variableOverrides == null || variableOverrides.isEmpty()) {
            // cached job definition may be used, if not outdated
            final long configurationLastModified = _tenantContext.getConfigurationFile().getLastModified();
            long lastModified = Math.max(_file.getLastModified(), configurationLastModified);
            if (_job == null || lastModified != _lastModifiedCache) {
                synchronized (this) {
                    lastModified = Math.max(_file.getLastModified(), configurationLastModified);
                    if (_job == null || lastModified != _lastModifiedCache) {
                        _lastModifiedCache = lastModified;
                        final AnalyzerBeansConfiguration configuration = _tenantContext.getConfiguration();
                        final MonitorJobReader reader = new MonitorJobReader(configuration, _file);
                        _job = reader.readJob();
                        _sourceDatastoreName = _job.getDatastore().getName();
                    }
                }
            }
            return _job;
        }

        final AnalyzerBeansConfiguration configuration = _tenantContext.getConfiguration();
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
    public String getGroupName() {
        // use datastore as group name
        return getSourceDatastoreName();
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
                    final AnalyzerBeansConfiguration configuration = _tenantContext.getConfiguration();
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

    @Override
    public JobMetrics getJobMetrics() {
        final AnalysisJob job = getAnalysisJob();

        final MetricValueUtils utils = new MetricValueUtils();
        final List<MetricGroup> metricGroups = utils.getMetricGroups(this, job);

        final JobMetrics metrics = new JobMetrics();
        metrics.setMetricGroups(metricGroups);
        metrics.setJob(new JobIdentifier(getName()));
        return metrics;
    }

	
}
