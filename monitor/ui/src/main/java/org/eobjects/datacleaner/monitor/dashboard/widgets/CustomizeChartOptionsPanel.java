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

import org.eobjects.datacleaner.monitor.dashboard.model.ChartOptions;
import org.eobjects.datacleaner.monitor.dashboard.model.ChartOptions.HorizontalAxisOption;
import org.eobjects.datacleaner.monitor.dashboard.model.ChartOptions.VerticalAxisOption;
import org.eobjects.datacleaner.monitor.dashboard.model.DefaultHAxisOption;
import org.eobjects.datacleaner.monitor.dashboard.model.DefaultVAxisOption;
import org.eobjects.datacleaner.monitor.dashboard.model.LatestNumberOfDaysHAxisOption;
import org.eobjects.datacleaner.monitor.shared.widgets.HeadingLabel;
import org.eobjects.datacleaner.monitor.shared.widgets.NumberTextBox;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.DatePickerCell;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.cellview.client.CellWidget;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;

/**
 * Panel used to customize the {@link ChartOptions} of a timeline
 */
public class CustomizeChartOptionsPanel extends FlowPanel {

    private final CellWidget<Date> _beginDatePicker;
    private final CellWidget<Date> _endDatePicker;
    private final NumberTextBox _heightBox;
    private final NumberTextBox _minimumValueBox;
    private final NumberTextBox _maximumValue;

    // three radio buttons that represent the three types of timeline date
    // selections
    private final RadioButton _timelineAllDatesRadio;
    private final RadioButton _timelineLastDaysRadio;
    private final RadioButton _timelineFromToRadio;
    private final NumberTextBox _latestNumberOfDaysBox;
    private CheckBox _logScaleCheckBox;

    public CustomizeChartOptionsPanel(ChartOptions chartOptions) {
        super();
        final HorizontalAxisOption horizontalAxisOption = chartOptions.getHorizontalAxisOption();

        _timelineAllDatesRadio = new RadioButton("timeline_scope_type", "All dates");
        _timelineLastDaysRadio = new RadioButton("timeline_scope_type", "The latest .. days");
        _timelineFromToRadio = new RadioButton("timeline_scope_type", "From ... to ...");

        final Date beginDate = horizontalAxisOption.getBeginDate();
        final Date endDate = horizontalAxisOption.getEndDate();

        final int latestNumberOfDays;
        if (horizontalAxisOption instanceof LatestNumberOfDaysHAxisOption) {
            latestNumberOfDays = ((LatestNumberOfDaysHAxisOption) horizontalAxisOption).getLatestNumberOfDays();
            _timelineLastDaysRadio.setValue(true);
        } else if (beginDate != null || endDate != null) {
            latestNumberOfDays = LatestNumberOfDaysHAxisOption.DEFAULT_NUMBER_OF_DAYS;
            _timelineFromToRadio.setValue(true);
        } else {
            latestNumberOfDays = LatestNumberOfDaysHAxisOption.DEFAULT_NUMBER_OF_DAYS;
            _timelineAllDatesRadio.setValue(true);
        }

        _beginDatePicker = createDatePickerWidget((beginDate == null ? getDefaultBeginDate() : beginDate));
        _endDatePicker = createDatePickerWidget((endDate == null ? new Date() : endDate));

        _latestNumberOfDaysBox = new NumberTextBox();
        _latestNumberOfDaysBox.setMaxLength(3);
        _latestNumberOfDaysBox.setNumberValue(latestNumberOfDays);

        final VerticalAxisOption verticalAxisOption = chartOptions.getVerticalAxisOption();
        _heightBox = new NumberTextBox();
        _heightBox.setNumberValue(verticalAxisOption.getHeight());

        _minimumValueBox = new NumberTextBox();
        _minimumValueBox.setNumberValue(verticalAxisOption.getMinimumValue());

        _maximumValue = new NumberTextBox();
        _maximumValue.setNumberValue(verticalAxisOption.getMaximumValue());

        _logScaleCheckBox = new CheckBox("Logarithmic scale?");
        _logScaleCheckBox.setValue(verticalAxisOption.isLogarithmicScale());

        addStyleName("CustomizeChartOptionsPanel");

        add(createHorizontalAxisOptionPanel());
        add(createVerticalAxisOptionPanel());
    }

