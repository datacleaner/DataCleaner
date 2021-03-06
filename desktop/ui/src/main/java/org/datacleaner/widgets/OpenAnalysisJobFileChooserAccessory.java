/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
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
package org.datacleaner.widgets;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.inject.Provider;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import org.apache.commons.vfs2.FileObject;
import org.datacleaner.actions.OpenAnalysisJobActionListener;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.job.AnalysisJobMetadata;
import org.datacleaner.job.JaxbJobReader;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.FileFilters;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetScreenResolutionAdjuster;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.windows.AnalysisJobBuilderWindow;
import org.datacleaner.windows.OpenAnalysisJobAsTemplateDialog;
import org.jdesktop.swingx.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

/**
 * The FileChooser "accessory" that will display analysis job information when
 * the user selected a job file.
 */
public class OpenAnalysisJobFileChooserAccessory extends DCPanel implements PropertyChangeListener {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(OpenAnalysisJobFileChooserAccessory.class);
    private static final ImageManager imageManager = ImageManager.get();
    private static final WidgetScreenResolutionAdjuster adjuster = WidgetScreenResolutionAdjuster.get();
    private static final ImageIcon ICON_APP = ImageManager.get().getImageIcon(IconUtils.APPLICATION_ICON);
    private static final int WIDTH = adjuster.adjust(220);
    private static final int HEIGHT = adjuster.adjust(10);

    private final DataCleanerConfiguration _configuration;
    private final DCFileChooser _fileChooser;
    private final DCPanel _centerPanel;
    private final JButton _openJobButton;
    private final WindowContext _windowContext;
    private final Provider<OpenAnalysisJobActionListener> _openAnalysisJobActionListenerProvider;

    private volatile FileObject _file;
    private volatile AnalysisJobMetadata _metadata;

    public OpenAnalysisJobFileChooserAccessory(final WindowContext windowContext,
            final DataCleanerConfiguration configuration, final DCFileChooser fileChooser,
            final Provider<OpenAnalysisJobActionListener> openAnalysisJobActionListenerProvider) {
        super();
        _windowContext = windowContext;
        _configuration = configuration;
        _centerPanel = new DCPanel();
        _centerPanel.setLayout(new VerticalLayout(0));
        _fileChooser = fileChooser;
        _fileChooser.addPropertyChangeListener(this);
        _openJobButton = getOpenJobButton();
        _openAnalysisJobActionListenerProvider = openAnalysisJobActionListenerProvider;

        setPreferredSize(WIDTH, HEIGHT);

        setBorder(new EmptyBorder(0, 10, 0, 0));
        setLayout(new BorderLayout());
        setVisible(false);

        final JLabel iconLabel = new JLabel(ICON_APP);

        final JLabel headerLabel = new JLabel("DataCleaner analysis job:");
        headerLabel.setFont(WidgetUtils.FONT_HEADER1);

        final DCPanel northPanel = new DCPanel();
        northPanel.setLayout(new VerticalLayout(0));
        northPanel.add(iconLabel);
        northPanel.add(Box.createVerticalStrut(10));
        northPanel.add(headerLabel);
        northPanel.add(Box.createVerticalStrut(10));
        northPanel.add(_centerPanel);
        northPanel.add(Box.createVerticalStrut(10));

        final DCPanel southPanel = new DCPanel();
        southPanel.setLayout(new VerticalLayout(0));
        northPanel.add(Box.createVerticalStrut(4));
        southPanel.add(_openJobButton);
        southPanel.add(Box.createVerticalStrut(4));
        southPanel.add(getOpenAsTemplateButton());

        add(WidgetUtils.scrolleable(northPanel), BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
    }

    private JButton getOpenJobButton() {
        final JButton openJobButton = new JButton("Open analysis job");
        openJobButton.addActionListener(e -> {
            final OpenAnalysisJobActionListener openAnalysisJobActionListener =
                    _openAnalysisJobActionListenerProvider.get();
            final Injector injector = openAnalysisJobActionListener.openAnalysisJob(_file);
            final AnalysisJobBuilderWindow window = injector.getInstance(AnalysisJobBuilderWindow.class);
            window.open();
            _fileChooser.cancelSelection();
        });
        return openJobButton;
    }

    private JButton getOpenAsTemplateButton() {
        final JButton openAsTemplateButton = new JButton("Open as template");
        openAsTemplateButton
                .setToolTipText("Allows you to open the job with a different datastore and different source columns.");
        openAsTemplateButton.addActionListener(e -> {
            final OpenAnalysisJobAsTemplateDialog dialog =
                    new OpenAnalysisJobAsTemplateDialog(_windowContext, _configuration, _file, _metadata,
                            _openAnalysisJobActionListenerProvider);
            _fileChooser.cancelSelection();
            dialog.setVisible(true);
        });
        return openAsTemplateButton;
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
            _file = _fileChooser.getSelectedFileObject();
            if (_file != null && _file.getName().getBaseName().endsWith(FileFilters.ANALYSIS_XML.getExtension())) {
                showFileInformation();
            } else {
                setVisible(false);
            }
        }
    }

