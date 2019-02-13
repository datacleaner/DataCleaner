/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;

import org.apache.commons.lang.ArrayUtils;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.widgets.DCCheckBox;
import org.datacleaner.widgets.DCCheckBox.Listener;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;

/**
 * Abstract implementation of the {@link PropertyWidget} interface, for array
 * properties that are represented using a list of checkboxes.
 *
 * @param <E>
 */
public abstract class AbstractMultipleCheckboxesPropertyWidget<E> extends AbstractPropertyWidget<E[]> {

    private final Listener<E> _changeListener = (item, selected) -> fireValueChanged();
    private final Map<String, DCCheckBox<E>> _checkBoxes;
    private final ActionListener _selectAllListener = new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e) {
            for (final JCheckBox cb : _checkBoxes.values()) {
                if (cb.isEnabled()) {
                    cb.setSelected(true);
                }
            }
            fireValueChanged();
        }
    };
    private final ActionListener _selectNoneListener = new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e) {
            for (final JCheckBox cb : _checkBoxes.values()) {
                cb.setSelected(false);
            }
            fireValueChanged();
        }
    };
    private final Class<E> _itemClass;
    private final DCPanel _buttonPanel;
    private final DCCheckBox<E> _notAvailableCheckBox;

    public AbstractMultipleCheckboxesPropertyWidget(final ComponentBuilder componentBuilder,
            final ConfiguredPropertyDescriptor propertyDescriptor, final Class<E> itemClass) {
        super(componentBuilder, propertyDescriptor);
        _itemClass = itemClass;
        _checkBoxes = new LinkedHashMap<>();
        setLayout(new VerticalLayout(2));

        _notAvailableCheckBox = new DCCheckBox<>(getNotAvailableText(), false);
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

        final JButton selectAllButton = WidgetFactory.createDefaultButton("Select all");
        selectAllButton.addActionListener(_selectAllListener);
        buttonPanel.add(selectAllButton);

        final JButton selectNoneButton = WidgetFactory.createDefaultButton("Select none");
        selectNoneButton.addActionListener(_selectNoneListener);
        buttonPanel.add(selectNoneButton);
        return buttonPanel;
    }

    public void initialize(final E[] values) {
        if (values != null) {
            // add all registered values
            for (final E item : values) {
                addCheckBox(item, true);
            }
        }

        // add all available checkboxes
        final E[] availableValues = getAvailableValues();
        for (final E item : availableValues) {
            addCheckBox(item, isEnabled(item, values));
        }

        updateVisibility();
    }

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

    protected JCheckBox addCheckBox(final E item, final boolean checked) {
        final String name = getName(item);
        DCCheckBox<E> checkBox = _checkBoxes.get(name);
        if (checkBox != null) {
            checkBox.setSelected(checked);
            return checkBox;
        }
        checkBox = new DCCheckBox<>(name, checked);
        checkBox.setValue(item);
        checkBox.setOpaque(false);
        checkBox.addListener(_changeListener);
        _checkBoxes.put(name, checkBox);
        add(checkBox);

        updateVisibility();
        updateUI();

        return checkBox;
    }

    protected void editCheckBox(final E oldvalue, final E newValue) {
        final String name = getName(oldvalue);
        final DCCheckBox<E> checkBox = _checkBoxes.get(name);
        if (checkBox != null) {
            _checkBoxes.remove(name);
            checkBox.addListener(_changeListener);
            final boolean isSelected = checkBox.isSelected();
            checkBox.setValue(newValue);
            checkBox.setSelected(isSelected, true);
            _checkBoxes.put(name, checkBox);
            add(checkBox);
            // 'fireValueChanged' changes the value in the component builder. Otherwise the listener 
            // is activated at the later point(when the window is opened again) *
            fireValueChanged();
        } else {
            addCheckBox(newValue, true);
        }
        updateVisibility();
        updateUI();
    }

    protected void removeCheckBox(final E item) {
        final DCCheckBox<E> checkBox = _checkBoxes.remove(getName(item));
        if (checkBox != null) {
            remove(checkBox);
        }

        updateVisibility();
    }

    private boolean isEnabled(final E value, final E[] enabledValues) {
        if (enabledValues == null || enabledValues.length == 0) {
            return false;
        }
        for (final E currentValue : enabledValues) {
            if (currentValue.equals(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSet() {
        for (final JCheckBox checkBox : getCheckBoxes()) {
            if (checkBox.isSelected()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public E[] getValue() {
        final List<E> result = new ArrayList<>();
        final Collection<DCCheckBox<E>> checkBoxes = _checkBoxes.values();
        for (final DCCheckBox<E> cb : checkBoxes) {
            if (cb.isSelected()) {
                result.add(cb.getValue());
            }
        }
        @SuppressWarnings("unchecked") final E[] array = (E[]) Array.newInstance(_itemClass, result.size());
        return result.toArray(array);
    }

    @Override
    protected void setValue(final E[] values) {
        // if checkBoxes is empty it means that the value is being set before
        // initializing the widget. This can occur in subclasses and automatic
        // creating of checkboxes should be done.
        if (_checkBoxes.isEmpty() && values != null) {
            for (final E value : values) {
                addCheckBox(value, true);
            }
        }

        // update selections in checkboxes
        for (final DCCheckBox<E> cb : _checkBoxes.values()) {
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
