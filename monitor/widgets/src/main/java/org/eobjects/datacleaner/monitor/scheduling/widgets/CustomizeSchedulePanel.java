/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.monitor.scheduling.widgets;

import java.util.Date;
import java.util.List;

import org.eobjects.datacleaner.monitor.scheduling.SchedulingServiceAsync;
import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.eobjects.datacleaner.monitor.shared.model.DCUserInputException;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

/**
 * Panel used to customize the schedule expression of a
 * {@link ScheduleDefinition}.
 */
public class CustomizeSchedulePanel extends Composite {

    interface MyUiBinder extends UiBinder<Widget, CustomizeSchedulePanel> {
    }

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    private final ScheduleDefinition _schedule;

    @UiField
    RadioButton periodicTriggerRadio;

    @UiField(provided = true)
    SuggestBox periodicTriggerExpressionTextBox;

    @UiField
    RadioButton dependentTriggerRadio;

    @UiField
    ListBox dependentTriggerJobListBox;

    @UiField
    RadioButton manualTriggerRadio;

    @UiField
    RadioButton oneTimeTriggerRadio;

    @UiField
    DateBox dateBox;

    private final SchedulingServiceAsync _service;
    private final TenantIdentifier _tenant;
    private Date serverDate;
    private String serverDateAsString;

    public CustomizeSchedulePanel(SchedulingServiceAsync service, TenantIdentifier tenant, ScheduleDefinition schedule) {
        super();

        _service = service;
        _tenant = tenant;
        _schedule = schedule;
        _service.getServerDate(new DCAsyncCallback<String>() {

            @Override
            public void onSuccess(String result) {
                serverDateAsString = result;
                serverDate = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss").parse(serverDateAsString);
            }
        });

        MultiWordSuggestOracle suggestions = new MultiWordSuggestOracle();
        suggestions.add("@yearly");
        suggestions.add("@monthly");
        suggestions.add("@weekly");
        suggestions.add("@daily");
        suggestions.add("@hourly");
        suggestions.add("@minutely");
        periodicTriggerExpressionTextBox = new SuggestBox(suggestions);
        periodicTriggerExpressionTextBox.getValueBox().addFocusHandler(new FocusHandler() {
            @Override
            public void onFocus(FocusEvent event) {
                periodicTriggerRadio.setValue(true);
                Element elementById = DOM.getElementById("periodicErrorMessage");
                if (periodicTriggerExpressionTextBox.getText().equals("")) {
                    elementById.setInnerHTML("Select cron expreesion for periodic scheduling");
                } else {
                    elementById.setInnerHTML("");
                }
            }
        });

        initWidget(uiBinder.createAndBindUi(this));

        dateBox.setFormat(new DateBox.DefaultFormat(DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss")));
        dateBox.getDatePicker().setWidth("200px");

        dateBox.getTextBox().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                oneTimeTriggerRadio.setValue(true);
                Element elementByIdForDate = DOM.getElementById("serverDate");
                elementByIdForDate.setInnerHTML("Date with respect to Server Time :" + serverDateAsString);
            }
        });

        dateBox.getTextBox().addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                Element elementById = DOM.getElementById("errorMessage");
                if (dateBox.getValue() == null) {
                    elementById.setInnerHTML("Select date for one time schedule");
                }
                else {
                    Date scheduleDate = dateBox.getValue();
                    elementById.setInnerHTML("");
                    if (scheduleDate.before(serverDate)) {
                        elementById.setInnerHTML("Past date can not be selected for one time schedule");
                    }
                    else {
                        elementById.setInnerHTML("");
                    }
                }
            }
        });

        oneTimeTriggerRadio.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                Element elementById = DOM.getElementById("errorMessage");
                Element elementByIdForDate = DOM.getElementById("serverDate");
                elementByIdForDate.setInnerHTML("Date with respect to Server Time :" + serverDateAsString);
                if (dateBox.getValue() == null) {
                    elementById.setInnerHTML("Select date for one time schedule");
                }
            }
        });

        dateBox.addValueChangeHandler(new ValueChangeHandler<Date>() {

            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                Date scheduleDate = dateBox.getValue();
                Element elementById = DOM.getElementById("errorMessage");
                elementById.setInnerHTML("");
                if (scheduleDate.before(serverDate)) {
                    elementById.setInnerHTML("Past date can not be selected for one time schedule");
                }
                else {
                    elementById.setInnerHTML("");
                }
            }
        });

        final String expression = _schedule.getCronExpression();
        final JobIdentifier scheduleAfterJob = _schedule.getDependentJob();
        final String expressionForOneTime = _schedule.getDateForOneTimeSchedule();

        if (expression != null) {
            periodicTriggerRadio.setValue(true);
            periodicTriggerExpressionTextBox.setText(expression);
        } else if (expressionForOneTime != null) {
            oneTimeTriggerRadio.setValue(true);
            dateBox.getTextBox().setValue(expressionForOneTime);
        }
        else if (scheduleAfterJob != null) {
            dependentTriggerRadio.setValue(true);
            dependentTriggerJobListBox.addItem(scheduleAfterJob.getName());
            dependentTriggerJobListBox.setSelectedIndex(0);
        } else {
            manualTriggerRadio.setValue(true);
        }

        dependentTriggerJobListBox.addFocusHandler(new FocusHandler() {

            @Override
            public void onFocus(FocusEvent event) {
                dependentTriggerRadio.setValue(true);
            }
        });

        _service.getDependentJobCandidates(_tenant, _schedule, new DCAsyncCallback<List<JobIdentifier>>() {
            @Override
            public void onSuccess(List<JobIdentifier> result) {
                for (JobIdentifier jobIdentifier : result) {
                    final String jobName = jobIdentifier.getName();
                    if (scheduleAfterJob != null && jobName.equals(scheduleAfterJob.getName())) {
                        // already added
                    } else {
                        dependentTriggerJobListBox.addItem(jobName);
                    }
                }
            }
        });
    }

    public ScheduleDefinition getUpdatedSchedule() {
        _schedule.setCronExpression(null);
        _schedule.setDependentJob(null);
        _schedule.setDateForOneTimeSchedule(null);

        if (periodicTriggerRadio.getValue()) {
            if (periodicTriggerExpressionTextBox.getText().equals("")) {
                throw new DCUserInputException("Please select a cron expreesion for periodic scheduling");
            } else {
                _schedule.setCronExpression(periodicTriggerExpressionTextBox.getText());
            }
        }
        if (oneTimeTriggerRadio.getValue()) {
            if (dateBox.getValue() == null) {
                throw new DCUserInputException("Please select a date for one time scheduling");
            }
            else {
                Date scheduleDate = dateBox.getValue();
                if (scheduleDate.before(serverDate)) {
                    throw new DCUserInputException("Past date cannot be selected.Please select a date in future");
                } else {
                    _schedule.setDateForOneTimeSchedule(dateBox.getTextBox().getText());
                }
            }
        }

        if (dependentTriggerRadio.getValue()) {
            final int index = dependentTriggerJobListBox.getSelectedIndex();
            final String dependentJobName = dependentTriggerJobListBox.getValue(index);
            _schedule.setDependentJob(new JobIdentifier(dependentJobName));
        }
        return _schedule;
    }
}