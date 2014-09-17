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
package org.eobjects.datacleaner.monitor.server.jaxb;

import java.io.OutputStream;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.eobjects.datacleaner.monitor.dashboard.model.ChartOptions;
import org.eobjects.datacleaner.monitor.dashboard.model.ChartOptions.HorizontalAxisOption;
import org.eobjects.datacleaner.monitor.dashboard.model.ChartOptions.VerticalAxisOption;
import org.eobjects.datacleaner.monitor.dashboard.model.DefaultVAxisOption;
import org.eobjects.datacleaner.monitor.dashboard.model.LatestNumberOfDaysHAxisOption;
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineDefinition;
import org.eobjects.datacleaner.monitor.jaxb.ChartOptionsType;
import org.eobjects.datacleaner.monitor.jaxb.ChartOptionsType.HorizontalAxis;
import org.eobjects.datacleaner.monitor.jaxb.ChartOptionsType.HorizontalAxis.FixedAxis;
import org.eobjects.datacleaner.monitor.jaxb.ChartOptionsType.HorizontalAxis.RollingAxis;
import org.eobjects.datacleaner.monitor.jaxb.ChartOptionsType.VerticalAxis;
import org.eobjects.datacleaner.monitor.jaxb.MetricType;
import org.eobjects.datacleaner.monitor.jaxb.MetricsType;
import org.eobjects.datacleaner.monitor.jaxb.Timeline;
import org.eobjects.datacleaner.monitor.server.TimelineReader;
import org.eobjects.datacleaner.monitor.server.TimelineWriter;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;

/**
 * Jaxb based {@link TimelineReader} of .analysis.timeline.xml files.
 */
public class JaxbTimelineWriter extends AbstractJaxbAdaptor<Timeline> implements TimelineWriter {

    public JaxbTimelineWriter() {
        super(Timeline.class);
    }

    @Override
    public void write(TimelineDefinition timelineDefinition, OutputStream outputStream) {
        Timeline timeline = createTimeline(timelineDefinition);

        marshal(timeline, outputStream);
    }

    private Timeline createTimeline(TimelineDefinition timelineDefinition) {
        final MetricsType metricsType = new MetricsType();
        final List<MetricIdentifier> metrics = timelineDefinition.getMetrics();
        for (MetricIdentifier metric : metrics) {
            final MetricType metricType = new JaxbMetricAdaptor().serialize(metric);

            metricsType.getMetric().add(metricType);
        }

        final Timeline timeline = new Timeline();
        final JobIdentifier jobIdentifier = timelineDefinition.getJobIdentifier();
        if (jobIdentifier != null) {
            timeline.setJobName(jobIdentifier.getName());
        }
        timeline.setMetrics(metricsType);

        timeline.setChartOptions(createChartOptionsType(timelineDefinition.getChartOptions()));

        return timeline;
    }

    private ChartOptionsType createChartOptionsType(ChartOptions chartOptions) {
        if (chartOptions == null) {
            return null;
        }

        final HorizontalAxis horizontalAxis = createHorizontalAxis(chartOptions.getHorizontalAxisOption());
        final VerticalAxis verticalAxis = createVerticalAxis(chartOptions.getVerticalAxisOption());

        final ChartOptionsType chartOptionsType = new ChartOptionsType();
        chartOptionsType.setHorizontalAxis(horizontalAxis);
        chartOptionsType.setVerticalAxis(verticalAxis);
        return chartOptionsType;
    }

    private HorizontalAxis createHorizontalAxis(HorizontalAxisOption axis) {
        if (axis == null) {
            return null;
        }

        final HorizontalAxis horizontalAxis = new HorizontalAxis();

        if (axis instanceof LatestNumberOfDaysHAxisOption) {
            final int latestNumberOfDays = ((LatestNumberOfDaysHAxisOption) axis).getLatestNumberOfDays();

            final RollingAxis rollingAxis = new RollingAxis();
            rollingAxis.setLatestNumberOfDays(latestNumberOfDays);
            horizontalAxis.setRollingAxis(rollingAxis);
        } else {
            final XMLGregorianCalendar beginDate = createDate(axis.getBeginDate());
            final XMLGregorianCalendar endDate = createDate(axis.getEndDate());

            final FixedAxis fixedAxis = new FixedAxis();
            fixedAxis.setBeginDate(beginDate);
            fixedAxis.setEndDate(endDate);
            horizontalAxis.setFixedAxis(fixedAxis);
        }

        return horizontalAxis;
    }

    private VerticalAxis createVerticalAxis(VerticalAxisOption axis) {
        if (axis == null) {
            return null;
        }

        final VerticalAxis verticalAxis = new VerticalAxis();

        if (axis instanceof DefaultVAxisOption) {
            if (((DefaultVAxisOption) axis).isHeightSet()) {
                verticalAxis.setHeight(axis.getHeight());
            } else {
                // don't set the height explicitly.
            }
        } else {
            verticalAxis.setHeight(axis.getHeight());
        }

        verticalAxis.setLogarithmicScale(axis.isLogarithmicScale());
        verticalAxis.setMinimumValue(axis.getMinimumValue());
        verticalAxis.setMaximumValue(axis.getMaximumValue());

        return verticalAxis;
    }

}