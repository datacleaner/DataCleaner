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

import javax.inject.Inject;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.commons.vfs2.FileObject;
import org.apache.metamodel.util.FileHelper;
import org.datacleaner.Version;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.extensions.ExtensionPackage;
import org.datacleaner.guice.DCModule;
import org.datacleaner.guice.DCModuleImpl;
import org.datacleaner.job.AnalysisJobMetadata;
import org.datacleaner.job.ComponentConfigurationException;
import org.datacleaner.job.JaxbJobReader;
import org.datacleaner.job.NoSuchComponentException;
import org.datacleaner.job.NoSuchDatastoreException;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.result.AnalysisResult;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.ChangeAwareObjectInputStream;
import org.datacleaner.util.FileFilters;
import org.datacleaner.util.VFSUtils;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCFileChooser;
import org.datacleaner.widgets.OpenAnalysisJobFileChooserAccessory;
import org.datacleaner.windows.AnalysisJobBuilderWindow;
import org.datacleaner.windows.OpenAnalysisJobAsTemplateDialog;
import org.datacleaner.windows.ResultWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Providers;

/**
 * ActionListener that will display an "Open file" dialog which allows the user
 * to select a job file.
 *
 * The class also contains a few reusable static methods for opening job files
 * without showing the dialog.
 */
public class OpenAnalysisJobActionListener implements ActionListener {

    private static final Logger logger = LoggerFactory.getLogger(OpenAnalysisJobActionListener.class);

    private final DataCleanerConfiguration _configuration;
    private final AnalysisJobBuilderWindow _parentWindow;
    private final WindowContext _windowContext;
    private final DCModule _parentModule;
    private final UserPreferences _userPreferences;

    @Inject
    public OpenAnalysisJobActionListener(final AnalysisJobBuilderWindow parentWindow,
            final DataCleanerConfiguration configuration, final WindowContext windowContext,
            final DCModule parentModule, final UserPreferences userPreferences) {
        _parentWindow = parentWindow;
        _configuration = configuration;
        _windowContext = windowContext;
        _parentModule = parentModule;
        _userPreferences = userPreferences;
    }

    public static Injector open(final FileObject file, final DataCleanerConfiguration configuration,
            final Injector injector) {
        final UserPreferences userPreferences = injector.getInstance(UserPreferences.class);
        final OpenAnalysisJobActionListener openAnalysisJobActionListener =
                new OpenAnalysisJobActionListener(null, configuration, null, injector.getInstance(DCModule.class),
                        userPreferences);
        return openAnalysisJobActionListener.openAnalysisJob(file);
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        final DCFileChooser fileChooser = new DCFileChooser(_userPreferences.getAnalysisJobDirectory());

        final OpenAnalysisJobFileChooserAccessory accessory =
                new OpenAnalysisJobFileChooserAccessory(_windowContext, _configuration, fileChooser,
                        Providers.of(this));
        fileChooser.setAccessory(accessory);

        fileChooser.addChoosableFileFilter(FileFilters.ANALYSIS_XML);
        fileChooser.addChoosableFileFilter(FileFilters.ANALYSIS_RESULT_SER);
        fileChooser.setFileFilter(FileFilters
                .combined("DataCleaner analysis files", FileFilters.ANALYSIS_XML, FileFilters.ANALYSIS_RESULT_SER));
        final int openFileResult = fileChooser.showOpenDialog(_parentWindow.toComponent());

        if (openFileResult == JFileChooser.APPROVE_OPTION) {
            final FileObject file = fileChooser.getSelectedFileObject();
            openFile(file);
        }
    }

    public void openFile(final FileObject file) {
        if (file.getName().getBaseName().endsWith(FileFilters.ANALYSIS_RESULT_SER.getExtension())) {
            openAnalysisResult(file, _parentModule);
        } else {
            final Injector injector = openAnalysisJob(file);
            if (injector == null) {
                // this may happen, in which case the error was signalled to the
                // user already
                return;
            }

            final AnalysisJobBuilderWindow window = injector.getInstance(AnalysisJobBuilderWindow.class);
            window.open();

            if (_parentWindow != null && !_parentWindow.isDatastoreSet()) {
                _parentWindow.close();
            }
        }
    }

