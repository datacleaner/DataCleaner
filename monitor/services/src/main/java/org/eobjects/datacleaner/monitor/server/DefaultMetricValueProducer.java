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
import java.util.List;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.MetricDescriptor;
import org.eobjects.analyzer.descriptors.MetricParameters;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.datacleaner.monitor.configuration.ResultContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DefaultMetricValueProducer implements MetricValueProducer {

	TenantContextFactory _tenantContextFactory;

	private static final Logger logger = LoggerFactory
			.getLogger(DashboardServiceImpl.class);

	@Autowired
	public DefaultMetricValueProducer(TenantContextFactory tenantContextFactory) {
		_tenantContextFactory = tenantContextFactory;
	}

	@Override
	public MetricValues getMetricValues(
			List<MetricIdentifier> metricIdentifiers,
			RepositoryFile resultFile, TenantIdentifier tenant,
			JobIdentifier jobIdentifier) {

		final int metricCount = metricIdentifiers.size();

		TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
		ResultContext resultContext = tenantContext.getResult(resultFile
				.getName());
		AnalysisJob analysisJob = tenantContext.getJob(jobIdentifier.getName())
				.getAnalysisJob();
		MetricValueUtils metricValueUtils = new MetricValueUtils();
		final AnalysisResult analysisResult = resultContext.getAnalysisResult();
		final List<AnalyzerJob> analyzerJobs = new ArrayList<AnalyzerJob>(
				metricCount);
		final List<MetricDescriptor> metricDescriptors = new ArrayList<MetricDescriptor>(
				metricCount);
		final List<MetricParameters> metricParameters = new ArrayList<MetricParameters>(
				metricCount);

		for (MetricIdentifier metricIdentifier : metricIdentifiers) {
			final AnalyzerJob analyzerJob = metricValueUtils.getAnalyzerJob(
					metricIdentifier, analysisJob);
			analyzerJobs.add(analyzerJob);

			final MetricDescriptor metricDescriptor = getMetricDescriptor(
					metricIdentifier, analyzerJob);
			metricDescriptors.add(metricDescriptor);

			MetricParameters parameter = createMetricParameter(
					metricIdentifier, metricDescriptor, analyzerJob);
			metricParameters.add(parameter);
		}

		final List<Number> metricValuesList = new ArrayList<Number>(metricCount);
		for (int i = 0; i < metricCount; i++) {
			final MetricIdentifier metricIdentifier = metricIdentifiers.get(i);
			final AnalyzerJob job = analyzerJobs.get(i);
			final MetricDescriptor metric = metricDescriptors.get(i);
			final MetricParameters parameters = metricParameters.get(i);

			final AnalyzerResult analyzerResult = metricValueUtils.getResult(
					analysisResult, job, metricIdentifier);

			final Number metricValue = metric.getValue(analyzerResult,
					parameters);
			metricValuesList.add(metricValue);
		}
		MetricValuesImpl metricValuesImpl = new MetricValuesImpl();
		metricValuesImpl.setMetricDate(analysisResult.getCreationDate());
		metricValuesImpl.setMetricValues(metricValuesList);
		return metricValuesImpl;
	}

	private MetricDescriptor getMetricDescriptor(
			final MetricIdentifier metricIdentifier,
			final AnalyzerJob analyzerJob) {
		AnalyzerBeanDescriptor<?> analyzerDescriptor = analyzerJob
				.getDescriptor();
		MetricDescriptor metric = analyzerDescriptor
				.getResultMetric(metricIdentifier.getMetricDescriptorName());

		if (metric == null) {
			logger.error(
					"Did not find any metric descriptors with name '{}' in {}",
					metricIdentifier.getMetricDescriptorName(),
					analyzerDescriptor.getResultClass());
		}
		return metric;
	}

	private MetricParameters createMetricParameter(
			final MetricIdentifier metricIdentifier,
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
				logger.warn("Could not find any input column with name '{}'",
						paramColumnName);
			}
			queryInputColumn = candidate;
		}

		return new MetricParameters(queryString, queryInputColumn);
	}

}
