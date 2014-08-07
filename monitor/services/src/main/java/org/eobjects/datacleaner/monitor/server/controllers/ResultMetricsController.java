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
package org.eobjects.datacleaner.monitor.server.controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.StringEscapeUtils;
import org.eobjects.datacleaner.monitor.configuration.ResultContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.jaxb.MetricType;
import org.eobjects.datacleaner.monitor.jaxb.MetricsType;
import org.eobjects.datacleaner.monitor.job.JobContext;
import org.eobjects.datacleaner.monitor.job.MetricJobContext;
import org.eobjects.datacleaner.monitor.job.MetricValues;
import org.eobjects.datacleaner.monitor.server.MetricValueProducer;
import org.eobjects.datacleaner.monitor.server.jaxb.JaxbMetricAdaptor;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.JobMetrics;
import org.eobjects.datacleaner.monitor.shared.model.MetricGroup;
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
    public void getMetricsXml(@PathVariable("tenant") final String tenant, @PathVariable("result") String resultName,
            final HttpServletRequest request, final HttpServletResponse response) throws IOException {

        final JaxbMetricAdaptor adaptor = new JaxbMetricAdaptor();

        final MetricsType metricsType = adaptor.read(request.getInputStream());
        final List<MetricIdentifier> metricList = new ArrayList<MetricIdentifier>();
        for (MetricType metricType : metricsType.getMetric()) {
            final MetricIdentifier metric = adaptor.deserialize(metricType);
            metricList.add(metric);
        }

        logger.debug("Getting XML metrics from result '{}': {}", resultName, metricList);

        final MetricValues metricValues = getMetricValues(resultName, tenant, metricList);
        final XMLGregorianCalendar xmlDate = adaptor.createDate(metricValues.getMetricDate());

        response.setContentType("application/xml");

        try (final PrintWriter out = response.getWriter()) {
            out.write("<result>");

            out.write("\n  <metric-date>");
            out.write(xmlDate.toXMLFormat());
            out.write("</metric-date>");

            out.write("\n  <metric-values>");
            for (int i = 0; i < metricList.size(); i++) {
                final String displayName = metricList.get(i).getDisplayName();
                final Number value = metricValues.getValues().get(i);
                out.write("\n    <metric-value>");
                out.write("\n      <display-name>" + StringEscapeUtils.escapeXml(displayName) + "</display-name>");
                if (value == null) {
                    out.write("\n      <value />");
                } else {
                    out.write("\n      <value>" + StringEscapeUtils.escapeXml(value + "") + "</value>");
                }
                out.write("\n    </metric-value>");
            }
            out.write("\n  </metric-values>");
            out.write("\n</result>");

            out.flush();
        }
    }

    @RolesAllowed(SecurityRoles.VIEWER)
    @RequestMapping(value = "/{tenant}/results/{result:.+}.metrics", method = RequestMethod.GET, produces = "application/json", consumes = "application/json")
    @ResponseBody
    public List<MetricIdentifier> getMetricsJson(@PathVariable("tenant") final String tenant,
            @PathVariable("result") String resultName) throws IOException {

        resultName = resultName.replaceAll("\\+", " ");

        final TenantContext context = _contextFactory.getContext(tenant);

        final List<MetricIdentifier> result = new ArrayList<MetricIdentifier>();

        final ResultContext resultContext = context.getResult(resultName);
        final JobContext job = resultContext.getJob();

        if (!(job instanceof MetricJobContext)) {
            throw new UnsupportedOperationException("Job not compatible with operation: " + job);
        }

        final JobMetrics jobMetrics = ((MetricJobContext) job).getJobMetrics();
        final List<MetricGroup> metricGroups = jobMetrics.getMetricGroups();
        for (final MetricGroup metricGroup : metricGroups) {
            final List<MetricIdentifier> metrics = metricGroup.getMetrics();
            for (MetricIdentifier metricIdentifier : metrics) {
                result.add(metricIdentifier);
            }
        }

        return result;
    }

    @RolesAllowed(SecurityRoles.VIEWER)
    @RequestMapping(value = "/{tenant}/results/{result:.+}.metrics", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    @ResponseBody
    public Map<String, ?> postMetricsJson(@PathVariable("tenant") final String tenant,
            @PathVariable("result") String resultName, @RequestBody MetricIdentifier[] metricIdentifiers)
            throws IOException {

        final List<MetricIdentifier> metricList = Arrays.asList(metricIdentifiers);

        logger.debug("Getting JSON metrics from result '{}': {}", resultName, metricList);

        final MetricValues metricValues = getMetricValues(resultName, tenant, metricList);

        final Map<String, Object> result = new HashMap<String, Object>();
        result.put("metricDate", metricValues.getMetricDate());

        final List<Map<String, Object>> metricValuesMaps = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < metricList.size(); i++) {
            final String displayName = metricList.get(i).getDisplayName();
            final Number value = metricValues.getValues().get(i);
            final LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("displayName", displayName);
            map.put("value", value);
            metricValuesMaps.add(map);
        }
        result.put("metricValues", metricValuesMaps);
        return result;
    }

    public MetricValues getMetricValues(String resultName, String tenant, List<MetricIdentifier> metricList) {
        resultName = resultName.replaceAll("\\+", " ");

        final TenantContext context = _contextFactory.getContext(tenant);

        final ResultContext resultContext = context.getResult(resultName);

        final JobContext job = resultContext.getJob();
        final JobIdentifier jobIdentifier = new JobIdentifier(job.getName());
        final RepositoryFile resultFile = resultContext.getResultFile();

        final MetricValues metricValues = _metricValueProducer.getMetricValues(metricList, resultFile,
                new TenantIdentifier(tenant), jobIdentifier);
        return metricValues;
    }

}
