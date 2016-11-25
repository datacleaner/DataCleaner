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
package org.datacleaner.util;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Stroke;

import org.datacleaner.widgets.result.DCDrawingSupplier;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAnchor;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PieLabelLinkStyle;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.UnitType;

/**
 * Contains features related to layout and styling of JFreeChart based charts.
 */
public final class ChartUtils {

    /**
     * The threshold value advised to use when considering if there are too many
     * categories/bars/slices for a chart to render or not.
     */
    public static final int CATEGORY_COUNT_DISPLAY_THRESHOLD =
            SystemProperties.getInt("datacleaner.chart.categories.threshold", 1000);
    public static final int WIDTH_WIDE = 900;
    public static final int WIDTH_NORMAL = 600;
    public static final int HEIGHT_WIDE = 400;
    public static final int HEIGHT_NORMAL = 600;
    private static final Stroke STROKE_NORMAL = new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final Stroke STROKE_WIDE = new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    private ChartUtils() {
        // prevent instantiation
    }

    public static ChartPanel createPanel(final JFreeChart chart, final boolean wide) {
        if (wide) {
            return createPanel(chart, WIDTH_WIDE, HEIGHT_WIDE);
        } else {
            return createPanel(chart, WIDTH_NORMAL, HEIGHT_NORMAL);
        }
    }

    public static ChartPanel createPanel(final JFreeChart chart, final int width, final int height) {
        final ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new Dimension(width, height));
        return panel;
    }

    public static void applyStyles(final JFreeChart chart) {
        final TextTitle title = chart.getTitle();
        if (title != null) {
            title.setFont(WidgetUtils.FONT_HEADER1);
            title.setBackgroundPaint(WidgetUtils.BG_COLOR_BRIGHTEST);
        }

        for (int i = 0; i < chart.getSubtitleCount(); i++) {
            final Title subtitle = chart.getSubtitle(i);
            if (subtitle instanceof TextTitle) {
                ((TextTitle) subtitle).setFont(WidgetUtils.FONT_NORMAL);
            }
        }

        final LegendTitle legend = chart.getLegend();
        if (legend != null) {
            legend.setItemFont(WidgetUtils.FONT_SMALL);
        }

        // transparent background
        chart.setBackgroundPaint(WidgetUtils.BG_COLOR_BRIGHTEST);
        chart.setBorderVisible(false);

        final Plot plot = chart.getPlot();
        plot.setInsets(new RectangleInsets(UnitType.ABSOLUTE, 0d, 0d, 0d, 0d));
        plot.setBackgroundPaint(WidgetUtils.BG_COLOR_BRIGHTEST);
        plot.setOutlinePaint(WidgetUtils.BG_COLOR_BRIGHTEST);
        plot.setOutlineVisible(true);

        if (plot instanceof PiePlot) {
            // tweaks for pie charts
            final PiePlot piePlot = (PiePlot) plot;
            piePlot.setBaseSectionOutlinePaint(WidgetUtils.BG_COLOR_DARK);
            piePlot.setBaseSectionOutlineStroke(STROKE_NORMAL);
            piePlot.setLabelFont(WidgetUtils.FONT_SMALL);
            piePlot.setLabelBackgroundPaint(WidgetUtils.BG_COLOR_BRIGHT);
            piePlot.setLabelOutlineStroke(STROKE_NORMAL);
            piePlot.setLabelPaint(WidgetUtils.BG_COLOR_DARK);
            piePlot.setSectionOutlinesVisible(false);
            piePlot.setLabelLinkStyle(PieLabelLinkStyle.QUAD_CURVE);
            piePlot.setDrawingSupplier(new DCDrawingSupplier());

        } else if (plot instanceof CategoryPlot) {
            // tweaks for bar charts
            final CategoryPlot categoryPlot = (CategoryPlot) plot;

            final int columnCount = categoryPlot.getDataset().getColumnCount();
            if (columnCount > 1) {
                categoryPlot.setDomainGridlinesVisible(true);
            } else {
                categoryPlot.setDomainGridlinesVisible(false);
            }
            categoryPlot.setDomainGridlinePaint(WidgetUtils.BG_COLOR_DARK);
            categoryPlot.setDomainGridlinePosition(CategoryAnchor.END);

            categoryPlot.getDomainAxis().setLabelFont(WidgetUtils.FONT_SMALL);
            categoryPlot.getDomainAxis().setTickLabelFont(WidgetUtils.FONT_SMALL);
            categoryPlot.getRangeAxis().setLabelFont(WidgetUtils.FONT_SMALL);
            categoryPlot.getRangeAxis().setTickLabelFont(WidgetUtils.FONT_SMALL);
            categoryPlot.setDrawingSupplier(new DCDrawingSupplier());

            final CategoryItemRenderer renderer = categoryPlot.getRenderer();
            renderer.setBaseOutlinePaint(WidgetUtils.BG_COLOR_DARK);
            renderer.setBaseOutlineStroke(STROKE_WIDE);

            if (renderer instanceof BarRenderer) {
                final BarRenderer barRenderer = (BarRenderer) renderer;
                barRenderer.setShadowPaint(WidgetUtils.BG_COLOR_BRIGHT);
                barRenderer.setBarPainter(new StandardBarPainter());
            }

        } else if (plot instanceof XYPlot) {
            // tweaks for line charts
            final XYPlot xyPlot = (XYPlot) plot;

            xyPlot.setDrawingSupplier(new DCDrawingSupplier());

            xyPlot.getDomainAxis().setLabelFont(WidgetUtils.FONT_SMALL);
            xyPlot.getDomainAxis().setTickLabelFont(WidgetUtils.FONT_SMALL);
            xyPlot.getRangeAxis().setLabelFont(WidgetUtils.FONT_SMALL);
            xyPlot.getRangeAxis().setTickLabelFont(WidgetUtils.FONT_SMALL);

            final XYItemRenderer renderer = xyPlot.getRenderer();
            final int seriesCount = xyPlot.getSeriesCount();
            for (int i = 0; i < seriesCount; i++) {
                renderer.setSeriesStroke(i, STROKE_WIDE);
            }
        }
    }
}
