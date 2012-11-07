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

import org.eobjects.datacleaner.monitor.shared.model.JobMetrics;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Panel for defining a metric, either based on a formula or a single metric
 * identifier.
 */
public class DefineMetricPanel extends FlowPanel {

    private final CheckBox _formulaCheckBox;
    private final List<SelectMetricPanel> _selectMetricPanels;
    private final TextBox _formulaTextBox;
    private final JobMetrics _jobMetrics;

    public DefineMetricPanel(JobMetrics jobMetrics, MetricIdentifier existingMetric) {
        super();
        addStyleName("DefineMetricPanel");

        final boolean formulaBased = (existingMetric == null ? false : existingMetric.isFormulaBased());

        _jobMetrics = jobMetrics;
        _selectMetricPanels = new ArrayList<SelectMetricPanel>();

        _formulaTextBox = new TextBox();
        _formulaTextBox.setVisible(formulaBased);

        _formulaCheckBox = new CheckBox("Metric formula?");
        _formulaCheckBox.setTitle("Let this metric be a formula, comprising multiple child metrics in a calculation?");
        _formulaCheckBox.setValue(formulaBased);

        _formulaCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                _formulaTextBox.setVisible(event.getValue());
            }
        });

        add(_formulaCheckBox);
        add(_formulaTextBox);

        if (formulaBased) {
            _formulaTextBox.setText(existingMetric.getFormula());
            final List<MetricIdentifier> children = existingMetric.getChildren();
            for (MetricIdentifier child : children) {
                addSelection(new SelectMetricPanel(_jobMetrics, child, formulaBased));
            }
        } else {
            addSelection(new SelectMetricPanel(_jobMetrics, existingMetric, formulaBased));
        }

        // add listener for limiting amount of metric selections
        _formulaCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                final boolean formulaBased = event.getValue();
                if (!formulaBased) {
                    while (_selectMetricPanels.size() > 1) {
                        removeSelection();
                    }
                } else {
                    while (_selectMetricPanels.size() < 2) {
                        addSelection(new SelectMetricPanel(_jobMetrics, null, true));
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

                _formulaTextBox.setText(formulaBuilder.toString());
            }
        });
    }

    private void removeSelection() {
        final SelectMetricPanel panel = _selectMetricPanels.remove(_selectMetricPanels.size() - 1);
        remove(panel);
    }

    private void addSelection(final SelectMetricPanel selectMetricPanel) {
        _selectMetricPanels.add(selectMetricPanel);
        add(selectMetricPanel);
    }

    public MetricIdentifier getMetric() {
        if (_formulaCheckBox.getValue()) {
            final String formula = _formulaTextBox.getValue();
            final List<MetricIdentifier> children = new ArrayList<MetricIdentifier>();
            for (SelectMetricPanel panel : _selectMetricPanels) {
                MetricIdentifier childMetric = panel.getMetric();
                children.add(childMetric);
            }
            return new MetricIdentifier(formula, formula, children);
        } else {
            return _selectMetricPanels.get(0).getMetric();
        }
    }

}
