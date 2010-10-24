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

import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.error.ErrorInfo;
import org.jdesktop.swingx.error.ErrorLevel;

public final class WidgetUtils {

	public static final Font FONT_HEADER = new Font("Sans", Font.BOLD, 15);
	public static final Font FONT_MONOSPACE = new Font("Monospaced", Font.PLAIN, 14);
	public static final Font FONT_NORMAL = new Font("Sans", Font.PLAIN, 12);

	// #f0f0ff
	public static final Color BG_COLOR_BRIGHT = new ColorUIResource(240, 240, 255);

	// slightly darker than LIGHT
	public static final Color BG_COLOR_LESS_BRIGHT = new ColorUIResource(220, 220, 220);

	// white
	public static final Color BG_COLOR_BRIGHTEST = ColorUIResource.WHITE;

	// white
	public static final Color BG_COLOR_MEDIUM = new ColorUIResource(186, 190, 200);

	// #353f48 (brownish dark gray)
	public static final Color BG_COLOR_DARK = new ColorUIResource(53, 63, 72);

	// #5d656d
	public static final Color BG_COLOR_LESS_DARK = new ColorUIResource(93, 101, 109);

	// #2a323a (more brownish dark gray)
	public static final Color BG_COLOR_DARKEST = new ColorUIResource(42, 50, 58);

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

	public static JScrollPane scrolleable(JComponent comp) {
		JScrollPane scroll = new JScrollPane(comp);
		scroll.setOpaque(false);
		scroll.getViewport().setOpaque(false);
		return scroll;
	}
}
