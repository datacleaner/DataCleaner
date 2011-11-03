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
package org.eobjects.datacleaner.widgets.properties;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.inject.Inject;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.jdesktop.swingx.JXDatePicker;

public class SingleDatePropertyWidget extends AbstractPropertyWidget<Date> {

	private static final long serialVersionUID = 1L;

	private final JXDatePicker _datePicker;

	@Inject
	public SingleDatePropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		super(beanJobBuilder, propertyDescriptor);
		_datePicker = new JXDatePicker();
		_datePicker.setFormats("yyyy-MM-dd");
		_datePicker.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				fireValueChanged();
			}
		});
		Date currentValue = getCurrentValue();
		if (currentValue != null) {
			_datePicker.setDate(currentValue);
		}
		add(_datePicker);
	}

	@Override
	public Date getValue() {
		return _datePicker.getDate();
	}

	@Override
	protected void setValue(Date value) {
		_datePicker.setDate(value);
	}
}
