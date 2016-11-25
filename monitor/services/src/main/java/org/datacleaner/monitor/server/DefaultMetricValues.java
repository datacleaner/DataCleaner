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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.datacleaner.descriptors.MetricDescriptor;
import org.datacleaner.descriptors.MetricParameters;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.monitor.job.MetricJobContext;
import org.datacleaner.monitor.job.MetricJobEngine;
import org.datacleaner.monitor.job.MetricValues;
import org.datacleaner.monitor.shared.model.MetricIdentifier;
import org.datacleaner.result.AnalysisResult;

/**
 * Default {@link MetricValues} implementation, calculates the metric values
 * directly from the {@link AnalysisResult}.
 */
public final class DefaultMetricValues implements MetricValues {

    private final MetricJobEngine<?> _jobEngine;
    private final MetricJobContext _job;
    private final Date _metricDate;
    private final List<MetricIdentifier> _metricIdentifiers;
    private final AnalysisResult _analysisResult;
    private final AnalysisJob _analysisJob;

    public DefaultMetricValues(final MetricJobEngine<?> jobEngine, final MetricJobContext job,
            final List<MetricIdentifier> metricIdentifiers, final AnalysisResult analysisResult,
            final AnalysisJob analysisJob) {
        _jobEngine = jobEngine;
        _job = job;
        _metricIdentifiers = metricIdentifiers;
        _analysisResult = analysisResult;
        _analysisJob = analysisJob;
        _metricDate = analysisResult.getCreationDate();
    }

    public DefaultMetricValues(final MetricJobEngine<?> jobEngine, final MetricJobContext job,
            final List<MetricIdentifier> metricIdentifiers, final AnalysisResult analysisResult) {
        _jobEngine = jobEngine;
        _job = job;
        _metricIdentifiers = metricIdentifiers;
        _analysisResult = analysisResult;
        _analysisJob = null;
        _metricDate = analysisResult.getCreationDate();
    }

    @Override
    public Date getMetricDate() {
        return _metricDate;
    }

    @Override
    public List<Number> getValues() {
        final int metricCount = _metricIdentifiers.size();
        final List<ComponentJob> componentJobs = new ArrayList<>(metricCount);
        final List<MetricDescriptor> metricDescriptors = new ArrayList<>(metricCount);
        final List<MetricParameters> metricParameters = new ArrayList<>(metricCount);
        final MetricValueUtils metricValueUtils = new MetricValueUtils();

        for (final MetricIdentifier metricIdentifier : _metricIdentifiers) {
            final ComponentJob analyzerJob =
                    metricValueUtils.getComponentJob(metricIdentifier, _analysisJob, _analysisResult);
            componentJobs.add(analyzerJob);

            final MetricDescriptor metricDescriptor =
                    metricValueUtils.getMetricDescriptor(metricIdentifier, _analysisJob, analyzerJob, _analysisResult);
            metricDescriptors.add(metricDescriptor);

            final MetricParameters parameter =
                    metricValueUtils.getParameters(_jobEngine, _job, metricIdentifier, metricDescriptor, analyzerJob);
            metricParameters.add(parameter);
        }

        final List<Number> metricValuesList = new ArrayList<>(metricCount);
        for (int i = 0; i < metricCount; i++) {
            final MetricIdentifier metricIdentifier = _metricIdentifiers.get(i);
            final ComponentJob job = componentJobs.get(i);
            final MetricDescriptor metric = metricDescriptors.get(i);
            final MetricParameters parameters = metricParameters.get(i);

            final Number metricValue = metricValueUtils
                    .getMetricValue(_jobEngine, _job, metricIdentifier, metric, _analysisJob, job, _analysisResult,
                            parameters);
            metricValuesList.add(metricValue);
        }

        return metricValuesList;
    }
}
