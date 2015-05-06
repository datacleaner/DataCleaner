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
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.configuration.TenantContextFactoryImpl;
import org.datacleaner.monitor.dashboard.DashboardService;
import org.datacleaner.monitor.dashboard.model.DashboardGroup;
import org.datacleaner.monitor.dashboard.model.TimelineData;
import org.datacleaner.monitor.dashboard.model.TimelineDataRow;
import org.datacleaner.monitor.dashboard.model.TimelineDefinition;
import org.datacleaner.monitor.dashboard.model.TimelineIdentifier;
import org.datacleaner.monitor.server.dao.ResultDao;
import org.datacleaner.monitor.server.dao.ResultDaoImpl;
import org.datacleaner.monitor.server.dao.TimelineDao;
import org.datacleaner.monitor.server.dao.TimelineDaoImpl;
import org.datacleaner.monitor.server.job.MockJobEngineManager;
import org.datacleaner.monitor.shared.DescriptorService;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.JobMetrics;
import org.datacleaner.monitor.shared.model.MetricGroup;
import org.datacleaner.monitor.shared.model.MetricIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.repository.file.FileRepository;

public class DashboardServiceImplTest extends TestCase {

    private final FileRepository repository = new FileRepository("src/test/resources/example_repo");
    private final MockJobEngineManager jobEngineManager = new MockJobEngineManager();
    private final TenantContextFactory tenantContextFactory = new TenantContextFactoryImpl(repository,
            new DataCleanerEnvironmentImpl(), jobEngineManager);
    private final MetricValueProducer metricValueCache = new DefaultMetricValueProducer(tenantContextFactory,
            jobEngineManager);

    public void testSkipInvalidResultFiles() throws Exception {
        final TenantIdentifier tenant = new TenantIdentifier("tenant4");
        final TimelineDefinition timeline = new TimelineDefinition();
        timeline.setJobIdentifier(new JobIdentifier("my job"));
        final List<MetricIdentifier> metrics = new ArrayList<>();
        final MetricIdentifier metric = new MetricIdentifier("my metric", "Pattern finder", null, "PRODUCTCODE",
                "Pattern count", null, null, false, false);
        metrics.add(metric);
        timeline.setMetrics(metrics);

        final List<RepositoryFile> resultFiles = tenantContextFactory.getContext(tenant).getResultFolder()
                .getFiles("my job", ".analysis.result.dat");
        assertEquals(2, resultFiles.size());

        final List<TimelineDataRow> data = DashboardServiceImpl.getTimelineData(tenant, timeline, resultFiles,
                metricValueCache);
        assertNotNull(data);
        assertEquals(1, data.size());

        // this is the pattern count for product code in the one result file
        // that is properly deserializable
        assertEquals("[1]", data.get(0).getMetricValues().toString());
    }

