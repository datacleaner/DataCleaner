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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
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

public class WelcomePanel extends DCSplashPanel {
    
    private static final Logger logger = LoggerFactory.getLogger(WelcomePanel.class);

    private static final long serialVersionUID = 1L;

    private final UserPreferences _userPreferences;
    private final AnalysisJobBuilderWindow _window;
    private final OpenAnalysisJobActionListener _openAnalysisJobActionListener;

    public WelcomePanel(final AnalysisJobBuilderWindow window, final UserPreferences userPreferences,
            final OpenAnalysisJobActionListener openAnalysisJobActionListener) {
        super();
        _window = window;
        _userPreferences = userPreferences;
        _openAnalysisJobActionListener = openAnalysisJobActionListener;

        setLayout(new BorderLayout());

        final DCLabel welcomeLabel = createTitleLabel("Welcome!");
        add(welcomeLabel, BorderLayout.NORTH);

        final JComponent wizardListPanel = createWizardListPanel();
        add(wizardListPanel, BorderLayout.CENTER);

        final DCPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);

    }

    private DCPanel createButtonPanel() {
        final JButton newJobButton = WidgetFactory.createPrimaryButton("New job from scratch", IconUtils.MODEL_JOB);
        newJobButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                _window.changePanel(AnalysisWindowPanelType.SELECT_DS);
            }
        });

        final PopupButton recentJobsButton = WidgetFactory.createDarkPopupButton("Recent jobs",
                IconUtils.FILE_HOME_FOLDER);

        final JButton browseJobsButton = WidgetFactory.createDarkButton("Browse jobs", IconUtils.FILE_FOLDER);
        browseJobsButton.addActionListener(_openAnalysisJobActionListener);

        final List<FileObject> recentJobFiles = getRecentJobFiles();
        final JPopupMenu recentJobsMenu = recentJobsButton.getMenu();
        for (int i = 0; i < recentJobFiles.size(); i++) {
            final FileObject jobFile = recentJobFiles.get(i);
            final JMenuItem menuItem = new OpenAnalysisJobMenuItem(jobFile, _openAnalysisJobActionListener);
            recentJobsMenu.add(menuItem);
        }

        final JButton manageDatastoresButton = WidgetFactory.createDarkButton("Manage datastores",
                IconUtils.GENERIC_DATASTORE_IMAGEPATH);
        manageDatastoresButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _window.changePanel(AnalysisWindowPanelType.MANAGE_DS);
            }
        });

        final DCPanel buttonPanel = new DCPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(newJobButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(browseJobsButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(recentJobsButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(manageDatastoresButton);
        buttonPanel.setBorder(new EmptyBorder(0, 0, 6, 0));
        
        return buttonPanel;
    }

    private JComponent createWizardListPanel() {
        final DCLabel subtitleLabel = DCLabel.bright("What's your question for DataCleaner?");
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

        final DCPanel wizardListPanel = new DCPanel();
        wizardListPanel.setLayout(new VerticalLayout(14));
        wizardListPanel.add(Box.createVerticalStrut(1));
        wizardListPanel.add(subtitleLabel);
        wizardListPanel.add(Box.createVerticalStrut(1));
        wizardListPanel.add(questionPanel1);
        wizardListPanel.add(questionPanel2);
        wizardListPanel.add(questionPanel3);
        wizardListPanel.add(Box.createVerticalStrut(1));

        return wrapContentInScrollerWithMaxWidth(wizardListPanel);
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
