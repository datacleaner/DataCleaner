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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.MetricDescriptor;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalysisJobMetadata;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.JaxbJobReader;
import org.eobjects.analyzer.util.LabelUtils;
import org.eobjects.datacleaner.monitor.server.MetricValueUtils;
import org.eobjects.datacleaner.monitor.server.MonitorJobReader;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.JobMetrics;
import org.eobjects.datacleaner.monitor.shared.model.MetricGroup;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.util.FileFilters;
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
    public String getName() {
        final int extensionLength = FileFilters.ANALYSIS_XML.getExtension().length();
        final String filename = _file.getName();
        return filename.substring(0, filename.length() - extensionLength);
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

    @Override
    public JobMetrics getJobMetrics() {
        final AnalysisJob job = getAnalysisJob();
        final MetricValueUtils metricValueUtils = new MetricValueUtils();

        final Collection<AnalyzerJob> analyzerJobs = job.getAnalyzerJobs();

        final List<MetricGroup> metricGroups = new ArrayList<MetricGroup>();
        for (AnalyzerJob analyzerJob : analyzerJobs) {
            final Set<MetricDescriptor> metricDescriptors = analyzerJob.getDescriptor().getResultMetrics();
            if (!metricDescriptors.isEmpty()) {
                final String label = LabelUtils.getLabel(analyzerJob);
                final InputColumn<?> identifyingInputColumn = metricValueUtils.getIdentifyingInputColumn(analyzerJob);
                final List<MetricIdentifier> metricIdentifiers = new ArrayList<MetricIdentifier>();

                for (MetricDescriptor metricDescriptor : metricDescriptors) {
                    MetricIdentifier metricIdentifier = new MetricIdentifier();
                    metricIdentifier.setAnalyzerDescriptorName(analyzerJob.getDescriptor().getDisplayName());
                    metricIdentifier.setAnalyzerName(analyzerJob.getName());
                    if (identifyingInputColumn != null) {
                        metricIdentifier.setAnalyzerInputName(identifyingInputColumn.getName());
                    }
                    metricIdentifier.setMetricDescriptorName(metricDescriptor.getName());
                    metricIdentifier.setParameterizedByColumnName(metricDescriptor.isParameterizedByInputColumn());
                    metricIdentifier.setParameterizedByQueryString(metricDescriptor.isParameterizedByString());

                    metricIdentifiers.add(metricIdentifier);
                }

                final List<String> columnNames = new ArrayList<String>();
                final Set<ConfiguredPropertyDescriptor> inputProperties = analyzerJob.getDescriptor()
                        .getConfiguredPropertiesForInput(false);
                for (ConfiguredPropertyDescriptor inputProperty : inputProperties) {
                    final Object input = analyzerJob.getConfiguration().getProperty(inputProperty);
                    if (input instanceof InputColumn) {
                        String columnName = ((InputColumn<?>) input).getName();
                        columnNames.add(columnName);
                    } else if (input instanceof InputColumn[]) {
                        InputColumn<?>[] inputColumns = (InputColumn<?>[]) input;
                        for (InputColumn<?> inputColumn : inputColumns) {
                            String columnName = inputColumn.getName();
                            if (!columnNames.contains(columnName)) {
                                columnNames.add(columnName);
                            }
                        }
                    }
                }

                final MetricGroup metricGroup = new MetricGroup();
                metricGroup.setName(label);
                metricGroup.setMetrics(metricIdentifiers);
                metricGroup.setColumnNames(columnNames);
                metricGroups.add(metricGroup);
            }
        }

        final JobMetrics metrics = new JobMetrics();
        metrics.setMetricGroups(metricGroups);
        metrics.setJob(new JobIdentifier(getName()));
        return metrics;
    }
}
