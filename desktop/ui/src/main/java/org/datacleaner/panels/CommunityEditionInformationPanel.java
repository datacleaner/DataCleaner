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

import javax.swing.JButton;
import javax.swing.JPanel;

import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.action.OpenBrowserAction;

/**
 * A panel that shows information about the community edition and professional
 * edition
 */
public class CommunityEditionInformationPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Color _background = WidgetUtils.BG_COLOR_BRIGHTEST;
    private final Color _foreground = WidgetUtils.BG_COLOR_DARKEST;

    public CommunityEditionInformationPanel() {
        super();
        setLayout(new VerticalLayout(10));

        DCLabel header = DCLabel.darkMultiLine("You are right now using\n" + "DataCleaner community edition");
        header.setFont(WidgetUtils.FONT_HEADER1);
        header.setIcon(ImageManager.get().getImageIcon("images/editions/community.png"));
        add(header);

        DCLabel text1 = DCLabel
                .darkMultiLine("We are happy that you are trying out the community edition of DataCleaner. Please be aware that this product is not commercially supported and although there is an open source community to help you, we recommend getting the professional edition if you are employing DataCleaner in a commercial setting.");
        add(text1);

        DCLabel text2 = DCLabel
                .darkMultiLine("With DataCleaner professional edition you also get additional goodies; such as national identifier checks, duplicate detection, DQ metric exports and more.");
        add(text2);

        JButton tryProfessionalButton = WidgetFactory.createDefaultButton("Try professional edition",
                IconUtils.APPLICATION_ICON);
        tryProfessionalButton.addActionListener(new OpenBrowserAction("https://datacleaner.org/get_datacleaner"));
        add(DCPanel.around(tryProfessionalButton));

        JButton compareEditionsButton = WidgetFactory.createDefaultButton("Compare the editions", IconUtils.WEBSITE);
        compareEditionsButton.addActionListener(new OpenBrowserAction("https://datacleaner.org/editions"));
        add(DCPanel.around(compareEditionsButton));
    }

    @Override
    public Color getBackground() {
        return _background;
    }

    @Override
    public Color getForeground() {
        return _foreground;
    }
}
