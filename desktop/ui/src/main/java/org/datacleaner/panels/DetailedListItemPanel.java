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
package org.datacleaner.panels;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;

public class DetailedListItemPanel extends DCPanel {

    private static final Color COLOR_NORMAL = WidgetUtils.BG_COLOR_LESS_BRIGHT;
    private static final Color COLOR_HOVER = WidgetUtils.BG_COLOR_BRIGHTEST;

    private static final long serialVersionUID = 1L;

    public DetailedListItemPanel(final String title, final String body) {
        this(null, title, body);
    }

    public DetailedListItemPanel(final Icon icon, final String title, final String body) {
        super(WidgetUtils.BG_SEMI_TRANSPARENT_BRIGHT);
        setLayout(new GridBagLayout());
        setBorder(WidgetUtils.BORDER_LIST_ITEM_SUBTLE);
        
        final DCLabel titleLabel = DCLabel.bright(title);
        titleLabel.setFont(WidgetUtils.FONT_BANNER);
        titleLabel.setForeground(COLOR_HOVER);
        titleLabel.setBorder(new EmptyBorder(12, 12, 6, 0));

        final JTextArea bodyLabel = new JTextArea();
        bodyLabel.setLineWrap(true);
        bodyLabel.setWrapStyleWord(true);
        bodyLabel.setText(body);
        bodyLabel.setEditable(false);
        bodyLabel.setOpaque(false);
        bodyLabel.setFont(WidgetUtils.FONT_HEADER2);
        bodyLabel.setForeground(COLOR_NORMAL);
        bodyLabel.setBorder(new EmptyBorder(6, 12, 12, 0));

        final JSeparator horizontalRule = new JSeparator(JSeparator.HORIZONTAL);
        horizontalRule.setForeground(WidgetUtils.BG_COLOR_ORANGE_MEDIUM);
        horizontalRule.setBackground(WidgetUtils.BG_COLOR_ORANGE_MEDIUM);

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        final MouseAdapter mouseListener = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                bodyLabel.setForeground(COLOR_HOVER);
                setBorder(WidgetUtils.BORDER_LIST_ITEM_HIGHLIGHTED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                bodyLabel.setForeground(COLOR_NORMAL);
                setBorder(WidgetUtils.BORDER_LIST_ITEM_SUBTLE);
            }
        };
        addMouseListener(mouseListener);
        bodyLabel.addMouseListener(mouseListener);

        GridBagConstraints c = new GridBagConstraints();

        Insets questionInsets = new Insets(5, 5, 5, 5);

        if (icon != null) {
            c.gridx = 0;
            c.gridy = 0;
            c.gridheight = 3;
            c.insets = questionInsets;
            add(new JLabel(icon), c);
        }

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1.0;
        c.insets = questionInsets;
        c.anchor = GridBagConstraints.LINE_START;
        add(titleLabel, c);

        if (icon == null) {
            c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 1;
            c.gridy = 1;
            c.weightx = 1.0;
            c.insets = questionInsets;
            c.anchor = GridBagConstraints.LINE_START;
            add(horizontalRule, c);
        }

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 1.0;
        c.insets = questionInsets;
        c.anchor = GridBagConstraints.LINE_START;
        add(bodyLabel, c);
    }
}
