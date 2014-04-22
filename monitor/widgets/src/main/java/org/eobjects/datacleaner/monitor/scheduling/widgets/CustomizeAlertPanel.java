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

import org.eobjects.datacleaner.monitor.scheduling.model.AlertDefinition;
import org.eobjects.datacleaner.monitor.scheduling.model.AlertSeverity;
import org.eobjects.datacleaner.monitor.shared.model.DCUserInputException;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.JobMetrics;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.widgets.NumberTextBox;
import org.eobjects.datacleaner.monitor.shared.widgets.MetricAnchor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Panel used to customize/edit an {@link AlertDefinition}.
 */
public class CustomizeAlertPanel extends Composite {
    
    interface MyUiBinder extends UiBinder<Widget, CustomizeAlertPanel> {
    }

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    private final AlertDefinition _alert;
    private final RadioButton _severityIntelligenceRadio;
    private final RadioButton _severitySurveillanceRadio;
    private final RadioButton _severityWarningRadio;
    private final RadioButton _severityFatalRadio;

    @UiField
    TextBox descriptionTextBox;

    @UiField(provided=true)
    MetricAnchor metricAnchor;

    @UiField
    NumberTextBox minimumValueTextBox;

    @UiField
    NumberTextBox maximumValueTextBox;

    @UiField
    FlowPanel severityPanel;

    public CustomizeAlertPanel(TenantIdentifier tenant, JobIdentifier job, AlertDefinition alert, JobMetrics result) {
        super();

        _alert = alert;
        metricAnchor = new MetricAnchor(tenant);
        metricAnchor.setJobMetrics(result);
        metricAnchor.setMetric(_alert.getMetricIdentifier());

        initWidget(uiBinder.createAndBindUi(this));

        _severityIntelligenceRadio = createRadioButton("alert_severity", "Intelligence", AlertSeverity.INTELLIGENCE);
        _severitySurveillanceRadio = createRadioButton("alert_severity", "Surveillance", AlertSeverity.SURVEILLANCE);
        _severityWarningRadio = createRadioButton("alert_severity", "Warning", AlertSeverity.WARNING);
        _severityFatalRadio = createRadioButton("alert_severity", "Fatal", AlertSeverity.FATAL);

        if (getSelectedSeverity() == null) {
            // set default severity
            _severityIntelligenceRadio.setValue(true);
        }

        descriptionTextBox.setText(_alert.getDescription());
        minimumValueTextBox.setNumberValue(_alert.getMinimumValue());
        maximumValueTextBox.setNumberValue(_alert.getMaximumValue());
    }

    private RadioButton createRadioButton(String group, String label, AlertSeverity severity) {
        RadioButton radioButton = new RadioButton(group, label);
        radioButton.addStyleDependentName(severity.toString());
        if (_alert.getSeverity() == severity) {
            radioButton.setValue(true);
        }
        severityPanel.add(radioButton);
        return radioButton;
    }

    public AlertDefinition updateAlert() {
        final Integer max = maximumValueTextBox.getNumberValue();
        final Integer min = minimumValueTextBox.getNumberValue();
        
        if (max == null && min == null) {
            throw new DCUserInputException("Please enter a maximum or a minimum value for the selected metric.");
        }
        
        _alert.setMetricIdentifier(metricAnchor.getMetric());
        _alert.setDescription(descriptionTextBox.getText());
        _alert.setMaximumValue(max);
        _alert.setMinimumValue(min);
        _alert.setSeverity(getSelectedSeverity());
        return _alert;
    }

    private AlertSeverity getSelectedSeverity() {
        if (_severityIntelligenceRadio.getValue().booleanValue()) {
            return AlertSeverity.INTELLIGENCE;
        }
        if (_severitySurveillanceRadio.getValue().booleanValue()) {
            return AlertSeverity.SURVEILLANCE;
        }
        if (_severityWarningRadio.getValue().booleanValue()) {
            return AlertSeverity.WARNING;
        }
        if (_severityFatalRadio.getValue().booleanValue()) {
            return AlertSeverity.FATAL;
        }
        return null;
    }

}
