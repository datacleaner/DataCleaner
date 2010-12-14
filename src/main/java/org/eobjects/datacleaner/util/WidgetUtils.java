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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.error.ErrorInfo;
import org.jdesktop.swingx.error.ErrorLevel;

public final class WidgetUtils {

	public static final Font FONT_BANNER = new FontUIResource("Trebuchet MS", Font.PLAIN, 25);
	public static final Font FONT_HEADER = new FontUIResource("Trebuchet MS", Font.BOLD, 15);
	public static final Font FONT_MONOSPACE = new FontUIResource("Monospaced", Font.PLAIN, 14);
	public static final Font FONT_NORMAL = new FontUIResource("SansSerif", Font.PLAIN, 12);
	public static final Font FONT_SMALL = new FontUIResource("SansSerif", Font.PLAIN, 10);

	// the three blue variants in the DataCleaner logo (#5594dd, #235da0,
	// #023a7c)
	public static final Color BG_COLOR_BLUE_BRIGHT = new ColorUIResource(85, 148, 221);
	public static final Color BG_COLOR_BLUE_MEDIUM = new ColorUIResource(35, 93, 160);
	public static final Color BG_COLOR_BLUE_DARK = new ColorUIResource(2, 58, 124);

	// the three orange/yellow/brown variants in the DataCleaner logo
	public static final Color BG_COLOR_ORANGE_BRIGHT = new ColorUIResource(255, 168, 0);
	public static final Color BG_COLOR_ORANGE_MEDIUM = new ColorUIResource(225, 102, 5);
	public static final Color BG_COLOR_ORANGE_DARK = new ColorUIResource(168, 99, 15);

	// #e1e1e1 (silver-ish)
	public static final Color BG_COLOR_BRIGHT = new ColorUIResource(225, 225, 225);

	// slightly darker than BRIGHT
	public static final Color BG_COLOR_LESS_BRIGHT = new ColorUIResource(210, 210, 210);

	// white
	public static final Color BG_COLOR_BRIGHTEST = ColorUIResource.WHITE;

	// #353f48 (brownish dark gray)
	public static final Color BG_COLOR_DARK = new ColorUIResource(53, 63, 72);

	public static final Color BG_COLOR_MEDIUM = new ColorUIResource(150, 150, 150);

	// #5d656d (less brownish dark gray)
	public static final Color BG_COLOR_LESS_DARK = new ColorUIResource(93, 101, 109);

	// #2a323a (more brownish dark gray)
	public static final Color BG_COLOR_DARKEST = new ColorUIResource(42, 50, 58);

	// additional colors, only intended for special widget coloring such as
	// charts etc.
	public static final Color ADDITIONAL_COLOR_GREEN_BRIGHT = new ColorUIResource(175, 229, 123);
	public static final Color ADDITIONAL_COLOR_RED_BRIGHT = new ColorUIResource(221, 84, 84);

	public static final int BORDER_WIDE_WIDTH = 4;

	public static final Border BORDER_WIDE = new LineBorder(BG_COLOR_DARK, BORDER_WIDE_WIDTH);
	public static final Border BORDER_EMPTY = new EmptyBorder(WidgetUtils.BORDER_WIDE_WIDTH, WidgetUtils.BORDER_WIDE_WIDTH,
			WidgetUtils.BORDER_WIDE_WIDTH, WidgetUtils.BORDER_WIDE_WIDTH);
	public static final Border BORDER_THIN = new LineBorder(BG_COLOR_DARK);

	/**
	 * A highlighter for coloring odd/even rows in a table
	 */
	public static final Highlighter LIBERELLO_HIGHLIGHTER = HighlighterFactory.createAlternateStriping(BG_COLOR_BRIGHT,
			BG_COLOR_BRIGHTEST);

	/**
	 * Slightly moderated version of COLOR.FACTOR
	 */
	private static final double COLOR_SCALE_FACTOR = 0.9;

	private WidgetUtils() {
		// prevent instantiation
	}

	public static void centerOnScreen(Component component) {
		Dimension paneSize = component.getSize();
		Dimension screenSize = component.getToolkit().getScreenSize();
		component.setLocation((screenSize.width - paneSize.width) / 2, (screenSize.height - paneSize.height) / 2);
	}

	/**
	 * Adds a component to a panel with a grid bag layout
	 * 
	 * @param comp
	 * @param panel
	 * @param gridx
	 * @param gridy
	 * @param width
	 * @param height
	 * @param anchor
	 */
	public static void addToGridBag(JComponent comp, JPanel panel, int gridx, int gridy, int width, int height, int anchor) {
		addToGridBag(comp, panel, gridx, gridy, width, height, anchor, 2);
	}

