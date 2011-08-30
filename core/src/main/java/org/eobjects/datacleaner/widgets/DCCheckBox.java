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

import javax.swing.JCheckBox;

/**
 * A checkbox that carries a value, which is convenient for modelling the object
 * that the checkbox represents (such as a Dictionary, a Datastore etc.)
 * 
 * @author Kasper Sørensen
 * 
 * @param <E>
 */
public class DCCheckBox<E> extends JCheckBox {

	private static final long serialVersionUID = 1L;

	private E _value;

	public DCCheckBox(String text, boolean selected) {
		super(text, selected);
	}

	public E getValue() {
		return _value;
	}

	public void setValue(E value) {
		_value = value;
	}
}
