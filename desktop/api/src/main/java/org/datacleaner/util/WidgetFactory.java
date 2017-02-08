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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.text.Format;
import java.text.ParseException;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.metal.MetalButtonUI;

import org.datacleaner.components.convert.ConvertToNumberTransformer;
import org.datacleaner.widgets.DCTaskPaneContainer;
import org.datacleaner.widgets.PopupButton;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXCollapsiblePane.Direction;
import org.jdesktop.swingx.JXFormattedTextField;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTextArea;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.JXTitledPanel;
import org.jdesktop.swingx.plaf.basic.BasicStatusBarUI;
import org.jdesktop.swingx.plaf.metal.MetalStatusBarUI;
import org.jdesktop.swingx.prompt.PromptSupport.FocusBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

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

    public static JMenu createMenu(final String text, final char mnemonic) {
        final JMenu menu = new JMenu();
        menu.setText(text);
        menu.setMnemonic(mnemonic);
        return menu;
    }

    public static JMenuItem createMenuItem(final String text, final Icon icon) {
        final JMenuItem menu = new JMenuItem();
        menu.setText(text);
        if (icon != null) {
            menu.setIcon(icon);
        }
        return menu;
    }

    public static JMenuItem createMenuItem(final String text, final String iconPath) {
        Icon icon = null;
        if (iconPath != null) {
            icon = ImageManager.get().getImageIcon(iconPath, IconUtils.ICON_SIZE_MENU_ITEM);
        }
        return createMenuItem(text, icon);
    }

    private static Icon getButtonIcon(final String imagePath) {
        if (Strings.isNullOrEmpty(imagePath)) {
            return null;
        }
        return ImageManager.get().getImageIcon(imagePath, IconUtils.ICON_SIZE_BUTTON);
    }

    public static PopupButton createDarkPopupButton(final String text, final String imagePath) {
        final PopupButton b = new PopupButton(text, getButtonIcon(imagePath));
        b.setFocusPainted(false);
        WidgetUtils.setDarkButtonStyle(b);
        return b;
    }

    public static PopupButton createDefaultPopupButton(final String text, final String imagePath) {
        final PopupButton b = new PopupButton(text, getButtonIcon(imagePath));
        b.setFocusPainted(false);
        WidgetUtils.setDefaultButtonStyle(b);
        return b;
    }

    public static PopupButton createPrimaryPopupButton(final String text, final String imagePath) {
        final PopupButton b = new PopupButton(text, getButtonIcon(imagePath));
        b.setFocusPainted(false);
        WidgetUtils.setPrimaryButtonStyle(b);
        return b;
    }

    public static PopupButton createSmallPopupButton(final String text, final String imagePath) {
        final PopupButton b =
                new PopupButton(text, ImageManager.get().getImageIcon(imagePath, IconUtils.ICON_SIZE_SMALL));

        b.setFont(WidgetUtils.FONT_SMALL);
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setUI(new MetalButtonUI());
        b.setBackground(WidgetUtils.COLOR_WELL_BACKGROUND);

        final MatteBorder outerBorder = new MatteBorder(1, 1, 1, 1, WidgetUtils.BG_COLOR_LESS_BRIGHT);
        b.setBorder(new CompoundBorder(outerBorder, new EmptyBorder(2, 4, 2, 4)));
        b.setFocusPainted(false);

        return b;
    }

    private static JButton createBasicButton(final String text, final Icon icon) {
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

    public static JButton createPrimaryButton(final String text, final String imagePath) {
        return createPrimaryButton(text, getButtonIcon(imagePath));
    }

    public static JButton createPrimaryButton(final String text, final Icon icon) {
        final JButton b = createBasicButton(text, icon);
        WidgetUtils.setPrimaryButtonStyle(b);
        return b;
    }

    public static JButton createDefaultButton(final String text) {
        return createDefaultButton(text, (Icon) null);
    }

    public static JButton createDefaultButton(final String text, final String imagePath) {
        return createDefaultButton(text, getButtonIcon(imagePath));
    }

    public static JButton createDefaultButton(final String text, final Icon icon) {
        final JButton b = createBasicButton(text, icon);
        WidgetUtils.setDefaultButtonStyle(b);
        return b;
    }

    public static JButton createDarkButton(final String text, final String imagePath) {
        return createDarkButton(text, getButtonIcon(imagePath));
    }

    public static JButton createDarkButton(final String text, final Icon icon) {
        final JButton b = createBasicButton(text, icon);
        WidgetUtils.setDarkButtonStyle(b);
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
    public static JButton createButton(final String text) {
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
    public static JButton createButton(final String text, final String imagePath) {
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
    public static JButton createButton(final String text, final Icon icon) {
        return createDarkButton(text, icon);
    }

    public static JXStatusBar createStatusBar(final JComponent comp) {
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
        final JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
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

    public static JButton createToolbarButton(final String text, final String iconPath) {
        final ImageIcon icon;
        if (iconPath == null) {
            icon = null;
        } else {
            icon = ImageManager.get().getImageIcon(iconPath, IconUtils.ICON_SIZE_SMALL);
        }
        final JButton button = new JButton(text, icon);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(final MouseEvent e) {
                button.setForeground(WidgetUtils.BG_COLOR_BLUE_BRIGHT);
            }

            @Override
            public void mouseExited(final MouseEvent e) {
                button.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
            }
        });

        WidgetUtils.setDarkButtonStyle(button);
        return button;
    }

    public static JButton createSmallButton(final String imagePath) {
        return createSmallButton(null, imagePath);
    }

    public static JButton createSmallButton(final String text, final String imagePath) {
        final Icon icon = ImageManager.get().getImageIcon(imagePath, IconUtils.ICON_SIZE_SMALL);
        return createSmallButton(text, icon);
    }

    public static JButton createSmallButton(final Icon icon) {
        return createSmallButton(null, icon);
    }

    public static JButton createSmallButton(final String text, final Icon icon) {
        final JButton b = new JButton(icon);
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
        return new DCTaskPaneContainer();
    }

    public static JXTaskPane createTaskPane(final String title, final String imagePath) {
        final ImageIcon icon;
        if (Strings.isNullOrEmpty(imagePath)) {
            icon = null;
        } else {
            icon = ImageManager.get().getImageIcon(imagePath, IconUtils.ICON_SIZE_TASK_PANE);
        }
        return createTaskPane(title, icon);
    }

    public static JXTaskPane createTaskPane(final String title, final Icon icon) {
        final JXTaskPane taskPane = new JXTaskPane();
        final Container cp = taskPane.getContentPane();
        ((JComponent) cp).setBorder(new MatteBorder(0, 1, 1, 1, WidgetUtils.BG_COLOR_LESS_DARK));
        taskPane.setFocusable(false);
        taskPane.setTitle(title);
        if (icon != null) {
            taskPane.setIcon(icon);
        }
        return taskPane;
    }

    public static JXTitledPanel createTitledPanel(final String title, final JComponent content) {
        final JXTitledPanel titlePanel = new JXTitledPanel(title, content);
        titlePanel.setFocusable(false);
        titlePanel.setTitlePainter((graphics2D, panel, width, height) -> {
            graphics2D.setColor(WidgetUtils.BG_COLOR_LESS_DARK);
            graphics2D.fillRect(0, 0, width, height);
        });

        return titlePanel;
    }

    public static JXTextField createTextField() {
        return createTextField(null);
    }

    public static JXTextField createTextField(final String promptText) {
        return createTextField(promptText, TEXT_FIELD_COLUMNS);
    }

    public static JXTextField createTextField(final String promptText, final int columns) {
        final JXTextField tf = new JXTextField(promptText);
        tf.setColumns(columns);
        if (promptText != null) {
            tf.setFocusBehavior(FocusBehavior.SHOW_PROMPT);
            tf.setToolTipText(promptText);
        }
        return tf;
    }

    public static JXFormattedTextField createFormattedTextField(final String promptText, final int columns,
            final Format format) {
        final JXFormattedTextField tf = new JXFormattedTextField(promptText);
        // Stupid JXFormattedTextField will not pass along a formatter to the constructor.
        tf.setFormatterFactory(new JFormattedTextField.AbstractFormatterFactory() {
            private JFormattedTextField.AbstractFormatter _formatter;

            @Override
            public synchronized JFormattedTextField.AbstractFormatter getFormatter(final JFormattedTextField tf) {
                if (_formatter == null) {
                    _formatter = new JFormattedTextField.AbstractFormatter() {

                        private static final long serialVersionUID = 1L;

                        @Override
                        public Object stringToValue(final String text) throws ParseException {
                            return format.parseObject(text);
                        }

                        @Override
                        public String valueToString(final Object value) throws ParseException {
                            if (value == null) {
                                return "";
                            }

                            return format.format(value);
                        }
                    };
                }
                return _formatter;
            }
        });
        tf.setColumns(columns);
        if (promptText != null) {
            tf.setFocusBehavior(FocusBehavior.SHOW_PROMPT);
            tf.setToolTipText(promptText);
        }
        return tf;
    }

    public static JXTextArea createTextArea(final String promptText) {
        final JXTextArea ta = new JXTextArea(promptText);
        ta.setColumns(17);
        ta.setRows(6);
        ta.setBorder(new CompoundBorder(WidgetUtils.BORDER_THIN, new EmptyBorder(2, 2, 2, 2)));
        return ta;
    }

    public static JButton createImageButton(final ImageIcon icon) {
        final JButton button = new JButton(icon);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setBorder(null);
        button.setOpaque(false);
        return button;
    }

    public static JXCollapsiblePane createCollapsiblePane(final Direction direction) {
        final JXCollapsiblePane collapsiblePane = new JXCollapsiblePane(direction);
        collapsiblePane.setOpaque(false);

        // hack to make it non-opaque!
        try {
            final Field field = JXCollapsiblePane.class.getDeclaredField("wrapper");
            field.setAccessible(true);
            final JViewport viewPort = (JViewport) field.get(collapsiblePane);
            viewPort.setOpaque(false);
            final JComponent component = (JComponent) viewPort.getView();
            component.setOpaque(false);
        } catch (final Exception e) {
            logger.info("Failed to make JXCollapsiblePane non-opaque", e);
        }
        return collapsiblePane;
    }

    public static JPasswordField createPasswordField() {
        return createPasswordField(TEXT_FIELD_COLUMNS);
    }

    public static JPasswordField createPasswordField(final int columns) {
        final JPasswordField field = new JPasswordField(columns);
        field.setFont(new Font("LucidaSans", Font.PLAIN, 12));
        return field;
    }

    public static JDialog createModalDialog(final Component component, final Window parentWindow, final String title,
            final boolean resizable) {
        final JDialog dialog;
        if (parentWindow instanceof Frame) {
            dialog = new JDialog((Frame) parentWindow, title, true);
        } else if (parentWindow instanceof Dialog) {
            dialog = new JDialog((Dialog) parentWindow, title, true);
        } else {
            throw new UnsupportedOperationException(
                    "Cannot create dialog for a component without a frame or dialog parent");
        }

        final Container contentPane = dialog.getContentPane();

        contentPane.setLayout(new BorderLayout());
        contentPane.add(component, BorderLayout.CENTER);
        dialog.setResizable(resizable);
        if (JDialog.isDefaultLookAndFeelDecorated()) {
            final boolean supportsWindowDecorations = UIManager.getLookAndFeel().getSupportsWindowDecorations();
            if (supportsWindowDecorations) {
                dialog.setUndecorated(true);
            }
        }
        dialog.pack();
        dialog.setLocationRelativeTo(parentWindow);
        return dialog;
    }

    public static JDialog createModalDialog(final Component component, final Component parentComponent,
            final String title, final boolean resizable) {
        Component windowComponent = parentComponent;
        while (!(windowComponent instanceof Window) && windowComponent != null) {
            windowComponent = windowComponent.getParent();
        }

        return createModalDialog(component, (Window) windowComponent, title, resizable);
    }

    public static Integer showMaxRowsDialog(final int defaultValue) {
        final String maxRowsString =
                JOptionPane.showInputDialog("How many records do you want to process?", defaultValue);
        if (Strings.isNullOrEmpty(maxRowsString)) {
            return null;
        }
        final Number maxRows = ConvertToNumberTransformer.transformValue(maxRowsString);
        if (maxRows == null || maxRows.intValue() < 1) {
            WidgetUtils.showErrorMessage("Not a valid number", "Please enter a valid number of records.");
            return null;
        }
        return maxRows.intValue();
    }

}
