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

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Insets;
import java.lang.reflect.Field;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.metal.MetalButtonUI;

import org.datacleaner.widgets.DCTaskPaneContainer;
import org.datacleaner.widgets.DarkButtonUI;
import org.elasticsearch.common.base.Strings;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXCollapsiblePane.Direction;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTextArea;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.plaf.basic.BasicStatusBarUI;
import org.jdesktop.swingx.plaf.metal.MetalStatusBarUI;
import org.jdesktop.swingx.prompt.PromptSupport.FocusBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class for various commonly used widgets in DataCleaner. Typically the
 * factory is being used to cut down boilerplate code for typical features such
 * as setting mnemonics, tooltips etc.
 */
public final class WidgetFactory {

    /**
     * Default {@link JTextField} columns width value
     */
    public static final int TEXT_FIELD_COLUMNS = 17;

    private static final Logger logger = LoggerFactory.getLogger(WidgetFactory.class);

    public static JMenu createMenu(String text, char mnemonic) {
        JMenu menu = new JMenu();
        menu.setText(text);
        menu.setMnemonic(mnemonic);
        return menu;
    }

    public static JMenuItem createMenuItem(String text, Icon icon) {
        JMenuItem menu = new JMenuItem();
        menu.setText(text);
        if (icon != null) {
            menu.setIcon(icon);
        }
        return menu;
    }

    public static JMenuItem createMenuItem(String text, String iconPath) {
        Icon icon = null;
        if (iconPath != null) {
            icon = ImageManager.get().getImageIcon(iconPath, IconUtils.ICON_SIZE_SMALL);
        }
        return createMenuItem(text, icon);
    }

    private static Icon getButtonIcon(String imagePath) {
        if (Strings.isNullOrEmpty(imagePath)) {
            return null;
        }
        final ImageIcon icon = ImageManager.get().getImageIcon(imagePath, IconUtils.ICON_SIZE_MEDIUM);
        return icon;
    }

    private static JButton createBasicButton(String text, Icon icon) {
        final JButton b = new JButton();
        if (text != null) {
            b.setText(text);
        }
        if (icon != null) {
            b.setIcon(icon);
        }
        b.setFocusPainted(false);
        return b;
    }

    public static JButton createPrimaryButton(String text, String imagePath) {
        return createPrimaryButton(text, getButtonIcon(imagePath));
    }

    public static JButton createPrimaryButton(String text, Icon icon) {
        final JButton b = createBasicButton(text, icon);
        WidgetUtils.setPrimaryButtonStyle(b);
        return b;
    }

    public static JButton createDefaultButton(String text) {
        return createDefaultButton(text, (Icon) null);
    }

    public static JButton createDefaultButton(String text, String imagePath) {
        return createDefaultButton(text, getButtonIcon(imagePath));
    }

    public static JButton createDefaultButton(String text, Icon icon) {
        final JButton b = createBasicButton(text, icon);
        WidgetUtils.setDefaultButtonStyle(b);
        return b;
    }

    public static JButton createDarkButton(String text, String imagePath) {
        return createDarkButton(text, getButtonIcon(imagePath));
    }

    public static JButton createDarkButton(String text, Icon icon) {
        final JButton b = createBasicButton(text, icon);
        b.setUI(DarkButtonUI.get());
        final MatteBorder outerBorder = new MatteBorder(1, 1, 1, 1, WidgetUtils.BG_COLOR_LESS_DARK);
        b.setBorder(new CompoundBorder(outerBorder, new EmptyBorder(2, 4, 2, 4)));
        return b;
    }

    /**
     * 
     * @param text
     * @return
     * 
     * @deprecated use
     */
    @Deprecated
    public static JButton createButton(String text) {
        return createButton(text, (Icon) null);
    }

    /**
     * 
     * @param text
     * @param imagePath
     * @return
     * 
     * @deprecated use {@link #createDarkButton(String, String)},
     *             {@link #createPrimaryButton(String, String)},
     *             {@link #createDefaultButton(String, String)} or
     *             {@link #createSmallButton(String)} instead.
     */
    @Deprecated
    public static JButton createButton(String text, String imagePath) {
        return createDarkButton(text, imagePath);
    }

    /**
     * 
     * @param text
     * @param icon
     * @return
     * 
     * @deprecated use {@link #createDarkButton(String, Icon)},
     *             {@link #createPrimaryButton(String, Icon)} or
     *             {@link #createSmallButton(Icon)} instead.
     */
    @Deprecated
    public static JButton createButton(String text, Icon icon) {
        return createDarkButton(text, icon);
    }

