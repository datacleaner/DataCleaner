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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.JTextComponent;

import org.apache.metamodel.util.FileHelper;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.widgets.DarkButtonUI;
import org.datacleaner.widgets.DarkToggleButtonUI;
import org.datacleaner.widgets.DefaultButtonUI;
import org.datacleaner.widgets.DefaultToggleButtonUI;
import org.datacleaner.widgets.PrimaryButtonUI;
import org.datacleaner.widgets.table.DCTablePanel;
import org.datacleaner.windows.ErrorDialog;
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

    public static final float FONT_SIZE_SMALL = 11f;

    private static final Map<String, Font> fonts;

    public static final Font FONT_UBUNTU_PLAIN;
    public static final Font FONT_UBUNTU_BOLD;
    public static final Font FONT_UBUNTU_ITALIC;
    public static final Font FONT_UBUNTU_BOLD_ITALIC;

    public static final Font FONT_OPENSANS_PLAIN;
    public static final Font FONT_OPENSANS_BOLD;
    public static final Font FONT_OPENSANS_ITALIC;
    public static final Font FONT_OPENSANS_BOLD_ITALIC;

    public static final Font FONT_FONTAWESOME;

    static {
        fonts = new HashMap<String, Font>();

        Font[] fontArray = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        for (Font font : fontArray) {
            if (font.isPlain()) {
                fonts.put(font.getName(), font);
            }
        }

        FONT_UBUNTU_PLAIN = createFont("fonts/Ubuntu-R.ttf");
        FONT_UBUNTU_ITALIC = createFont("fonts/Ubuntu-RI.ttf");
        FONT_UBUNTU_BOLD = createFont("fonts/Ubuntu-B.ttf");
        FONT_UBUNTU_BOLD_ITALIC = createFont("fonts/Ubuntu-BI.ttf");

        FONT_OPENSANS_PLAIN = createFont("fonts/OpenSans-Regular.ttf");
        FONT_OPENSANS_ITALIC = createFont("fonts/OpenSans-Italic.ttf");
        FONT_OPENSANS_BOLD = createFont("fonts/OpenSans-Bold.ttf");
        FONT_OPENSANS_BOLD_ITALIC = createFont("fonts/OpenSans-BoldItalic.ttf");
        
        FONT_FONTAWESOME = createFont("fonts/FontAwesome-4.3.0.ttf").deriveFont(14f);

        fonts.put(FONT_UBUNTU_PLAIN.getName(), FONT_UBUNTU_PLAIN);
        fonts.put(FONT_OPENSANS_PLAIN.getName(), FONT_OPENSANS_PLAIN);
    }

    public static final Font FONT_BANNER = FONT_UBUNTU_PLAIN.deriveFont(20f);
    public static final Font FONT_HEADER1 = FONT_UBUNTU_PLAIN.deriveFont(17f);
    public static final Font FONT_HEADER2 = FONT_UBUNTU_PLAIN.deriveFont(15f);
    public static final Font FONT_MONOSPACE = new FontUIResource("Monospaced", Font.PLAIN, 14);
    public static final Font FONT_NORMAL = FONT_OPENSANS_PLAIN.deriveFont(13f);
    public static final Font FONT_SMALL = FONT_OPENSANS_PLAIN.deriveFont(FONT_SIZE_SMALL);
    public static final Font FONT_TABLE_HEADER = FONT_NORMAL.deriveFont(Font.BOLD);

    public static final int SCROLL_UNIT_INCREMENT = 20;

    // blue base color of DC styling (#3b76bc)
    public static final Color BG_COLOR_BLUE_MEDIUM = new ColorUIResource(59, 118, 188);
    public static final Color BG_COLOR_BLUE_BRIGHT = slightlyBrighter(BG_COLOR_BLUE_MEDIUM);
    public static final Color BG_COLOR_BLUE_DARK = slightlyDarker(BG_COLOR_BLUE_MEDIUM);

    // orange base color of DC styling (#ffa800)
    public static final Color BG_COLOR_ORANGE_MEDIUM = new ColorUIResource(225, 168, 0);
    public static final Color BG_COLOR_ORANGE_BRIGHT = slightlyBrighter(BG_COLOR_ORANGE_MEDIUM);
    public static final Color BG_COLOR_ORANGE_DARK = slightlyDarker(BG_COLOR_ORANGE_MEDIUM);

    // white with 10% alpha/opacity
    public static final Color BG_SEMI_TRANSPARENT = new ColorUIResource(new Color(0.0f, 0.0f, 0.0f, 0.05f));

    // pale yellow color which work fine for information/help text fields.
    // #f4f4d3
    public static final Color BG_COLOR_PALE_YELLOW = new ColorUIResource(244, 244, 211);

    // white
    public static final Color BG_COLOR_BRIGHTEST = new ColorUIResource(Color.WHITE);

    // #e1e1e1 (silver-ish)
    public static final Color BG_COLOR_BRIGHT = new ColorUIResource(245, 245, 245);

    // slightly darker than BRIGHT
    public static final Color BG_COLOR_LESS_BRIGHT = new ColorUIResource(220, 220, 220);

    // #a0a0a0
    public static final Color BG_COLOR_MEDIUM = new ColorUIResource(130, 140, 150);

    public static final Color BG_COLOR_LESS_DARK = new ColorUIResource(55, 55, 55);

    public static final Color BG_COLOR_DARK = new ColorUIResource(33, 33, 33);

    public static final Color BG_COLOR_DARKEST = new ColorUIResource(Color.BLACK);

    public static final Color COLOR_DEFAULT_BACKGROUND = BG_COLOR_BRIGHTEST;
    public static final Color COLOR_WELL_BACKGROUND = BG_COLOR_BRIGHT;
    public static final Color COLOR_ALTERNATIVE_BACKGROUND = BG_COLOR_DARK;

    // additional colors, only intended for special widget coloring such as
    // charts etc.
    public static final Color ADDITIONAL_COLOR_GREEN_BRIGHT = new ColorUIResource(123, 207, 38);

    // Red: #d32424
    public static final Color ADDITIONAL_COLOR_RED_BRIGHT = new ColorUIResource(211, 36, 36);

    // Purple: #d3249c
    public static final Color ADDITIONAL_COLOR_PURPLE_BRIGHT = new ColorUIResource(211, 36, 156);

    // Cyan: #24d1d3
    public static final Color ADDITIONAL_COLOR_CYAN_BRIGHT = new ColorUIResource(36, 209, 211);

    public static final int BORDER_WIDE_WIDTH = 4;

    public static final Border BORDER_SHADOW = new DropShadowBorder(WidgetUtils.BG_COLOR_DARK, 6);

    public static final Border BORDER_WIDE_ALTERNATIVE = new LineBorder(COLOR_ALTERNATIVE_BACKGROUND, BORDER_WIDE_WIDTH);
    public static final Border BORDER_WIDE_DEFAULT = new LineBorder(COLOR_DEFAULT_BACKGROUND, BORDER_WIDE_WIDTH);
    public static final Border BORDER_WIDE_WELL = new LineBorder(COLOR_WELL_BACKGROUND, BORDER_WIDE_WIDTH);

    @Deprecated
    public static final Border BORDER_WIDE = BORDER_WIDE_ALTERNATIVE;

    @Deprecated
    public static final Border BORDER_WIDE_BRIGHTEST = BORDER_WIDE_DEFAULT;

    public static final Border BORDER_EMPTY = new EmptyBorder(WidgetUtils.BORDER_WIDE_WIDTH,
            WidgetUtils.BORDER_WIDE_WIDTH, WidgetUtils.BORDER_WIDE_WIDTH, WidgetUtils.BORDER_WIDE_WIDTH);
    public static Border BORDER_TOP_PADDING = new EmptyBorder(10, 0, 0, 0);

    public static final Border BORDER_THIN = new LineBorder(BG_COLOR_LESS_BRIGHT);
    public static final Border BORDER_THIN_DARK = new LineBorder(BG_COLOR_DARK);

    public static final Border BORDER_LIST_ITEM = new CompoundBorder(new MatteBorder(0, 3, 0, 0, BG_COLOR_BLUE_MEDIUM),
            new MatteBorder(0, 0, 1, 0, WidgetUtils.BG_COLOR_LESS_BRIGHT));

    public static final Border BORDER_LIST_ITEM_LEFT_ONLY = new CompoundBorder(new MatteBorder(0, 3, 0, 0,
            BG_COLOR_BLUE_MEDIUM), new EmptyBorder(0, 0, 1, 0));

    public static final Border BORDER_LIST_ITEM_HIGHLIGHTED = new CompoundBorder(new MatteBorder(0, 3, 0, 0,
            BG_COLOR_BLUE_MEDIUM), new MatteBorder(0, 0, 1, 0, WidgetUtils.BG_COLOR_BLUE_MEDIUM));

    public static final Border BORDER_LIST_ITEM_SUBTLE = new CompoundBorder(new MatteBorder(0, 3, 0, 0,
            BG_COLOR_BLUE_MEDIUM), new MatteBorder(0, 0, 1, 0, WidgetUtils.BG_COLOR_MEDIUM));

    public static final Border BORDER_EMPHASIZE_FIELD = new LineBorder(ADDITIONAL_COLOR_RED_BRIGHT, 2, false);
    public static final Border BORDER_INPUT = new CompoundBorder(BORDER_THIN, BORDER_EMPTY);

    public static final Border BORDER_TABLE_PANEL = new MatteBorder(1, 1, 0, 0, BG_COLOR_LESS_BRIGHT);

    public static final Border BORDER_BUTTON_DARK = new EmptyBorder(BORDER_WIDE_WIDTH, 10, BORDER_WIDE_WIDTH, 10);

    public static final Border BORDER_BUTTON_DARK_WITH_LINE = new CompoundBorder(new LineBorder(BG_COLOR_LESS_DARK, 1,
            false), new EmptyBorder(BORDER_WIDE_WIDTH - 1, 9, BORDER_WIDE_WIDTH - 1, 9));

    public static final Border BORDER_BUTTON_DEFAULT = new CompoundBorder(
            new LineBorder(BG_COLOR_LESS_BRIGHT, 1, false), new EmptyBorder(BORDER_WIDE_WIDTH - 1, 9,
                    BORDER_WIDE_WIDTH - 1, 9));
    public static final Border BORDER_BUTTON_PRIMARY = new EmptyBorder(BORDER_WIDE_WIDTH, 10, BORDER_WIDE_WIDTH, 10);

    public static final Object BORDER_MENU_ITEM = new EmptyBorder(2, 2, 2, 2);

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
    public static final int DEFAULT_ANCHOR = GridBagConstraints.WEST;

    /**
     * Commonly used insets - empty
     */
    public static final Insets INSETS_EMPTY = new Insets(0, 0, 0, 0);

    private WidgetUtils() {
        // prevent instantiation
    }

    /**
     * Invokes a {@link Runnable} as soon as possible. If this is the swing
     * event dispatch thread, it will be run now, or else later using
     * {@link SwingUtilities#invokeLater(Runnable)}
     * 
     * @param runnable
     */
    public static void invokeSwingAction(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }

    private static Font createFont(String path) {
        final URL url = ResourceManager.get().getUrl(path);
        if (url == null) {
            throw new IllegalArgumentException("Font resource not found: " + path);
        }

        InputStream in = null;
        try {
            in = url.openStream();
            return Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            FileHelper.safeClose(in);
        }
    }

    public static void centerOnScreen(Component component) {
        final Dimension paneSize = component.getSize();
        final Dimension screenSize = component.getToolkit().getScreenSize();
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
    public static void addToGridBag(Component comp, JPanel panel, int gridx, int gridy, int width, int height,
            int anchor) {
        addToGridBag(comp, panel, gridx, gridy, width, height, anchor, DEFAULT_PADDING);
    }

    public static void addToGridBag(Component comp, JPanel panel, int gridx, int gridy, int width, int height,
            int anchor, int padding) {
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
    public static void addToGridBag(Component comp, JPanel panel, int gridx, int gridy, int width, int height,
            int anchor, int padding, double weightx, double weighty) {
        addToGridBag(comp, panel, gridx, gridy, width, height, anchor, padding, weightx, weighty,
                GridBagConstraints.HORIZONTAL);
    }

    public static void addToGridBag(Component comp, JPanel panel, int gridx, int gridy, int width, int height,
            int anchor, int padding, double weightx, double weighty, int fill) {
        LayoutManager layout = panel.getLayout();
        if (!(layout instanceof GridBagLayout)) {
            layout = new GridBagLayout();
            panel.setLayout(layout);
        }
        final GridBagLayout gridBagLayout = (GridBagLayout) layout;
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = gridx;
        constraints.gridy = gridy;
        constraints.gridwidth = width;
        constraints.gridheight = height;
        constraints.weightx = weightx;
        constraints.weighty = weighty;
        constraints.anchor = anchor;
        constraints.fill = fill;
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

    public static void addToGridBag(Component comp, JPanel panel, int x, int y, int anchor, double weightx,
            double weighty) {
        addToGridBag(comp, panel, x, y, 1, 1, anchor, DEFAULT_PADDING, weightx, weighty);
    }

    /**
     * Adds a component to a panel with a grid bag layout
     * 
     * @param comp
     * @param panel
     * @param gridx
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

    public static void showErrorMessage(final String shortMessage, final String detailedMessage) {
        final String finalDetailedMessage = detailedMessage == null ? "" : detailedMessage;
        final String finalShortMessage = shortMessage == null ? "" : shortMessage;
        final ErrorDialog dialog = new ErrorDialog(finalShortMessage, finalDetailedMessage);

        dialog.setModal(true);
        dialog.open();
    }

    public static void showErrorMessage(final String shortMessage, final String detailedMessage,
            final Throwable exception) {
        final Throwable presentedException = ErrorUtils.unwrapForPresentation(exception);
        if (exception == null) {
            showErrorMessage(shortMessage, detailedMessage);
            return;
        }

        final String finalDetailedMessage = detailedMessage == null ? "" : detailedMessage;
        final String finalShortMessage = shortMessage == null ? "" : shortMessage;

        final ErrorInfo info = new ErrorInfo(finalShortMessage, finalDetailedMessage, null, "error",
                presentedException, ErrorLevel.SEVERE, null);
        final JXErrorPane errorPane = new JXErrorPane();
        errorPane.setErrorInfo(info);

        final JDialog dialog = JXErrorPane.createDialog(null, errorPane);

        centerOnScreen(dialog);
        JXErrorPane.setDefaultLocale(Locale.ENGLISH);
        dialog.setLocale(Locale.ENGLISH);
        dialog.setModal(true);
        dialog.setTitle(finalShortMessage);
        dialog.setVisible(true);
        dialog.toFront();
    }

    public static void showErrorMessage(final String shortMessage, final Throwable exception) {
        StringBuilder sb = new StringBuilder();
        Throwable e = exception;
        while (e != null) {
            if (sb.length() != 0) {
                sb.append("\n\n");
            }
            String message = e.getMessage();
            if (StringUtils.isNullOrEmpty(message)) {
                message = "(" + e.getClass().getName() + ")";
            }
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
        scroll.getVerticalScrollBar().setUnitIncrement(SCROLL_UNIT_INCREMENT);
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
                (int) (color.getGreen() * COLOR_SCALE_FACTOR), 0), Math.max(
                (int) (color.getBlue() * COLOR_SCALE_FACTOR), 0));
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

    public static DCPanel decorateWithShadow(JComponent comp) {
        final boolean outline;
        if (comp instanceof DCTablePanel) {
            // table panels has it's own special outline
            outline = false;
        } else {
            outline = true;
        }

        return decorateWithShadow(comp, outline, 4);
    }

    /**
     * Decorates a JComponent with a nice shadow border. Since not all
     * JComponents handle opacity correctly, they will be wrapped inside a
     * DCPanel, which actually has the border.
     * 
     * @param comp
     * @param outline
     * @param margin
     * @return
     */
    public static DCPanel decorateWithShadow(JComponent comp, boolean outline, int margin) {
        DCPanel panel = new DCPanel();
        panel.setLayout(new BorderLayout());
        Border border = BORDER_SHADOW;
        if (outline) {
            border = new CompoundBorder(border, BORDER_THIN);
        }
        if (margin > 0) {
            border = new CompoundBorder(new EmptyBorder(margin, margin, margin, margin), border);
        }
        panel.setBorder(border);
        panel.add(comp, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Finds a font that is capable of displaying the provided text.
     * 
     * @param text
     *            the text to display.
     * @param fallbackFont
     *            the font to fall back to in case no capable font was found
     * @return a font suitable for displaying the text
     */
    public static Font findCompatibleFont(String text, Font fallbackFont) {
        final String[] searchFonts = new String[] { Font.SANS_SERIF, Font.SERIF, "Verdana", "Arial Unicode MS",
                "MS UI Gothic", "MS Mincho", "MS Gothic", "Osaka" };
        for (String fontName : searchFonts) {
            Font font = fonts.get(fontName);
            if (font == null) {
                font = new Font(fontName, fallbackFont.getStyle(), fallbackFont.getSize());
            }

            if (font.canDisplayUpTo(text) == -1) {
                logger.info("Font '{}' was capable, returning", fontName);
                font = font.deriveFont(fallbackFont.getSize2D());
                return font;
            }
        }

        logger.warn("Didn't find any capable fonts for text '{}'", text);
        return fallbackFont;
    }

    public static void setAppropriateFont(JLabel label) {
        String text = label.getText();
        if (text == null) {
            return;
        }
        Font font = label.getFont();
        int canDisplay = font.canDisplayUpTo(text);
        if (canDisplay != -1) {
            logger.warn("Default font ('{}') was unable to display text ('{}'), searching for alternative.",
                    font.getName(), text);

            // if the label contains undisplayable characters, look for a
            // different font able of displaying the characters.
            font = findCompatibleFont(text, font);
            label.setFont(font);
        }
    }

    public static void setPrimaryButtonStyle(AbstractButton b) {
        b.setUI(PrimaryButtonUI.get());
    }

    public static void setDarkButtonStyle(AbstractButton b) {
        if (b instanceof JToggleButton) {
            b.setUI(DarkToggleButtonUI.get());
        } else {
            b.setUI(DarkButtonUI.get());
        }
    }

    public static void setDefaultButtonStyle(AbstractButton b) {
        if (b instanceof JToggleButton) {
            b.setUI(DefaultToggleButtonUI.get());
        } else {
            b.setUI(DefaultButtonUI.get());
        }
    }
    
    public static void disableComponent(JComponent component) {
        component.setEnabled(false);
        component.setBackground(BG_COLOR_BRIGHT.darker());
        component.setBorder(BORDER_BUTTON_DARK);
    }
    
    public static void enableComponent(JComponent component) {
        component.setEnabled(true);
        component.setBackground(BG_COLOR_BRIGHTEST);
        component.setBorder(BORDER_INPUT);
    }
}
