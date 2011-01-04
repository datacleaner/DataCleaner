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

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.util.LabelUtils;

public class SingleBooleanPropertyWidget extends AbstractPropertyWidget<Boolean> {

	private static final long serialVersionUID = 1L;

	private final JCheckBox _checkBox;
	private final JComboBox _comboBox;

	public SingleBooleanPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		super(beanJobBuilder, propertyDescriptor);

		Boolean currentValue = (Boolean) beanJobBuilder.getConfiguredProperty(propertyDescriptor);

		if (propertyDescriptor.isRequired()) {
			_checkBox = new JCheckBox();
			_comboBox = null;
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
		} else {
			_checkBox = null;
			_comboBox = new JComboBox(new String[] { "true", "false", LabelUtils.NULL_LABEL });

			if (currentValue == null) {
				_comboBox.setSelectedItem(LabelUtils.NULL_LABEL);
			} else {
				_comboBox.setSelectedItem(currentValue.toString());
			}

			_comboBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					fireValueChanged();
				}
			});

			add(_comboBox);
		}
	}

	@Override
	public boolean isSet() {
		if (_comboBox != null) {
			String selectedItem = (String) _comboBox.getSelectedItem();
			if (LabelUtils.NULL_LABEL.equals(selectedItem)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Boolean getValue() {
		if (_checkBox == null) {
			String selectedItem = (String) _comboBox.getSelectedItem();
			if (LabelUtils.NULL_LABEL.equals(selectedItem)) {
				return null;
			} else {
				return Boolean.parseBoolean(selectedItem);
			}
		} else {
			return _checkBox.isSelected();
		}
	}

	@Override
	protected void setValue(Boolean value) {
		if (_checkBox == null) {
			if (value == null) {
				_comboBox.setSelectedItem(LabelUtils.NULL_LABEL);
			} else {
				if (value.booleanValue()) {
					_comboBox.setSelectedItem("true");
				} else {
					_comboBox.setSelectedItem("false");
				}
			}
		} else {
			if (value == null) {
				_checkBox.setSelected(false);
			} else {
				_checkBox.setSelected(value.booleanValue());
			}
		}
	}

}
