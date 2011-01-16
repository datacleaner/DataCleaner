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

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JProgressBar;

import org.eobjects.datacleaner.util.WidgetUtils;

public class DCProgressBar extends JProgressBar {

	private static final long serialVersionUID = 1L;

	public DCProgressBar(int min, int max) {
		super(min, max);
		setMinimumSize(new Dimension(10, 30));
		setMaximumSize(new Dimension(1000, 30));
		setOpaque(false);
	}

	public DCProgressBar() {
		this(0, 100);
	}

	@Override
	public void paint(Graphics g) {
		final int width = getWidth();
		final int height = getHeight();

		if (isOpaque()) {
			g.setColor(WidgetUtils.BG_COLOR_DARK);
			g.fillRect(0, 0, width, height);
		}

		final int minimum = getMinimum();
		final int adjustedMax = getMaximum() - minimum;
		final int adjustedValue = getValue() - minimum;

		final int barWidth = (int) (width * adjustedValue * 1.0 / adjustedMax);

		if (barWidth > 0) {
			g.setColor(WidgetUtils.BG_COLOR_BLUE_BRIGHT);
			g.fillRect(0, 0, barWidth, height / 2);
			g.setColor(WidgetUtils.slightlyDarker(WidgetUtils.BG_COLOR_BLUE_BRIGHT));
			g.fillRect(0, height / 2, barWidth, height / 2);

			g.setColor(WidgetUtils.slightlyBrighter(WidgetUtils.BG_COLOR_BLUE_BRIGHT));
			g.drawRect(0, 0, barWidth, height);
		}
	}
}
