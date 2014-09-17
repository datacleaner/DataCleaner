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
package org.eobjects.datacleaner.widgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.LookAndFeelManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.jdesktop.swingx.HorizontalLayout;

/**
 * Represents a set of related buttons; typically a set of toggle buttons or a
 * button with an alternate mode/dropdown selector.
 */
public class ComboButton extends JPanel {

    private static final long serialVersionUID = 1L;

    private final List<AbstractButton> _buttons;
    private final ActionListener _commonToggleButtonActionListener;

    /**
     * Constructs a {@link ComboButton}.
     */
    public ComboButton() {
        super(new HorizontalLayout(0));
        _buttons = new ArrayList<AbstractButton>(2);
        _commonToggleButtonActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (AbstractButton button : _buttons) {
                    Object source = e.getSource();
                    if (source != button) {
                        button.setSelected(false);
                    }
                }

            }
        };

        Border border = new CompoundBorder(WidgetUtils.BORDER_SHADOW, WidgetUtils.BORDER_THIN);

        setBorder(border);
    }

    /**
     * Adds a button to the {@link ComboButton}.
     * 
     * @param text
     *            the text of the button
     * @param toggleButton
     *            whether or not this button should be a toggle button (true) or
     *            a regular button (false)
     * @return
     */
    public AbstractButton addButton(String text, boolean toggleButton) {
        return addButton(null, text, toggleButton);
    }

    /**
     * 
     * @param icon
     *            the icon of the button
     * @param text
     *            the text of the button
     * @param toggleButton
     *            whether or not this button should be a toggle button (true) or
     *            a regular button (false)
     * @return
     */
    public AbstractButton addButton(Icon icon, String text, boolean toggleButton) {
        AbstractButton button;
        if (toggleButton) {
            button = new JToggleButton(text, icon);
            button.addActionListener(_commonToggleButtonActionListener);
        } else {
            button = new JButton(text, icon);
        }
        button.setOpaque(false);
        button.setBorderPainted(false);
        _buttons.add(button);

        add(button);

        return button;
    }

    // a simple test app
    public static void main(String[] args) {
        LookAndFeelManager.get().init();

        ImageIcon icon = ImageManager.get().getImageIcon("images/actions/add.png", 32);

        final ComboButton comboButton1 = new ComboButton();
        comboButton1.addButton(icon, "Foo!", true);
        comboButton1.addButton("Boo!", true);

        final ComboButton comboButton2 = new ComboButton();
        comboButton1.addButton(icon, "Foo!", false);
        comboButton1.addButton("Boo!", false);
        comboButton1.addButton("Mrr!", true);
        comboButton1.addButton("Rrrh!", true);

        final DCPanel panel = new DCPanel();
        panel.add(comboButton1);

        panel.add(comboButton2);

        JButton regularButton = new JButton("Regular button");
        panel.add(regularButton);

        final JFrame frame = new JFrame("test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.add(panel);

        frame.pack();
        frame.setVisible(true);
    }
}