	/**
	 * Adds a component to a panel with a grid bag layout
	 * 
	 * @param comp
	 * @param panel
	 * @param gridx
	 * @param gridy
	 * @param width
	 * @param height
	 * @param anchor
	 * @param padding
	 */
	public static void addToGridBag(JComponent comp, JPanel panel, int gridx, int gridy, int width, int height, int anchor,
			int padding) {
		LayoutManager layout = panel.getLayout();
		if (!(layout instanceof GridBagLayout)) {
			layout = new GridBagLayout();
			panel.setLayout(layout);
		}
		GridBagLayout gridBagLayout = (GridBagLayout) layout;
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = gridx;
		constraints.gridy = gridy;
		constraints.gridwidth = width;
		constraints.gridheight = height;
		constraints.anchor = anchor;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(padding, padding, padding, padding);
		gridBagLayout.addLayoutComponent(comp, constraints);
		panel.add(comp);
	}

	/**
	 * Adds a component to a panel with a grid bag layout
	 * 
	 * @param comp
	 * @param panel
	 * @param gridx
	 * @param gridy
	 * @param width
	 * @param height
	 */
	public static void addToGridBag(JComponent comp, JPanel panel, int gridx, int gridy, int width, int height) {
		addToGridBag(comp, panel, gridx, gridy, width, height, GridBagConstraints.NORTHWEST);
	}

	/**
	 * Adds a component to a panel with a grid bag layout
	 * 
	 * @param comp
	 * @param panel
	 * @param gridx
	 * @param gridy
	 */
	public static void addToGridBag(JComponent comp, JPanel panel, int gridx, int gridy) {
		addToGridBag(comp, panel, gridx, gridy, 1, 1);
	}

	/**
	 * Adds a component to a panel with a grid bag layout
	 * 
	 * @param comp
	 * @param panel
	 * @param gridx
	 * @param gridy
	 * @param anchor
	 */
	public static void addToGridBag(JComponent comp, JPanel panel, int gridx, int gridy, int anchor) {
		addToGridBag(comp, panel, gridx, gridy, 1, 1, anchor);
	}

	public static void addAligned(Container container, JComponent component) {
		component.setAlignmentX(Component.LEFT_ALIGNMENT);
		component.setAlignmentY(Component.TOP_ALIGNMENT);
		container.add(component);
	}

	public static void showErrorMessage(String shortMessage, String detailedMessage, Throwable exception) {
		JXErrorPane.setDefaultLocale(Locale.ENGLISH);
		JXErrorPane errorPane = new JXErrorPane();
		ErrorInfo info = new ErrorInfo(shortMessage, detailedMessage, null, "error", exception, ErrorLevel.SEVERE, null);
		errorPane.setErrorInfo(info);
		JDialog dialog = JXErrorPane.createDialog(WindowManager.getInstance().getMainWindow(), errorPane);
		dialog.setTitle(shortMessage);
		dialog.setVisible(true);
	}

	public static JScrollPane scrolleable(final JComponent comp) {
		final JScrollPane scroll = new JScrollPane(comp);
		scroll.setOpaque(false);
		scroll.getViewport().setOpaque(false);
		return scroll;
	}

	/**
	 * Moderated version of Color.darker()
	 * 
	 * @param color
	 * @return
	 */
	public static Color slightlyDarker(Color color) {
		return new Color(Math.max((int) (color.getRed() * COLOR_SCALE_FACTOR), 0), Math.max(
				(int) (color.getGreen() * COLOR_SCALE_FACTOR), 0), Math.max((int) (color.getBlue() * COLOR_SCALE_FACTOR), 0));
	}

	/**
	 * Moderated version of Color.brighter()
	 * 
	 * @param color
	 * @return
	 */
	public static Color slightlyBrighter(Color color) {
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();

		/*
		 * From 2D group: 1. black.brighter() should return grey 2. applying
		 * brighter to blue will always return blue, brighter 3. non pure color
		 * (non zero rgb) will eventually return white
		 */
		int i = (int) (1.0 / (1.0 - COLOR_SCALE_FACTOR));
		if (r == 0 && g == 0 && b == 0) {
			return new Color(i, i, i);
		}

		if (r > 0 && r < i)
			r = i;
		if (g > 0 && g < i)
			g = i;
		if (b > 0 && b < i)
			b = i;

		return new Color(Math.min((int) (r / COLOR_SCALE_FACTOR), 255), Math.min((int) (g / COLOR_SCALE_FACTOR), 255),
				Math.min((int) (b / COLOR_SCALE_FACTOR), 255));
	}
}
