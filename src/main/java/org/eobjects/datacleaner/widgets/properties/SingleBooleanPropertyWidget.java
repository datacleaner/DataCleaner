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

import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;

public class SingleBooleanPropertyWidget extends AbstractPropertyWidget<Boolean> {

	private static final long serialVersionUID = 1L;

	private final JCheckBox _checkBox;

	public SingleBooleanPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		super(beanJobBuilder, propertyDescriptor);
		_checkBox = new JCheckBox();
		Boolean currentValue = (Boolean) beanJobBuilder.getConfiguredProperty(propertyDescriptor);
		_checkBox.setOpaque(false);
		if (currentValue != null) {
			_checkBox.setSelected(currentValue.booleanValue());
		}
		_checkBox.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				fireValueChanged();
			}
		});
		add(_checkBox);
	}

	@Override
	public boolean isSet() {
		return true;
	}

	@Override
	public Boolean getValue() {
		return _checkBox.isSelected();
	}

	@Override
	protected void setValue(Boolean value) {
		if (value == null) {
			_checkBox.setSelected(false);
		}
		_checkBox.setSelected(value.booleanValue());
	}

}
