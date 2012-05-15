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

import java.io.OutputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.eobjects.analyzer.util.JaxbValidationEventHandler;
import org.eobjects.datacleaner.monitor.timeline.model.MetricIdentifier;
import org.eobjects.datacleaner.monitor.timeline.model.TimelineDefinition;
import org.eobjects.datacleaner.timeline.jaxb.MetricType;
import org.eobjects.datacleaner.timeline.jaxb.MetricsType;
import org.eobjects.datacleaner.timeline.jaxb.ObjectFactory;
import org.eobjects.datacleaner.timeline.jaxb.Timeline;

/**
 * Jaxb based {@link TimelineReader} of .analysis.timeline.xml files.
 */
public class JaxbTimelineWriter implements TimelineWriter {

    private final JAXBContext _jaxbContext;
    private final ObjectFactory _objectFactory;

    public JaxbTimelineWriter() {
        _objectFactory = new ObjectFactory();
        try {
            _jaxbContext = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName(),
                    ObjectFactory.class.getClassLoader());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void write(TimelineDefinition timelineDefinition, OutputStream outputStream) {
        Timeline timeline = createTimeline(timelineDefinition);

        marshal(timeline, outputStream);
    }

    public void marshal(Timeline timeline, OutputStream outputStream) {
        Marshaller marshaller = createMarshaller();
        try {
            marshaller.marshal(timeline, outputStream);
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    private Marshaller createMarshaller() {
        try {
            Marshaller marshaller = _jaxbContext.createMarshaller();
            marshaller.setEventHandler(new JaxbValidationEventHandler());
            return marshaller;
        } catch (JAXBException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Timeline createTimeline(TimelineDefinition timelineDefinition) {
        final MetricsType metricsType = _objectFactory.createMetricsType();
        final List<MetricIdentifier> metrics = timelineDefinition.getMetrics();
        for (MetricIdentifier metric : metrics) {
            final MetricType metricType = _objectFactory.createMetricType();
            metricType.setAnalyzerDescriptorName(metric.getAnalyzerDescriptorName());
            metricType.setAnalyzerInput(metric.getAnalyzerInputName());
            metricType.setAnalyzerName(metric.getAnalyzerName());
            metricType.setMetricDescriptorName(metric.getMetricDescriptorName());
            metricType.setMetricParamColumnName(metric.getParamColumnName());
            metricType.setMetricParamQueryString(metric.getParamQueryString());
            
            metricsType.getMetric().add(metricType);
        }
        
        final Timeline timeline = _objectFactory.createTimeline();
        timeline.setJobPath(timelineDefinition.getJobIdentifier().getPath());
        timeline.setMetrics(metricsType);
        
        return timeline;
    }

}