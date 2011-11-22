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

import org.apache.commons.lang.ArrayUtils;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.widgets.DCCheckBox;
import org.eobjects.datacleaner.widgets.DCCheckBox.Listener;
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

	private final Listener<E> CHANGE_LISTENER = new Listener<E>() {
		@Override
		public void onItemSelected(E item, boolean selected) {
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
	private final DCCheckBox<E> _notAvailableCheckBox;

	public AbstractMultipleCheckboxesPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			ConfiguredPropertyDescriptor propertyDescriptor, Class<E> itemClass) {
		super(beanJobBuilder, propertyDescriptor);
		_itemClass = itemClass;
		_checkBoxes = new LinkedHashMap<String, DCCheckBox<E>>();
		setLayout(new VerticalLayout(2));

		_notAvailableCheckBox = new DCCheckBox<E>(getNotAvailableText(), false);
		_notAvailableCheckBox.setOpaque(false);
		_notAvailableCheckBox.setEnabled(false);

		_buttonPanel = createButtonPanel();

		add(_buttonPanel);
	}

	private void updateVisibility() {
		_buttonPanel.setVisible(_checkBoxes.size() > 3);
		if (_checkBoxes.isEmpty()) {
			add(_notAvailableCheckBox);
		} else {
			remove(_notAvailableCheckBox);
		}
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

	public void initialize(E[] values) {
		if (values != null) {
			// add all registered values
			for (E item : values) {
				addCheckBox(item, true);	
			}
		}
		
		// add all available checkboxes
		E[] availableValues = getAvailableValues();
		for (E item : availableValues) {
			addCheckBox(item, isEnabled(item, values));
		}
		
		updateVisibility();
	};

	protected JCheckBox[] getCheckBoxes() {
		return _checkBoxes.values().toArray(new JCheckBox[_checkBoxes.size()]);
	}

	protected abstract E[] getAvailableValues();

	/**
	 * Gets the text for an optional disabled checkbox in case no items are
	 * available.
	 * 
	 * @return
	 */
	protected abstract String getNotAvailableText();

	protected JCheckBox addCheckBox(E item, boolean checked) {
		final String name = getName(item);
		DCCheckBox<E> checkBox = _checkBoxes.get(name);
		if (checkBox != null) {
			checkBox.setSelected(checked);
			return checkBox;
		}
		checkBox = new DCCheckBox<E>(name, checked);
		checkBox.setValue(item);
		checkBox.setOpaque(false);
		checkBox.addListener(CHANGE_LISTENER);
		_checkBoxes.put(name, checkBox);
		add(checkBox);

		updateVisibility();
		updateUI();

		return checkBox;
	}

	protected void removeCheckBox(E item) {
		DCCheckBox<E> checkBox = _checkBoxes.remove(getName(item));
		if (checkBox != null) {
			remove(checkBox);
		}

		updateVisibility();
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
	protected void setValue(E[] values) {
		// if checkBoxes is empty it means that the value is being set before
		// initializing the widget. This can occur in subclasses and automatic
		// creating of checkboxes should be done.
		if (_checkBoxes.isEmpty()) {
			for (E value : values) {
				addCheckBox(value, true);
			}
		}

		// update selections in checkboxes
		for (DCCheckBox<E> cb : _checkBoxes.values()) {
			if (ArrayUtils.contains(values, cb.getValue())) {
				cb.setSelected(true);
			} else {
				cb.setSelected(false);
			}
		}

		updateVisibility();
	}

	protected abstract String getName(E item);
}
