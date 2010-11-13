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
package org.eobjects.datacleaner.widgets.builder;

import java.awt.Dimension;

import javax.swing.JComponent;

public class WidgetBuilder<E extends JComponent> {

	protected E _component;

	public WidgetBuilder(E component) {
		_component = component;
	}

	public E toComponent() {
		return _component;
	}

	public WidgetBuilder<E> applySize(Dimension d) {
		_component.setSize(d);
		_component.setPreferredSize(d);
		return this;
	}

	public WidgetBuilder<E> applySize(Integer width, Integer height) {
		Dimension d = new Dimension();
		if (width != null) {
			d.width = width;
		}
		if (height != null) {
			d.height = height;
		}
		return applySize(d);
	}

	public WidgetBuilder<E> applyTooltip(String text) {
		_component.setToolTipText(text);
		return this;
	}
}