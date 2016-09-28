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
package org.datacleaner.widgets;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.datacleaner.panels.RightInformationPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetUtils;

public class PlugableRightPanelLabel extends JLabel {

    private static final long serialVersionUID = 1L;
    private final RightInformationPanel _rightPanel;
    private final String _labelName;

    public PlugableRightPanelLabel(RightInformationPanel rightPanel, PlugabblePanel pluggedPanel) {
        super(pluggedPanel.getName());
        setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);

        _labelName = pluggedPanel.getName();
        _rightPanel = rightPanel;
        _rightPanel.addTabToPane(_labelName, pluggedPanel);

        final ImageIcon imageIcon = ImageManager.get().getImageIcon(pluggedPanel.getImagePath(),
                IconUtils.ICON_SIZE_SMALL);
        setIcon(imageIcon);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onMouseClick();
            }
        });
    }

    private void onMouseClick() {
        _rightPanel.toggleWindow(_labelName);
    }

}
