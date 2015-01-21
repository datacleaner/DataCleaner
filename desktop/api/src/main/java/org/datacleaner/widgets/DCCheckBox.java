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
package org.datacleaner.widgets;

import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetUtils;

/**
 * A checkbox that carries a value, which is convenient for modelling the object
 * that the checkbox represents (such as a Dictionary, a Datastore etc.)
 * 
 * @param <E> the type of element that the checkbox represents
 */
public class DCCheckBox<E> extends JCheckBox implements MouseListener, ItemListener {

	private static final long serialVersionUID = 1L;

	public static interface Listener<E> {
		public void onItemSelected(E item, boolean selected);
	}

	private static final Border HOVER_BORDER = new LineBorder(WidgetUtils.BG_COLOR_LESS_BRIGHT, 1);
	private static final Border REGULAR_BORDER = new EmptyBorder(1, 1, 1, 1);

	private final List<Listener<E>> _listeners = new ArrayList<Listener<E>>();
	private E _value;
	private volatile Border _previousBorder;

	public DCCheckBox(String text, boolean selected) {
		super(text, selected);
		if (!StringUtils.isNullOrEmpty(text)) {
			setBorder(REGULAR_BORDER);
			setBorderPainted(true);
			addMouseListener(this);
		}
		super.addItemListener(this);
	}

	public E getValue() {
		return _value;
	}

	public void setValue(E value) {
		_value = value;
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		notifyListeners();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if (_previousBorder == null) {
			_previousBorder = getBorder();
		}
		setBorder(HOVER_BORDER);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		setBorder(_previousBorder);
		_previousBorder = null;
	}

	/**
	 * Adds a listener as the first in the list of listeners.
	 * 
	 * @param listener
	 */
	public void addListenerToHead(Listener<E> listener) {
		_listeners.add(0, listener);
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

	public void notifyListeners() {
		notifyListeners(getValue(), isSelected());
	}

	private void notifyListeners(E item, boolean selected) {
		// notify listeners
		for (Listener<E> listener : _listeners) {
			listener.onItemSelected(item, selected);
		}
	}

    public void setSelected(boolean selected, boolean notifyListeners) {
        final boolean previous = isSelected();
        if (selected == previous) {
            return;
        }
        setSelected(selected);
        notifyListeners();
    }
}
