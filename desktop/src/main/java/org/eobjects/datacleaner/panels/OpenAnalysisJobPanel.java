/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.panels;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.apache.commons.vfs2.FileObject;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalysisJobMetadata;
import org.eobjects.analyzer.job.JaxbJobReader;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.actions.OpenAnalysisJobActionListener;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;

import com.google.common.base.Strings;

/**
 * Panel that is shown in the {@link WelcomePanel} to present a recently opened
 * {@link AnalysisJob}.
 */
public class OpenAnalysisJobPanel extends DCPanel {

    private static final long serialVersionUID = 1L;

    private static final Dimension PREFERRED_SIZE = new Dimension(278, 75);

    private final FileObject _file;

    private OpenAnalysisJobActionListener _openAnalysisJobActionListener;

    public OpenAnalysisJobPanel(final FileObject file, final AnalyzerBeansConfiguration configuration,
            final OpenAnalysisJobActionListener openAnalysisJobActionListener) {
        super(WidgetUtils.BG_COLOR_LESS_BRIGHT, WidgetUtils.BG_COLOR_LESS_BRIGHT);
        _file = file;
        _openAnalysisJobActionListener = openAnalysisJobActionListener;

        setLayout(new HorizontalLayout(4));
        setBorder(WidgetUtils.BORDER_LIST_ITEM);

        final AnalysisJobMetadata metadata = new JaxbJobReader(configuration).readMetadata(_file);
        final String jobName = metadata.getJobName();
        final String jobDescription = metadata.getJobDescription();

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
        titleButton.setToolTipText(filename);
        titleButton.setOpaque(false);
        titleButton.setMargin(new Insets(0, 0, 0, 0));
        titleButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        titleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _openAnalysisJobActionListener.openFile(_file);
            }
        });
        labelListPanel.add(DCPanel.around(titleButton));

        if (!Strings.isNullOrEmpty(jobDescription)) {
            String desc = StringUtils.replaceWhitespaces(jobDescription, " ");
            desc = StringUtils.replaceAll(desc, "  ", " ");
            final JLabel label = new JLabel(desc);
            label.setFont(WidgetUtils.FONT_SMALL);
            labelListPanel.add(label);
        }

        final Icon icon;
        final String datastoreName = metadata.getDatastoreName();
        if (!StringUtils.isNullOrEmpty(datastoreName)) {
            final JLabel label = new JLabel("» " + datastoreName);
            label.setFont(WidgetUtils.FONT_SMALL);
            labelListPanel.add(label);

            final Datastore datastore = configuration.getDatastoreCatalog().getDatastore(datastoreName);
            icon = IconUtils.getDatastoreSpecificAnalysisJobIcon(datastore);
        } else {
            icon = ImageManager.get().getImageIcon(IconUtils.MODEL_JOB, IconUtils.ICON_SIZE_LARGE);
        }

        add(new JLabel(icon));
        add(labelListPanel);
    }

    @Override
    public Dimension getPreferredSize() {
        return PREFERRED_SIZE;
    }
}
