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

import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.eobjects.datacleaner.monitor.configuration.ConfigurationCache;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.timeline.TimelineService;
import org.eobjects.datacleaner.monitor.timeline.model.JobMetrics;
import org.eobjects.datacleaner.monitor.timeline.model.MetricGroup;
import org.eobjects.datacleaner.monitor.timeline.model.MetricIdentifier;
import org.eobjects.datacleaner.monitor.timeline.model.TimelineData;
import org.eobjects.datacleaner.monitor.timeline.model.TimelineDataRow;
import org.eobjects.datacleaner.monitor.timeline.model.TimelineDefinition;
import org.eobjects.datacleaner.monitor.timeline.model.TimelineGroup;
import org.eobjects.datacleaner.monitor.timeline.model.TimelineIdentifier;
import org.eobjects.datacleaner.repository.file.FileRepository;

public class TimelineServiceImplTest extends TestCase {

    public void testBasicInteraction() throws Exception {
        final FileRepository repository = new FileRepository("src/main/webapp/repository");
        final ConfigurationCache configurationCache = new ConfigurationCache(repository);
        final TimelineService service = new TimelineServiceImpl(repository, configurationCache);

        final TenantIdentifier tenant = new TenantIdentifier("DC");
        assertEquals("TenantIdentifier[DC]", tenant.toString());

        final List<JobIdentifier> jobs = service.getJobs(tenant);
        assertEquals(2, jobs.size());
        
        Collections.sort(jobs);

        final JobIdentifier job = jobs.get(0);
        assertEquals("JobIdentifier[name=product_profiling]",
                job.toString());

        final JobMetrics jobMetrics = service.getJobMetrics(tenant, job);
        assertEquals("JobMetrics[product_profiling metrics]", jobMetrics.toString());
        assertSame(job, jobMetrics.getJob());

        final List<MetricGroup> metricGroups = jobMetrics.getMetricGroups();
        assertEquals(3, metricGroups.size());

        assertEquals("MetricGroup[Pattern finder (PRODUCTCODE)]", metricGroups.get(0).toString());
        assertEquals("MetricGroup[Value distribution (PRODUCTLINE)]", metricGroups.get(1).toString());
        assertEquals("MetricGroup[Vendor whitelist check (Reference data matcher) (PRODUCTVENDOR)]", metricGroups
                .get(2).toString());

        List<MetricIdentifier> metrics = metricGroups.get(2).getMetrics();
        assertEquals(
                "[MetricIdentifier[analyzerInputName=PRODUCTVENDOR,metricDescriptorName=Row count], MetricIdentifier[analyzerInputName=PRODUCTVENDOR,metricDescriptorName=Null count], MetricIdentifier[analyzerInputName=PRODUCTVENDOR,metricDescriptorName=True count], MetricIdentifier[analyzerInputName=PRODUCTVENDOR,metricDescriptorName=False count], MetricIdentifier[analyzerInputName=PRODUCTVENDOR,metricDescriptorName=Total combination count], MetricIdentifier[analyzerInputName=PRODUCTVENDOR,metricDescriptorName=Combination count]]",
                metrics.toString());

        metrics = metricGroups.get(0).getMetrics();
        // 2 metrics in the Pattern finder
        assertEquals(2, metrics.size());
        assertEquals("PRODUCTCODE", metrics.get(0).getAnalyzerInputName());
        assertEquals("Match count", metrics.get(0).getMetricDescriptorName());

        List<TimelineIdentifier> timelines = service.getTimelines(tenant, null);
        assertEquals(0, timelines.size());

        final List<TimelineGroup> timelineGroups = service.getTimelineGroups(tenant);
        assertEquals(2, timelineGroups.size());
        
        timelines = service.getTimelines(tenant, timelineGroups.get(0));
        assertEquals(3, timelines.size());

        final TimelineIdentifier timelineIdentifier = timelines.get(0);
        assertEquals(
                "TimelineIdentifier[name=Product code patterns (the lower the better),path=/DC/timelines/Product data/Product code patterns (the lower the better).analysis.timeline.xml]",
                timelineIdentifier.toString());

        final TimelineDefinition timelineDefinition = service.getTimelineDefinition(tenant, timelineIdentifier);
        assertEquals("TimelineDefinition[job=JobIdentifier[name=product_profiling],metrics=[MetricIdentifier[analyzerInputName=PRODUCTCODE,metricDescriptorName=Pattern count], MetricIdentifier[analyzerInputName=PRODUCTVENDOR,metricDescriptorName=False count,paramQueryString=PRODUCTVENDOR in 'vendor whitelist']]]",
                timelineDefinition.toString());

        final TimelineData timelineData = service.getTimelineData(tenant, timelineDefinition);
        assertEquals("TimelineData[6 rows]", timelineData.toString());

        final List<TimelineDataRow> rows = timelineData.getRows();
        assertEquals(6, rows.size());

        assertEquals("TimelineDataRow[date=2012-6-6 15:46,metricValues=[5, 0]]", rows.get(0).toString());
    }
}
