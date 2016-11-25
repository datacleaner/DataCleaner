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
package org.datacleaner.monitor.pentaho;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.datacleaner.api.InputColumn;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.monitor.configuration.ResultContext;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.configuration.TenantContextFactoryImpl;
import org.datacleaner.monitor.job.JobEngineManager;
import org.datacleaner.monitor.job.MetricValues;
import org.datacleaner.monitor.server.job.SimpleJobEngineManager;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.JobMetrics;
import org.datacleaner.monitor.shared.model.MetricGroup;
import org.datacleaner.monitor.shared.model.MetricIdentifier;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.file.FileRepository;

import junit.framework.TestCase;

public class PentahoJobEngineTest extends TestCase {

    TenantContextFactory tenantContextFactory;
    TenantContext tenantContext;
    PentahoJobEngine jobEngine;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        jobEngine = new PentahoJobEngine();
        final Repository repository = new FileRepository("src/test/resources/repo");
        final JobEngineManager jobEngineManager = new SimpleJobEngineManager(jobEngine);
        tenantContextFactory =
                new TenantContextFactoryImpl(repository, new DataCleanerEnvironmentImpl(), jobEngineManager);
        tenantContext = tenantContextFactory.getContext("dc");
    }

    public void testWorkingWithMetrics() throws Exception {
        final PentahoJobContext job = jobEngine.getJobContext(tenantContext, new JobIdentifier("Sample Pentaho job"));

        final JobMetrics metrics = job.getJobMetrics();
        assertNotNull(metrics);

        final List<MetricGroup> groups = metrics.getMetricGroups();
        assertEquals("[MetricGroup[Sample Pentaho job]]", groups.toString());

        final MetricIdentifier metric = groups.get(0).getMetric("Lines written");
        assertEquals("MetricIdentifier[analyzerInputName=null,metricDescriptorName=Lines written]", metric.toString());

        final ResultContext result = tenantContext.getResult("Sample Pentaho job-1364228636342");

        final Collection<InputColumn<?>> columns = jobEngine.getMetricParameterColumns(job, null);

        assertEquals("[MockInputColumn[name=A], MockInputColumn[name=dummy]]", columns.toString());

        final List<MetricIdentifier> metricIdentifiers = new ArrayList<>();
        final MetricIdentifier copy1 = metric.copy();
        copy1.setParamColumnName("dummy");
        metricIdentifiers.add(copy1);
        final MetricIdentifier copy2 = metric.copy();
        copy2.setParamColumnName("A");
        metricIdentifiers.add(copy2);

        final MetricValues metricValues = jobEngine.getMetricValues(job, result, metricIdentifiers);
        assertEquals(result.getResultFile().getLastModified(), metricValues.getMetricDate().getTime());
        assertEquals("[100000000, 100000000]", metricValues.getValues().toString());
    }
}
