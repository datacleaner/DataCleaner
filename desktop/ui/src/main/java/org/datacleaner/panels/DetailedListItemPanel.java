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
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;

public class DetailedListItemPanel extends DCPanel {

    private static final Color COLOR_NORMAL = WidgetUtils.BG_COLOR_MEDIUM;
    private static final Color COLOR_HOVER = WidgetUtils.BG_COLOR_DARK;

    private static final int MARGIN_LEFT = 20;

    private static final long serialVersionUID = 1L;
    private final JTextArea _bodyLabel;
    private final Border _normalBorder;
    private final Border _hoverBorder;
    private boolean _hoverEffect;

    public DetailedListItemPanel(final String title, final String body) {
        this(null, title, body);
    }

    public DetailedListItemPanel(final Icon icon, final String title, final String body) {
        this(WidgetUtils.BG_SEMI_TRANSPARENT, WidgetUtils.BORDER_LIST_ITEM_SUBTLE,
                WidgetUtils.BORDER_LIST_ITEM_HIGHLIGHTED, icon, title, body);
    }

    public DetailedListItemPanel(final Color backgroundColor, final Border normalBorder, final Border hoverBorder,
            final Icon icon, final String title, final String body) {
        super(backgroundColor);
        _hoverEffect = false;
        setLayout(new GridBagLayout());
        setBorder(normalBorder);

        _normalBorder = normalBorder;
        _hoverBorder = hoverBorder;

        final DCLabel titleLabel = DCLabel.dark(title);
        titleLabel.setFont(WidgetUtils.FONT_BANNER);
        titleLabel.setForeground(COLOR_HOVER);
        titleLabel.setBorder(new EmptyBorder(12, MARGIN_LEFT, 4, 12));

        _bodyLabel = new JTextArea();
        _bodyLabel.setLineWrap(true);
        _bodyLabel.setWrapStyleWord(true);
        _bodyLabel.setText(body);
        _bodyLabel.setEditable(false);
        _bodyLabel.setOpaque(false);
        _bodyLabel.setFont(WidgetUtils.FONT_HEADER2);
        _bodyLabel.setForeground(COLOR_NORMAL);
        _bodyLabel.setBorder(new EmptyBorder(4, MARGIN_LEFT, 12, 12));

        GridBagConstraints c = new GridBagConstraints();

        if (icon != null) {
            c.gridx = 0;
            c.gridy = 0;
            c.gridheight = 3;
            c.insets = new Insets(5, MARGIN_LEFT, 5, 5);
            add(new JLabel(icon), c);
        }

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.LINE_START;
        add(titleLabel, c);

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.LINE_START;
        add(_bodyLabel, c);
    }

    public void addBelow(JComponent component) {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 3;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(0, MARGIN_LEFT, 5, 5);
        add(component, c);
    }

    private void installHoverEffect() {
        if (_hoverEffect) {
            // already installed
            return;
        }
        _hoverEffect = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        final MouseAdapter hoverMouseListener = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                _bodyLabel.setForeground(COLOR_HOVER);
                setBorder(_hoverBorder);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                _bodyLabel.setForeground(COLOR_NORMAL);
                setBorder(_normalBorder);
            }
        };
        addMouseListener(hoverMouseListener);
    }

    @Override
    public synchronized void addMouseListener(MouseListener mouseListener) {
        installHoverEffect();
        super.addMouseListener(mouseListener);
        _bodyLabel.addMouseListener(mouseListener);
    }

    @Override
    public synchronized void removeMouseListener(MouseListener mouseListener) {
        super.removeMouseListener(mouseListener);
        _bodyLabel.removeMouseListener(mouseListener);
    }
}
