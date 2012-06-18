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

import org.eobjects.datacleaner.monitor.scheduling.model.AlertDefinition;
import org.eobjects.datacleaner.monitor.scheduling.model.AlertSeverity;
import org.eobjects.datacleaner.monitor.shared.widgets.NumberTextBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
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

    @UiField
    Label metricLabel;

    @UiField
    NumberTextBox minimumValueTextBox;

    @UiField
    NumberTextBox maximumValueTextBox;

    @UiField
    FlowPanel severityPanel;

    public CustomizeAlertPanel(AlertDefinition alert) {
        super();

        _alert = alert;

        initWidget(uiBinder.createAndBindUi(this));

        _severityIntelligenceRadio = createRadioButton("alert_severity", "Intelligence", AlertSeverity.INTELLIGENCE);
        _severitySurveillanceRadio = createRadioButton("alert_severity", "Surveillance", AlertSeverity.SURVEILLANCE);
        _severityWarningRadio = createRadioButton("alert_severity", "Warning", AlertSeverity.WARNING);
        _severityFatalRadio = createRadioButton("alert_severity", "Fatal", AlertSeverity.FATAL);

        descriptionTextBox.setText(_alert.getDescription());
        metricLabel.setText(_alert.getMetricIdentifier().getDisplayName());
        minimumValueTextBox.setNumberValue(_alert.getMinimumValue());
        maximumValueTextBox.setNumberValue(_alert.getMaximumValue());
    }

    private RadioButton createRadioButton(String group, String label, AlertSeverity severity) {
        RadioButton radioButton = new RadioButton(group, label);
        if (_alert.getSeverity() == severity) {
            radioButton.setValue(true);
        }
        severityPanel.add(radioButton);
        return radioButton;
    }

    public void updateAlert() {
        _alert.setDescription(descriptionTextBox.getText());
        _alert.setMaximumValue(maximumValueTextBox.getNumberValue());
        _alert.setMinimumValue(minimumValueTextBox.getNumberValue());
        _alert.setSeverity(getSelectedSeverity());
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
