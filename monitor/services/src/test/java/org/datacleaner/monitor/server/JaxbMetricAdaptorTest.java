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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.datacleaner.monitor.dashboard.model.TimelineDefinition;
import org.datacleaner.monitor.jaxb.MetricType;
import org.datacleaner.monitor.jaxb.MetricsType;
import org.datacleaner.monitor.server.jaxb.JaxbMetricAdaptor;
import org.datacleaner.monitor.server.jaxb.JaxbTimelineReader;
import org.datacleaner.monitor.server.jaxb.JaxbTimelineWriter;
import org.datacleaner.monitor.shared.model.MetricIdentifier;

public class JaxbMetricAdaptorTest extends TestCase {

    private final MetricIdentifier metric1 = new MetricIdentifier("Null strings", "String analyzer", null,
            "My strings", "Null count", null, "My strings", false, true);
    private final MetricIdentifier metric2 = new MetricIdentifier(null, "Number analyzer", null, "My numbers",
            "Null count", null, "My numbers", false, true);

    public void testSerializeSingleMetric() throws Exception {
        TimelineDefinition timelineDefinition = new TimelineDefinition();
        timelineDefinition.setMetrics(Arrays.asList(metric1));

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        new JaxbTimelineWriter().write(timelineDefinition, out);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

        timelineDefinition = new JaxbTimelineReader().read(in);

        assertEquals(1, timelineDefinition.getMetrics().size());
        MetricIdentifier metric = timelineDefinition.getMetrics().get(0);

        assertFalse(metric.isFormulaBased());
        assertEquals(
                "MetricIdentifier[analyzerInputName=My strings,metricDescriptorName=Null count,paramColumnName=My strings]",
                metric.toString());

        assertEquals(metric1, metric);
    }

    public void testSerializeFormulaMetric() throws Exception {
        final MetricIdentifier formulaMetricIdentifier = new MetricIdentifier("My formula metric",
                "Null strings / Null count", Arrays.asList(metric1, metric2));

        TimelineDefinition timelineDefinition = new TimelineDefinition();
        timelineDefinition.setMetrics(Arrays.asList(formulaMetricIdentifier));

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        new JaxbTimelineWriter().write(timelineDefinition, out);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

        timelineDefinition = new JaxbTimelineReader().read(in);

        assertEquals(1, timelineDefinition.getMetrics().size());
        MetricIdentifier metric = timelineDefinition.getMetrics().get(0);

        assertTrue(metric.isFormulaBased());
        assertEquals("MetricIdentifier[formula=Null strings / Null count]", metric.toString());

        List<MetricIdentifier> children = metric.getChildren();
        assertEquals(2, children.size());

        MetricIdentifier child1 = children.get(0);
        MetricIdentifier child2 = children.get(1);

        assertEquals(
                "MetricIdentifier[analyzerInputName=My strings,metricDescriptorName=Null count,paramColumnName=My strings]",
                child1.toString());
        assertEquals(
                "MetricIdentifier[analyzerInputName=My numbers,metricDescriptorName=Null count,paramColumnName=My numbers]",
                child2.toString());

        assertEquals(metric1, child1);
        assertEquals(metric2, child2);
    }
    
    public void testReadMetricsList() throws Exception {
        JaxbMetricAdaptor adaptor = new JaxbMetricAdaptor();
        MetricsType metrics;
        try (FileInputStream in = new FileInputStream("src/test/resources/jaxb_metrics.xml")) {
            metrics = adaptor.read(in);
        }
        
        List<MetricType> metricList = metrics.getMetric();
        assertEquals(3, metricList.size());
        
        MetricType metricType = metricList.get(0);
        assertEquals("Record count", metricType.getMetricDisplayName());
        
        MetricIdentifier metric = adaptor.deserialize(metricType);
        assertEquals("MetricIdentifier[analyzerInputName=null,metricDescriptorName=Row count]", metric.toString());
    }
}
