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

import javax.swing.JLabel;

import org.datacleaner.Version;
import org.datacleaner.panels.CommunityEditionInformationPanel;
import org.datacleaner.panels.DCGlassPane;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetUtils;

/**
 * Label that shows the current edition of DataCleaner
 */
public class LicenceAndEditionStatusLabel extends JLabel {

    private static final long serialVersionUID = 1L;

    private static final String EDITION = Version.getEdition();

    private final CommunityEditionInformationPanel _communityEditionInformationPanel;

    public LicenceAndEditionStatusLabel(DCGlassPane glassPane) {
        super(EDITION);

        setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);

        if (Version.isCommunityEdition()) {
            _communityEditionInformationPanel = new CommunityEditionInformationPanel(glassPane);
            setIcon(ImageManager.get().getImageIcon("images/editions/community.png", IconUtils.ICON_SIZE_SMALL));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    onMouseClick();
                }
            });
        } else {
            setIcon(ImageManager.get().getImageIcon(IconUtils.APPLICATION_ICON, IconUtils.ICON_SIZE_SMALL));
            _communityEditionInformationPanel = null;
        }

    }

    protected void onMouseClick() {
        if (_communityEditionInformationPanel.isVisible()) {
            _communityEditionInformationPanel.moveOut(0);
        } else {
            _communityEditionInformationPanel.moveIn(0);
        }
    }
}
