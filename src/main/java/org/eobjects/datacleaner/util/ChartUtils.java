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
package org.eobjects.datacleaner.util;

import java.awt.BasicStroke;
import java.awt.Stroke;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.UnitType;

public final class ChartUtils {

	private static final Stroke normalStroke = new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	private static final Stroke wideStroke = new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

	private ChartUtils() {
		// prevent instantiation
	}

	public static void applyStyles(JFreeChart chart) {
		chart.getTitle().setFont(WidgetUtils.FONT_HEADER);
		chart.setBackgroundPaint(null);
		chart.setBorderVisible(false);


		final Plot plot = chart.getPlot();
		plot.setInsets(new RectangleInsets(UnitType.ABSOLUTE, 0d, 0d, 0d, 0d));
		plot.setBackgroundPaint(WidgetUtils.BG_COLOR_BRIGHTEST);
		plot.setOutlinePaint(WidgetUtils.BG_COLOR_MEDIUM);
		plot.setOutlineVisible(true);
		if (plot instanceof PiePlot) {
			final PiePlot piePlot = (PiePlot) plot;
			piePlot.setBaseSectionOutlinePaint(WidgetUtils.BG_COLOR_DARK);
			piePlot.setBaseSectionOutlineStroke(normalStroke);
			piePlot.setLabelFont(WidgetUtils.FONT_SMALL);
			piePlot.setLabelBackgroundPaint(WidgetUtils.BG_COLOR_BRIGHT);
			piePlot.setLabelOutlineStroke(normalStroke);
			piePlot.setLabelPaint(WidgetUtils.BG_COLOR_DARK);
			piePlot.setSectionOutlinesVisible(false);
		} else if (plot instanceof XYPlot) {
			final XYPlot xyPlot = (XYPlot) plot;
			
			xyPlot.getDomainAxis().setLabelFont(WidgetUtils.FONT_SMALL);
			xyPlot.getDomainAxis().setTickLabelFont(WidgetUtils.FONT_SMALL);
			xyPlot.getRangeAxis().setLabelFont(WidgetUtils.FONT_SMALL);
			xyPlot.getRangeAxis().setTickLabelFont(WidgetUtils.FONT_SMALL);
			
			
			
			final XYItemRenderer renderer = xyPlot.getRenderer();
			final int seriesCount = xyPlot.getSeriesCount();
			for (int i = 0; i < seriesCount; i++) {
				renderer.setSeriesStroke(i, wideStroke);
			}
		}
	}
}
