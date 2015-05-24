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

import java.awt.Dimension;
import java.awt.image.ImageObserver;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.datacleaner.util.ImageManager;

public class LoadingIcon extends JLabel {

    private static final long serialVersionUID = 1L;

    public static final String ICON_PATH = "images/status/loading.gif";

    /**
     * @deprecated instantiate a new {@link ImageIcon} based on
     *             {@link #ICON_PATH} because the animation requires a single
     *             {@link ImageObserver} which means that the icon should not be
     *             shared.
     */
    @Deprecated
    public static final ImageIcon ICON = ImageManager.get().getImageIcon(ICON_PATH);

    public LoadingIcon() {
        super();
        ImageIcon icon = ImageManager.get().getImageIcon(ICON_PATH);
        setIcon(icon);
        icon.setImageObserver(this);
        setHorizontalAlignment(JLabel.CENTER);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ICON.setImageObserver(null);
    }

    public LoadingIcon setPreferredSize(int w, int h) {
        setPreferredSize(new Dimension(w, h));
        return this;
    }
}
