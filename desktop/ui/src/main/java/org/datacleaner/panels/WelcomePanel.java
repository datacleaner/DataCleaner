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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.jdesktop.swingx.VerticalLayout;

public class WelcomePanel extends DCPanel {

    private static final long serialVersionUID = 1L;

    public WelcomePanel() {
        super(WidgetUtils.COLOR_WELL_BACKGROUND);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setLayout(new VerticalLayout(10));

        final DCLabel welcomeLabel = new DCLabel(false, "Welcome!", WidgetUtils.BG_COLOR_BLUE_MEDIUM, null);
        welcomeLabel.setFont(WidgetUtils.FONT_HEADER1);
        add(welcomeLabel);

        final DCLabel subtitleLabel = DCLabel.dark("What's your question for DataCleaner?");
        subtitleLabel.setFont(WidgetUtils.FONT_HEADER2);
        add(subtitleLabel);
        subtitleLabel.setBorder(WidgetUtils.BORDER_EMPTY);
        final WelcomeQuestionPanel questionPanel1 = new WelcomeQuestionPanel(
                "<html>Are my <b>addresses correct</b> and <b>up-to-date</b>?</html>",
                "Use the Neopost Address Correction and Mail Suppression services on your contact list to correct your addresses and check if people have moved to new places or if they have passed away.");
        add(questionPanel1);

        final WelcomeQuestionPanel questionPanel2 = new WelcomeQuestionPanel(
                "<html>Do I have <b>duplicate</b> customers?</html>",
                "Inspect your customers with DataCleaner’s Duplicate Detection function to identify the possible duplicated records in your database or file.");
        add(questionPanel2);

        final WelcomeQuestionPanel questionPanel3 = new WelcomeQuestionPanel(
                "<html>Are my records properly <b>filled</b>?</html>",
                "Validate the proper completeness and conformity with rules of your records. Use this wizard to configure common data profiling features to suit the fields of your data set.");
        add(questionPanel3);

        final DCLabel otherOptionsLabel = DCLabel.dark("Other options");
        otherOptionsLabel.setFont(WidgetUtils.FONT_HEADER2);
        add(otherOptionsLabel);

        final JButton newJobButton = new JButton("New job from scratch");
        WidgetUtils.setPrimaryButtonStyle(newJobButton);

        final JButton openJobButton = new JButton("Open job");
        WidgetUtils.setDefaultButtonStyle(openJobButton);

        final JButton recentJobsButton = new JButton("Recent jobs");
        WidgetUtils.setDefaultButtonStyle(recentJobsButton);

        final JButton manageDatastoresButton = new JButton("Manage datastores");
        manageDatastoresButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                
            }
        });
        WidgetUtils.setDefaultButtonStyle(manageDatastoresButton);
        
        DCPanel buttonPanel = new DCPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
        buttonPanel.add(newJobButton);
        buttonPanel.add(openJobButton);
        buttonPanel.add(recentJobsButton);
        buttonPanel.add(manageDatastoresButton);
        add(buttonPanel);
    }

}