    public void testBasicInteraction() throws Exception {
        final ResultDao resultDao = new ResultDaoImpl(tenantContextFactory, null);
        final TimelineDao timelineDao = new TimelineDaoImpl(tenantContextFactory, repository);

        final DescriptorService descriptorService = new DescriptorServiceImpl(tenantContextFactory, resultDao);
        final DashboardService service = new DashboardServiceImpl(tenantContextFactory, metricValueCache, resultDao,
                timelineDao);

        final TenantIdentifier tenant = new TenantIdentifier("tenant1");
        assertEquals("TenantIdentifier[tenant1]", tenant.toString());

        final List<JobIdentifier> jobs = service.getJobs(tenant);
        assertEquals(9, jobs.size());

        Collections.sort(jobs);

        final JobIdentifier job = jobs.get(6);
        assertEquals("JobIdentifier[name=product_profiling]", job.toString());

        final JobMetrics jobMetrics = descriptorService.getJobMetrics(tenant, job);
        assertEquals("JobMetrics[product_profiling metrics]", jobMetrics.toString());
        assertEquals(job, jobMetrics.getJob());

        final List<MetricGroup> metricGroups = jobMetrics.getMetricGroups();
        assertEquals(3, metricGroups.size());

        assertEquals("MetricGroup[Pattern finder (PRODUCTCODE)]", metricGroups.get(0).toString());
        assertEquals("MetricGroup[Value distribution (PRODUCTLINE)]", metricGroups.get(1).toString());
        assertEquals("MetricGroup[Vendor whitelist check (PRODUCTVENDOR)]", metricGroups.get(2).toString());

        List<MetricIdentifier> metrics = metricGroups.get(2).getMetrics();
        assertEquals(
                "[MetricIdentifier[analyzerInputName=PRODUCTVENDOR,metricDescriptorName=Row count], MetricIdentifier[analyzerInputName=PRODUCTVENDOR,metricDescriptorName=Null count], MetricIdentifier[analyzerInputName=PRODUCTVENDOR,metricDescriptorName=True count], MetricIdentifier[analyzerInputName=PRODUCTVENDOR,metricDescriptorName=False count], MetricIdentifier[analyzerInputName=PRODUCTVENDOR,metricDescriptorName=Total combination count], MetricIdentifier[analyzerInputName=PRODUCTVENDOR,metricDescriptorName=Combination count]]",
                metrics.toString());

        metrics = metricGroups.get(0).getMetrics();
        // 2 metrics in the Pattern finder
        assertEquals(2, metrics.size());
        assertEquals("Unexpected: " + metrics.get(0), "PRODUCTCODE", metrics.get(0).getAnalyzerInputName());
        assertEquals("Unexpected: " + metrics.get(0), "Match count", metrics.get(0).getMetricDescriptorName());

        List<TimelineIdentifier> timelines = service.getTimelines(tenant, null);
        assertEquals(0, timelines.size());

        final List<DashboardGroup> timelineGroups = service.getDashboardGroups(tenant);
        assertEquals(2, timelineGroups.size());

        timelines = service.getTimelines(tenant, timelineGroups.get(0));
        final TimelineIdentifier timelineIdentifier = timelines.get(0);
        if (timelineIdentifier.getName().equalsIgnoreCase("Product code patterns (the lower the better)")) {
            assertEquals(
                    "TimelineIdentifier[name=Product code patterns (the lower the better),path=/tenant1/timelines/Product data/Product code patterns (the lower the better).analysis.timeline.xml]",
                    timelineIdentifier.toString());
            assertEquals(3, timelines.size());
            final TimelineDefinition timelineDefinition = service.getTimelineDefinition(tenant, timelineIdentifier);
            assertEquals(
                    "TimelineDefinition[job=JobIdentifier[name=product_profiling],metrics=[MetricIdentifier[analyzerInputName=PRODUCTCODE,metricDescriptorName=Pattern count], MetricIdentifier[analyzerInputName=PRODUCTVENDOR,metricDescriptorName=False count,paramQueryString=PRODUCTVENDOR in 'vendor whitelist']]]",
                    timelineDefinition.toString());
            final TimelineData timelineData = service.getTimelineData(tenant, timelineDefinition);
            assertEquals("TimelineData[6 rows]", timelineData.toString());
            final List<TimelineDataRow> rows = timelineData.getRows();
            assertEquals(6, rows.size());
            List<Number> actualMetricValues = rows.get(0).getMetricValues();
            List<Number> expectedMetricValues = new ArrayList<Number>();
            expectedMetricValues.add(5);
            expectedMetricValues.add(0);
            assertEquals(expectedMetricValues, actualMetricValues);
            assertNotNull(rows.get(0).getDate());
        } else {
            assertEquals(1, timelines.size());
        }
    }

    public void testFormulaBasedTimeline() throws Exception {
        final FileRepository repository = new FileRepository("src/test/resources/example_repo");
        MockJobEngineManager jobEngineManager = new MockJobEngineManager();
        final TenantContextFactory contextFactory = new TenantContextFactoryImpl(repository,
                new DataCleanerEnvironmentImpl(), jobEngineManager);
        final MetricValueProducer metricValueCache = new DefaultMetricValueProducer(contextFactory, jobEngineManager);
        final ResultDao resultDao = new ResultDaoImpl(contextFactory, null);
        final TimelineDao timelineDao = new TimelineDaoImpl(contextFactory, repository);

        final DashboardService service = new DashboardServiceImpl(contextFactory, metricValueCache, resultDao,
                timelineDao);

        final TenantIdentifier tenant = new TenantIdentifier("tenant1");

        String name = "Product type distribution (the lower the better).analysis.timeline.xml";
        String path = "/tenant1/timelines/Product data/Product type distribution (the lower the better).analysis.timeline.xml";
        DashboardGroup group = new DashboardGroup("Product data");
        TimelineDefinition timeline = service.getTimelineDefinition(tenant, new TimelineIdentifier(name, path, group));

        TimelineData data = service.getTimelineData(tenant, timeline);

        List<TimelineDataRow> rows = data.getRows();
        assertEquals(6, rows.size());

        assertEquals("[11, 0, 20, 22, 110]", rows.get(0).getMetricValues().toString());
    }
}
