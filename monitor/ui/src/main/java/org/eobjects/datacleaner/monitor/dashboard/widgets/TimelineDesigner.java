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
package org.eobjects.datacleaner.monitor.dashboard.widgets;

import java.util.Date;
import java.util.List;

import org.eobjects.datacleaner.monitor.dashboard.model.ChartOptions;
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineData;
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineDataRow;
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineDefinition;
import org.eobjects.datacleaner.monitor.dashboard.util.ColorProvider;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.gflot.client.DataPoint;
import com.googlecode.gflot.client.PlotModel;
import com.googlecode.gflot.client.PlotSelectionArea;
import com.googlecode.gflot.client.Series;
import com.googlecode.gflot.client.SeriesHandler;
import com.googlecode.gflot.client.SimplePlot;
import com.googlecode.gflot.client.event.PlotClickListener;
import com.googlecode.gflot.client.event.PlotHoverListener;
import com.googlecode.gflot.client.event.PlotItem;
import com.googlecode.gflot.client.event.PlotPosition;
import com.googlecode.gflot.client.event.PlotSelectedListener;
import com.googlecode.gflot.client.jsni.Plot;
import com.googlecode.gflot.client.options.AbstractAxisOptions;
import com.googlecode.gflot.client.options.AbstractAxisOptions.TransformAxis;
import com.googlecode.gflot.client.options.AxisOptions;
import com.googlecode.gflot.client.options.GlobalSeriesOptions;
import com.googlecode.gflot.client.options.GridOptions;
import com.googlecode.gflot.client.options.LegendOptions;
import com.googlecode.gflot.client.options.LineSeriesOptions;
import com.googlecode.gflot.client.options.PlotOptions;
import com.googlecode.gflot.client.options.PointsSeriesOptions;
import com.googlecode.gflot.client.options.Range;
import com.googlecode.gflot.client.options.SelectionOptions;
import com.googlecode.gflot.client.options.SelectionOptions.SelectionMode;
import com.googlecode.gflot.client.options.TimeSeriesAxisOptions;

/**
 * 
 * Gflot design implementation for TimeLines
 * 
 */
public class TimelineDesigner {

    /**
     * The width of the full panel, minus the width of the group selection
     * panel, minus 10 px margin
     */
    public static final int WIDTH = 540;

    /**
     * UI Binder
     */
    private static Binder _binder = GWT.create(Binder.class);

    @UiField
    HTMLPanel container;

    private final TimelineDefinition _timelineDefinition;
    private final TimelineData _timelineData;
    private final TimelinePanel _timelinePanel;
    private final boolean _isDashboardEditor;

    private final PopupPanel _popup;

    // the data point most recently hovered across (to compensate for 'too
    // precise' clickhandler).
    private PlotItem _activePlotItem;

    interface Binder extends UiBinder<Widget, TimelineDesigner> {
    }

    public TimelineDesigner(TimelineDefinition timelineDefinition, TimelineData timelineData,
            TimelinePanel timelinePanel, boolean isDashboardEditor) {
        _timelineDefinition = timelineDefinition;
        _timelineData = timelineData;
        _timelinePanel = timelinePanel;
        _isDashboardEditor = isDashboardEditor;
        _popup = new PopupPanel();
        _popup.setStyleName("timeline_popup");
    }

    /**
     * legend panel
     */
    private LegendPanel legendPanel;

    /**
     * get legend Panel widget to be added to timeline
     * 
     * @return
     */
    public LegendPanel getLegendPanel() {
        return legendPanel;
    }

