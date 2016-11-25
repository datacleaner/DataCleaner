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
package org.datacleaner.monitor.server.jaxb;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.datacleaner.monitor.dashboard.model.ChartOptions;
import org.datacleaner.monitor.dashboard.model.ChartOptions.HorizontalAxisOption;
import org.datacleaner.monitor.dashboard.model.ChartOptions.VerticalAxisOption;
import org.datacleaner.monitor.dashboard.model.DefaultHAxisOption;
import org.datacleaner.monitor.dashboard.model.DefaultVAxisOption;
import org.datacleaner.monitor.dashboard.model.LatestNumberOfDaysHAxisOption;
import org.datacleaner.monitor.dashboard.model.TimelineDefinition;
import org.datacleaner.monitor.jaxb.ChartOptionsType;
import org.datacleaner.monitor.jaxb.ChartOptionsType.HorizontalAxis;
import org.datacleaner.monitor.jaxb.ChartOptionsType.HorizontalAxis.FixedAxis;
import org.datacleaner.monitor.jaxb.ChartOptionsType.HorizontalAxis.RollingAxis;
import org.datacleaner.monitor.jaxb.ChartOptionsType.VerticalAxis;
import org.datacleaner.monitor.jaxb.MetricType;
import org.datacleaner.monitor.jaxb.Timeline;
import org.datacleaner.monitor.server.TimelineReader;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.MetricIdentifier;

/**
 * JAXB based {@link TimelineReader} of .analysis.timeline.xml files.
 */
public class JaxbTimelineReader extends AbstractJaxbAdaptor<Timeline> implements TimelineReader {

    public JaxbTimelineReader() {
        super(Timeline.class);
    }

    public Timeline unmarshallTimeline(final InputStream inputStream) {
        return unmarshal(inputStream);
    }

    @Override
    public TimelineDefinition read(final InputStream inputStream) {
        final Timeline timeline = unmarshallTimeline(inputStream);

        return createTimeline(timeline);
    }

    private TimelineDefinition createTimeline(final Timeline timeline) {
        final JobIdentifier jobIdentifier = new JobIdentifier(timeline.getJobName());

        final TimelineDefinition timelineDefinition = new TimelineDefinition();
        timelineDefinition.setJobIdentifier(jobIdentifier);

        final List<MetricIdentifier> metrics = createMetrics(timeline);
        timelineDefinition.setMetrics(metrics);

        final ChartOptions chartOptions = createChartOptions(timeline);
        timelineDefinition.setChartOptions(chartOptions);

        return timelineDefinition;
    }

    private ChartOptions createChartOptions(final Timeline timeline) {
        final ChartOptionsType chartOptionsType = timeline.getChartOptions();
        if (chartOptionsType == null) {
            return null;
        }

        final HorizontalAxisOption horizontalAxisOption =
                createHorizontalAxisOption(chartOptionsType.getHorizontalAxis());
        final VerticalAxisOption verticalAxisOption = createVericalAxisOption(chartOptionsType.getVerticalAxis());

        return new ChartOptions(horizontalAxisOption, verticalAxisOption);
    }

    private VerticalAxisOption createVericalAxisOption(final VerticalAxis axis) {
        if (axis == null) {
            return new DefaultVAxisOption();
        }

        final Integer height = axis.getHeight();
        final boolean logarithmicScale = axis.isLogarithmicScale();
        final Integer minimumValue = axis.getMinimumValue();
        final Integer maximumValue = axis.getMaximumValue();

        return new DefaultVAxisOption(height, minimumValue, maximumValue, logarithmicScale);
    }

    private HorizontalAxisOption createHorizontalAxisOption(final HorizontalAxis axis) {
        if (axis == null) {
            return new DefaultHAxisOption();
        }

        final FixedAxis fixedAxis = axis.getFixedAxis();
        final RollingAxis rollingAxis = axis.getRollingAxis();
        if (fixedAxis != null) {
            final Date beginDate = createDate(fixedAxis.getBeginDate());
            final Date endDate = createDate(fixedAxis.getEndDate());
            return new DefaultHAxisOption(beginDate, endDate);
        } else if (rollingAxis != null) {
            final int latestNumberOfDays = rollingAxis.getLatestNumberOfDays();
            return new LatestNumberOfDaysHAxisOption(latestNumberOfDays);
        } else {
            return new DefaultHAxisOption();
        }
    }

    public List<MetricIdentifier> createMetrics(final Timeline timeline) {
        final List<MetricType> metricTypes = timeline.getMetrics().getMetric();
        final List<MetricIdentifier> metrics = new ArrayList<>(metricTypes.size());
        for (final MetricType metricType : metricTypes) {
            final MetricIdentifier metricIdentifier = new JaxbMetricAdaptor().deserialize(metricType);

            metrics.add(metricIdentifier);
        }
        return metrics;
    }
}
