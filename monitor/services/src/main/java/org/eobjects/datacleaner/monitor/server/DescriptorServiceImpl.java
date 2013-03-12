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
import java.util.Collection;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.MetricDescriptor;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.NoSuchComponentException;
import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.monitor.configuration.JobContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.shared.DescriptorNotFoundException;
import org.eobjects.datacleaner.monitor.shared.DescriptorService;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.JobMetrics;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.util.FileFilters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Main implementation of the {@link DescriptorService} interface.
 */
@Component
public class DescriptorServiceImpl implements DescriptorService {

    private static final Logger logger = LoggerFactory.getLogger(DescriptorServiceImpl.class);

    private final TenantContextFactory _tenantContextFactory;

    @Autowired
    public DescriptorServiceImpl(TenantContextFactory tenantContextFactory) {
        _tenantContextFactory = tenantContextFactory;
    }

    @Override
    public JobMetrics getJobMetrics(final TenantIdentifier tenant, final JobIdentifier jobIdentifier)
            throws DescriptorNotFoundException {
        try {
            final JobContext jobContext = _tenantContextFactory.getContext(tenant).getJob(jobIdentifier.getName());
            return jobContext.getJobMetrics();
        } catch (NoSuchComponentException e) {
            logger.warn("Encountered exception while get Job metrics", e);
            throw new DescriptorNotFoundException(e.getMessage());
        }
    }

    @Override
    public Collection<String> getMetricParameterSuggestions(TenantIdentifier tenant, JobIdentifier job,
            MetricIdentifier metric) {
        if (metric == null || metric.isFormulaBased()) {
            return new ArrayList<String>(0);
        }

        final String analyzerDescriptorName = metric.getAnalyzerDescriptorName();
        final String metricDescriptorName = metric.getMetricDescriptorName();
        if (StringUtils.isNullOrEmpty(analyzerDescriptorName) || StringUtils.isNullOrEmpty(metricDescriptorName)) {
            return new ArrayList<String>(0);
        }

        final TenantContext context = _tenantContextFactory.getContext(tenant);
        final MetricValueUtils metricValueUtils = new MetricValueUtils();

        final AnalyzerBeansConfiguration configuration = context.getConfiguration();

        final AnalyzerBeanDescriptor<?> analyzerDescriptor = configuration.getDescriptorProvider()
                .getAnalyzerBeanDescriptorByDisplayName(analyzerDescriptorName);
        final MetricDescriptor metricDescriptor = analyzerDescriptor.getResultMetric(metricDescriptorName);

        if (!metricDescriptor.isParameterizedByString()) {
            return null;
        }

        final RepositoryFolder resultsFolder = context.getResultFolder();
        final String jobName = job.getName();

        final RepositoryFile resultFile = resultsFolder.getLatestFile(jobName,
                FileFilters.ANALYSIS_RESULT_SER.getExtension());
        if (resultFile == null) {
            return new ArrayList<String>(0);
        }

        final AnalysisResult analysisResult = context.getResult(resultFile.getName()).getAnalysisResult();

        final AnalysisJob analysisJob = context.getJob(job.getName()).getAnalysisJob();
        final AnalyzerJob analyzerJob = metricValueUtils.getAnalyzerJob(metric, analysisJob);

        final AnalyzerResult result = metricValueUtils.getResult(analysisResult, analyzerJob, metric);

        final Collection<String> suggestions = metricDescriptor.getMetricParameterSuggestions(result);

        // make sure we can send it across the GWT-RPC wire.
        if (suggestions instanceof ArrayList) {
            return suggestions;
        }
        return new ArrayList<String>(suggestions);
    }
}