    private Date getDefaultBeginDate() {
        return new LatestNumberOfDaysHAxisOption().getBeginDate();
    }

    private CellWidget<Date> createDatePickerWidget(Date date) {
        CellWidget<Date> cellWidget = new CellWidget<Date>(createDatePickerCell(), date);
        cellWidget.addStyleName("DateSelectionWidget");
        return cellWidget;
    }

    private Cell<Date> createDatePickerCell() {
        DatePickerCell cell = new DatePickerCell(DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT));
        return cell;
    }

    private Panel createHorizontalAxisOptionPanel() {
        final FlowPanel panel = new FlowPanel();
        panel.addStyleName("HorizontalAxisOptionPanel");

        panel.add(new HeadingLabel("Horizontal axis"));

        panel.add(new Label("Please select the appropriate time period for the horizontal axis."));

        panel.add(_timelineAllDatesRadio);
        panel.add(createTimelineRadioSpecPanel(new Label("All observations will be included")));

        panel.add(_timelineFromToRadio);
        panel.add(createTimelineRadioSpecPanel(new Label("Begin date: "), _beginDatePicker));
        panel.add(createTimelineRadioSpecPanel(new Label("End date: "), _endDatePicker));

        panel.add(_timelineLastDaysRadio);
        panel.add(createTimelineRadioSpecPanel(_latestNumberOfDaysBox, new Label(" days")));

        return panel;
    }

    private Panel createTimelineRadioSpecPanel(Widget... widgets) {
        final FlowPanel panel = new FlowPanel();
        panel.addStyleName("TimelineRadioSpecPanel");
        for (Widget widget : widgets) {
            panel.add(widget);
        }
        return panel;
    }

    private Panel createVerticalAxisOptionPanel() {
        final FlowPanel panel = new FlowPanel();
        panel.addStyleName("VerticalAxisOptionPanel");

        panel.add(new HeadingLabel("Vertical axis"));
        panel.add(new Label("Please select appropriate properties for the vertical axis layout."));

        panel.add(createTimelineRadioSpecPanel(new Label("Height (pixels): "), _heightBox));

        panel.add(createTimelineRadioSpecPanel(new Label("Maximum value: "), _maximumValue));

        panel.add(createTimelineRadioSpecPanel(new Label("Minimum value: "), _minimumValueBox));

        panel.add(createTimelineRadioSpecPanel(_logScaleCheckBox));

        return panel;
    }

    public ChartOptions getChartOptions() {
        final HorizontalAxisOption horizontalAxisOption;
        if (_timelineAllDatesRadio.getValue().booleanValue()) {
            horizontalAxisOption = new DefaultHAxisOption();
        } else if (_timelineLastDaysRadio.getValue().booleanValue()) {
            final Integer latestNumberOfDays = _latestNumberOfDaysBox.getNumberValue();
            horizontalAxisOption = new LatestNumberOfDaysHAxisOption(latestNumberOfDays);
        } else {
            final Date beginDate = _beginDatePicker.getValue();
            final Date endDate = _endDatePicker.getValue();
            horizontalAxisOption = new DefaultHAxisOption(beginDate, endDate);
        }

        final Integer height = _heightBox.getNumberValue();
        final Integer minimumValue = _minimumValueBox.getNumberValue();
        final Integer maximumValue = _maximumValue.getNumberValue();
        final boolean logarithmicScale = _logScaleCheckBox.getValue();
        final VerticalAxisOption verticalAxisOption = new DefaultVAxisOption(height, minimumValue, maximumValue,
                logarithmicScale);

        final ChartOptions chartOptions = new ChartOptions(horizontalAxisOption, verticalAxisOption);
        return chartOptions;
    }
}
