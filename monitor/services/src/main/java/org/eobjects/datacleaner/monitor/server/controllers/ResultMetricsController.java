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
package org.eobjects.datacleaner.monitor.server.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;

import org.eobjects.datacleaner.monitor.configuration.JobContext;
import org.eobjects.datacleaner.monitor.configuration.ResultContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.jaxb.MetricType;
import org.eobjects.datacleaner.monitor.jaxb.MetricsType;
import org.eobjects.datacleaner.monitor.server.MetricValueProducer;
import org.eobjects.datacleaner.monitor.server.MetricValues;
import org.eobjects.datacleaner.monitor.server.jaxb.JaxbMetricAdaptor;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.SecurityRoles;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ResultMetricsController {

    private static final Logger logger = LoggerFactory.getLogger(ResultMetricsController.class);

    @Autowired
    TenantContextFactory _contextFactory;

    @Autowired
    MetricValueProducer _metricValueProducer;

    @RolesAllowed(SecurityRoles.VIEWER)
    @RequestMapping(value = "/{tenant}/results/{result:.+}.metrics.xml", method = RequestMethod.POST, produces = "application/xml", consumes = "application/xml")
    @ResponseBody
    public Map<String, ?> getMetricsXml(@PathVariable("tenant") final String tenant,
            @PathVariable("result") String resultName, final HttpServletRequest request) throws IOException {
        
        final JaxbMetricAdaptor adaptor = new JaxbMetricAdaptor();

        final MetricsType metricsType = adaptor.read(request.getInputStream());
        final List<MetricIdentifier> metricList = new ArrayList<MetricIdentifier>();
        for (MetricType metricType : metricsType.getMetric()) {
            final MetricIdentifier metric = adaptor.deserialize(metricType);
            metricList.add(metric);
        }

        logger.debug("Getting XML metrics from result '{}': {}", resultName, metricList);

        // TODO: Represent as XML instead of JSON
        
        return getMetrics(resultName, tenant, metricList);
    }

    @RolesAllowed(SecurityRoles.VIEWER)
    @RequestMapping(value = "/{tenant}/results/{result:.+}.metrics", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    @ResponseBody
    public Map<String, ?> getMetricsJson(@PathVariable("tenant") final String tenant,
            @PathVariable("result") String resultName, @RequestBody MetricIdentifier[] metricIdentifiers)
            throws IOException {

        final List<MetricIdentifier> metricList = Arrays.asList(metricIdentifiers);

        logger.debug("Getting JSON metrics from result '{}': {}", resultName, metricList);

        return getMetrics(resultName, tenant, metricList);
    }

    private Map<String, Object> getMetrics(String resultName, String tenant, List<MetricIdentifier> metricList) {
        resultName = resultName.replaceAll("\\+", " ");

        final TenantContext context = _contextFactory.getContext(tenant);

        final ResultContext resultContext = context.getResult(resultName);

        final JobContext job = resultContext.getJob();
        final JobIdentifier jobIdentifier = new JobIdentifier(job.getName());
        final RepositoryFile resultFile = resultContext.getResultFile();

        final MetricValues metricValues = _metricValueProducer.getMetricValues(metricList, resultFile,
                new TenantIdentifier(tenant), jobIdentifier);

        final Map<String, Object> result = new HashMap<String, Object>();
        result.put("metricsDate", metricValues.getMetricDate());
        result.put("metricValues", metricValues.getValues());
        return result;
    }

}
