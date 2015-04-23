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
import java.awt.Cursor;
import java.awt.Dimension;
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
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.datacleaner.Version;
import org.datacleaner.actions.OpenAnalysisJobActionListener;
import org.datacleaner.guice.InjectorBuilder;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.SystemProperties;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.OpenAnalysisJobMenuItem;
import org.datacleaner.widgets.PopupButton;
import org.datacleaner.widgets.PopupButton.MenuPosition;
import org.datacleaner.windows.AnalysisJobBuilderWindow;
import org.datacleaner.windows.AnalysisJobBuilderWindow.AnalysisWindowPanelType;
import org.jdesktop.swingx.JXEditorPane;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.action.OpenBrowserAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.net.UrlEscapers;
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
    private final JComponent _buttonPanel;
    private final JComponent _titleLabel;

    public WelcomePanel(final AnalysisJobBuilderWindow window, final UserPreferences userPreferences,
            final OpenAnalysisJobActionListener openAnalysisJobActionListener, final InjectorBuilder injectorBuilder) {
        super(window);
        _userPreferences = userPreferences;
        _openAnalysisJobActionListener = openAnalysisJobActionListener;
        _injectorBuilder = injectorBuilder;

        _titleLabel = createTitleLabel("Welcome to DataCleaner", false);
        _buttonPanel = createButtonPanel();

        final JComponent contentPanel = createContentPanel();

        setLayout(new BorderLayout());
        add(_titleLabel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(_buttonPanel, BorderLayout.SOUTH);
    }

    public JComponent getButtonPanel() {
        return _buttonPanel;
    }

    public JComponent getTitleLabel() {
        return _titleLabel;
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
            if (Version.isCommunityEdition()) {
                final JXEditorPane editorPane = new JXEditorPane("text/html",
                        "You're now using the <b>Community Edition</b> of DataCleaner.<br/><br/>"
                                + "We hope that you enjoy this free product. We encourage you to also check out the "
                                + "commercial DataCleaner editions which feature added functionality, "
                                + "helpful getting started wizards and commercial support. "
                                + "You can find more information about them online.");
                editorPane.setEditable(false);
                editorPane.setOpaque(false);
                editorPane.setFont(WidgetUtils.FONT_HEADER2);
                editorPane.setPreferredSize(new Dimension(DCSplashPanel.WIDTH_CONTENT, 120));

                final JButton tryProfessionalButton = WidgetFactory.createDefaultButton("Try professional edition",
                        IconUtils.APPLICATION_ICON);
                tryProfessionalButton
                        .addActionListener(new OpenBrowserAction("http://datacleaner.org/get_datacleaner"));

                final JButton readMoreButton = WidgetFactory.createDefaultButton("Compare the editions",
                        IconUtils.WEBSITE);
                readMoreButton.addActionListener(new OpenBrowserAction("http://datacleaner.org/editions"));

                final JButton discussionForumButton = WidgetFactory.createDefaultButton("Visit the discussion forum",
                        "images/menu/forum.png");
                discussionForumButton
                        .setToolTipText("Visit the online discussion forum for questions and answers in the community");
                final OpenBrowserAction forumActionListener = new OpenBrowserAction("http://datacleaner.org/forum");
                discussionForumButton.addActionListener(forumActionListener);

                final JButton twitterButton = WidgetFactory.createDefaultButton(null, "images/menu/twitter.png");
                twitterButton.setToolTipText("Spread the message about #DataCleaner on Twitter");
                twitterButton.addActionListener(new OpenBrowserAction("https://twitter.com/intent/tweet?text="
                        + UrlEscapers.urlFormParameterEscaper().escape(
                                "I'm using @DataCleaner (v. " + Version.getVersion()
                                        + ") for some really fancy #dataquality stuff!")));

                final JButton linkedInButton = WidgetFactory.createDefaultButton(null, "images/menu/linkedin.png");
                linkedInButton.setToolTipText("Join our LinkedIn group of users and professionals");
                linkedInButton.addActionListener(new OpenBrowserAction("http://www.linkedin.com/groups?gid=3352784"));

                final JLabel loveFeedbackAnimation = new JLabel(ImageManager.get().getImageIcon(
                        "images/window/we_love_community_and_feedback.gif"), JLabel.LEFT);
                loveFeedbackAnimation.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                loveFeedbackAnimation.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        forumActionListener.actionPerformed(null);
                    }
                });

                final DCPanel innerPanel = new DCPanel();
                innerPanel.setLayout(new VerticalLayout());
                innerPanel.setBorder(new CompoundBorder(WidgetUtils.BORDER_LIST_ITEM_LEFT_ONLY, new EmptyBorder(0, 20,
                        0, 0)));
                innerPanel.add(editorPane);
                innerPanel.add(DCPanel.flow(tryProfessionalButton, readMoreButton));
                innerPanel.add(Box.createVerticalStrut(80));
                innerPanel.add(loveFeedbackAnimation);
                innerPanel.add(Box.createVerticalStrut(20));
                innerPanel.add(DCPanel.flow(discussionForumButton, twitterButton, linkedInButton));
                innerPanel.add(Box.createVerticalStrut(5));

                result.setLayout(new VerticalLayout());
                result.add(Box.createVerticalStrut(100));
                result.add(innerPanel);
            }
        }

        return wrapContent(result);
    }

    private JComponent createButtonPanel() {
        final String newJobText = SystemProperties.getString(SystemProperties.UI_DESKTOP_TEXT_NEW_JOB_BUTTON,
                "Build new job");
        final JButton newJobButton = WidgetFactory.createPrimaryButton(newJobText, IconUtils.MODEL_JOB);
        newJobButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                getWindow().changePanel(AnalysisWindowPanelType.SELECT_DS);
            }
        });

        final PopupButton recentJobsButton = WidgetFactory.createDefaultPopupButton("Recent jobs",
                IconUtils.FILE_HOME_FOLDER);
        recentJobsButton.setMenuPosition(MenuPosition.TOP);
        recentJobsButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                refreshRecentJobs(recentJobsButton);
            }
        });
        final JButton browseJobsButton = WidgetFactory.createDefaultButton("Browse jobs", IconUtils.FILE_FOLDER);
        browseJobsButton.addActionListener(_openAnalysisJobActionListener);

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

    private void refreshRecentJobs(final PopupButton recentJobsButton) {
        final List<FileObject> recentJobFiles = getRecentJobFiles();
        final JPopupMenu recentJobsMenu = recentJobsButton.getMenu();
        /*
         * The menu is rebuild every time the user clicks on the menu, so the
         * content is removed so that we do not have duplicates
         */
        recentJobsMenu.removeAll();

        for (int i = 0; i < recentJobFiles.size(); i++) {
            final FileObject jobFile = recentJobFiles.get(i);
            final JMenuItem menuItem = new OpenAnalysisJobMenuItem(jobFile, _openAnalysisJobActionListener);
            recentJobsMenu.add(menuItem);
        }
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
