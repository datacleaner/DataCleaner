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
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.gflot.client.DataPoint;
import com.googlecode.gflot.client.PlotModel;
import com.googlecode.gflot.client.Series;
import com.googlecode.gflot.client.SeriesHandler;
import com.googlecode.gflot.client.SimplePlot;
import com.googlecode.gflot.client.event.PlotClickListener;
import com.googlecode.gflot.client.event.PlotHoverListener;
import com.googlecode.gflot.client.event.PlotItem;
import com.googlecode.gflot.client.event.PlotPosition;
import com.googlecode.gflot.client.jsni.Plot;
import com.googlecode.gflot.client.options.AbstractAxisOptions.TransformAxis;
import com.googlecode.gflot.client.options.AxisOptions;
import com.googlecode.gflot.client.options.GlobalSeriesOptions;
import com.googlecode.gflot.client.options.GridOptions;
import com.googlecode.gflot.client.options.LegendOptions;
import com.googlecode.gflot.client.options.LineSeriesOptions;
import com.googlecode.gflot.client.options.PlotOptions;
import com.googlecode.gflot.client.options.PointsSeriesOptions;
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

    private TimelineDefinition _timelineDefinition;
    private TimelineData _timelineData;
    private TimelinePanel _timelinePanel;
    private boolean _isDashboardEditor;

    interface Binder extends UiBinder<Widget, TimelineDesigner> {
    }

    public TimelineDesigner(TimelineDefinition timelineDefinition, TimelineData timelineData,
            TimelinePanel timelinePanel, boolean isDashboardEditor) {
        _timelineDefinition = timelineDefinition;
        _timelineData = timelineData;
        _timelinePanel = timelinePanel;
        _isDashboardEditor = isDashboardEditor;
    }

    /**
     * UI Binder
     */
    private static Binder _binder = GWT.create(Binder.class);

    @UiField(provided = true)
    SimplePlot plot;

    /**
     * Paragraph to add the hovering series
     */
    @UiField
    ParagraphElement hovering;
    /**
     * legend panel
     */
    private LegendPanel legendPanel;
    
    /**
     * get legend Panel widget to be added to timeline
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

        PlotModel model = new PlotModel();
        PlotOptions plotOptions = PlotOptions.create();
        

        plotOptions.setGlobalSeriesOptions(GlobalSeriesOptions.create()
                .setLineSeriesOptions(LineSeriesOptions.create().setShow(true).setLineWidth(2))
                .setPointsOptions(PointsSeriesOptions.create().setShow(true).setRadius(1)));
        LegendOptions legendOptions = LegendOptions.create();
        legendOptions.setShow(false);
        plotOptions.setLegendOptions(legendOptions);

        // TODO:
        // if (_timelineIdentifier != null) {
        // options.setTitle(_timelineIdentifier.getName());
        // }

        if (_timelineData.getRows().size() == 1) {
            plotOptions.getGlobalSeriesOptions().getPointsSeriesOptions().setRadius(6);
        } else if (_timelineData.getRows().size() < 10) {
            plotOptions.getGlobalSeriesOptions().getPointsSeriesOptions().setRadius(3);
        } else if (_timelineData.getRows().size() < 20) {
            plotOptions.getGlobalSeriesOptions().getPointsSeriesOptions().setRadius(2);
        }

        plotOptions.setGridOptions(GridOptions.create().setShow(true).setBorderWidth(0).setBorderColor("#221f1f")
                .setHoverable(true).setMouseActiveRadius(2).setClickable(true));

        TimeSeriesAxisOptions xAxisOptions = TimeSeriesAxisOptions.create();
        
        AxisOptions yAxisOptions = AxisOptions.create();
        
        if(logarithmicScale){
            //TODO:
//            transformToLogarithmicScale(yAxisOptions);
//            xAxisOptions.setTransform(new TransformAxis() {
//                
//                @Override
//                public double transform(double value) {
//                    
//                    return Math.log(value);
//                }
//                
//                @Override
//                public double inverseTransform(double value) {
//                    return Math.exp(value);
//                }
//            });
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
        plot = new SimplePlot(model, plotOptions);
        plot.setHeight(height);
        plot.setWidth(WIDTH);
        final PopupPanel popup = new PopupPanel();
        final com.google.gwt.user.client.ui.Label dateLabel = new com.google.gwt.user.client.ui.Label();
        popup.add(dateLabel);
        plot.addStyleName("TimelineChart");
        final List<MetricIdentifier> metrics = _timelineDefinition.getMetrics();
        for (int index = 0; index < metrics.size(); index++) {
            createTimeLineAndLegendItems(colorProvider, model, metrics, index);
        }

        addPlotClickListener();
        addHoverListener(popup, dateLabel);

        return _binder.createAndBindUi(this);
    }

    private void transformToLogarithmicScale(AxisOptions axisOptions) {
        axisOptions.setTransform(new TransformAxis() {
            
            @Override
            public double transform(double value) {
                return Math.log(value);
            }
            
            @Override
            public double inverseTransform(double value) {
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
            series.add(DataPoint.of(timelineDataRow.getDate().getTime(), timelineDataRow.getMetricValues().get(index)
                    .doubleValue()));
        }
    }

    private void addPlotClickListener() {
        plot.addClickListener(new PlotClickListener() {
            @Override
            public void onPlotClick(Plot plot, PlotPosition position, PlotItem item) {
                new DrillToProfilingResultSelectHandler(item, _timelineDefinition, _timelineData).onSelect();
            }
        }, true);
    }

    private void addHoverListener(final PopupPanel popup, final com.google.gwt.user.client.ui.Label dateLabel) {
        plot.addHoverListener(new PlotHoverListener() {
            @Override
            public void onPlotHover(Plot plot, PlotPosition position, PlotItem item) {
                Date date = _timelineData.getRows().get(item.getSeriesIndex()).getDate();

                dateLabel.setText(date + " " + item.getSeries().getLabel() + " "
                        + item.getSeries().getData().getY(item.getDataIndex()));
                popup.setPopupPosition(position.getPageX() + 10, position.getPageY() - 25);
                popup.setStyleName("timeline_popup");
                popup.show();
            }
        }, true);

        plot.addDomHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                hovering.setInnerText("");
                popup.hide();
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
