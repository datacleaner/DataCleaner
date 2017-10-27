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
package org.datacleaner.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.inject.Inject;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.metamodel.util.FileHelper;
import org.datacleaner.Version;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.AnalysisJobMetadata;
import org.datacleaner.job.JaxbJobMetadataFactoryImpl;
import org.datacleaner.job.JaxbJobWriter;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.NoResultProducingComponentsException;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.FileFilters;
import org.datacleaner.util.VFSUtils;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCFileChooser;
import org.datacleaner.windows.AnalysisJobBuilderWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * ActionListener for saving an analysis job
 */
public final class SaveAnalysisJobActionListener implements ActionListener {

    public static final String ACTION_COMMAND_SAVE_AS = "SAVE_AS";
    private static final String LABEL_TEXT_SAVING_JOB = "Saving job...";
    private static final Logger logger = LoggerFactory.getLogger(SaveAnalysisJobActionListener.class);

    private final AnalysisJobBuilder _analysisJobBuilder;
    private final AnalysisJobBuilderWindow _window;
    private final UserPreferences _userPreferences;
    private final DataCleanerConfiguration _configuration;
    public boolean _saved;

    @Inject
    protected SaveAnalysisJobActionListener(final AnalysisJobBuilderWindow window,
            final AnalysisJobBuilder analysisJobBuilder, final UserPreferences userPreferences,
            final DataCleanerConfiguration configuration) {
        _window = window;
        _analysisJobBuilder = analysisJobBuilder;
        _userPreferences = userPreferences;
        _configuration = configuration;
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        _saved = false;
        final String actionCommand = event.getActionCommand();

        _window.setStatusLabelNotice();
        _window.setStatusLabelText(LABEL_TEXT_SAVING_JOB);

        AnalysisJob analysisJob;
        try {
            _window.applyPropertyValues();
            analysisJob = _analysisJobBuilder.toAnalysisJob();
        } catch (final Exception e) {
            if (e instanceof NoResultProducingComponentsException) {
                final int result = JOptionPane.showConfirmDialog(_window.toComponent(),
                        "You job does not have any result-producing components in it, and is thus 'incomplete'. "
                                + "Do you want to save it anyway?", "No result producing components in job",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (result == JOptionPane.YES_OPTION) {
                    analysisJob = _analysisJobBuilder.toAnalysisJob(false);
                } else {
                    return;
                }
            } else {
                String detail = _window.getStatusLabelText();
                if (LABEL_TEXT_SAVING_JOB.equals(detail)) {
                    detail = e.getMessage();
                }
                WidgetUtils.showErrorMessage("Errors in job",
                        "Please fix the errors that exist in the job before saving it:\n\n" + detail, e);
                return;
            }
        }

        final FileObject existingFile = _window.getJobFile();

        final FileObject file;
        if (existingFile == null || ACTION_COMMAND_SAVE_AS.equals(actionCommand)) {
            // ask the user to select a file to save to ("Save as" scenario)
            final DCFileChooser fileChooser = new DCFileChooser(_userPreferences.getAnalysisJobDirectory());
            fileChooser.setFileFilter(FileFilters.ANALYSIS_XML);

            final int result = fileChooser.showSaveDialog(_window.toComponent());
            if (result != JFileChooser.APPROVE_OPTION) {
                return;
            }
            final FileObject candidate = fileChooser.getSelectedFileObject();

            final boolean exists;
            try {
                final String baseName = candidate.getName().getBaseName();
                if (!baseName.endsWith(".xml")) {
                    final FileObject parent = candidate.getParent();
                    file = parent.resolveFile(baseName + FileFilters.ANALYSIS_XML.getExtension());
                } else {
                    file = candidate;
                }
                exists = file.exists();
            } catch (final FileSystemException e) {
                throw new IllegalStateException("Failed to prepare file for saving", e);
            }

            if (exists) {
                final int overwrite = JOptionPane.showConfirmDialog(_window.toComponent(),
                        "Are you sure you want to overwrite the file '" + file.getName() + "'?",
                        "Overwrite existing file?", JOptionPane.YES_NO_OPTION);
                if (overwrite != JOptionPane.YES_OPTION) {
                    return;
                }
            }
        } else {
            // overwrite existing file ("Save" scenario).
            file = existingFile;
        }

        try {
            final FileObject parent = file.getParent();
            final File parentFile = VFSUtils.toFile(parent);
            if (parentFile != null) {
                _userPreferences.setAnalysisJobDirectory(parentFile);
            }
        } catch (final FileSystemException e) {
            logger.warn("Failed to determine parent of {}: {}", file, e.getMessage());
        }

        final AnalysisJobMetadata existingMetadata = analysisJob.getMetadata();
        final String jobName = existingMetadata.getJobName();
        final String jobVersion = existingMetadata.getJobVersion();

        final String author;
        if (Strings.isNullOrEmpty(existingMetadata.getAuthor())) {
            author = System.getProperty("user.name");
        } else {
            author = existingMetadata.getAuthor();
        }

        final String jobDescription;
        if (Strings.isNullOrEmpty(existingMetadata.getJobDescription())) {
            jobDescription = "Created with DataCleaner " + Version.getEdition() + " " + Version.getVersion();
        } else {
            jobDescription = existingMetadata.getJobDescription();
        }

        final JaxbJobWriter writer = new JaxbJobWriter(_configuration,
                new JaxbJobMetadataFactoryImpl(author, jobName, jobDescription, jobVersion));

        OutputStream outputStream = null;
        try {
            outputStream = file.getContent().getOutputStream();
            writer.write(analysisJob, outputStream);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        } finally {
            FileHelper.safeClose(outputStream);
        }
        
        _userPreferences.addRecentJobFile(file);
        _window.setJobFile(file);

        _window.setStatusLabelNotice();
        _window.setStatusLabelText("Saved job to file " + file.getName().getBaseName());
        _saved = true;
    }

    public boolean isSaved() {
        return _saved;
    }
}
