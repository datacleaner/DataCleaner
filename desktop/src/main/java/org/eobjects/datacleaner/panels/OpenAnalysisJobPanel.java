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

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.Icon;

import org.apache.commons.vfs2.FileObject;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalysisJobMetadata;
import org.eobjects.analyzer.job.JaxbJobReader;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.Alignment;
import org.eobjects.datacleaner.widgets.DCLabel;

import com.google.common.base.Strings;

/**
 * Panel that is shown in the {@link WelcomePanel} to present a recently opened
 * {@link AnalysisJob}.
 */
public class OpenAnalysisJobPanel extends DCPanel {

    private static final long serialVersionUID = 1L;

    private static final Dimension PREFERRED_SIZE = new Dimension(278, 70);

    private final FileObject _file;

    public OpenAnalysisJobPanel(FileObject file, AnalyzerBeansConfiguration configuration) {
        super(WidgetUtils.BG_COLOR_LESS_BRIGHT, WidgetUtils.BG_COLOR_LESS_BRIGHT);
        _file = file;
        setLayout(new FlowLayout(Alignment.LEFT.getFlowLayoutAlignment()));
        setBorder(WidgetUtils.BORDER_LIST_ITEM);

        final AnalysisJobMetadata metadata = new JaxbJobReader(configuration).readMetadata(_file);
        final String jobName = metadata.getJobName();
        final String jobDescription = metadata.getJobDescription();

        final StringBuilder sb = new StringBuilder();

        final String title;
        if (Strings.isNullOrEmpty(jobName)) {
            final String filename = file.getName().getBaseName();
            final String extension = FileFilters.ANALYSIS_XML.getExtension();
            if (filename.toLowerCase().endsWith(extension)) {
                title = filename.substring(0, filename.length() - extension.length());
            } else {
                title = filename;
            }
        } else {
            title = jobName;
        }
        sb.append("<b>");
        sb.append(title);
        sb.append("</b>");

        if (!Strings.isNullOrEmpty(jobDescription)) {
            sb.append("<br/>");
            sb.append(jobDescription);
        }

        final Icon icon;
        final String datastoreName = metadata.getDatastoreName();
        if (!StringUtils.isNullOrEmpty(datastoreName)) {
            sb.append("<br/>");
            sb.append(datastoreName);

            final Datastore datastore = configuration.getDatastoreCatalog().getDatastore(datastoreName);
            icon = IconUtils.getDatastoreSpecificAnalysisJobIcon(datastore);
        } else {
            icon = IconUtils.getDatastoreSpecificAnalysisJobIcon(null);
        }

        final DCLabel label = DCLabel.dark("<html>" + sb + "</html>");
        label.setIconTextGap(10);
        label.setHorizontalAlignment(Alignment.LEFT.getLabelAlignment());
        label.setIcon(icon);
        add(label);
    }

    @Override
    public Dimension getPreferredSize() {
        return PREFERRED_SIZE;
    }
}
