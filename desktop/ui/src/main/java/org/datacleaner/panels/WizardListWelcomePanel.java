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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.inject.Inject;
import javax.swing.Box;

import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.windows.AnalysisJobBuilderWindow;
import org.datacleaner.windows.AnalysisJobBuilderWindow.AnalysisWindowPanelType;
import org.jdesktop.swingx.VerticalLayout;

/**
 * A pluggable content part for the {@link WelcomePanel}'s content that will
 * show a list of selectable quick start wizards.
 */
public class WizardListWelcomePanel extends DCPanel {

    private static final long serialVersionUID = 1L;

    private final AnalysisJobBuilderWindow _window;

    @Inject
    public WizardListWelcomePanel(AnalysisJobBuilderWindow window) {
        super();
        setLayout(new VerticalLayout(14));

        _window = window;

        final DCLabel subtitleLabel = DCLabel.bright("What questions about your data can we help you with?");
        subtitleLabel.setFont(WidgetUtils.FONT_HEADER1);
        subtitleLabel.setBorder(WidgetUtils.BORDER_EMPTY);
        final DetailedListItemPanel questionPanel1 = new DetailedListItemPanel(
                "<html>Are my <b>addresses correct</b> and <b>up-to-date</b>?</html>",
                "Use the Neopost Address Correction and Mail Suppression services on your contact list to correct your addresses and check if people have moved to new places or if they have passed away.");
        questionPanel1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                _window.changePanel(AnalysisWindowPanelType.SELECT_DS);
            }
        });

        final DetailedListItemPanel questionPanel2 = new DetailedListItemPanel(
                "<html>Do I have <b>duplicate</b> customers?</html>",
                "Inspect your customers with DataCleaner’s Duplicate Detection function to identify the possible duplicated records in your database or file.");

        final DetailedListItemPanel questionPanel3 = new DetailedListItemPanel(
                "<html>Are my records properly <b>filled</b>?</html>",
                "Validate the proper completeness and conformity with rules of your records. Use this wizard to configure common data profiling features to suit the fields of your data set.");

        add(Box.createVerticalStrut(1));
        add(subtitleLabel);
        add(Box.createVerticalStrut(1));
        add(questionPanel1);
        add(questionPanel2);
        add(questionPanel3);
        add(Box.createVerticalStrut(1));
    }
}
