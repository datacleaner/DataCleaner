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

import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.datacleaner.monitor.configuration.ResultContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.job.JobContext;
import org.eobjects.datacleaner.monitor.server.job.DataCleanerAnalysisJobContext;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of {@link MetricValueProducer}. Will read files from
 * the repository to calculate metrics.
 */
public class DefaultMetricValueProducer implements MetricValueProducer {

    private static final Logger logger = LoggerFactory.getLogger(DefaultMetricValueProducer.class);

    private final TenantContextFactory _tenantContextFactory;

    @Autowired
    public DefaultMetricValueProducer(TenantContextFactory tenantContextFactory) {
        _tenantContextFactory = tenantContextFactory;
    }

    @Override
    public MetricValues getMetricValues(List<MetricIdentifier> metricIdentifiers, RepositoryFile resultFile,
            TenantIdentifier tenant, JobIdentifier jobIdentifier) {

        TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        String resultFilename = resultFile.getName();
        ResultContext resultContext = tenantContext.getResult(resultFilename);
        AnalysisResult analysisResult;
        try {
            analysisResult = resultContext.getAnalysisResult();
        } catch (IllegalStateException e) {
            logger.warn(
                    "Could not get AnalysisResult (and thereby metric values) from result file: "
                            + resultFile.getQualifiedPath(), e);
            final Date date = new Date(resultFile.getLastModified());
            return new SimpleMetricValues(date, new ArrayList<Number>(metricIdentifiers.size()));
        }
        String jobName = jobIdentifier.getName();
        JobContext job = tenantContext.getJob(jobName);
        AnalysisJob analysisJob = ((DataCleanerAnalysisJobContext) job).getAnalysisJob();

        return new DefaultMetricValues(metricIdentifiers, analysisResult, analysisJob);
    }

}
