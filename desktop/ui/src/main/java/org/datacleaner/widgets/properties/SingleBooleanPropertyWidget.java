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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.inject.Inject;
import javax.swing.JCheckBox;

import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.widgets.DCComboBox;
import org.datacleaner.widgets.DCComboBox.Listener;

public class SingleBooleanPropertyWidget extends AbstractPropertyWidget<Boolean> {

	private final JCheckBox _checkBox;
	private final DCComboBox<String> _comboBox;

	@Inject
	public SingleBooleanPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
			ComponentBuilder componentBuilder) {
		super(componentBuilder, propertyDescriptor);

		Boolean currentValue = getCurrentValue();
		
		boolean useCheckBox = propertyDescriptor.isRequired() || propertyDescriptor.getBaseType().isPrimitive();

		if (useCheckBox) {
			_checkBox = new JCheckBox();
			_comboBox = null;
			_checkBox.setOpaque(false);
			if (currentValue != null) {
				_checkBox.setSelected(currentValue.booleanValue());
			}
			_checkBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					fireValueChanged();
				}
			});
			add(_checkBox);
		} else {
			_checkBox = null;
			_comboBox = new DCComboBox<String>(new String[] { "true", "false", LabelUtils.NULL_LABEL });

			if (currentValue == null) {
				_comboBox.setSelectedItem(LabelUtils.NULL_LABEL);
			} else {
				_comboBox.setSelectedItem(currentValue.toString());
			}

			_comboBox.addListener(new Listener<String>() {
				@Override
				public void onItemSelected(String item) {
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
