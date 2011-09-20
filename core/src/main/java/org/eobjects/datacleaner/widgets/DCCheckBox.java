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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JCheckBox;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.util.WidgetUtils;

/**
 * A checkbox that carries a value, which is convenient for modelling the object
 * that the checkbox represents (such as a Dictionary, a Datastore etc.)
 * 
 * @author Kasper Sørensen
 * 
 * @param <E>
 */
public class DCCheckBox<E> extends JCheckBox implements MouseListener {

	private static final long serialVersionUID = 1L;

	private static final Border HOVER_BORDER = new LineBorder(WidgetUtils.BG_COLOR_LESS_BRIGHT, 1);
	private static final Border REGULAR_BORDER = new EmptyBorder(1, 1, 1, 1);

	private E _value;

	private volatile Border _previousBorder;

	public DCCheckBox(String text, boolean selected) {
		super(text, selected);
		if (!StringUtils.isNullOrEmpty(text)) {
			setBorder(REGULAR_BORDER);
			setBorderPainted(true);
			addMouseListener(this);
		}
	}

	public E getValue() {
		return _value;
	}

	public void setValue(E value) {
		_value = value;
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
}
