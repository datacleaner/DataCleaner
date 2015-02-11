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
import java.awt.event.MouseListener;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;

public class DetailedListItemPanel extends DCPanel {

    private static final Color COLOR_NORMAL = WidgetUtils.BG_COLOR_MEDIUM;
    private static final Color COLOR_HOVER = WidgetUtils.BG_COLOR_DARK;

    private static final long serialVersionUID = 1L;
    private final JTextArea _bodyLabel;

    public DetailedListItemPanel(final String title, final String body) {
        this(null, title, body);
    }

    public DetailedListItemPanel(final Icon icon, final String title, final String body) {
        super(WidgetUtils.BG_SEMI_TRANSPARENT);
        setLayout(new GridBagLayout());
        setBorder(WidgetUtils.BORDER_LIST_ITEM_SUBTLE);

        final DCLabel titleLabel = DCLabel.bright(title);
        titleLabel.setFont(WidgetUtils.FONT_BANNER);
        titleLabel.setForeground(COLOR_HOVER);
        titleLabel.setBorder(new EmptyBorder(12, 12, 4, 12));

        _bodyLabel = new JTextArea();
        _bodyLabel.setLineWrap(true);
        _bodyLabel.setWrapStyleWord(true);
        _bodyLabel.setText(body);
        _bodyLabel.setEditable(false);
        _bodyLabel.setOpaque(false);
        _bodyLabel.setFont(WidgetUtils.FONT_HEADER2);
        _bodyLabel.setForeground(COLOR_NORMAL);
        _bodyLabel.setBorder(new EmptyBorder(4, 12, 12, 12));

        final JSeparator horizontalRule = new JSeparator(JSeparator.HORIZONTAL);
        horizontalRule.setForeground(WidgetUtils.BG_COLOR_ORANGE_MEDIUM);
        horizontalRule.setBackground(WidgetUtils.BG_COLOR_ORANGE_MEDIUM);

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        final MouseAdapter mouseListener = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                _bodyLabel.setForeground(COLOR_HOVER);
                setBorder(WidgetUtils.BORDER_LIST_ITEM_HIGHLIGHTED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                _bodyLabel.setForeground(COLOR_NORMAL);
                setBorder(WidgetUtils.BORDER_LIST_ITEM_SUBTLE);
            }
        };
        addMouseListener(mouseListener);

        GridBagConstraints c = new GridBagConstraints();

        if (icon != null) {
            c.gridx = 0;
            c.gridy = 0;
            c.gridheight = 3;
            c.insets = new Insets(5, 5, 5, 5);
            add(new JLabel(icon), c);
        }

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.LINE_START;
        add(titleLabel, c);

        if (icon == null) {
            c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 1;
            c.gridy = 1;
            c.weightx = 1.0;
            c.insets = new Insets(10, 12, 10, 12);
            c.anchor = GridBagConstraints.LINE_START;
            add(horizontalRule, c);
        }

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.LINE_START;
        add(_bodyLabel, c);
    }
    
    @Override
    public synchronized void addMouseListener(MouseListener mouseListener) {
        super.addMouseListener(mouseListener);
        _bodyLabel.addMouseListener(mouseListener);
    }
    
    @Override
    public synchronized void removeMouseListener(MouseListener mouseListener) {
        super.removeMouseListener(mouseListener);
        _bodyLabel.removeMouseListener(mouseListener);
    }
}
