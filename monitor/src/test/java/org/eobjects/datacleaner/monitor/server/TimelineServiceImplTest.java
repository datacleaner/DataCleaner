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

import java.util.List;

import junit.framework.TestCase;

import org.eobjects.datacleaner.monitor.configuration.ConfigurationCache;
import org.eobjects.datacleaner.monitor.timeline.TimelineService;
import org.eobjects.datacleaner.monitor.timeline.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.timeline.model.JobMetrics;
import org.eobjects.datacleaner.monitor.timeline.model.MetricGroup;
import org.eobjects.datacleaner.monitor.timeline.model.MetricIdentifier;
import org.eobjects.datacleaner.monitor.timeline.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.timeline.model.TimelineData;
import org.eobjects.datacleaner.monitor.timeline.model.TimelineDataRow;
import org.eobjects.datacleaner.monitor.timeline.model.TimelineDefinition;
import org.eobjects.datacleaner.monitor.timeline.model.TimelineIdentifier;
import org.eobjects.datacleaner.repository.file.FileRepository;

public class TimelineServiceImplTest extends TestCase {

    public void testBasicInteraction() throws Exception {
        final FileRepository repository = new FileRepository("src/main/webapp/repository");
        final ConfigurationCache configurationCache = new ConfigurationCache(repository);
        final TimelineService service = new TimelineServiceImpl(repository, configurationCache);

        final TenantIdentifier tenant = new TenantIdentifier("DC");
        assertEquals("TenantIdentifier[DC]", tenant.toString());

        final List<JobIdentifier> jobs = service.getSavedJobs(tenant);
        assertEquals(1, jobs.size());

        final JobIdentifier job = jobs.get(0);
        assertEquals("JobIdentifier[name=customers,path=/DC/jobs/customers.analysis.xml]", job.toString());

        final JobMetrics jobMetrics = service.getJobMetrics(tenant, job);
        assertEquals("JobMetrics[customers metrics]", jobMetrics.toString());
        assertSame(job, jobMetrics.getJob());

        final List<MetricGroup> metricGroups = jobMetrics.getMetricGroups();
        assertEquals(4, metricGroups.size());

        assertEquals("MetricGroup[Pattern finder (CONTACTLASTNAME)]", metricGroups.get(0).toString());
        assertEquals("MetricGroup[Pattern finder (CONTACTFIRSTNAME)]", metricGroups.get(1).toString());
        assertEquals("MetricGroup[Value distribution (CITY)]", metricGroups.get(2).toString());
        assertEquals("MetricGroup[String analyzer (CITY,CONTACTLASTNAME,CONTACTFIRSTNAME)]", metricGroups.get(3).toString());

        List<MetricIdentifier> metrics = metricGroups.get(2).getMetrics();
        assertEquals("[MetricIdentifier[analyzerInputName=CITY,metricDescriptorName=Distinct count], "
                + "MetricIdentifier[analyzerInputName=CITY,metricDescriptorName=Null count], "
                + "MetricIdentifier[analyzerInputName=CITY,metricDescriptorName=Unique count], "
                + "MetricIdentifier[analyzerInputName=CITY,metricDescriptorName=Value count]]", metrics.toString());
        
        metrics = metricGroups.get(3).getMetrics();
        // 20 metrics in the String analyzer
        assertEquals(20, metrics.size());
        assertEquals(null, metrics.get(0).getAnalyzerInputName());
        assertEquals("Avg chars", metrics.get(0).getMetricDescriptorName());

        final List<TimelineIdentifier> timelines = service.getSavedTimelines(tenant);
        assertEquals(1, timelines.size());

        final TimelineIdentifier timelineIdentifier = timelines.get(0);
        assertEquals(
                "TimelineIdentifier[name=customer_patterns,path=/DC/timelines/customer_patterns.analysis.timeline.xml]",
                timelineIdentifier.toString());

        final TimelineDefinition timelineDefinition = service.getTimelineDefinition(tenant, timelineIdentifier);
        assertEquals("TimelineDefinition[job=JobIdentifier[name=null,path=/DC/jobs/customers.analysis.xml],metrics=["
                + "MetricIdentifier[analyzerInputName=CONTACTLASTNAME,metricDescriptorName=Pattern count], "
                + "MetricIdentifier[analyzerInputName=CITY,metricDescriptorName=Value count,paramQueryString=Madrid], "
                + "MetricIdentifier[analyzerInputName=CITY,metricDescriptorName=Value count,paramQueryString=NYC], "
                + "MetricIdentifier[analyzerInputName=null,metricDescriptorName=Digit chars,paramColumnName=CITY]]]",
                timelineDefinition.toString());
        
        final TimelineData timelineData = service.getTimelineData(tenant, timelineDefinition);
        assertEquals("TimelineData[3 rows]", timelineData.toString());
        
        final List<TimelineDataRow> rows = timelineData.getRows();
        assertEquals(3, rows.size());
        
        assertEquals("TimelineDataRow[date=2012-5-7 21:12,metricValues=[8, 6, 5, 8]]", rows.get(0).toString());
        assertEquals("TimelineDataRow[date=2012-5-8 16:15,metricValues=[8, 6, 5, 8]]", rows.get(1).toString());
        assertEquals("TimelineDataRow[date=2012-5-8 23:12,metricValues=[8, 0, 133, 0]]", rows.get(2).toString());
    }
}
