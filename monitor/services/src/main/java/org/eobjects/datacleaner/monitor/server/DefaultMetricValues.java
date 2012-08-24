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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.MetricDescriptor;
import org.eobjects.analyzer.descriptors.MetricParameters;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@link MetricValues} implementation, calculates the metric values
 * directly from the {@link AnalysisResult}.
 */
public final class DefaultMetricValues implements MetricValues {

    private static final Logger logger = LoggerFactory.getLogger(DefaultMetricValues.class);
    
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

            final MetricDescriptor metricDescriptor = getMetricDescriptor(metricIdentifier, analyzerJob);
            metricDescriptors.add(metricDescriptor);

            MetricParameters parameter = createMetricParameter(metricIdentifier, metricDescriptor, analyzerJob);
            metricParameters.add(parameter);
        }

        final List<Number> metricValuesList = new ArrayList<Number>(metricCount);
        for (int i = 0; i < metricCount; i++) {
            final MetricIdentifier metricIdentifier = _metricIdentifiers.get(i);
            final AnalyzerJob job = analyzerJobs.get(i);
            final MetricDescriptor metric = metricDescriptors.get(i);
            final MetricParameters parameters = metricParameters.get(i);

            final AnalyzerResult analyzerResult = metricValueUtils.getResult(_analysisResult, job, metricIdentifier);

            final Number metricValue = metric.getValue(analyzerResult, parameters);
            metricValuesList.add(metricValue);
        }

        return metricValuesList;
    }

    private MetricDescriptor getMetricDescriptor(final MetricIdentifier metricIdentifier, final AnalyzerJob analyzerJob) {
        AnalyzerBeanDescriptor<?> analyzerDescriptor = analyzerJob.getDescriptor();
        MetricDescriptor metric = analyzerDescriptor.getResultMetric(metricIdentifier.getMetricDescriptorName());

        if (metric == null) {
            logger.error("Did not find any metric descriptors with name '{}' in {}",
                    metricIdentifier.getMetricDescriptorName(), analyzerDescriptor.getResultClass());
        }
        return metric;
    }

    private MetricParameters createMetricParameter(final MetricIdentifier metricIdentifier,
            final MetricDescriptor metricDescriptor, AnalyzerJob analyzerJob) {
        final String queryString;
        final InputColumn<?> queryInputColumn;

        final String paramQueryString = metricIdentifier.getParamQueryString();
        if (paramQueryString == null) {
            queryString = null;
        } else {
            queryString = paramQueryString;
        }

        final String paramColumnName = metricIdentifier.getParamColumnName();
        if (paramColumnName == null) {
            queryInputColumn = null;
        } else {
            InputColumn<?>[] inputColumns = analyzerJob.getInput();
            InputColumn<?> candidate = null;
            for (InputColumn<?> inputColumn : inputColumns) {
                if (paramColumnName.equals(inputColumn.getName())) {
                    candidate = inputColumn;
                    break;
                }
            }
            if (candidate == null) {
                logger.warn("Could not find any input column with name '{}'", paramColumnName);
            }
            queryInputColumn = candidate;
        }

        return new MetricParameters(queryString, queryInputColumn);
    }
}