    /**
     * Create plot returns widget for TimeLine
     */
    public Widget createPlot() {

        ColorProvider colorProvider = new ColorProvider();

        final ChartOptions chartOptions = _timelineDefinition.getChartOptions();
        final Integer height = chartOptions.getVerticalAxisOption().getHeight();
        final Integer maximumValue = chartOptions.getVerticalAxisOption().getMaximumValue();
        final Integer minimumValue = chartOptions.getVerticalAxisOption().getMinimumValue();
        final boolean logarithmicScale = chartOptions.getVerticalAxisOption().isLogarithmicScale();

        final PlotModel model = new PlotModel();
        final PlotOptions plotOptions = PlotOptions.create();

        // use selection plugin to allow zooming
        final SelectionOptions selectionOptions = SelectionOptions.create();
        selectionOptions.setMode(SelectionMode.XY);
        plotOptions.setSelectionOptions(selectionOptions);

        plotOptions.setGlobalSeriesOptions(GlobalSeriesOptions.create()
                .setLineSeriesOptions(LineSeriesOptions.create().setShow(true).setLineWidth(2))
                .setPointsOptions(PointsSeriesOptions.create().setShow(true).setRadius(1)));
        LegendOptions legendOptions = LegendOptions.create();
        legendOptions.setShow(false);
        plotOptions.setLegendOptions(legendOptions);

        final GlobalSeriesOptions globalSeriesOptions = plotOptions.getGlobalSeriesOptions();
        final PointsSeriesOptions pointsSeriesOptions = globalSeriesOptions.getPointsSeriesOptions();
        final int pointCount = _timelineData.getRows().size();

        if (pointCount < 8) {
            pointsSeriesOptions.setRadius(4);
        } else if (pointCount < 16) {
            pointsSeriesOptions.setRadius(3);
        } else {
            pointsSeriesOptions.setRadius(2);
        }

        GWT.log("Setting point radius: " + pointsSeriesOptions.getRadius() + " (for " + pointCount + " points)");

        plotOptions.setGridOptions(GridOptions.create().setShow(true).setBorderWidth(0).setBorderColor("#221f1f")
                .setHoverable(true).setMouseActiveRadius(2).setClickable(true));

        final TimeSeriesAxisOptions xAxisOptions = TimeSeriesAxisOptions.create();

        final AxisOptions yAxisOptions = AxisOptions.create();

        if (logarithmicScale) {
            transformToLogarithmicScale(yAxisOptions);
        }

        if (minimumValue != null) {
            yAxisOptions.setMinimum(minimumValue);
        }
        if (maximumValue != null) {
            yAxisOptions.setMaximum(maximumValue);
        }

        plotOptions.addXAxisOptions(xAxisOptions);
        plotOptions.addYAxisOptions(yAxisOptions);

        legendPanel = new LegendPanel();

        final List<MetricIdentifier> metrics = _timelineDefinition.getMetrics();
        for (int index = 0; index < metrics.size(); index++) {
            createTimeLineAndLegendItems(colorProvider, model, metrics, index);
        }

        final SimplePlot plot = createPlot(model, plotOptions, height);

        final Widget widget = _binder.createAndBindUi(this);

        container.setWidth(WIDTH + "px");
        container.clear();
        container.add(plot);

        return widget;
    }

    private SimplePlot createPlot(PlotModel model, PlotOptions plotOptions, Integer height) {
        final SimplePlot plot = new SimplePlot(model, plotOptions);
        plot.setWidth(WIDTH);
        plot.setStyleName("TimelineChart");

        addPlotClickListener(plot);
        addHoverListener(plot);
        addSelectedListener(plot, plotOptions, height);
        return plot;
    }

    private void addSelectedListener(SimplePlot plot, final PlotOptions plotOptions, final Integer height) {
        final PlotModel model = plot.getModel();
        // use selections to zoom
        plot.addSelectedListener(new PlotSelectedListener() {
            @Override
            public void onPlotSelected(PlotSelectionArea area) {
                GWT.log("Got a selection: " + area);

                final Range yRange = area.getY();
                final AbstractAxisOptions<?> yAxisOptions = plotOptions.getYAxisOptions(1);
                yAxisOptions.setMinimum(yRange.getFrom());
                yAxisOptions.setMaximum(yRange.getTo());

                final Range xRange = area.getX();
                final AbstractAxisOptions<?> xAxisOptions = plotOptions.getXAxisOptions(1);
                xAxisOptions.setMinimum(xRange.getFrom());
                xAxisOptions.setMaximum(xRange.getTo());

                final SimplePlot newPlot = createPlot(model, plotOptions, height);
                container.clear();
                container.add(newPlot);
                container.add(createZoomOutButton(model, plotOptions, height));
                _activePlotItem = null;
            }
        });
    }

