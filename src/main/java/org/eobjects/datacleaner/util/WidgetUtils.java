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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.JTextComponent;

import org.eobjects.analyzer.util.StringUtils;
import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.border.DropShadowBorder;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.error.ErrorInfo;
import org.jdesktop.swingx.error.ErrorLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class WidgetUtils {

	private static final Logger logger = LoggerFactory.getLogger(WidgetUtils.class);

	public static final Font FONT_BANNER = new FontUIResource("Trebuchet MS", Font.PLAIN, 25);
	public static final Font FONT_HEADER = new FontUIResource("Trebuchet MS", Font.BOLD, 15);
	public static final Font FONT_TABLE_HEADER = new FontUIResource("Trebuchet MS", Font.BOLD, 13);
	public static final Font FONT_MONOSPACE = new FontUIResource("Monospaced", Font.PLAIN, 14);
	public static final Font FONT_NORMAL = new FontUIResource("LucidaSans", Font.PLAIN, 12);
	public static final Font FONT_SMALL = new FontUIResource("LucidaSans", Font.PLAIN, 10);

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

	public static final Color BG_COLOR_MEDIUM = new ColorUIResource(150, 150, 150);

	public static final Color BG_COLOR_LESS_DARK = new ColorUIResource(95, 95, 95);

	public static final Color BG_COLOR_DARK = new ColorUIResource(70, 70, 70);

	public static final Color BG_COLOR_DARKEST = new ColorUIResource(40, 40, 40);

	// additional colors, only intended for special widget coloring such as
	// charts etc.
	public static final Color ADDITIONAL_COLOR_GREEN_BRIGHT = new ColorUIResource(175, 229, 123);
	public static final Color ADDITIONAL_COLOR_RED_BRIGHT = new ColorUIResource(221, 84, 84);

	public static final int BORDER_WIDE_WIDTH = 4;

	public static final Border BORDER_SHADOW = new DropShadowBorder(WidgetUtils.BG_COLOR_DARK, 6);
	public static final Border BORDER_WIDE = new LineBorder(BG_COLOR_DARK, BORDER_WIDE_WIDTH);
	public static final Border BORDER_EMPTY = new EmptyBorder(WidgetUtils.BORDER_WIDE_WIDTH, WidgetUtils.BORDER_WIDE_WIDTH,
			WidgetUtils.BORDER_WIDE_WIDTH, WidgetUtils.BORDER_WIDE_WIDTH);
	public static final Border BORDER_THIN = new LineBorder(BG_COLOR_MEDIUM);
	public static final Border BORDER_LIST_ITEM = new MatteBorder(0, 2, 1, 0, WidgetUtils.BG_COLOR_MEDIUM);

	/**
	 * A highlighter for coloring odd/even rows in a table
	 */
	public static final Highlighter LIBERELLO_HIGHLIGHTER = HighlighterFactory.createAlternateStriping(
			colorBetween(BG_COLOR_BRIGHTEST, BG_COLOR_BRIGHT), BG_COLOR_BRIGHTEST);

	/**
	 * Slightly moderated version of COLOR.FACTOR
	 */
	private static final double COLOR_SCALE_FACTOR = 0.9;

	// grid bag contraint defaults
	public static final int DEFAULT_PADDING = 2;
	public static final int DEFAULT_ANCHOR = GridBagConstraints.NORTHWEST;

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
	public static void addToGridBag(Component comp, JPanel panel, int gridx, int gridy, int width, int height, int anchor) {
		addToGridBag(comp, panel, gridx, gridy, width, height, anchor, DEFAULT_PADDING);
	}

	public static void addToGridBag(Component comp, JPanel panel, int gridx, int gridy, int width, int height, int anchor,
			int padding) {
		addToGridBag(comp, panel, gridx, gridy, width, height, anchor, padding, 0.0, 0.0);
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
	public static void addToGridBag(Component comp, JPanel panel, int gridx, int gridy, int width, int height, int anchor,
			int padding, double weightx, double weighty) {
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
		constraints.weightx = weightx;
		constraints.weighty = weighty;
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
	public static void addToGridBag(Component comp, JPanel panel, int gridx, int gridy, int width, int height) {
		addToGridBag(comp, panel, gridx, gridy, width, height, DEFAULT_ANCHOR);
	}

	public static void addToGridBag(Component comp, JPanel panel, int x, int y, int anchor, double weightx, double weighty) {
		addToGridBag(comp, panel, x, y, 1, 1, anchor, DEFAULT_PADDING, weightx, weighty);
	}

	/**
	 * Adds a component to a panel with a grid bag layout
	 * 
	 * @param comp
	 * @param panel
	 * @param gridxs
	 * @param gridy
	 */
	public static void addToGridBag(Component comp, JPanel panel, int gridx, int gridy) {
		addToGridBag(comp, panel, gridx, gridy, 1, 1);
	}

	public static void addToGridBag(Component comp, JPanel panel, int gridx, int gridy, double weightx, double weighty) {
		addToGridBag(comp, panel, gridx, gridy, 1, 1, DEFAULT_ANCHOR, DEFAULT_PADDING, weightx, weighty);
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
	public static void addToGridBag(Component comp, JPanel panel, int gridx, int gridy, int anchor) {
		addToGridBag(comp, panel, gridx, gridy, 1, 1, anchor);
	}

	public static void addAligned(Container container, JComponent component) {
		component.setAlignmentX(Component.LEFT_ALIGNMENT);
		component.setAlignmentY(Component.TOP_ALIGNMENT);
		container.add(component);
	}

	public static void showErrorMessage(final String shortMessage, final String detailedMessage, final Throwable exception) {
		JXErrorPane.setDefaultLocale(Locale.ENGLISH);
		final JXErrorPane errorPane = new JXErrorPane();
		final ErrorInfo info = new ErrorInfo(shortMessage, detailedMessage, null, "error", exception, ErrorLevel.SEVERE,
				null);
		errorPane.setErrorInfo(info);
		final JDialog dialog = JXErrorPane.createDialog(null, errorPane);
		centerOnScreen(dialog);
		dialog.setLocale(Locale.ENGLISH);
		dialog.setModal(true);
		dialog.setTitle(shortMessage);
		dialog.setVisible(true);
	}

	public static void showErrorMessage(final String shortMessage, final Throwable exception) {
		StringBuilder sb = new StringBuilder();
		Throwable e = exception;
		while (e != null) {
			if (sb.length() != 0) {
				sb.append("\n\n");
			}
			String message = e.getMessage();
			sb.append(message);
			e = e.getCause();
		}
		showErrorMessage(shortMessage, sb.toString(), exception);
	}

	public static JScrollPane scrolleable(final JComponent comp) {
		final JScrollPane scroll = new JScrollPane();
		if (comp != null) {
			scroll.setViewportView(comp);
		}
		scroll.setOpaque(false);
		scroll.getViewport().setOpaque(false);
		return scroll;
	}

	/**
	 * Creates a color that is in between two colors, in terms of RGB balance.
	 * 
	 * @param c1
	 * @param c2
	 * @return
	 */
	public static Color colorBetween(Color c1, Color c2) {
		int red = (c1.getRed() + c2.getRed()) / 2;
		int green = (c1.getGreen() + c2.getGreen()) / 2;
		int blue = (c1.getBlue() + c2.getBlue()) / 2;
		return new Color(red, green, blue);
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

	public static String extractText(Component comp) {
		if (comp instanceof JLabel) {
			return ((JLabel) comp).getText();
		} else if (comp instanceof JTextComponent) {
			return ((JTextComponent) comp).getText();
		} else if (comp instanceof Container) {
			Component[] children = ((Container) comp).getComponents();
			StringBuilder sb = new StringBuilder();
			for (Component child : children) {
				String text = extractText(child);
				if (!StringUtils.isNullOrEmpty(text)) {
					if (sb.length() > 0) {
						sb.append(' ');
					}
					sb.append(text);
				}
			}
			return sb.toString();
		}

		logger.warn("Could not extract text from component: {}", comp);
		return "";
	}
}
