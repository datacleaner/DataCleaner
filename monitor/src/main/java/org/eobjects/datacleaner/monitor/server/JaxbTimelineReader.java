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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.eobjects.analyzer.util.JaxbValidationEventHandler;
import org.eobjects.datacleaner.monitor.jaxb.MetricType;
import org.eobjects.datacleaner.monitor.jaxb.ObjectFactory;
import org.eobjects.datacleaner.monitor.jaxb.Timeline;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;
import org.eobjects.datacleaner.monitor.timeline.model.TimelineDefinition;

/**
 * JAXB based {@link TimelineReader} of .analysis.timeline.xml files.
 */
public class JaxbTimelineReader implements TimelineReader {

    private final JAXBContext _jaxbContext;

    public JaxbTimelineReader() {
        try {
            _jaxbContext = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName(),
                    ObjectFactory.class.getClassLoader());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public Timeline unmarshallTimeline(InputStream inputStream) {
        try {
            Unmarshaller unmarshaller = _jaxbContext.createUnmarshaller();
            unmarshaller.setEventHandler(new JaxbValidationEventHandler());
            Timeline timeline = (Timeline) unmarshaller.unmarshal(inputStream);
            return timeline;
        } catch (JAXBException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public TimelineDefinition read(InputStream inputStream) {
        final Timeline timeline = unmarshallTimeline(inputStream);

        return createTimeline(timeline);
    }

    private TimelineDefinition createTimeline(Timeline timeline) {
        final JobIdentifier jobIdentifier = new JobIdentifier();
        jobIdentifier.setName(timeline.getJobName());

        final TimelineDefinition timelineDefinition = new TimelineDefinition();
        timelineDefinition.setJobIdentifier(jobIdentifier);

        final List<MetricIdentifier> metrics = createMetrics(timeline);
        timelineDefinition.setMetrics(metrics);

        return timelineDefinition;
    }

    public List<MetricIdentifier> createMetrics(Timeline timeline) {
        final List<MetricType> metricTypes = timeline.getMetrics().getMetric();
        final List<MetricIdentifier> metrics = new ArrayList<MetricIdentifier>(metricTypes.size());
        for (MetricType metricType : metricTypes) {
            final MetricIdentifier metricIdentifier = new MetricIdentifier();
            metricIdentifier.setAnalyzerDescriptorName(metricType.getAnalyzerDescriptorName());
            metricIdentifier.setAnalyzerName(metricType.getAnalyzerName());
            metricIdentifier.setAnalyzerInputName(metricType.getAnalyzerInput());
            metricIdentifier.setMetricDescriptorName(metricType.getMetricDescriptorName());
            metricIdentifier.setParamColumnName(metricType.getMetricParamColumnName());
            metricIdentifier.setParamQueryString(metricType.getMetricParamQueryString());
            metricIdentifier.setParameterizedByColumnName(metricType.getMetricParamColumnName() != null);
            metricIdentifier.setParameterizedByQueryString(metricType.getMetricParamQueryString() != null);

            metrics.add(metricIdentifier);
        }
        return metrics;
    }
}
