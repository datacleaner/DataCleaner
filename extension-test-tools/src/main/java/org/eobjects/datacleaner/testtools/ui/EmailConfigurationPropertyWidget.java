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
package org.eobjects.datacleaner.testtools.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.testtools.EmailConfiguration;
import org.eobjects.datacleaner.widgets.properties.AbstractPropertyWidget;

public class EmailConfigurationPropertyWidget extends
		AbstractPropertyWidget<EmailConfiguration> {

	private static final long serialVersionUID = 1L;
	private JComboBox _combo;

	public EmailConfigurationPropertyWidget(
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			ConfiguredPropertyDescriptor propertyDescriptor) {
		super(beanJobBuilder, propertyDescriptor);

		_combo = new JComboBox();
		_combo.setRenderer(new EmailConfigurationRenderer());
		_combo.addItem("- no email notification -");

		// TODO: Add notification options, probably from userpreferences.

		_combo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fireValueChanged();
			}
		});

		add(_combo);
	}

	@Override
	public void initialize(EmailConfiguration value) {
		super.initialize(value);
	}

	@Override
	public EmailConfiguration getValue() {
		Object item = _combo.getSelectedItem();
		if (item instanceof EmailConfiguration) {
			return (EmailConfiguration) item;
		}
		return null;
	}

	@Override
	protected void setValue(EmailConfiguration value) {
		if (value == null) {
			_combo.setSelectedIndex(0);
		} else {
			_combo.setSelectedItem(value);
		}
	}

}
