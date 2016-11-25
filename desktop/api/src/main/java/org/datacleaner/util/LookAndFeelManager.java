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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.LookAndFeel;
import javax.swing.PopupFactory;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import org.datacleaner.widgets.DCComboBoxUI;
import org.datacleaner.widgets.DCScrollBarUI;
import org.datacleaner.widgets.tooltip.DCPopupFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoodies.looks.LookUtils;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;

/**
 * Class that encapsulates all central configuration of look and feel and
 * similar Swing constructs.
 */
public final class LookAndFeelManager {

    private static final Logger logger = LoggerFactory.getLogger(LookAndFeelManager.class);
    private static final LookAndFeelManager instance = new LookAndFeelManager();
    private static final ImageManager imageManager = ImageManager.get();

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
        try {
            final LookAndFeel laf = new PlasticXPLookAndFeel();
            UIManager.setLookAndFeel(laf);
            logger.info("Look and feel set to: {}", UIManager.getLookAndFeel());
        } catch (final UnsupportedLookAndFeelException e) {
            throw new IllegalStateException(e);
        }

        UIManager.put("ClassLoader", LookUtils.class.getClassLoader());

        final Set<Object> propertyKeys = UIManager.getLookAndFeelDefaults().keySet();

        for (final Object propertyKey : propertyKeys) {
            if (propertyKey instanceof String) {
                final String str = (String) propertyKey;

                if (str.endsWith(".font")) {
                    // set default font
                    UIManager.put(propertyKey, WidgetUtils.FONT_NORMAL);
                } else if (str.endsWith(".background")) {
                    // set default background color
                    UIManager.put(propertyKey, WidgetUtils.COLOR_DEFAULT_BACKGROUND);
                }
            }
        }

        ToolTipManager.sharedInstance().setInitialDelay(500);
        PopupFactory.setSharedInstance(new DCPopupFactory());

        final EmptyBorder emptyBorder = new EmptyBorder(0, 0, 0, 0);
        UIManager.put("ScrollPane.border", emptyBorder);

        // OptionPane background and Panel background are linked because the
        // JOptionPane features unstyleable and opaque Panels.
        UIManager.put("OptionPane.background", WidgetUtils.COLOR_WELL_BACKGROUND);
        UIManager.put("Panel.background", WidgetUtils.COLOR_WELL_BACKGROUND);

        UIManager.put("List.selectionForeground", WidgetUtils.BG_COLOR_BRIGHTEST);
        UIManager.put("List.selectionBackground", WidgetUtils.BG_COLOR_LESS_DARK);
        UIManager.put("List.focusCellHighlightBorder", WidgetUtils.BORDER_THIN);

        UIManager.put("Tree.selectionForeground", WidgetUtils.BG_COLOR_BRIGHTEST);
        UIManager.put("Tree.selectionBackground", WidgetUtils.BG_COLOR_LESS_DARK);
        UIManager.put("Tree.selectionBorderColor", WidgetUtils.BG_COLOR_MEDIUM);

        UIManager.put("Table.selectionForeground", WidgetUtils.BG_COLOR_BRIGHTEST);
        UIManager.put("Table.selectionBackground", WidgetUtils.BG_COLOR_LESS_DARK);
        UIManager.put("Table.focusCellHighlightBorder", WidgetUtils.BORDER_THIN);

        // splitpane "flattening" (remove bevel like borders in divider)
        UIManager.put("SplitPane.border", new EmptyBorder(0, 0, 0, 0));
        UIManager.put("SplitPaneDivider.border", new EmptyBorder(0, 0, 0, 0));

        final Color menuBackground = WidgetUtils.BG_COLOR_LESS_BRIGHT;
        UIManager.put("PopupMenu.background", menuBackground);
        UIManager.put("PopupMenu.foreground", WidgetUtils.BG_COLOR_DARK);
        UIManager.put("PopupMenu.border", emptyBorder);
        UIManager.put("Menu.background", menuBackground);
        UIManager.put("Menu.foreground", WidgetUtils.BG_COLOR_DARK);
        UIManager.put("Menu.border", WidgetUtils.BORDER_MENU_ITEM);
        UIManager.put("MenuItem.selectionForeground", WidgetUtils.BG_COLOR_DARKEST);
        UIManager.put("MenuItem.selectionBackground", WidgetUtils.BG_COLOR_BLUE_DARK);
        UIManager.put("MenuItem.background", menuBackground);
        UIManager.put("MenuItem.foreground", WidgetUtils.BG_COLOR_DARK);
        UIManager.put("MenuItem.border", WidgetUtils.BORDER_MENU_ITEM);
        UIManager.put("MenuBar.background", menuBackground);
        UIManager.put("MenuBar.foreground", WidgetUtils.BG_COLOR_DARK);
        UIManager.put("MenuBar.border", emptyBorder);

