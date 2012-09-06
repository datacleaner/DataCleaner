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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eobjects.datacleaner.monitor.dashboard.DashboardServiceAsync;
import org.eobjects.datacleaner.monitor.dashboard.model.ChartOptions;
import org.eobjects.datacleaner.monitor.dashboard.model.DefaultVAxisOption;
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineData;
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineDataRow;
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineDefinition;
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineIdentifier;
import org.eobjects.datacleaner.monitor.dashboard.util.ColorProvider;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.widgets.ButtonPanel;
import org.eobjects.datacleaner.monitor.shared.widgets.HeadingLabel;
import org.eobjects.datacleaner.monitor.shared.widgets.LoadingIndicator;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.ChartArea;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.LegendPosition;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.corechart.AxisOptions;
import com.google.gwt.visualization.client.visualizations.corechart.LineChart;
import com.google.gwt.visualization.client.visualizations.corechart.Options;

/**
 * Panel that displays a timeline.
 */
public class TimelinePanel extends FlowPanel {

    /**
     * The width of the full panel, minus the width of the group selection
     * panel, minus 10 px margin
     */
    private static final int WIDTH = 540;

    private final DashboardServiceAsync _service;
    private final LoadingIndicator _loadingIndicator;
    private final TenantIdentifier _tenant;
    private final HeadingLabel _header;
    private final DashboardGroupPanel _timelineGroupPanel;
    private final Button _saveButton;
    private final Button _deleteButton;
    private final boolean _isDashboardEditor;

    private TimelineIdentifier _timelineIdentifier;
    private TimelineDefinition _timelineDefinition;
    private TimelineData _timelineData;

