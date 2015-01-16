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
import java.util.Date;

import javax.inject.Inject;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.util.convert.NowDate;
import org.datacleaner.util.convert.TodayDate;
import org.datacleaner.util.convert.YesterdayDate;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.widgets.Alignment;
import org.jdesktop.swingx.JXDatePicker;

public class SingleDatePropertyWidget extends AbstractPropertyWidget<Date> {

    private final JXDatePicker _datePicker;
    private final JRadioButton _dateCustomRadio;
    private final JRadioButton _dateNowRadio;
    private final JRadioButton _dateTodayRadio;
    private final JRadioButton _dateYesterdayRadio;

    @Inject
    public SingleDatePropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
            ComponentBuilder componentBuilder) {
        super(componentBuilder, propertyDescriptor);

        _datePicker = new JXDatePicker();
        _datePicker.setFormats("yyyy-MM-dd");

        _dateCustomRadio = new JRadioButton("Select: ");
        _dateNowRadio = new JRadioButton("Now");
        _dateTodayRadio = new JRadioButton("Today");
        _dateYesterdayRadio = new JRadioButton("Yesterday");

        _datePicker.addActionListener(fireValueChangedActionListener());
        _dateCustomRadio.addActionListener(fireValueChangedActionListener());
        _dateNowRadio.addActionListener(fireValueChangedActionListener());
        _dateTodayRadio.addActionListener(fireValueChangedActionListener());
        _dateYesterdayRadio.addActionListener(fireValueChangedActionListener());

        final ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(_dateCustomRadio);
        buttonGroup.add(_dateNowRadio);
        buttonGroup.add(_dateTodayRadio);
        buttonGroup.add(_dateYesterdayRadio);

        final DCPanel panel = new DCPanel();
        panel.setLayout(new BorderLayout());
        panel.add(DCPanel.flow(Alignment.LEFT, 4, 0, _dateCustomRadio, _datePicker), BorderLayout.CENTER);
        panel.add(DCPanel.flow(Alignment.LEFT, 4, 0, _dateNowRadio, _dateTodayRadio, _dateYesterdayRadio),
                BorderLayout.SOUTH);

        add(panel);

        Date currentValue = getCurrentValue();
        setValue(currentValue);
    }

    @Override
    public Date getValue() {
        if (_dateNowRadio.isSelected()) {
            return new NowDate();
        } else if (_dateTodayRadio.isSelected()) {
            return new TodayDate();
        } else if (_dateYesterdayRadio.isSelected()) {
            return new YesterdayDate();
        } else {
            return _datePicker.getDate();
        }
    }

    @Override
    protected void setValue(Date value) {
        if (value instanceof NowDate) {
            _dateNowRadio.setSelected(true);
        } else if (value instanceof TodayDate) {
            _dateTodayRadio.setSelected(true);
        } else if (value instanceof YesterdayDate) {
            _dateYesterdayRadio.setSelected(true);
        } else {
            _dateCustomRadio.setSelected(true);
            if (value != null) {
                _datePicker.setDate(value);
            }
        }
    }
}
