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
import java.util.Collection;
import java.util.List;

import org.datacleaner.job.NoSuchComponentException;
import org.datacleaner.util.StringUtils;
import org.datacleaner.monitor.configuration.ResultContext;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.job.JobContext;
import org.datacleaner.monitor.job.MetricJobContext;
import org.datacleaner.monitor.job.MetricJobEngine;
import org.datacleaner.monitor.server.dao.ResultDao;
import org.datacleaner.monitor.shared.DescriptorNotFoundException;
import org.datacleaner.monitor.shared.DescriptorService;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.JobMetrics;
import org.datacleaner.monitor.shared.model.MetricGroup;
import org.datacleaner.monitor.shared.model.MetricIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
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
    private final ResultDao _resultDao;

    @Autowired
    public DescriptorServiceImpl(TenantContextFactory tenantContextFactory, ResultDao resultDao) {
        _tenantContextFactory = tenantContextFactory;
        _resultDao = resultDao;
    }

    @Override
    public JobMetrics getJobMetrics(final TenantIdentifier tenant, final JobIdentifier jobIdentifier)
            throws DescriptorNotFoundException {
        try {
            final JobContext jobContext = _tenantContextFactory.getContext(tenant).getJob(jobIdentifier.getName());
            if (jobContext instanceof MetricJobContext) {
                return ((MetricJobContext) jobContext).getJobMetrics();
            }

            final List<MetricGroup> list = new ArrayList<MetricGroup>(0);
            return new JobMetrics(jobIdentifier, list);
        } catch (NoSuchComponentException e) {
            logger.warn("Encountered exception while get Job metrics", e);
            throw new DescriptorNotFoundException(e.getMessage());
        }
    }

    @Override
    public Collection<String> getMetricParameterSuggestions(TenantIdentifier tenant, JobIdentifier jobIdentifier,
            MetricIdentifier metric) {
        if (metric == null || metric.isFormulaBased()) {
            return new ArrayList<String>(0);
        }

        final String analyzerDescriptorName = metric.getAnalyzerDescriptorName();
        final String metricDescriptorName = metric.getMetricDescriptorName();
        if (StringUtils.isNullOrEmpty(analyzerDescriptorName) || StringUtils.isNullOrEmpty(metricDescriptorName)) {
            return new ArrayList<String>(0);
        }
        
        final ResultContext result = _resultDao.getLatestResult(tenant, jobIdentifier);
        if (result == null) {
            return new ArrayList<String>(0);
        }
        final MetricJobContext job = (MetricJobContext) result.getJob();
        final MetricJobEngine<?> jobEngine = job.getJobEngine();

        return jobEngine.getMetricParameterSuggestions(job, result, metric);
    }
}
