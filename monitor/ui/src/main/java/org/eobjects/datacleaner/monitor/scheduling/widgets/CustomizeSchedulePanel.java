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
package org.eobjects.datacleaner.monitor.scheduling.widgets;

import java.util.List;

import org.eobjects.datacleaner.monitor.scheduling.SchedulingServiceAsync;
import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.Widget;

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

    private final SchedulingServiceAsync _service;
    private final TenantIdentifier _tenant;

    public CustomizeSchedulePanel(SchedulingServiceAsync service, TenantIdentifier tenant, ScheduleDefinition schedule) {
        super();

        _service = service;
        _tenant = tenant;
        _schedule = schedule;

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
            }
        });

        initWidget(uiBinder.createAndBindUi(this));

        final String expression = _schedule.getCronExpression();
        final JobIdentifier scheduleAfterJob = _schedule.getDependentJob();
        if (expression != null) {
            periodicTriggerRadio.setValue(true);
            periodicTriggerExpressionTextBox.setText(expression);
        } else if (scheduleAfterJob != null) {
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

        if (periodicTriggerRadio.getValue()) {
            _schedule.setCronExpression(periodicTriggerExpressionTextBox.getText());
        }

        if (dependentTriggerRadio.getValue()) {
            final int index = dependentTriggerJobListBox.getSelectedIndex();
            final String dependentJobName = dependentTriggerJobListBox.getValue(index);
            _schedule.setDependentJob(new JobIdentifier(dependentJobName));
        }

        return _schedule;
    }
}
