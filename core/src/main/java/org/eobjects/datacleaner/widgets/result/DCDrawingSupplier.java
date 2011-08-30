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
package org.eobjects.datacleaner.widgets.result;

import java.awt.Color;
import java.awt.Paint;

import org.eobjects.datacleaner.util.WidgetUtils;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.DrawingSupplier;

public class DCDrawingSupplier extends DefaultDrawingSupplier implements DrawingSupplier {

	private static final long serialVersionUID = 1L;

	public static final Color[] DEFAULT_FILL_COLORS = new Color[] { WidgetUtils.ADDITIONAL_COLOR_GREEN_BRIGHT,
			WidgetUtils.ADDITIONAL_COLOR_RED_BRIGHT, WidgetUtils.BG_COLOR_BLUE_BRIGHT, WidgetUtils.BG_COLOR_ORANGE_BRIGHT,
			WidgetUtils.ADDITIONAL_COLOR_PURPLE_BRIGHT, WidgetUtils.ADDITIONAL_COLOR_CYAN_BRIGHT };

	private volatile int _colorIndex;
	private final Color[] _fillColors;

	public DCDrawingSupplier() {
		this(DEFAULT_FILL_COLORS);
	}

	public DCDrawingSupplier(Color... fillColors) {
		_fillColors = fillColors;
		_colorIndex = 0;
	}

	@Override
	public Paint getNextPaint() {
		if (_colorIndex >= _fillColors.length) {
			_colorIndex = 0;
		}
		Color color = _fillColors[_colorIndex];
		_colorIndex++;
		return color;
	}
}
