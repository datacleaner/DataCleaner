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
package org.eobjects.datacleaner.monitor.shared.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eobjects.datacleaner.monitor.shared.model.DCUserInputException;
import org.eobjects.datacleaner.monitor.shared.model.JobMetrics;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Panel for defining a metric, either based on a formula or a single metric
 * identifier.
 */
public class DefineMetricPanel extends FlowPanel {

    private final TenantIdentifier _tenant;
    private final JobMetrics _jobMetrics;
    private final CheckBox _formulaCheckBox;
    private final TextBox _formulaTextBox;
    private final List<SelectMetricPanel> _selectMetricPanels;
    private final Button _formulaAddMetricButton;
    private final Button _formulaRemoveMetricButton;

    public DefineMetricPanel(final TenantIdentifier tenant, final JobMetrics jobMetrics,
            final MetricIdentifier existingMetric, final boolean formulaOnly) {
        super();
        addStyleName("DefineMetricPanel");

        final boolean formulaBased = (formulaOnly ? true : (existingMetric == null ? false : existingMetric
                .isFormulaBased()));

        _tenant = tenant;
        _jobMetrics = jobMetrics;
        _selectMetricPanels = new ArrayList<SelectMetricPanel>();

        _formulaAddMetricButton = new Button();
        _formulaAddMetricButton.addStyleName("AddMetricButton");
        _formulaAddMetricButton.setVisible(formulaBased);
        _formulaRemoveMetricButton = new Button();
        _formulaRemoveMetricButton.addStyleName("RemoveMetricButton");
        _formulaRemoveMetricButton.setVisible(formulaBased);

        _formulaTextBox = new TextBox();
        _formulaTextBox.addStyleName("FormulaTextBox");
        _formulaTextBox.setVisible(formulaBased);
        // provide an example template, which makes it convenient to do a
        // percentage calculation
        _formulaTextBox.setText("A * 100 / B");

        _formulaCheckBox = new CheckBox("Metric formula?");
        _formulaCheckBox.addStyleName("FormulaCheckBox");
        _formulaCheckBox.setTitle("Let this metric be a formula, comprising multiple child metrics in a calculation?");
        _formulaCheckBox.setValue(formulaBased);

        _formulaCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                final boolean formulaBased = event.getValue();
                _formulaTextBox.setVisible(formulaBased);
                _formulaAddMetricButton.setVisible(formulaBased);
                _formulaRemoveMetricButton.setVisible(formulaBased);
            }
        });

        if (!formulaOnly) {
            add(_formulaCheckBox);
        }
        add(_formulaTextBox);
        add(_formulaAddMetricButton);
        add(_formulaRemoveMetricButton);

        if (formulaBased) {
            if (existingMetric == null) {
                // a new formula is being defined
                updateFormulaState(true);
            } else {
                // an existing formula is being recreated
                _formulaTextBox.setText(existingMetric.getFormula());
                final List<MetricIdentifier> children = existingMetric.getChildren();
                for (MetricIdentifier child : children) {
                    addSelection(createSelectMetricPanel(child, formulaBased));
                }
            }
        } else {
            addSelection(createSelectMetricPanel(existingMetric, formulaBased));
        }

        // add listener for limiting amount of metric selections
        _formulaCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                final boolean formulaBased = event.getValue();
                updateFormulaState(formulaBased);
            }
        });

        _formulaAddMetricButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final SelectMetricPanel panel = createSelectMetricPanel(null, true);
                addSelection(panel);
            }
        });
        _formulaRemoveMetricButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (_selectMetricPanels.size() > 1) {
                    removeSelection();
                }
            }
        });
    }

    private SelectMetricPanel createSelectMetricPanel(MetricIdentifier child, boolean formulaBased) {
        SelectMetricPanel panel = new SelectMetricPanel(_tenant, _jobMetrics, child, formulaBased);
        if (child == null) {
            char c = 'A';
            c += _selectMetricPanels.size();
            panel.setDisplayName("" + c);
        }
        return panel;
    }

    private void updateFormulaState(final boolean formulaBased) {
        if (!formulaBased) {
            while (_selectMetricPanels.size() > 1) {
                removeSelection();
            }
        } else {
            while (_selectMetricPanels.size() < 2) {
                addSelection(createSelectMetricPanel(null, true));
            }
        }

        final StringBuilder formulaBuilder = new StringBuilder();
        char c = 'A';

        for (SelectMetricPanel panel : _selectMetricPanels) {
            // show display name, since it will be used in the formula
            // as a variable
            panel.setDisplayNameVisible(formulaBased);
            panel.setDisplayName("" + c);

            if (formulaBuilder.length() == 0) {
                formulaBuilder.append(c);
            } else {
                formulaBuilder.append(" + ");
                formulaBuilder.append(c);
            }

            c++;
        }
    }

    private void removeSelection() {
        final SelectMetricPanel panel = _selectMetricPanels.remove(_selectMetricPanels.size() - 1);
        remove(panel);
    }

    private void addSelection(final SelectMetricPanel selectMetricPanel) {
        _selectMetricPanels.add(selectMetricPanel);

        final String displayName = selectMetricPanel.getDisplayName();

        selectMetricPanel.addDisplayNameValueChangeHandler(new ValueChangeHandler<String>() {
            private String previousValue = displayName;

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                final String value = event.getValue();
                GWT.log("Value changed. Previous: " + previousValue + ", New: " + value);
                if (previousValue != null && !previousValue.trim().isEmpty()) {
                    final String formulaText = _formulaTextBox.getText();
                    final int position = formulaText.indexOf(previousValue);
                    if (position != -1) {
                        final String newFormula = formulaText.replace(previousValue, value);
                        _formulaTextBox.setText(newFormula);
                    }
                }
                previousValue = value;
            }
        });
        add(selectMetricPanel);
    }

    public MetricIdentifier getMetric() throws DCUserInputException {
        if (_formulaCheckBox.getValue()) {
            final String formula = _formulaTextBox.getValue();
            final List<MetricIdentifier> children = new ArrayList<MetricIdentifier>();
            for (SelectMetricPanel panel : _selectMetricPanels) {
                MetricIdentifier childMetric = panel.getMetric();
                validateFormulaChildMetric(childMetric);
                children.add(childMetric);
            }
            return new MetricIdentifier(formula, formula, children);
        } else {
            return _selectMetricPanels.get(0).getMetric();
        }
    }

    private void validateFormulaChildMetric(MetricIdentifier childMetric) throws DCUserInputException {
        final String displayName = childMetric.getDisplayName();
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new DCUserInputException("Please provide formula symbols for all child metrics.");
        }

        char firstChar = displayName.trim().charAt(0);
        if (!Character.isLetter(firstChar)) {
            throw new DCUserInputException("Formula symbol '" + displayName + "' must start with a letter.");
        }

        final int length = displayName.length();
        for (int i = 0; i < length; i++) {
            char c = displayName.charAt(i);
            if (c != ' ' && c != '_' && !Character.isLetter(c) && !Character.isDigit(c)) {
                throw new DCUserInputException("Formula symbol '" + displayName + "' contains invalid character: " + c);
            }
        }
    }

}