    public ResultWindow openAnalysisResult(final FileObject fileObject, final DCModule parentModule) {
        final AnalysisResult analysisResult;
        try {
            final ChangeAwareObjectInputStream is =
                    new ChangeAwareObjectInputStream(fileObject.getContent().getInputStream());
            try {
                is.addClassLoader(ExtensionPackage.getExtensionClassLoader());
                analysisResult = (AnalysisResult) is.readObject();
            } finally {
                FileHelper.safeClose(is);
            }
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }

        final File file = VFSUtils.toFile(fileObject);
        if (file != null) {
            _userPreferences.setAnalysisJobDirectory(file.getParentFile());
            _userPreferences.addRecentJobFile(fileObject);
        }

        final Injector injector = Guice.createInjector(new DCModuleImpl(parentModule, null) {
            public FileObject getJobFilename() {
                return fileObject;
            }

            @Override
            public AnalysisResult getAnalysisResult() {
                return analysisResult;
            }

            @Override
            public AnalysisJobBuilder getAnalysisJobBuilder(final DataCleanerConfiguration configuration) {
                return null;
            }
        });

        final ResultWindow resultWindow = injector.getInstance(ResultWindow.class);
        resultWindow.open();
        return resultWindow;
    }

    /**
     * Opens a job file
     *
     * @param file
     * @return
     */
    public Injector openAnalysisJob(final FileObject file) {
        final JaxbJobReader reader = new JaxbJobReader(_configuration);
        try {
            final AnalysisJobBuilder ajb = reader.create(file);

            return openAnalysisJob(file, ajb);
        } catch (final NoSuchComponentException e) {
            final String message;
            if (Version.EDITION_COMMUNITY.equals(Version.getEdition())) {
                message = "<html><p>Failed to open job because of a missing component:</p><pre>" + e.getMessage()
                        + "</pre>"
                        + "<p>This may happen if the job requires a <a href=\"https://datacleaner.org/editions\">Commercial Edition of DataCleaner</a>, or an extension that you do not have installed.</p></html>";
            } else {
                message = "<html>Failed to open job because of a missing component: " + e.getMessage() + "<br/><br/>"
                        + "This may happen if the job requires an extension that you do not have installed.</html>";
            }
            WidgetUtils.showErrorMessage("Cannot open job", message);

            return null;
        } catch (final NoSuchDatastoreException e) {
            if (_windowContext == null) {
                // This can happen in case of single-datastore + job file
                // bootstrapping of DC
                throw e;
            }

            final AnalysisJobMetadata metadata = reader.readMetadata(file);
            final int result = JOptionPane
                    .showConfirmDialog(null, e.getMessage() + "\n\nDo you wish to open this job as a template?",
                            "Error: " + e.getMessage(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                final OpenAnalysisJobAsTemplateDialog dialog =
                        new OpenAnalysisJobAsTemplateDialog(_windowContext, _configuration, file, metadata,
                                Providers.of(this));
                dialog.setVisible(true);
            }
            return null;
        } catch (final ComponentConfigurationException e) {
            final String message;
            final Throwable cause = e.getCause();
            if (cause != null) {
                // check for causes of the mis-configuration. If there's a cause
                // with a message, then show the message first and foremost
                // (usually a validation error).
                if (!Strings.isNullOrEmpty(cause.getMessage())) {
                    message = cause.getMessage();
                } else {
                    message = e.getMessage();
                }
            } else {
                message = e.getMessage();
            }

            WidgetUtils.showErrorMessage("Failed to validate job configuration", message, e);
            return null;
        } catch (final RuntimeException e) {
            logger.error("Unexpected failure when opening job: {}", file, e);
            throw e;
        }
    }

    /**
     * Opens a job builder
     *
     * @param fileObject
     * @param ajb
     * @return
     */
    public Injector openAnalysisJob(final FileObject fileObject, final AnalysisJobBuilder ajb) {
        final File file = VFSUtils.toFile(fileObject);

        if (file != null) {
            _userPreferences.setAnalysisJobDirectory(file.getParentFile());
            _userPreferences.addRecentJobFile(fileObject);
        }

        return Guice.createInjector(new DCModuleImpl(_parentModule, ajb) {
            public FileObject getJobFilename() {
                return fileObject;
            }

        });
    }
}