    public static JXStatusBar createStatusBar(JComponent comp) {
        final JXStatusBar statusBar = new JXStatusBar();
        statusBar.setUI(new MetalStatusBarUI());
        statusBar.putClientProperty(BasicStatusBarUI.AUTO_ADD_SEPARATOR, false);
        statusBar.setBackground(WidgetUtils.BG_COLOR_DARKEST);
        final MatteBorder outerBorder = new MatteBorder(1, 0, 0, 0, WidgetUtils.BG_COLOR_LESS_DARK);
        final EmptyBorder innerBorder = new EmptyBorder(2, 2, 2, 2);
        statusBar.setBorder(new CompoundBorder(outerBorder, innerBorder));
        final JXStatusBar.Constraint c1 = new JXStatusBar.Constraint(JXStatusBar.Constraint.ResizeBehavior.FILL);
        statusBar.add(comp, c1);
        return statusBar;
    }

    public static JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
        toolBar.setOpaque(false);
        toolBar.setBorder(null);
        toolBar.setRollover(true);
        toolBar.setFloatable(false);
        toolBar.setAlignmentY(JToolBar.LEFT_ALIGNMENT);
        return toolBar;
    }

    public static Component createToolBarSeparator() {
        return Box.createHorizontalGlue();
    }

    public static JButton createSmallButton(String imagePath) {
        return createSmallButton(null, imagePath);
    }

    public static JButton createSmallButton(String text, String imagePath) {
        Icon icon = ImageManager.get().getImageIcon(imagePath, IconUtils.ICON_SIZE_SMALL);
        return createSmallButton(text, icon);
    }

    public static JButton createSmallButton(Icon icon) {
        return createSmallButton(null, icon);
    }

    public static JButton createSmallButton(String text, Icon icon) {
        JButton b = new JButton(icon);
        if (text != null) {
            b.setText(text);
            b.setFont(WidgetUtils.FONT_SMALL);
        }
        b.setMargin(new Insets(0, 0, 0, 0));

        b.setUI(new MetalButtonUI());
        b.setBackground(WidgetUtils.COLOR_WELL_BACKGROUND);

        final MatteBorder outerBorder = new MatteBorder(1, 1, 1, 1, WidgetUtils.BG_COLOR_LESS_BRIGHT);
        b.setBorder(new CompoundBorder(outerBorder, new EmptyBorder(2, 4, 2, 4)));
        b.setFocusPainted(false);

        return b;
    }

    public static DCTaskPaneContainer createTaskPaneContainer() {
        DCTaskPaneContainer taskPaneContainer = new DCTaskPaneContainer();
        return taskPaneContainer;
    }

    public static JXTaskPane createTaskPane(String title, Icon icon) {
        JXTaskPane taskPane = new JXTaskPane();
        Container cp = taskPane.getContentPane();
        ((JComponent) cp).setBorder(new MatteBorder(0, 1, 1, 1, WidgetUtils.BG_COLOR_LESS_DARK));
        taskPane.setFocusable(false);
        taskPane.setTitle(title);
        if (icon != null) {
            taskPane.setIcon(icon);
        }
        return taskPane;
    }

    public static JXTextField createTextField() {
        return createTextField(null);
    }

    public static JXTextField createTextField(String promptText) {
        return createTextField(promptText, TEXT_FIELD_COLUMNS);
    }

    public static JXTextField createTextField(String promptText, int columns) {
        JXTextField tf = new JXTextField(promptText);
        tf.setColumns(columns);
        if (promptText != null) {
            tf.setFocusBehavior(FocusBehavior.SHOW_PROMPT);
            tf.setToolTipText(promptText);
        }
        return tf;
    }

    public static JXTextArea createTextArea(String promptText) {
        JXTextArea ta = new JXTextArea(promptText);
        ta.setColumns(17);
        ta.setRows(6);
        ta.setBorder(new CompoundBorder(WidgetUtils.BORDER_THIN, new EmptyBorder(2, 2, 2, 2)));
        return ta;
    }

    public static JButton createImageButton(ImageIcon icon) {
        final JButton button = new JButton(icon);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setBorder(null);
        button.setOpaque(false);
        return button;
    }

    public static JXCollapsiblePane createCollapsiblePane(Direction direction) {
        JXCollapsiblePane collapsiblePane = new JXCollapsiblePane(direction);
        collapsiblePane.setOpaque(false);

        // hack to make it non-opaque!
        try {
            Field field = JXCollapsiblePane.class.getDeclaredField("wrapper");
            field.setAccessible(true);
            JViewport viewPort = (JViewport) field.get(collapsiblePane);
            viewPort.setOpaque(false);
            JComponent component = (JComponent) viewPort.getView();
            component.setOpaque(false);
        } catch (Exception e) {
            logger.info("Failed to make JXCollapsiblePane non-opaque", e);
        }
        return collapsiblePane;
    }

    public static JPasswordField createPasswordField() {
        return createPasswordField(TEXT_FIELD_COLUMNS);
    }

    public static JPasswordField createPasswordField(int columns) {
        JPasswordField field = new JPasswordField(columns);
        field.setFont(new Font("LucidaSans", Font.PLAIN, 12));
        return field;
    }
}
