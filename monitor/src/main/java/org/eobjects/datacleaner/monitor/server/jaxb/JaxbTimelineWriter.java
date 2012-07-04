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
package org.eobjects.datacleaner.monitor.server.jaxb;

import java.io.OutputStream;
import java.util.List;

import org.eobjects.datacleaner.monitor.jaxb.MetricType;
import org.eobjects.datacleaner.monitor.jaxb.MetricsType;
import org.eobjects.datacleaner.monitor.jaxb.Timeline;
import org.eobjects.datacleaner.monitor.server.TimelineReader;
import org.eobjects.datacleaner.monitor.server.TimelineWriter;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;
import org.eobjects.datacleaner.monitor.timeline.model.TimelineDefinition;

/**
 * Jaxb based {@link TimelineReader} of .analysis.timeline.xml files.
 */
public class JaxbTimelineWriter extends JaxbWriter<Timeline> implements TimelineWriter {

    @Override
    public void write(TimelineDefinition timelineDefinition, OutputStream outputStream) {
        Timeline timeline = createTimeline(timelineDefinition);

        marshal(timeline, outputStream);
    }

    private Timeline createTimeline(TimelineDefinition timelineDefinition) {
        final MetricsType metricsType = getObjectFactory().createMetricsType();
        final List<MetricIdentifier> metrics = timelineDefinition.getMetrics();
        for (MetricIdentifier metric : metrics) {
            final MetricType metricType = getObjectFactory().createMetricType();
            metricType.setAnalyzerDescriptorName(metric.getAnalyzerDescriptorName());
            metricType.setAnalyzerInput(metric.getAnalyzerInputName());
            metricType.setAnalyzerName(metric.getAnalyzerName());
            metricType.setMetricDescriptorName(metric.getMetricDescriptorName());
            metricType.setMetricParamColumnName(metric.getParamColumnName());
            metricType.setMetricParamQueryString(metric.getParamQueryString());

            metricsType.getMetric().add(metricType);
        }

        final Timeline timeline = getObjectFactory().createTimeline();
        timeline.setJobName(timelineDefinition.getJobIdentifier().getName());
        timeline.setMetrics(metricsType);

        return timeline;
    }

}