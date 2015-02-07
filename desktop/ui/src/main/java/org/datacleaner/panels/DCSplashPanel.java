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

import java.awt.Dimension;
import java.awt.Image;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;

/**
 * Panel super class for those panels that show up as a splash in the welcoming
 * section of the application.
 */
public class DCSplashPanel extends DCPanel {

    private static final long serialVersionUID = 1L;
    private static final Image BACKGROUND_IMAGE = ImageManager.get().getImage(
            "images/window/welcome-panel-background.jpg");

    private static final int MAX_WIDTH = 900;

    public DCSplashPanel() {
        super(BACKGROUND_IMAGE, 50, 100, WidgetUtils.BG_COLOR_DARKEST);
    }

    /**
     * Creates a label for the title of the screen
     * 
     * @param string
     * @return
     */
    protected DCLabel createTitleLabel(String text) {
        DCLabel titleLabel = new DCLabel(false, text, WidgetUtils.BG_COLOR_BLUE_MEDIUM, null);
        titleLabel.setFont(WidgetUtils.FONT_BANNER);
        titleLabel.setBorder(new EmptyBorder(20, 20, 10, 0));
        return titleLabel;
    }

    /**
     * Wraps a content panel in a scroll pane and applies a maximum width to the
     * content to keep it nicely in place on the screen.
     * 
     * @param panel
     * @param maxWidth
     * @return
     */
    protected JComponent wrapContentInScrollerWithMaxWidth(DCPanel panel) {
        panel.setMaximumSize(new Dimension(MAX_WIDTH, Integer.MAX_VALUE));

        DCPanel wrappingPanel = new DCPanel();
        wrappingPanel.setLayout(new BoxLayout(wrappingPanel, BoxLayout.Y_AXIS));
        wrappingPanel.add(panel);

        JScrollPane scroll = WidgetUtils.scrolleable(wrappingPanel);
        return scroll;
    }
}
