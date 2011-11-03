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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.widgets.DCCheckBox;
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
			for (JCheckBox cb : _checkBoxes.values()) {
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
			for (JCheckBox cb : _checkBoxes.values()) {
				cb.setSelected(false);
			}
			fireValueChanged();
		}
	};

	private final Map<String, DCCheckBox<E>> _checkBoxes;
	private final Class<E> _itemClass;
	private final DCPanel _buttonPanel;
	private final JCheckBox _notAvailableCheckBox;

	public AbstractMultipleCheckboxesPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			ConfiguredPropertyDescriptor propertyDescriptor, Class<E> itemClass) {
		super(beanJobBuilder, propertyDescriptor);
		_itemClass = itemClass;
		_checkBoxes = new LinkedHashMap<String, DCCheckBox<E>>();
		setLayout(new VerticalLayout(2));

		_notAvailableCheckBox = new JCheckBox(getNotAvailableText());
		_notAvailableCheckBox.setOpaque(false);
		_notAvailableCheckBox.setEnabled(false);

		_buttonPanel = createButtonPanel();
	}

	protected DCPanel createButtonPanel() {
		final DCPanel buttonPanel = new DCPanel();
		buttonPanel.setLayout(new HorizontalLayout(2));

		JButton selectAllButton = new JButton("Select all");
		selectAllButton.addActionListener(SELECT_ALL_LISTENER);
		buttonPanel.add(selectAllButton);

		JButton selectNoneButton = new JButton("Select none");
		selectNoneButton.addActionListener(SELECT_NONE_LISTENER);
		buttonPanel.add(selectNoneButton);
		return buttonPanel;
	}

	protected JCheckBox[] getCheckBoxes() {
		if (_checkBoxes.isEmpty()) {
			updateComponents();
		}
		return _checkBoxes.values().toArray(new JCheckBox[_checkBoxes.size()]);
	}

	@Override
	public void onPanelAdd() {
		super.onPanelAdd();
		updateComponents();
	}

	protected abstract E[] getAvailableValues();

	protected void updateComponents() {
		E[] currentValues = getCurrentValue();
		updateComponents(currentValues);
	}

	protected void updateComponents(E[] values) {
		removeAll();
		_checkBoxes.clear();

		add(_buttonPanel);

		{
			E[] availableValues = getAvailableValues();
			for (E item : availableValues) {
				addCheckBox(item, isEnabled(item, values));
			}
			if (values != null) {
				for (E value : values) {
					addCheckBox(value, true);
				}
			}
		}

		_notAvailableCheckBox.setVisible(_checkBoxes.isEmpty());
		add(_notAvailableCheckBox);

		fireValueChanged();
	}

	/**
	 * Gets the text for an optional disabled checkbox in case no items are
	 * available.
	 * 
	 * @return
	 */
	protected abstract String getNotAvailableText();

	protected JCheckBox addCheckBox(E item, boolean checked) {
		DCCheckBox<E> checkBox = _checkBoxes.get(item);
		if (checkBox != null) {
			checkBox.setSelected(checked);
			return checkBox;
		}
		checkBox = new DCCheckBox<E>(getName(item), checked);
		checkBox.setValue(item);
		checkBox.setOpaque(false);
		checkBox.addActionListener(CHANGE_LISTENER);
		_checkBoxes.put(getName(item), checkBox);
		add(checkBox);

		_notAvailableCheckBox.setVisible(_checkBoxes.isEmpty());
		updateUI();
		
		return checkBox;
	}

	protected void removeCheckBox(E item) {
		DCCheckBox<E> checkBox = _checkBoxes.remove(getName(item));
		if (checkBox != null) {
			remove(checkBox);
		}

		_notAvailableCheckBox.setVisible(_checkBoxes.isEmpty());
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
		Collection<DCCheckBox<E>> checkBoxes = _checkBoxes.values();
		for (DCCheckBox<E> cb : checkBoxes) {
			if (cb.isSelected()) {
				result.add(cb.getValue());
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

	protected abstract String getName(E item);
}
