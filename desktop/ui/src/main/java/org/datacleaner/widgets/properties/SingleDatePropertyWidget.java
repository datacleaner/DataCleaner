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

import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.panels.DCPanel;

public class SingleDatePropertyWidget extends AbstractPropertyWidget<Date> {
    private static final String NOT_SET = "Not set! Please, select some date."; 

    private final SingleDatePropertySettingDialog _settingDialog;
    private final JLabel _valueLabel;
    private final JButton _changeButton;

    @Inject
    public SingleDatePropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
            ComponentBuilder componentBuilder) {
        super(componentBuilder, propertyDescriptor);
        
        _settingDialog = new SingleDatePropertySettingDialog(this);
        final Date currentValue = getCurrentValue();
        _valueLabel = new JLabel(currentValue == null ? NOT_SET : currentValue.toString());
        _changeButton = new JButton("Change");
        _changeButton.addActionListener(e -> _settingDialog.setVisible(true));
        final Dimension buttonSize = new Dimension(100, 25);
        _changeButton.setMinimumSize(buttonSize);
        _changeButton.setPreferredSize(buttonSize);
        _changeButton.setMaximumSize(buttonSize);

        final DCPanel panel = new DCPanel();
        panel.setLayout(new BorderLayout());
        panel.add(_valueLabel, BorderLayout.WEST);
        panel.add(_changeButton, BorderLayout.EAST);
        add(panel);
    }
    
    public void updateValue(String newValue) {
        _valueLabel.setText(newValue);
        fireValueChanged();
    }

    @Override
    protected void setValue(final Date value) {
        _settingDialog.setValue(value);
    }

    @Override
    public Date getValue() {
        return _settingDialog.getValue();
    }
}
