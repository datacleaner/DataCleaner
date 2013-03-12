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
package org.eobjects.datacleaner.monitor.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eobjects.analyzer.descriptors.MetricDescriptor;
import org.eobjects.analyzer.descriptors.MetricParameters;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;

/**
 * Default {@link MetricValues} implementation, calculates the metric values
 * directly from the {@link AnalysisResult}.
 */
public final class DefaultMetricValues implements MetricValues {

    private final Date _metricDate;
    private final List<MetricIdentifier> _metricIdentifiers;
    private final AnalysisResult _analysisResult;
    private final AnalysisJob _analysisJob;

    public DefaultMetricValues(List<MetricIdentifier> metricIdentifiers, AnalysisResult analysisResult,
            AnalysisJob analysisJob) {
        _metricIdentifiers = metricIdentifiers;
        _analysisResult = analysisResult;
        _analysisJob = analysisJob;
        _metricDate = analysisResult.getCreationDate();
    }

    @Override
    public Date getMetricDate() {
        return _metricDate;
    }

    @Override
    public List<Number> getValues() {
        final int metricCount = _metricIdentifiers.size();
        final List<AnalyzerJob> analyzerJobs = new ArrayList<AnalyzerJob>(metricCount);
        final List<MetricDescriptor> metricDescriptors = new ArrayList<MetricDescriptor>(metricCount);
        final List<MetricParameters> metricParameters = new ArrayList<MetricParameters>(metricCount);
        MetricValueUtils metricValueUtils = new MetricValueUtils();

        for (MetricIdentifier metricIdentifier : _metricIdentifiers) {
            final AnalyzerJob analyzerJob = metricValueUtils.getAnalyzerJob(metricIdentifier, _analysisJob);
            analyzerJobs.add(analyzerJob);

            final MetricDescriptor metricDescriptor = metricValueUtils.getMetricDescriptor(metricIdentifier,
                    _analysisJob, analyzerJob);
            metricDescriptors.add(metricDescriptor);

            MetricParameters parameter = metricValueUtils
                    .getParameters(metricIdentifier, metricDescriptor, analyzerJob);
            metricParameters.add(parameter);
        }

        final List<Number> metricValuesList = new ArrayList<Number>(metricCount);
        for (int i = 0; i < metricCount; i++) {
            final MetricIdentifier metricIdentifier = _metricIdentifiers.get(i);
            final AnalyzerJob job = analyzerJobs.get(i);
            final MetricDescriptor metric = metricDescriptors.get(i);
            final MetricParameters parameters = metricParameters.get(i);

            final Number metricValue = metricValueUtils.getMetricValue(metricIdentifier, metric, _analysisJob, job,
                    _analysisResult, parameters);
            metricValuesList.add(metricValue);
        }

        return metricValuesList;
    }
}
