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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JCheckBox;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.panels.DCPanel;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;

/**
 * Abstract implementation of the {@link PropertyWidget} interface, for array
 * properties that are represented using a list of checkboxes.
 * 
 * @author Kasper Sørensen
 * 
 * @param <E>
 */
public abstract class AbstractMultipleCheckboxesPropertyWidget<E> extends AbstractPropertyWidget<E[]> {

	private static final long serialVersionUID = 1L;

	private final ActionListener CHANGE_LISTENER = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			fireValueChanged();
		}
	};

	private final ActionListener SELECT_ALL_LISTENER = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			for (JCheckBox cb : _checkBoxes) {
				if (cb.isEnabled()) {
					cb.setSelected(true);
				}
			}
			fireValueChanged();
		}
	};

	private final ActionListener SELECT_NONE_LISTENER = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			for (JCheckBox cb : _checkBoxes) {
				cb.setSelected(false);
			}
			fireValueChanged();
		}
	};

	private final Map<String, E> _availableValues;
	private volatile JCheckBox[] _checkBoxes;
	private final Class<E> _itemClass;

	public AbstractMultipleCheckboxesPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			ConfiguredPropertyDescriptor propertyDescriptor, Class<E> itemClass) {
		super(beanJobBuilder, propertyDescriptor);
		_availableValues = new LinkedHashMap<String, E>();
		_itemClass = itemClass;
		setLayout(new VerticalLayout(2));
	}

	protected JCheckBox[] getCheckBoxes() {
		if (_checkBoxes == null) {
			updateComponents();
		}
		return _checkBoxes;
	}

	@Override
	public void addNotify() {
		super.addNotify();
		updateComponents();
	}

	protected abstract E[] getAvailableValues();

	protected abstract String getName(E item);

	protected void updateComponents() {
		@SuppressWarnings("unchecked")
		E[] currentValues = (E[]) getBeanJobBuilder().getConfiguredProperty(getPropertyDescriptor());
		updateComponents(currentValues);
	}

	protected void updateComponents(E[] values) {
		removeAll();

		_availableValues.clear();

		{
			E[] availableValues = getAvailableValues();
			for (E availableValue : availableValues) {
				_availableValues.put(getName(availableValue), availableValue);
			}
			if (values != null) {
				for (E value : values) {
					_availableValues.put(getName(value), value);
				}
			}
		}

		DCPanel buttonPanel = new DCPanel();
		buttonPanel.setLayout(new HorizontalLayout(2));

		JButton selectAllButton = new JButton("Select all");
		selectAllButton.addActionListener(SELECT_ALL_LISTENER);
		buttonPanel.add(selectAllButton);

		JButton selectNoneButton = new JButton("Select none");
		selectNoneButton.addActionListener(SELECT_NONE_LISTENER);
		buttonPanel.add(selectNoneButton);

		add(buttonPanel);

		_checkBoxes = new JCheckBox[_availableValues.size()];
		if (_checkBoxes.length == 0) {
			_checkBoxes = new JCheckBox[1];
			_checkBoxes[0] = new JCheckBox("- no string patterns available -");
			_checkBoxes[0].setOpaque(false);
			_checkBoxes[0].setEnabled(false);
			add(_checkBoxes[0]);
		} else {
			int i = 0;
			for (Entry<String, E> entry : _availableValues.entrySet()) {
				JCheckBox checkBox = new JCheckBox(entry.getKey(), isEnabled(entry.getValue(), values));
				checkBox.setOpaque(false);
				checkBox.addActionListener(CHANGE_LISTENER);
				_checkBoxes[i] = checkBox;
				add(checkBox);
				i++;
			}
		}
		fireValueChanged();
	}

	private boolean isEnabled(E value, E[] enabledValues) {
		if (enabledValues == null || enabledValues.length == 0) {
			return false;
		}
		for (E currentValue : enabledValues) {
			if (currentValue.equals(value)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isSet() {
		for (JCheckBox checkBox : getCheckBoxes()) {
			if (checkBox.isSelected()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public E[] getValue() {
		List<E> result = new ArrayList<E>();
		JCheckBox[] checkBoxes = getCheckBoxes();
		for (int i = 0; i < checkBoxes.length; i++) {
			if (checkBoxes[i].isSelected()) {
				String itemName = checkBoxes[i].getText();
				result.add(_availableValues.get(itemName));
			}
		}
		@SuppressWarnings("unchecked")
		E[] array = (E[]) Array.newInstance(_itemClass, result.size());
		return result.toArray(array);
	}

	@Override
	protected void setValue(E[] value) {
		updateComponents(value);
	}
}