    private void showFileInformation() {
        final JaxbJobReader reader = new JaxbJobReader(_configuration);

        try {
            _metadata = reader.readMetadata(_file);
        } catch (final Exception e) {
            // metadata could not be produced so we cannot display the file
            // information
            logger.warn("An unexpected error occurred reading metadata from file", e);
            setVisible(false);
            return;
        }

        _centerPanel.removeAll();

        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        final int separatorHeight = 6;

        final String jobName = _metadata.getJobName();
        if (jobName != null) {
            _centerPanel.add(new JLabel("<html><b>Job name:</b></html>"));
            _centerPanel.add(new JLabel(jobName));
            _centerPanel.add(Box.createVerticalStrut(separatorHeight));
        }
        final String jobDescription = _metadata.getJobDescription();
        if (jobDescription != null) {
            _centerPanel.add(new JLabel("<html><b>Job description:</b></html>"));
            final DCLabel descriptionLabel = DCLabel.darkMultiLine(jobDescription);
            descriptionLabel.setMaximumWidth(WIDTH);
            _centerPanel.add(descriptionLabel);
            _centerPanel.add(Box.createVerticalStrut(separatorHeight));
        }
        final String jobVersion = _metadata.getJobVersion();
        if (jobVersion != null) {
            _centerPanel.add(new JLabel("<html><b>Job version:</b></html>"));
            _centerPanel.add(new JLabel(jobVersion));
            _centerPanel.add(Box.createVerticalStrut(separatorHeight));
        }
        final String author = _metadata.getAuthor();
        if (author != null) {
            _centerPanel.add(new JLabel("<html><b>Author:</b></html>"));
            _centerPanel.add(new JLabel(author));
            _centerPanel.add(Box.createVerticalStrut(separatorHeight));
        }
        final Date createdDate = _metadata.getCreatedDate();
        if (createdDate != null) {
            _centerPanel.add(new JLabel("<html><b>Created:</b></html>"));
            _centerPanel.add(new JLabel(dateFormat.format(createdDate)));
            _centerPanel.add(Box.createVerticalStrut(separatorHeight));
        }
        final Date updatedDate = _metadata.getUpdatedDate();
        if (updatedDate != null) {
            _centerPanel.add(new JLabel("<html><b>Updated:</b></html>"));
            _centerPanel.add(new JLabel(dateFormat.format(updatedDate)));
            _centerPanel.add(Box.createVerticalStrut(separatorHeight));
        }

        final String datastoreName = _metadata.getDatastoreName();

        _centerPanel.add(new JLabel("<html><b>Datastore:</b></html>"));
        final JLabel datastoreLabel = new JLabel(datastoreName);

        final Datastore datastore = _configuration.getDatastoreCatalog().getDatastore(datastoreName);
        if (datastore == null) {
            _openJobButton.setEnabled(false);
            datastoreLabel.setIcon(imageManager.getImageIcon(IconUtils.STATUS_WARNING, IconUtils.ICON_SIZE_SMALL));
            datastoreLabel.setToolTipText("No such datastore: " + datastoreName);
        } else {
            _openJobButton.setEnabled(true);
            datastoreLabel.setIcon(IconUtils.getDatastoreIcon(datastore, IconUtils.ICON_SIZE_SMALL));
            datastoreLabel.setToolTipText(null);
        }

        _centerPanel.add(datastoreLabel);
        _centerPanel.add(Box.createVerticalStrut(separatorHeight));
        _centerPanel.add(new JLabel("<html><b>Source columns:</b></html>"));
        final List<String> paths = _metadata.getSourceColumnPaths();
        for (final String path : paths) {
            final JLabel columnLabel = new JLabel(path);
            columnLabel.setIcon(imageManager.getImageIcon("images/model/column.png", IconUtils.ICON_SIZE_SMALL));
            _centerPanel.add(columnLabel);
        }

        _centerPanel.updateUI();
        setVisible(true);
    }
}
