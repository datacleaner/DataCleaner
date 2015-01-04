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
package org.eobjects.datacleaner.monitor.server;

import java.util.List;

import org.eobjects.datacleaner.monitor.configuration.ResultContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.job.JobEngineManager;
import org.eobjects.datacleaner.monitor.job.MetricJobContext;
import org.eobjects.datacleaner.monitor.job.MetricJobEngine;
import org.eobjects.datacleaner.monitor.job.MetricValues;
import org.eobjects.datacleaner.monitor.server.job.DataCleanerJobEngine;
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
    private final JobEngineManager _jobEngineManager;

    @Autowired
    public DefaultMetricValueProducer(TenantContextFactory tenantContextFactory, JobEngineManager jobEngineManager) {
        _tenantContextFactory = tenantContextFactory;
        _jobEngineManager = jobEngineManager;
    }

    @Override
    public MetricValues getMetricValues(List<MetricIdentifier> metricIdentifiers, RepositoryFile resultFile,
            TenantIdentifier tenant, JobIdentifier jobIdentifier) {
        final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        final String resultFilename = resultFile.getName();
        final ResultContext resultContext = tenantContext.getResult(resultFilename);

        final String jobName = jobIdentifier.getName();
        final MetricJobContext job = (MetricJobContext) tenantContext.getJob(jobName);
        final MetricJobEngine<? extends MetricJobContext> jobEngine;
        if (job == null) {
            logger.warn("Job {} does not exist. Cannot resolve job engine, so defaulting to DataCleanerJobEngine.",
                    jobName);
            jobEngine = _jobEngineManager.getJobEngineOfType(DataCleanerJobEngine.class);
        } else {
            jobEngine = job.getJobEngine();
        }
        return jobEngine.getMetricValues(job, resultContext, metricIdentifiers);
    }
}
