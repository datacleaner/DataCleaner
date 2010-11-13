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
package org.eobjects.datacleaner.widgets.tooltip;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JToolTip;

public class DCToolTip extends JToolTip {

	private static final long serialVersionUID = 1L;
	private final JComponent _tooltipComponent;

	public DCToolTip(JComponent owner, JComponent tooltipComponent) {
		super();
		_tooltipComponent = tooltipComponent;
		setComponent(owner);
	}

	public JComponent getTooltipComponent() {
		return _tooltipComponent;
	}

	@Override
	public void paint(Graphics g) {
		System.out.println("tooltip.paint");

		g.setColor(Color.RED);
		g.fillRect(0, 0, 100, 100);
	}
}
