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

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public final class Spinner {
    private static final String ICON_PATH = "images/status/loading-bar.gif";
    private static JFrame frame = null;

    private Spinner() {
    }

    private static JFrame getFrame() {
        if (frame == null) {
            frame = new JFrame("Loading...");
            ImageIcon image = new ImageIcon(Spinner.class.getClassLoader().getResource(ICON_PATH));
            JLabel label = new JLabel("", image, JLabel.CENTER);
            frame.add(label);

            final Toolkit toolkit = Toolkit.getDefaultToolkit();
            final Dimension screenSize = toolkit.getScreenSize();
            int iconWidth = image.getIconWidth();
            int iconHeight = image.getIconHeight();
            final int x = (screenSize.width - frame.getWidth() - iconWidth) / 2;
            final int y = (screenSize.height - frame.getHeight() - iconHeight) / 2;

            frame.setBounds(x, y, iconWidth, iconHeight);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setUndecorated(true);
        }

        return frame;
    }

    public static void showSpinner() {
        getFrame().setVisible(true);
    }

    public static void hideSpinner() {
        getFrame().setVisible(false);
    }
}
