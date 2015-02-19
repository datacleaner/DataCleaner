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
import org.datacleaner.guice.InjectorBuilder;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.SystemProperties;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.widgets.OpenAnalysisJobMenuItem;
import org.datacleaner.widgets.PopupButton;
import org.datacleaner.widgets.PopupButton.MenuPosition;
import org.datacleaner.windows.AnalysisJobBuilderWindow;
import org.datacleaner.windows.AnalysisJobBuilderWindow.AnalysisWindowPanelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.inject.Injector;

/**
 * The initial panel that is shown to the user when starting the application.
 * This panel features a pluggable content part (see
 * {@link SystemProperties#UI_DESKTOP_WELCOME_PANEL} and a button panels where a
 * new job can be started, an existing job can be opened and datastores can be
 * managed.
 */
public class WelcomePanel extends DCSplashPanel {

    private static final Logger logger = LoggerFactory.getLogger(WelcomePanel.class);

    private static final long serialVersionUID = 1L;

    private final UserPreferences _userPreferences;
    private final OpenAnalysisJobActionListener _openAnalysisJobActionListener;
    private final InjectorBuilder _injectorBuilder;

    public WelcomePanel(final AnalysisJobBuilderWindow window, final UserPreferences userPreferences,
            final OpenAnalysisJobActionListener openAnalysisJobActionListener, final InjectorBuilder injectorBuilder) {
        super(window);
        _userPreferences = userPreferences;
        _openAnalysisJobActionListener = openAnalysisJobActionListener;
        _injectorBuilder = injectorBuilder;

        setLayout(new BorderLayout());

        final JComponent welcomeLabel = createTitleLabel("Welcome to DataCleaner", false);
        add(welcomeLabel, BorderLayout.NORTH);

        final JComponent contentPanel = createContentPanel();
        add(contentPanel, BorderLayout.CENTER);

        final JComponent buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JComponent createContentPanel() {
        JComponent result = null;

        final String welcomePanelClassName = SystemProperties
                .getString(SystemProperties.UI_DESKTOP_WELCOME_PANEL, null);
        if (!Strings.isNullOrEmpty(welcomePanelClassName)) {
            final Injector injector = _injectorBuilder.with(WelcomePanel.class, this).createInjector();
            try {
                @SuppressWarnings("unchecked")
                final Class<? extends JComponent> componentClass = (Class<? extends JComponent>) Class
                        .forName(welcomePanelClassName);

                result = injector.getInstance(componentClass);
            } catch (Exception e) {
                logger.error("Failed to instantiate welcome panel class: {}", welcomePanelClassName, e);
            }
        }

        if (result == null) {
            result = new DCPanel();
        }

        return wrapContent(result);
    }

    private JComponent createButtonPanel() {
        final JButton newJobButton = WidgetFactory.createPrimaryButton("New job from scratch", IconUtils.MODEL_JOB);
        newJobButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                getWindow().changePanel(AnalysisWindowPanelType.SELECT_DS);
            }
        });

        final PopupButton recentJobsButton = WidgetFactory.createDefaultPopupButton("Recent jobs",
                IconUtils.FILE_HOME_FOLDER);
        recentJobsButton.setMenuPosition(MenuPosition.TOP);

        final JButton browseJobsButton = WidgetFactory.createDefaultButton("Browse jobs", IconUtils.FILE_FOLDER);
        browseJobsButton.addActionListener(_openAnalysisJobActionListener);

        final List<FileObject> recentJobFiles = getRecentJobFiles();
        final JPopupMenu recentJobsMenu = recentJobsButton.getMenu();
        for (int i = 0; i < recentJobFiles.size(); i++) {
            final FileObject jobFile = recentJobFiles.get(i);
            final JMenuItem menuItem = new OpenAnalysisJobMenuItem(jobFile, _openAnalysisJobActionListener);
            recentJobsMenu.add(menuItem);
        }

        final JButton manageDatastoresButton = WidgetFactory.createDefaultButton("Manage datastores",
                IconUtils.GENERIC_DATASTORE_IMAGEPATH);
        manageDatastoresButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getWindow().changePanel(AnalysisWindowPanelType.MANAGE_DS);
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
        buttonPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        return wrapContent(buttonPanel);
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
