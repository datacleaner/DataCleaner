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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.apache.commons.vfs2.FileObject;
import org.datacleaner.actions.OpenAnalysisJobActionListener;
import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.AnalysisJobMetadata;
import org.datacleaner.job.JaxbJobReader;
import org.datacleaner.user.DemoConfiguration;
import org.datacleaner.util.FileFilters;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.windows.ResultWindow;
import org.jdesktop.swingx.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.inject.Injector;

/**
 * Panel that is shown in the {@link DatastoreManagementPanel} to present a recently opened
 * {@link AnalysisJob}.
 */
public class OpenAnalysisJobPanel extends DCPanel {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(OpenAnalysisJobPanel.class);
    private static final Dimension PREFERRED_SIZE = new Dimension(278, 75);

    private static final Icon demoBadgeIcon = ImageManager.get().getImageIcon("images/window/badge-demo.png");
    private static final Icon executeIcon = ImageManager.get().getImageIcon(IconUtils.ACTION_EXECUTE,
            IconUtils.ICON_SIZE_SMALL);

    private final FileObject _file;

    private OpenAnalysisJobActionListener _openAnalysisJobActionListener;

    public OpenAnalysisJobPanel(final FileObject file, final AnalyzerBeansConfiguration configuration,
            final OpenAnalysisJobActionListener openAnalysisJobActionListener) {
        super(WidgetUtils.COLOR_WELL_BACKGROUND);
        _file = file;
        _openAnalysisJobActionListener = openAnalysisJobActionListener;

        setLayout(new BorderLayout());
        setBorder(WidgetUtils.BORDER_LIST_ITEM);

        final AnalysisJobMetadata metadata = getMetadata(configuration);
        final String jobName = metadata.getJobName();
        final String jobDescription = metadata.getJobDescription();
        final String datastoreName = metadata.getDatastoreName();

        final boolean isDemoJob = isDemoJob(metadata);

        final DCPanel labelListPanel = new DCPanel();
        labelListPanel.setLayout(new VerticalLayout(4));
        labelListPanel.setBorder(new EmptyBorder(4, 4, 4, 0));

        final String title;
        final String filename = file.getName().getBaseName();
        if (Strings.isNullOrEmpty(jobName)) {
            final String extension = FileFilters.ANALYSIS_XML.getExtension();
            if (filename.toLowerCase().endsWith(extension)) {
                title = filename.substring(0, filename.length() - extension.length());
            } else {
                title = filename;
            }
        } else {
            title = jobName;
        }

        final JButton titleButton = new JButton(title);
        titleButton.setFont(WidgetUtils.FONT_HEADER1);
        titleButton.setForeground(WidgetUtils.BG_COLOR_BLUE_MEDIUM);
        titleButton.setHorizontalAlignment(SwingConstants.LEFT);
        titleButton.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, WidgetUtils.BG_COLOR_MEDIUM));
        titleButton.setToolTipText("Open job");
        titleButton.setOpaque(false);
        titleButton.setMargin(new Insets(0, 0, 0, 0));
        titleButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        titleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isDemoDatastoreConfigured(datastoreName, configuration)) {
                    _openAnalysisJobActionListener.openFile(_file);
                }
            }
        });

        final JButton executeButton = new JButton(executeIcon);
        executeButton.setOpaque(false);
        executeButton.setToolTipText("Execute job directly");
        executeButton.setMargin(new Insets(0, 0, 0, 0));
        executeButton.setBorderPainted(false);
        executeButton.setHorizontalAlignment(SwingConstants.RIGHT);
        executeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isDemoDatastoreConfigured(datastoreName, configuration)) {
                    final ImageIcon executeIconLarge = ImageManager.get().getImageIcon(IconUtils.ACTION_EXECUTE);
                    final String question = "Are you sure you want to execute the job\n'" + title + "'?";
                    final int choice = JOptionPane.showConfirmDialog(null, question, "Execute job?",
                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, executeIconLarge);
                    if (choice == JOptionPane.YES_OPTION) {
                        final Injector injector = _openAnalysisJobActionListener.openAnalysisJob(_file);
                        if (injector != null) {
                            final ResultWindow resultWindow = injector.getInstance(ResultWindow.class);
                            resultWindow.open();
                            resultWindow.startAnalysis();
                        }
                    }
                }
            }
        });

        final DCPanel titlePanel = new DCPanel();
        titlePanel.setLayout(new BorderLayout());
        titlePanel.add(DCPanel.around(titleButton), BorderLayout.CENTER);
        titlePanel.add(executeButton, BorderLayout.EAST);

        labelListPanel.add(titlePanel);

        if (!Strings.isNullOrEmpty(jobDescription)) {
            String desc = StringUtils.replaceWhitespaces(jobDescription, " ");
            desc = StringUtils.replaceAll(desc, "  ", " ");
            final JLabel label = new JLabel(desc);
            label.setFont(WidgetUtils.FONT_SMALL);
            labelListPanel.add(label);
        }

        final Icon icon;
        {
            if (!StringUtils.isNullOrEmpty(datastoreName)) {
                final JLabel label = new JLabel("» " + datastoreName);
                label.setFont(WidgetUtils.FONT_SMALL);
                labelListPanel.add(label);

                final Datastore datastore = configuration.getDatastoreCatalog().getDatastore(datastoreName);
                if (isDemoJob) {
                    icon = demoBadgeIcon;
                } else {
                    icon = IconUtils.getDatastoreSpecificAnalysisJobIcon(datastore);
                }
            } else {
                icon = ImageManager.get().getImageIcon(IconUtils.MODEL_JOB, IconUtils.ICON_SIZE_LARGE);
            }
        }

        final JLabel iconLabel = new JLabel(icon);
        iconLabel.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));

        add(iconLabel, BorderLayout.WEST);
        add(labelListPanel, BorderLayout.CENTER);
    }

    private AnalysisJobMetadata getMetadata(AnalyzerBeansConfiguration configuration) {
        try {
            return new JaxbJobReader(configuration).readMetadata(_file);
        } catch (Exception e) {
            logger.warn("Failed to read metadata from file '" + _file + "'", e);
            return AnalysisJobMetadata.EMPTY_METADATA;
        }
    }

    private boolean isDemoJob(AnalysisJobMetadata metadata) {
        final Map<String, String> metadataProperties = metadata.getProperties();
        if (metadataProperties != null) {
            if ("true".equals(metadataProperties.get("DemoJob"))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Dimension getPreferredSize() {
        return PREFERRED_SIZE;
    }

    private boolean isDemoDatastoreConfigured(String datastoreName, AnalyzerBeansConfiguration configuration) {
        if (Strings.isNullOrEmpty(datastoreName)) {
            // datastore ISN'T configured, but it's not a demo datastore, so we
            // let it pass here.
            return true;
        }

        final Datastore datastore = configuration.getDatastoreCatalog().getDatastore(datastoreName);
        if (datastore == null) {
            // datastore doesn't exist, but it's not a demo datastore, so we let
            // it pass here.
            return true;
        }

        final boolean stopTheUser = DemoConfiguration.isUnconfiguredDemoDatastore(datastore);
        if (stopTheUser) {
            WidgetUtils.showErrorMessage("Datastore not configured", "Please configure the datastore '" + datastoreName
                    + "' before using the demo job.");
            return false;
        } else {
            return true;
        }
    }
}
