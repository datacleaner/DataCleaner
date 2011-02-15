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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.panels.DCPanel;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;

public class MultipleEnumPropertyWidget extends AbstractPropertyWidget<Enum<?>[]> {

	private final ChangeListener CHANGE_LISTENER = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			fireValueChanged();
		}
	};

	private final ActionListener selectAllActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			for (JCheckBox cb : _checkBoxes) {
				cb.setSelected(true);
			}
		}
	};

	private final ActionListener selectNoneActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			for (JCheckBox cb : _checkBoxes) {
				cb.setSelected(false);
			}
		}
	};

	private static final long serialVersionUID = 1L;

	private volatile JCheckBox[] _checkBoxes;
	private final Enum<?>[] _availableEnums;

	public MultipleEnumPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			ConfiguredPropertyDescriptor propertyDescriptor) {
		super(beanJobBuilder, propertyDescriptor);
		_availableEnums = (Enum<?>[]) getPropertyDescriptor().getBaseType().getEnumConstants();
		setLayout(new VerticalLayout(2));
		updateComponents();
	}

	private void updateComponents() {
		removeAll();

		Enum<?>[] currentValue = (Enum<?>[]) getBeanJobBuilder().getConfiguredProperty(getPropertyDescriptor());

		DCPanel buttonPanel = new DCPanel();
		buttonPanel.setLayout(new HorizontalLayout(2));

		JButton selectAllButton = new JButton("Select all");
		selectAllButton.addActionListener(selectAllActionListener);
		buttonPanel.add(selectAllButton);

		JButton selectNoneButton = new JButton("Select none");
		selectNoneButton.addActionListener(selectNoneActionListener);
		buttonPanel.add(selectNoneButton);

		add(buttonPanel);

		_checkBoxes = new JCheckBox[_availableEnums.length];
		if (_checkBoxes.length == 0) {
			_checkBoxes = new JCheckBox[1];
			_checkBoxes[0] = new JCheckBox("- no values available -");
			_checkBoxes[0].setOpaque(false);
			_checkBoxes[0].setEnabled(false);
			add(_checkBoxes[0]);
		} else {
			int i = 0;
			for (Enum<?> e : _availableEnums) {
				JCheckBox checkBox = new JCheckBox(e.name(), isEnabled(e, currentValue));
				checkBox.setOpaque(false);
				checkBox.addChangeListener(CHANGE_LISTENER);
				_checkBoxes[i] = checkBox;
				add(checkBox);
				i++;
			}
		}
		fireValueChanged();
	}

	private boolean isEnabled(Enum<?> e, Enum<?>[] currentValue) {
		if (currentValue == null || currentValue.length == 0) {
			return false;
		}
		for (Enum<?> cur : currentValue) {
			if (e.equals(cur)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isSet() {
		for (JCheckBox checkBox : _checkBoxes) {
			if (checkBox.isSelected()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Enum<?>[] getValue() {
		List<Enum<?>> result = new ArrayList<Enum<?>>();
		for (int i = 0; i < _checkBoxes.length; i++) {
			if (_checkBoxes[i].isSelected()) {
				result.add(_availableEnums[i]);
			}
		}

		// create an array of the specific type defined by the property
		Object array = Array.newInstance(getPropertyDescriptor().getBaseType(), result.size());
		for (int i = 0; i < result.size(); i++) {
			Array.set(array, i, result.get(i));
		}
		return (Enum<?>[]) array;
	}

	@Override
	protected void setValue(Enum<?>[] value) {
		updateComponents();
	}
}
