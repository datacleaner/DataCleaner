/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.actions;

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
import org.apache.commons.vfs2.provider.DelegateFileObject;
import org.apache.http.client.HttpClient;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.JaxbJobMetadataFactoryImpl;
import org.eobjects.analyzer.job.JaxbJobWriter;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.util.VFSUtils;
import org.eobjects.datacleaner.Version;
import org.eobjects.datacleaner.user.MonitorConnection;
import org.eobjects.datacleaner.user.UsageLogger;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCFileChooser;
import org.eobjects.datacleaner.windows.AnalysisJobBuilderWindow;
import org.eobjects.datacleaner.windows.MonitorConnectionDialog;
import org.eobjects.metamodel.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ActionListener for saving an analysis job
 */
public final class SaveAnalysisJobActionListener implements ActionListener {

    public static final String ACTION_COMMAND_SAVE_AS = "SAVE_AS";

    private static final Logger logger = LoggerFactory.getLogger(SaveAnalysisJobActionListener.class);

    private final AnalysisJobBuilder _analysisJobBuilder;
    private final AnalysisJobBuilderWindow _window;
    private final UserPreferences _userPreferences;
    private final UsageLogger _usageLogger;
    private final AnalyzerBeansConfiguration _configuration;
    private final HttpClient _httpClient;

    @Inject
    protected SaveAnalysisJobActionListener(AnalysisJobBuilderWindow window, AnalysisJobBuilder analysisJobBuilder,
            UserPreferences userPreferences, UsageLogger usageLogger, AnalyzerBeansConfiguration configuration,
            HttpClient httpClient) {
        _window = window;
        _analysisJobBuilder = analysisJobBuilder;
        _userPreferences = userPreferences;
        _usageLogger = usageLogger;
        _configuration = configuration;
        _httpClient = httpClient;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        final String actionCommand = event.getActionCommand();

        _usageLogger.log("Save analysis job");

        _window.setStatusLabelNotice();
        _window.setStatusLabelText("Saving job...");

        AnalysisJob analysisJob = null;
        try {
            _window.applyPropertyValues();
            analysisJob = _analysisJobBuilder.toAnalysisJob();
        } catch (Exception e) {
            WidgetUtils.showErrorMessage("Errors in job",
                    "Please fix the errors that exist in the job before saving it:\n\n" + _window.getStatusLabelText(),
                    e);
            return;
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
            FileObject candidate = fileChooser.getSelectedFileObject();

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
            } catch (FileSystemException e) {
                throw new IllegalStateException("Failed to prepare file for saving", e);
            }

            if (exists) {
                int overwrite = JOptionPane.showConfirmDialog(_window.toComponent(),
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
        } catch (FileSystemException e) {
            logger.warn("Failed to determine parent of {}: {}", file, e.getMessage());
        }

        final String author = _userPreferences.getUsername();
        final String jobName = null;
        final String jobDescription = "Created with DataCleaner " + Version.get();
        final String jobVersion = null;

        final JaxbJobWriter writer = new JaxbJobWriter(_configuration, new JaxbJobMetadataFactoryImpl(author, jobName,
                jobDescription, jobVersion));

        OutputStream outputStream = null;
        try {
            outputStream = file.getContent().getOutputStream();
            writer.write(analysisJob, outputStream);
        } catch (IOException e1) {
            throw new IllegalStateException(e1);
        } finally {
            FileHelper.safeClose(outputStream);
        }

        if (file instanceof DelegateFileObject) {
            // this "file" is probably a HTTP URL resource (often provided by DC
            // monitor)
            final DelegateFileObject delegateFileObject = (DelegateFileObject) file;
            final String scheme = file.getName().getScheme();

            if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
                final String uri = delegateFileObject.getName().getURI();
                final MonitorConnection monitorConnection = _userPreferences.getMonitorConnection();
                if (monitorConnection.matchesURI(uri) && monitorConnection.isAuthenticationEnabled()
                        && monitorConnection.getEncodedPassword() == null) {
                    // password is not configured, ask for it.
                    final MonitorConnectionDialog dialog = new MonitorConnectionDialog(_window.getWindowContext(),
                            _userPreferences, _httpClient);
                    dialog.openBlocking();
                }

                final PublishJobToMonitorActionListener publisher = new PublishJobToMonitorActionListener(
                        delegateFileObject, _window.getWindowContext(), _userPreferences, _httpClient);
                publisher.actionPerformed(event);
            } else {
                throw new UnsupportedOperationException("Unexpected delegate file object: " + delegateFileObject
                        + " (delegate: " + delegateFileObject.getDelegateFile() + ")");
            }
        } else {
            _userPreferences.addRecentJobFile(file);
        }

        _window.setJobFile(file);

        _window.setStatusLabelNotice();
        _window.setStatusLabelText("Saved job to file " + file.getName().getBaseName());
    }
}