    protected Widget createZoomOutButton(final PlotModel model, final PlotOptions plotOptions, final Integer height) {
        final Anchor button = new Anchor("Zoom out");
        button.setStyleName("ZoomOutButton");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final AbstractAxisOptions<?> yAxisOptions = plotOptions.getYAxisOptions(1);
                yAxisOptions.clearMinimum();
                yAxisOptions.clearMaximum();

                final AbstractAxisOptions<?> xAxisOptions = plotOptions.getXAxisOptions(1);
                xAxisOptions.clearMinimum();
                xAxisOptions.clearMaximum();

                final SimplePlot newPlot = createPlot(model, plotOptions, height);
                container.clear();
                container.add(newPlot);
                _activePlotItem = null;
            }
        });

        return button;
    }

    private void transformToLogarithmicScale(AxisOptions axisOptions) {
        axisOptions.setTransform(new TransformAxis() {

            @Override
            public double transform(double value) {
                if (value == 0) {
                    return -1;
                }
                return Math.log(value);
            }

            @Override
            public double inverseTransform(double value) {
                if (value == -1) {
                    return 0;
                }
                return Math.exp(value);
            }
        });
    }

    private void createTimeLineAndLegendItems(ColorProvider colorProvider, PlotModel model,
            final List<MetricIdentifier> metrics, int index) {
        String color = metrics.get(index).getMetricColor();
        color = (color == "" || color == null) ? colorProvider.getNextColor() : color;
        addLegendItem(legendPanel, metrics, index, color);
        SeriesHandler series = model.addSeries(Series.of(metrics.get(index).getDisplayName()).setColor(color));
        List<TimelineDataRow> rows = _timelineData.getRows();

        for (TimelineDataRow timelineDataRow : rows) {
            final Date date = timelineDataRow.getDate();
            final List<Number> metricValues = timelineDataRow.getMetricValues();
            if (metricValues.size() > index) {
                final Number value = metricValues.get(index);
                if (value == null) {
                    GWT.log("No value at index no. " + index + " @ date: " + date);
                } else {
                    series.add(DataPoint.of(date.getTime(), value.doubleValue()));
                }
            } else {
                GWT.log("Metric values list is too small for index no. " + index + " @ date: " + date);
            }
        }
    }

    private void addPlotClickListener(SimplePlot plot) {
        plot.addClickListener(new PlotClickListener() {
            @Override
            public void onPlotClick(Plot plot, PlotPosition position, PlotItem item) {
                GWT.log("Clicked! plot=" + plot + ", position=" + position + ", item=" + item);
                if (item == null) {
                    if (_activePlotItem == null) {
                        return;
                    }
                    // use the latest hovered item
                    item = _activePlotItem;
                }

                final DrillToProfilingResultSelectHandler handler = new DrillToProfilingResultSelectHandler(item,
                        _timelineDefinition, _timelineData);
                handler.onSelect();
            }
        }, false);
    }

    private void addHoverListener(SimplePlot plot) {
        plot.addHoverListener(new PlotHoverListener() {
            @Override
            public void onPlotHover(Plot plot, PlotPosition position, PlotItem item) {
                if (item == _activePlotItem && _popup.isShowing()) {
                    return;
                }
                _activePlotItem = item;

                final Integer dataIndex = item.getDataIndex();
                final Date date = _timelineData.getRows().get(dataIndex).getDate();
                final String formattedDate = DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT).format(date);

                final String metric = item.getSeries().getLabel();
                final double value = item.getSeries().getData().getY(dataIndex);
                final HTML label = new HTML("<b>" + formattedDate + "</b><br/> " + metric + ": <b>" + value + "</b>");

                _popup.setWidget(label);
                _popup.setPopupPosition(position.getPageX() + 10, position.getPageY() - 25);
                _popup.show();
            }
        }, true);

        plot.addDomHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                _activePlotItem = null;
                _popup.hide();
            }
        }, MouseOutEvent.getType());
    }

    private void addLegendItem(final LegendPanel legendPanel, final List<MetricIdentifier> metrics, int index,
            String color) {
        Legend legend = new Legend(metrics.get(index).getDisplayName(), color);
        legendPanel.addLegend(legend, new LegendClickHandler(metrics.get(index).getDisplayName(), metrics.get(index),
                _timelinePanel, legend, _isDashboardEditor));
    }

}