        // separator styling
        final Color separatorColor = WidgetUtils.slightlyDarker(menuBackground);
        UIManager.put("Separator.background", separatorColor);
        UIManager.put("Separator.foreground", separatorColor);
        UIManager.put("Separator.highlight", separatorColor);
        UIManager.put("Separator.shadow", separatorColor);

        // white background for input components
        UIManager.put("Tree.background", WidgetUtils.BG_COLOR_BRIGHTEST);
        UIManager.put("EditorPane.background", WidgetUtils.BG_COLOR_BRIGHTEST);
        UIManager.put("Spinner.background", WidgetUtils.BG_COLOR_BRIGHTEST);

        // Buttons
        // UIManager.put("Button.background", WidgetUtils.BG_COLOR_BLUE_MEDIUM);
        // UIManager.put("Button.darkShadow", WidgetUtils.BG_COLOR_BLUE_MEDIUM);
        // UIManager.put("Button.shadow", WidgetUtils.BG_COLOR_BLUE_MEDIUM);
        // UIManager.put("Button.highlight", WidgetUtils.BG_COLOR_BLUE_MEDIUM);
        // UIManager.put("Button.light", WidgetUtils.BG_COLOR_BLUE_MEDIUM);
        // UIManager.put("Button.select", WidgetUtils.BG_COLOR_BLUE_DARK);
        // UIManager.put("Button.foreground", WidgetUtils.BG_COLOR_BRIGHTEST);
        // UIManager.put("Button.is3DEnabled", Boolean.FALSE);
        // UIManager.put("Button.borderPaintsFocus", Boolean.FALSE);
        // UIManager.put("Button.border", WidgetUtils.BORDER_BUTTON);

        // Input fields
        UIManager.put("TextField.border", WidgetUtils.BORDER_INPUT);
        UIManager.put("TextField.background", WidgetUtils.BG_COLOR_BRIGHTEST);
        UIManager.put("TextField.disabledBackground", WidgetUtils.BG_COLOR_BRIGHT);

        UIManager.put("TextArea.border", WidgetUtils.BORDER_INPUT);
        UIManager.put("TextArea.background", WidgetUtils.BG_COLOR_BRIGHTEST);

        UIManager.put("PasswordField.border", WidgetUtils.BORDER_INPUT);
        UIManager.put("PasswordField.background", WidgetUtils.BG_COLOR_BRIGHTEST);
        UIManager.put("PasswordField.disabledBackground", WidgetUtils.BG_COLOR_BRIGHT);

        UIManager.put("FormattedTextField.border", WidgetUtils.BORDER_INPUT);
        UIManager.put("FormattedTextField.background", WidgetUtils.BG_COLOR_BRIGHTEST);

        UIManager.put("ComboBox.border", WidgetUtils.BORDER_INPUT);
        UIManager.put("ComboBox.background", WidgetUtils.BG_COLOR_BRIGHTEST);
        UIManager.put("ComboBox.arrowButtonBorder", BorderFactory.createEmptyBorder());
        UIManager.put("ComboBox.editorBorder", BorderFactory.createEmptyBorder());
        UIManager.put("ComboBox.selectionBackground", WidgetUtils.BG_COLOR_BRIGHTEST);
        UIManager.put("ComboBoxUI", DCComboBoxUI.class.getName());

        // table header styling
        UIManager.put("TableHeader.background", WidgetUtils.COLOR_WELL_BACKGROUND);
        UIManager.put("TableHeader.focusCellBackground", WidgetUtils.COLOR_WELL_BACKGROUND);
        UIManager.put("TableHeader.foreground", WidgetUtils.BG_COLOR_DARKEST);
        UIManager.put("TableHeader.font", WidgetUtils.FONT_TABLE_HEADER);
        UIManager.put("TableHeader.cellBorder",
                new CompoundBorder(new MatteBorder(0, 0, 0, 1, WidgetUtils.BG_COLOR_LESS_BRIGHT),
                        WidgetUtils.BORDER_EMPTY));

