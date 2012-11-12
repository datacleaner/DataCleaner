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
package org.eobjects.datacleaner.widgets;

import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/**
 * Defines a common combobox class that has a more convenient listening
 * mechanism (the JComboBox can be quite confusing because of the cycle of
 * {@link ActionListener}s and {@link ItemListener}s).
 * 
 * @author Kasper Sørensen
 * 
 * @param <E>
 *            the type of element in the combo
 */
public class DCComboBox<E> extends JComboBox implements ItemListener {

    private static final long serialVersionUID = 1L;

    public static interface Listener<E> {
        public void onItemSelected(E item);
    }

    private final List<Listener<E>> _listeners = new ArrayList<Listener<E>>();

    public DCComboBox() {
        this(new DefaultComboBoxModel());
    }

    public DCComboBox(Collection<E> items) {
        this(new DefaultComboBoxModel(items.toArray()));
    }

    public DCComboBox(E[] items) {
        this(new DefaultComboBoxModel(items));
    }

    public DCComboBox(ComboBoxModel model) {
        super(model);
        super.addItemListener(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public E getSelectedItem() {
        return (E) super.getSelectedItem();
    }

    @Override
    public void setSelectedItem(Object newItem) {
        final E previousItem = getSelectedItem();
        if (previousItem == newItem) {
            return;
        }

        @SuppressWarnings("unchecked")
        E item = (E) newItem;

        // super.setSelectedItem(...) will notify all listeners (through of the
        // item listener)
        super.setSelectedItem(item);
    }

    public void addListener(Listener<E> listener) {
        _listeners.add(listener);
    }

    public void removeListener(Listener<E> listener) {
        _listeners.remove(listener);
    }

    /**
     * @deprecated use {@link #addListener(Listener)} instead
     */
    @Deprecated
    @Override
    public void addItemListener(ItemListener aListener) {
        super.addItemListener(aListener);
    }

    /**
     * @deprecated use {@link #addListener(Listener)} instead
     */
    @Deprecated
    @Override
    public void addActionListener(ActionListener l) {
        super.addActionListener(l);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        final int stateChange = e.getStateChange();
        if (stateChange == ItemEvent.SELECTED) {
            @SuppressWarnings("unchecked")
            final E newItem = (E) e.getItem();
            notifyListeners(newItem);
        } else if (stateChange == ItemEvent.DESELECTED && getSelectedItem() == null) {
            // special case of selecting a "null" value. Even though "null" can
            // be added to the model of a combo, it does not get a SELECTED
            // event when chosen.
            notifyListeners(null);
        }
    }

    public void notifyListeners() {
        notifyListeners(getSelectedItem());
    }

    private void notifyListeners(E item) {
        // notify listeners
        for (Listener<E> listener : _listeners) {
            listener.onItemSelected(item);
        }
    }
}
