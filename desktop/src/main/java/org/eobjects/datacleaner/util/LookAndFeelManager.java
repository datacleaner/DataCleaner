/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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

import java.util.Set;

import javax.swing.LookAndFeel;
import javax.swing.PopupFactory;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.eobjects.datacleaner.widgets.tooltip.DCPopupFactory;
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

    private LookAndFeelManager() {
    }

    public void init() {
        try {
            LookAndFeel laf = new PlasticXPLookAndFeel();
            UIManager.setLookAndFeel(laf);
            logger.info("Look and feel set to: {}", UIManager.getLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            throw new IllegalStateException(e);
        }

        UIManager.put("ClassLoader", LookUtils.class.getClassLoader());

        Set<Object> propertyKeys = UIManager.getLookAndFeelDefaults().keySet();

        for (Object propertyKey : propertyKeys) {
            if (propertyKey instanceof String) {
                String str = (String) propertyKey;

                if (str.endsWith(".font")) {
                    // set default font
                    UIManager.put(propertyKey, WidgetUtils.FONT_NORMAL);
                } else if (str.endsWith(".background")) {
                    // set default background color
                    UIManager.put(propertyKey, WidgetUtils.BG_COLOR_BRIGHT);
                }
            }
        }

        ToolTipManager.sharedInstance().setInitialDelay(500);
        PopupFactory.setSharedInstance(new DCPopupFactory());

        EmptyBorder emptyBorder = new EmptyBorder(0, 0, 0, 0);
        LineBorder borderDarkest3 = new LineBorder(WidgetUtils.BG_COLOR_DARKEST, 3);
        UIManager.put("ScrollPane.border", emptyBorder);
        UIManager.put("Menu.border", borderDarkest3);
        UIManager.put("Menu.background", WidgetUtils.BG_COLOR_DARKEST);
        UIManager.put("Menu.foreground", WidgetUtils.BG_COLOR_BRIGHTEST);

        UIManager.put("MenuItem.selectionForeground", WidgetUtils.BG_COLOR_BRIGHTEST);
        UIManager.put("MenuItem.selectionBackground", WidgetUtils.BG_COLOR_LESS_DARK);

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

        UIManager.put("PopupMenu.border", emptyBorder);
        UIManager.put("PopupMenu.background", WidgetUtils.BG_COLOR_DARKEST);
        UIManager.put("PopupMenu.foreground", WidgetUtils.BG_COLOR_BRIGHTEST);
        UIManager.put("MenuItem.border", borderDarkest3);
        UIManager.put("MenuItem.background", WidgetUtils.BG_COLOR_DARKEST);
        UIManager.put("MenuItem.foreground", WidgetUtils.BG_COLOR_BRIGHTEST);
        UIManager.put("MenuBar.border", emptyBorder);
        UIManager.put("MenuBar.background", WidgetUtils.BG_COLOR_DARKEST);
        UIManager.put("MenuBar.foreground", WidgetUtils.BG_COLOR_BRIGHTEST);

        // white background for input components
        UIManager.put("Tree.background", WidgetUtils.BG_COLOR_BRIGHTEST);
        UIManager.put("TextArea.background", WidgetUtils.BG_COLOR_BRIGHTEST);
        UIManager.put("PasswordField.background", WidgetUtils.BG_COLOR_BRIGHTEST);
        UIManager.put("FormattedTextField.background", WidgetUtils.BG_COLOR_BRIGHTEST);
        UIManager.put("EditorPane.background", WidgetUtils.BG_COLOR_BRIGHTEST);
        UIManager.put("ComboBox.background", WidgetUtils.BG_COLOR_BRIGHTEST);
        UIManager.put("TextField.background", WidgetUtils.BG_COLOR_BRIGHTEST);
        UIManager.put("Spinner.background", WidgetUtils.BG_COLOR_BRIGHTEST);

        // table header styling
        UIManager.put("TableHeader.background", WidgetUtils.BG_COLOR_DARK);
        UIManager.put("TableHeader.focusCellBackground", WidgetUtils.BG_COLOR_LESS_DARK);
        UIManager.put("TableHeader.foreground", WidgetUtils.BG_COLOR_BRIGHTEST);
        UIManager.put("TableHeader.cellBorder", new LineBorder(WidgetUtils.BG_COLOR_LESS_DARK));

        // titled borders
        UIManager.put("TitledBorder.font", WidgetUtils.FONT_HEADER1);
        UIManager.put("TitledBorder.titleColor", WidgetUtils.BG_COLOR_BLUE_BRIGHT);

        // tool tip colors
        UIManager.put("ToolTip.background", WidgetUtils.BG_COLOR_DARK);
        UIManager.put("ToolTip.foreground", WidgetUtils.BG_COLOR_BRIGHTEST);
        UIManager.put("ToolTip.border", WidgetUtils.BORDER_THIN);

        // task pane colors
        UIManager.put("TaskPaneContainer.background", WidgetUtils.BG_COLOR_BRIGHTEST);
        UIManager.put("TaskPane.font", WidgetUtils.FONT_NORMAL);
        UIManager.put("TaskPane.titleForeground", WidgetUtils.BG_COLOR_BRIGHTEST);
        UIManager.put("TaskPane.titleBackgroundGradientStart", WidgetUtils.BG_COLOR_DARKEST);
        UIManager.put("TaskPane.titleBackgroundGradientEnd", WidgetUtils.BG_COLOR_DARKEST);
        UIManager.put("TaskPane.borderColor", WidgetUtils.BG_COLOR_DARKEST);
        UIManager.put("TaskPane.background", WidgetUtils.BG_COLOR_BRIGHT);

        // scrollbar color
        UIManager.put("ScrollBar.thumb", WidgetUtils.BG_COLOR_DARK);
        UIManager.put("ScrollBar.thumbHighlight", WidgetUtils.BG_COLOR_DARK);
        UIManager.put("ScrollBar.thumbShadow", WidgetUtils.BG_COLOR_DARK);

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
    }
}