    public TimelinePanel(TenantIdentifier tenant, DashboardServiceAsync service, TimelineIdentifier timelineIdentifier,
            DashboardGroupPanel timelineGroupPanel, boolean isDashboardEditor) {
        super();
        _tenant = tenant;
        _service = service;
        _timelineIdentifier = timelineIdentifier;
        _timelineGroupPanel = timelineGroupPanel;
        _isDashboardEditor = isDashboardEditor;
        _header = new HeadingLabel("");

        _loadingIndicator = new LoadingIndicator();
        _loadingIndicator.setHeight((DefaultVAxisOption.DEFAULT_HEIGHT + 4) + "px");

        _saveButton = new Button("");
        _saveButton.setVisible(isDashboardEditor);
        _saveButton.addStyleDependentName("ImageButton");
        _saveButton.setTitle("Save timeline");
        _saveButton.addStyleName("SaveButton");
        _saveButton.addClickHandler(new SaveTimelineClickHandler(_service, _tenant, this));
        _saveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // disable button once saved
                _saveButton.setEnabled(false);
            }
        });

        if (_timelineIdentifier != null) {
            // initially does not make sense to save an (unchanged) and
            // identifyable timeline.
            _saveButton.setEnabled(false);
        }

        _deleteButton = new Button();
        _deleteButton.setVisible(isDashboardEditor);
        _deleteButton.addStyleDependentName("ImageButton");
        _deleteButton.setTitle("Delete timeline");
        _deleteButton.addStyleName("DeleteButton");
        _deleteButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final boolean confirmation = Window.confirm("Are you sure you wish to delete this timeline?");
                if (confirmation) {
                    if (_timelineIdentifier != null) {
                        _service.removeTimeline(_tenant, _timelineIdentifier, new DCAsyncCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean result) {
                                // do nothing
                            }
                        });
                    }
                    _timelineGroupPanel.removeTimelinePanel(TimelinePanel.this);
                }
            }
        });

        addStyleName("TimelinePanel");
        add(createButtonPanel());
        updateHeader();
        setLoading();

        if (_timelineIdentifier != null) {
            _service.getTimelineDefinition(_tenant, _timelineIdentifier, new DCAsyncCallback<TimelineDefinition>() {
                @Override
                public void onSuccess(final TimelineDefinition definition) {
                    setTimelineDefinition(definition);
                }
            });
        }
    }

    private void setLoading() {
        if (getWidgetCount() == 2) {
            if (getWidget(1) == _loadingIndicator) {
                // the loading indicator is already showing correctly
                return;
            }
        }

        // clean up everything except the button panel
        while (getWidgetCount() > 1) {
            remove(1);
        }
        add(_loadingIndicator);
    }

    public TimelineIdentifier getTimelineIdentifier() {
        return _timelineIdentifier;
    }

    public void setTimelineIdentifier(TimelineIdentifier timelineIdentifier) {
        if (timelineIdentifier.equals(_timelineIdentifier)) {
            return;
        }

        _timelineIdentifier = timelineIdentifier;

        updateHeader();

        if (_timelineData != null) {
            setLoading();
            renderChart();
        }
    }

    private void updateHeader() {
        if (_timelineIdentifier == null) {
            _header.setText("<new timeline>");
        } else {
            _header.setText(_timelineIdentifier.getName());
        }
    }

    public TenantIdentifier getTenantIdentifier() {
        return _tenant;
    }

    public void setTimelineDefinition(final TimelineDefinition timelineDefinition, final boolean fireEvents) {
        if (timelineDefinition.equals(_timelineDefinition) && _timelineData != null) {
            return;
        }
        _timelineDefinition = timelineDefinition;
        if (fireEvents) {
            if (timelineDefinition.isChanged()) {
                _saveButton.setEnabled(true);
            }
            setLoading();
            _service.getTimelineData(_tenant, timelineDefinition, new DCAsyncCallback<TimelineData>() {
                @Override
                public void onSuccess(TimelineData data) {
                    setTimelineData(data);
                }
            });
        }
    }

    public void setTimelineDefinition(final TimelineDefinition timelineDefinition) {
        setTimelineDefinition(timelineDefinition, true);
    }

    public TimelineDefinition getTimelineDefinition() {
        return _timelineDefinition;
    }

    public void setTimelineData(final TimelineData timelineData) {
        if (timelineData.equals(_timelineData)) {
            return;
        }
        _timelineData = timelineData;

        renderChart();
    }

    private void renderChart() {
        final Runnable lineChartRunnable = new Runnable() {
            @Override
            public void run() {
                final Options options = Options.create();

                final ChartOptions chartOptions = _timelineDefinition.getChartOptions();

                final Integer height = chartOptions.getVerticalAxisOption().getHeight();
                final Integer maximumValue = chartOptions.getVerticalAxisOption().getMaximumValue();
                final Integer minimumValue = chartOptions.getVerticalAxisOption().getMinimumValue();
                final boolean logarithmicScale = chartOptions.getVerticalAxisOption().isLogarithmicScale();

                final ChartArea chartArea = ChartArea.create();
                chartArea.setLeft(50d);
                chartArea.setTop(10d);
                chartArea.setWidth(WIDTH - 60);
                chartArea.setHeight(height * 0.8d);
                options.setChartArea(chartArea);

                options.setWidth(WIDTH);
                options.setHeight(height);

                if (logarithmicScale || maximumValue != null || minimumValue != null) {
                    final AxisOptions axisOptions = AxisOptions.create();
                    if (minimumValue != null) {
                        axisOptions.setMinValue(minimumValue);
                    }
                    if (maximumValue != null) {
                        axisOptions.setMaxValue(maximumValue);
                    }

                    axisOptions.setIsLogScale(logarithmicScale);

                    options.setVAxisOptions(axisOptions);
                }

                if (_timelineIdentifier != null) {
                    options.setTitle(_timelineIdentifier.getName());
                }
                options.setLegend(LegendPosition.NONE);

                final AbstractDataTable dataTable = createDataTable(_timelineDefinition, _timelineData);
                final List<String> colors = createColors(_timelineDefinition, new ColorProvider());
                options.setColors(colors.toArray(new String[colors.size()]));

                final LineChart chart = new LineChart(dataTable, options);
                chart.addSelectHandler(new DrillToProfilingResultSelectHandler(chart, dataTable));
                chart.addStyleName("TimelineChart");

                remove(_loadingIndicator);
                add(chart);
                LegendPanel legendPanel = new LegendPanel();
                legendPanel.addStyleName("LegendPanel");
                for (int i = 1; i < dataTable.getNumberOfColumns(); i++) {
                    Legend legend = new Legend(dataTable.getColumnLabel(i), colors.get(i - 1));
                    legendPanel.addLegend(legend, new LegendClickHandler(dataTable.getColumnLabel(i),
                            _timelineDefinition.getMetrics().get(i - 1), TimelinePanel.this, legend, _isDashboardEditor));

                }
                add(legendPanel);
            }
        };
        VisualizationUtils.loadVisualizationApi(lineChartRunnable, LineChart.PACKAGE);
    }

    protected List<String> createColors(TimelineDefinition definition, ColorProvider colorProvider) {
        final List<String> colors = new ArrayList<String>();

        for (MetricIdentifier metricIdentifier : definition.getMetrics()) {
            String color = metricIdentifier.getMetricColor();
            if (color == "" || color == null) {
                color = colorProvider.getNextColor();
            }
            colors.add(color);
        }
        return colors;
    }

    public TimelineData getTimelineData() {
        return _timelineData;
    }

    private ButtonPanel createButtonPanel() {
        final Button customizeButton = new Button("");
        customizeButton.setVisible(_isDashboardEditor);
        customizeButton.addStyleDependentName("ImageButton");
        customizeButton.setTitle("Customize timeline");
        customizeButton.addStyleName("CustomizeButton");
        customizeButton.addClickHandler(new CustomizeTimelineHandler(_service, this));

        final Button copyButton = new Button("");
        copyButton.setVisible(_isDashboardEditor);
        copyButton.addStyleDependentName("ImageButton");
        copyButton.setTitle("Copy timeline");
        copyButton.addStyleName("CopyButton");
        copyButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                TimelinePanel copyPanel = new TimelinePanel(_tenant, _service, null, _timelineGroupPanel,
                        _isDashboardEditor);
                copyPanel.setTimelineDefinition(_timelineDefinition);
                _timelineGroupPanel.add(copyPanel);
            }
        });

        final ButtonPanel buttonPanel = new ButtonPanel();

        buttonPanel.add(_header);
        buttonPanel.add(customizeButton);
        buttonPanel.add(copyButton);
        buttonPanel.add(_saveButton);
        buttonPanel.add(_deleteButton);

        return buttonPanel;
    }

    private void addRow(DataTable data, Date date, String resultFilePath, List<Number> values) {
        int rowIndex = data.addRow();
        data.setValue(rowIndex, 0, date);
        data.setProperty(rowIndex, 0, DrillToProfilingResultSelectHandler.PROPERTY_NAME_RESULT_FILE, resultFilePath);
        for (int i = 0; i < values.size(); i++) {
            int columnIndex = i + 1;
            final Number value = values.get(i);
            final int intValue;
            if (value == null) {
                intValue = -1;
            } else {
                // TODO: Is it always an int?
                intValue = value.intValue();
            }
            data.setValue(rowIndex, columnIndex, intValue);
        }
    }

    private AbstractDataTable createDataTable(TimelineDefinition definition, TimelineData timelineData) {
        final DataTable data = DataTable.create();
        data.addColumn(ColumnType.DATE, "Date");

        final List<MetricIdentifier> metrics = definition.getMetrics();
        for (MetricIdentifier metricIdentifier : metrics) {
            data.addColumn(ColumnType.NUMBER, metricIdentifier.getDisplayName());
        }

        final List<TimelineDataRow> rows = timelineData.getRows();
        for (TimelineDataRow row : rows) {
            addRow(data, row.getDate(), row.getResultFilePath(), row.getMetricValues());
        }

        return data;
    }

    public DashboardGroupPanel getTimelineGroupPanel() {
        return _timelineGroupPanel;
    }

    @Override
    protected void onAttach() {
        super.onAttach();

        // (re) attaching charts needs re-rendering
        if (_timelineDefinition != null && _timelineData != null) {
            setLoading();
            renderChart();
        }
    }

    public void refreshTimelineDefiniton(boolean isSaveTimelineActive) {
        setLoading();
        renderChart();
        if (isSaveTimelineActive) {
            _saveButton.setEnabled(true);
        }
    }

}
