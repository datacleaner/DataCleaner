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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JLabel;

import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.widgets.Alignment;

public class SingleDatePropertyWidget extends AbstractPropertyWidget<Date> implements ActionListener {

    private final SingleDatePropertySettingDialog _settingDialog;
    private final JLabel _valueLabel;
    private final JButton _changeButton;

    @Inject
    public SingleDatePropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
            ComponentBuilder componentBuilder) {
        super(componentBuilder, propertyDescriptor);
        
        _settingDialog = new SingleDatePropertySettingDialog();
        _valueLabel = new JLabel(getCurrentValue().toString());
        _changeButton = new JButton("Change");
        _changeButton.addActionListener(this);

        final DCPanel panel = new DCPanel();
        panel.setLayout(new BorderLayout());
        panel.add(DCPanel.flow(Alignment.LEFT, 4, 0, _valueLabel, _changeButton), BorderLayout.NORTH);

        add(panel);

        Date currentValue = getCurrentValue();
        setValue(currentValue);
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        _settingDialog.setVisible(true);
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
