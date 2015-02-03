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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.datacleaner.actions.OpenAnalysisJobActionListener;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.OpenAnalysisJobMenuItem;
import org.datacleaner.widgets.PopupButton;
import org.datacleaner.windows.AnalysisJobBuilderWindow;
import org.datacleaner.windows.AnalysisJobBuilderWindow.AnalysisWindowPanelType;
import org.jdesktop.swingx.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WelcomePanel extends DCPanel {
    private static final Logger logger = LoggerFactory.getLogger(WelcomePanel.class);
    
    private static final long serialVersionUID = 1L;

    private static final Font WELCOME_BANNER_FONT = WidgetUtils.FONT_UBUNTU_PLAIN.deriveFont(30f);
    private static final Font WELCOME_SUBBANNER_FONT = WidgetUtils.FONT_UBUNTU_PLAIN.deriveFont(24f);
    private static final Font WELCOME_OTHER_OPTIONS_FONT = WidgetUtils.FONT_HEADER2;
    
    private UserPreferences _userPreferences;

    public WelcomePanel(final AnalysisJobBuilderWindow window, final UserPreferences userPreferences, final OpenAnalysisJobActionListener openAnalysisJobActionListener) {
        super(WidgetUtils.COLOR_WELL_BACKGROUND);
        this._userPreferences = userPreferences;
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setLayout(new VerticalLayout(10));

        final DCLabel welcomeLabel = new DCLabel(false, "Welcome!", WidgetUtils.BG_COLOR_BLUE_MEDIUM, null);
        welcomeLabel.setFont(WELCOME_BANNER_FONT);
        add(welcomeLabel);

        final DCLabel subtitleLabel = DCLabel.dark("What's your question for DataCleaner?");
        subtitleLabel.setFont(WELCOME_SUBBANNER_FONT);
        add(subtitleLabel);
        subtitleLabel.setBorder(WidgetUtils.BORDER_EMPTY);
        final DetailPanel questionPanel1 = new DetailPanel(
                "<html>Are my <b>addresses correct</b> and <b>up-to-date</b>?</html>",
                "Use the Neopost Address Correction and Mail Suppression services on your contact list to correct your addresses and check if people have moved to new places or if they have passed away.");
        questionPanel1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                window.changePanel(AnalysisWindowPanelType.SELECT_DS);
            }
        });
        add(questionPanel1);

        final DetailPanel questionPanel2 = new DetailPanel(
                "<html>Do I have <b>duplicate</b> customers?</html>",
                "Inspect your customers with DataCleaner’s Duplicate Detection function to identify the possible duplicated records in your database or file.");
        add(questionPanel2);

        final DetailPanel questionPanel3 = new DetailPanel(
                "<html>Are my records properly <b>filled</b>?</html>",
                "Validate the proper completeness and conformity with rules of your records. Use this wizard to configure common data profiling features to suit the fields of your data set.");
        add(questionPanel3);

        final DCLabel otherOptionsLabel = DCLabel.dark("Other options");
        otherOptionsLabel.setFont(WELCOME_OTHER_OPTIONS_FONT);
        add(otherOptionsLabel);

        final JButton newJobButton = new JButton("New job from scratch");
        newJobButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                window.changePanel(AnalysisWindowPanelType.SELECT_DS);
            }
        });
        WidgetUtils.setPrimaryButtonStyle(newJobButton);

        PopupButton recentJobsButton = WidgetFactory
                .createDefaultPopupButton("Recent jobs", IconUtils.FILE_HOME_FOLDER);
        
        JButton browseJobsButton = WidgetFactory.createDefaultButton("Browse jobs", IconUtils.FILE_FOLDER);
        browseJobsButton.addActionListener(openAnalysisJobActionListener);

        List<FileObject> recentJobFiles = getRecentJobFiles();
        final JPopupMenu recentJobsMenu = recentJobsButton.getMenu();
        for (int i = 0; i < recentJobFiles.size(); i++) {
            final FileObject jobFile = recentJobFiles.get(i);
            final JMenuItem menuItem = new OpenAnalysisJobMenuItem(jobFile, openAnalysisJobActionListener);
            recentJobsMenu.add(menuItem);
        }

        final JButton manageDatastoresButton = new JButton("Manage datastores");
        manageDatastoresButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                window.changePanel(AnalysisWindowPanelType.MANAGE_DS);
            }
        });
        WidgetUtils.setDefaultButtonStyle(manageDatastoresButton);
        
        DCPanel buttonPanel = new DCPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
        buttonPanel.add(newJobButton);
        buttonPanel.add(browseJobsButton);
        buttonPanel.add(recentJobsButton);
        buttonPanel.add(manageDatastoresButton);
        add(buttonPanel);
    }

    private List<FileObject> getRecentJobFiles() {
        final List<FileObject> recentJobFiles = _userPreferences.getRecentJobFiles();
        final List<FileObject> result = new ArrayList<>();
        for (FileObject fileObject : recentJobFiles) {
            try {
                if (fileObject.exists()) {
                    result.add(fileObject);
                    if (result.size() == 10) {
                        break;
                    }
                }
            } catch (FileSystemException ex) {
                logger.debug("Skipping file {} because of unexpected error", ex);
            }
        }
        return result;
    }

}
