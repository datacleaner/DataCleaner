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
package org.datacleaner.widgets.properties;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Date;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JLabel;

import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.convert.TodayDate;

public class SingleDatePropertyWidget extends AbstractPropertyWidget<Date> {
    private final SingleDatePropertySettingDialog _settingDialog;
    private final JLabel _valueLabel;
    private final JButton _changeButton;

    @Inject
    public SingleDatePropertyWidget(final ConfiguredPropertyDescriptor propertyDescriptor,
            final ComponentBuilder componentBuilder, final WindowContext windowContext) {
        super(componentBuilder, propertyDescriptor);

        _settingDialog = new SingleDatePropertySettingDialog(windowContext, this);
        _changeButton = createChangeButton();
        _valueLabel = createValueLabel();
        createContent();
    }

    @Override
    protected void onPanelRemove() {
        super.onPanelRemove();
        _settingDialog.dispose();
    }

    private JLabel createValueLabel() {
        final JLabel valueLabel = new JLabel();
        final Date currentValue = getCurrentValue() == null ? new TodayDate() : getCurrentValue();
        setValue(currentValue);
        fireValueChanged();
        valueLabel.setText(_settingDialog.getFormattedString());

        return valueLabel;
    }

    private JButton createChangeButton() {
        final JButton changeButton = new JButton("Select");
        changeButton.addActionListener(e -> _settingDialog.setVisible(true));
        final Dimension buttonSize = new Dimension(100, 25);
        changeButton.setMinimumSize(buttonSize);
        changeButton.setPreferredSize(buttonSize);
        changeButton.setMaximumSize(buttonSize);

        return changeButton;
    }

    private void createContent() {
        final DCPanel panel = new DCPanel();
        panel.setLayout(new BorderLayout());
        panel.add(_valueLabel, BorderLayout.WEST);
        panel.add(_changeButton, BorderLayout.EAST);
        add(panel);
    }

    public void updateValue(final String newValue) {
        _valueLabel.setText(newValue);
        fireValueChanged();
    }

    @Override
    public Date getValue() {
        return _settingDialog.getValue();
    }

    @Override
    protected void setValue(final Date value) {
        _settingDialog.setValue(value);
    }
}
