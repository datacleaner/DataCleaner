/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
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

import javax.swing.LookAndFeel;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

/**
 * Class that encapsulates all central configuration of look and feel and
 * similar Swing constructs.
 *
 * <p>
 * The visual theme is defined in the FlatLaf properties files in the
 * {@code org.datacleaner.themes} package. Both a light and a dark theme are
 * available; the active theme can be selected at runtime via
 * {@link #setDarkTheme(boolean)} and is persisted by the caller (see the
 * Options dialog) in the user preferences.
 * </p>
 */
public final class LookAndFeelManager {

    /**
     * System property used to select the UI theme ({@link #THEME_LIGHT} or
     * {@link #THEME_DARK}). Set programmatically before initialization.
     */
    public static final String SYSTEM_PROPERTY_THEME = "datacleaner.ui.theme";

    /**
     * Key in the user preferences' additional properties where the theme
     * ({@link #THEME_LIGHT} or {@link #THEME_DARK}) is persisted.
     */
    public static final String USER_PROPERTY_THEME = "ui.theme";

    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";

    private static final Logger logger = LoggerFactory.getLogger(LookAndFeelManager.class);
    private static final LookAndFeelManager instance = new LookAndFeelManager();
    private static final ImageManager imageManager = ImageManager.get();

    private boolean initialized;

    private LookAndFeelManager() {
    }

    /**
     * Gets the singleton instance of LookAndFeelManager.
     *
     * @return
     */
    public static LookAndFeelManager get() {
        return instance;
    }

    /**
     * Gets the singleton instance of LookAndFeelManager.
     *
     * @return
     *
     * @deprecated use {@link #get()} instead
     */
    @Deprecated
    public static LookAndFeelManager getInstance() {
        return get();
    }

    public void init() {
        init(null);
    }

    /**
     * Initializes the look and feel. Subsequent invocations are no-ops (the
     * method is also used as an initialization guard from widget code).
     *
     * @param laf
     *            the look and feel to install. If null, the theme selected via
     *            the {@value #SYSTEM_PROPERTY_THEME} system property is
     *            installed (light by default).
     */
    public void init(final LookAndFeel laf) {
        applyUiScale();

        if (initialized) {
            return;
        }
        initialized = true;

        // make the bundled fonts resolvable by family name for the FlatLaf
        // theme properties (e.g. "defaultFont")
        WidgetUtils.registerFonts();

        // load the DataCleaner theme properties with every FlatLaf theme
        FlatLaf.registerCustomDefaultsSource("org.datacleaner.themes");

        try {
            UIManager.setLookAndFeel(laf != null ? laf : createThemeLaf());
            logger.info("Look and feel set to: {}", UIManager.getLookAndFeel());
        } catch (final UnsupportedLookAndFeelException e) {
            throw new IllegalStateException(e);
        }

        // Explicitly set the DataCleaner design fonts. FlatLaf's "system font
        // policy" would otherwise rescale the theme's defaultFont down to the
        // desktop font size (e.g. 13pt -> ~10pt with default Linux/GNOME or
        // Windows settings), making the whole UI much smaller than
        // DataCleaner's design (13pt Muli). All FlatLaf component fonts
        // (e.g. "TableHeader.font = +bold") derive from "defaultFont", so a
        // single override restores the design sizes. Users can still zoom
        // everything via the datacleaner.ui.scale property.
        UIManager.put("defaultFont", WidgetUtils.FONT_NORMAL);
        UIManager.put("TitledBorder.font", WidgetUtils.FONT_HEADER1);

        // file chooser icons (bundled images, not expressible in theme properties)
        UIManager.put("FileChooser.detailsViewIcon", imageManager.getImageIcon("images/filetypes/view-details.png"));
        UIManager.put("FileChooser.listViewIcon", imageManager.getImageIcon("images/filetypes/view-list.png"));
        UIManager.put("FileChooser.homeFolderIcon", imageManager.getImageIcon("images/filetypes/home-folder.png"));
        UIManager.put("FileChooser.newFolderIcon", imageManager.getImageIcon("images/filetypes/new-folder.png"));
        UIManager.put("FileChooser.upFolderIcon", imageManager.getImageIcon("images/filetypes/parent-folder.png"));

        // tool tip behaviour
        ToolTipManager.sharedInstance().setInitialDelay(100);
        ToolTipManager.sharedInstance().setDismissDelay(10 * 1000);
    }

    /**
     * Gets whether the dark theme is active.
     *
     * @return true if the dark theme is selected (default is false/light).
     */
    public boolean isDarkTheme() {
        return THEME_DARK.equalsIgnoreCase(System.getProperty(SYSTEM_PROPERTY_THEME, THEME_LIGHT));
    }

    /**
     * Switches between the light and dark UI themes at runtime. Installs the
     * corresponding FlatLaf theme and refreshes all open windows. The caller
     * is responsible for persisting the choice.
     *
     * @param dark true to enable the dark theme, false for the light theme.
     */
    public void setDarkTheme(final boolean dark) {
        final String currentTheme = System.getProperty(SYSTEM_PROPERTY_THEME, THEME_LIGHT);
        final String newTheme = dark ? THEME_DARK : THEME_LIGHT;
        if (newTheme.equalsIgnoreCase(currentTheme)) {
            return;
        }

        System.setProperty(SYSTEM_PROPERTY_THEME, newTheme);
        try {
            UIManager.setLookAndFeel(createThemeLaf());
            logger.info("Look and feel set to: {}", UIManager.getLookAndFeel());

            // refresh all components in all windows
            FlatLaf.updateUI();
        } catch (final UnsupportedLookAndFeelException e) {
            throw new IllegalStateException(e);
        }
    }

    private LookAndFeel createThemeLaf() {
        return isDarkTheme() ? new FlatDarkLaf() : new FlatLightLaf();
    }

    /**
     * Applies the UI scale (zoom) configured via the {@code datacleaner.ui.scale}
     * system property, e.g. {@code -Ddatacleaner.ui.scale=150} for 150% zoom.
     * Accepts percentages ({@code 150}), factors ({@code 1.5}) and suffixed
     * values ({@code 150%} / {@code 1.5x}), delegating to FlatLaf's
     * {@code flatlaf.uiScale} mechanism which scales fonts, paddings, icons
     * (FlatLaf UIScale).
     */
    private void applyUiScale() {
        final String scale = System.getProperty("datacleaner.ui.scale");
        if (scale == null || scale.trim().isEmpty() || System.getProperty("flatlaf.uiScale") != null) {
            return;
        }

        String value = scale.trim();
        if (value.indexOf('.') < 0 && !value.endsWith("%") && !value.endsWith("x")) {
            // bare integers (e.g. "150") are percentages
            value = value + "%";
        }
        System.setProperty("flatlaf.uiScale", value);
        logger.info("UI scale set to: {}", value);
    }
}