        // titled borders
        UIManager.put("TitledBorder.font", WidgetUtils.FONT_HEADER1);
        UIManager.put("TitledBorder.titleColor", WidgetUtils.BG_COLOR_BLUE_MEDIUM);

        // tool tip colors
        UIManager.put("ToolTip.background", WidgetUtils.COLOR_ALTERNATIVE_BACKGROUND);
        UIManager.put("ToolTip.foreground", WidgetUtils.BG_COLOR_BRIGHT);
        UIManager.put("ToolTip.border", WidgetUtils.BORDER_WIDE_ALTERNATIVE);

        // task pane colors
        UIManager.put("TaskPaneContainer.background", WidgetUtils.COLOR_WELL_BACKGROUND);
        UIManager.put("TaskPane.background", WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        UIManager.put("TaskPane.font", WidgetUtils.FONT_TABLE_HEADER);
        UIManager.put("TaskPane.titleForeground", WidgetUtils.BG_COLOR_BRIGHTEST);
        UIManager.put("TaskPane.titleBackgroundGradientStart", WidgetUtils.COLOR_ALTERNATIVE_BACKGROUND);
        UIManager.put("TaskPane.titleBackgroundGradientEnd", WidgetUtils.COLOR_ALTERNATIVE_BACKGROUND);
        UIManager.put("TaskPane.borderColor", WidgetUtils.BG_COLOR_LESS_DARK);

        // scrollbar color
        UIManager.put("ScrollBar.thumb", WidgetUtils.BG_COLOR_LESS_BRIGHT);
        UIManager.put("ScrollBar.thumbHighlight", WidgetUtils.slightlyDarker(WidgetUtils.BG_COLOR_LESS_BRIGHT));
        UIManager.put("ScrollBar.thumbShadow", WidgetUtils.slightlyDarker(WidgetUtils.BG_COLOR_LESS_BRIGHT));
        UIManager.put("ScrollBar.thumbDarkShadow", WidgetUtils.BG_COLOR_LESS_BRIGHT);
        UIManager.put("ScrollBar.minimumThumbSize", new Dimension(30, 30));
        UIManager.put("ScrollBar.highlight", WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        UIManager.put("ScrollBar.shadow", WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        UIManager.put("ScrollBar.darkShadow", WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        UIManager.put("ScrollBar.background", WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        UIManager.put("ScrollBar.foreground", WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        UIManager.put("ScrollBar.track", WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        UIManager.put("ScrollBar.trackForeground", WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        UIManager.put("ScrollBar.trackHighlight", WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        UIManager.put("ScrollBar.trackHighlightForeground", WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        UIManager.put("ScrollBarUI", DCScrollBarUI.class.getName());

        // progressbar color
        UIManager.put("ProgressBar.foreground", WidgetUtils.BG_COLOR_BLUE_BRIGHT);

        // file chooser
        UIManager.put("FileChooser.detailsViewIcon", imageManager.getImageIcon("images/filetypes/view-details.png"));
        UIManager.put("FileChooser.listViewIcon", imageManager.getImageIcon("images/filetypes/view-list.png"));
        UIManager.put("FileChooser.homeFolderIcon", imageManager.getImageIcon("images/filetypes/home-folder.png"));
        UIManager.put("FileChooser.newFolderIcon", imageManager.getImageIcon("images/filetypes/new-folder.png"));
        UIManager.put("FileChooser.upFolderIcon", imageManager.getImageIcon("images/filetypes/parent-folder.png"));

        // date picker month view
        UIManager.put("JXMonthView.foreground", WidgetUtils.BG_COLOR_DARK);
        UIManager.put("JXMonthView.monthStringForeground", WidgetUtils.BG_COLOR_DARK);
        UIManager.put("JXMonthView.daysOfTheWeekForeground", WidgetUtils.BG_COLOR_DARK);
        UIManager.put("JXMonthView.weekOfTheYearForeground", WidgetUtils.BG_COLOR_DARK);
        UIManager.put("JXMonthView.unselectableDayForeground", WidgetUtils.BG_COLOR_MEDIUM);

        // sets the default font to use in Swing html elements
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(WidgetUtils.FONT_NORMAL);

        // tool tip tweaks
        ToolTipManager.sharedInstance().setDismissDelay(10 * 1000);
        ToolTipManager.sharedInstance().setInitialDelay(100);
    }
}
