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
package org.eobjects.datacleaner.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.inject.Inject;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.commons.vfs2.FileObject;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.AnalysisJobMetadata;
import org.eobjects.analyzer.job.JaxbJobReader;
import org.eobjects.analyzer.job.NoSuchDatastoreException;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.util.ChangeAwareObjectInputStream;
import org.eobjects.analyzer.util.VFSUtils;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.guice.DCModule;
import org.eobjects.datacleaner.user.ExtensionPackage;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.datacleaner.widgets.DCFileChooser;
import org.eobjects.datacleaner.widgets.OpenAnalysisJobFileChooserAccessory;
import org.eobjects.datacleaner.windows.AnalysisJobBuilderWindow;
import org.eobjects.datacleaner.windows.OpenAnalysisJobAsTemplateDialog;
import org.eobjects.datacleaner.windows.ResultWindow;
import org.eobjects.metamodel.util.FileHelper;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Providers;

/**
 * ActionListener that will display an "Open file" dialog which allows the user
 * to select a job file.
 * 
 * The class also contains a few reusable static methods for opening job files
 * without showing the dialog.
 * 
 * @author Kasper Sørensen
 */
public class OpenAnalysisJobActionListener implements ActionListener {

    private final AnalyzerBeansConfiguration _configuration;
    private final AnalysisJobBuilderWindow _parentWindow;
    private final WindowContext _windowContext;
    private final DCModule _parentModule;
    private final UserPreferences _userPreferences;

    @Inject
    public OpenAnalysisJobActionListener(AnalysisJobBuilderWindow parentWindow,
            AnalyzerBeansConfiguration configuration, WindowContext windowContext, DCModule parentModule,
            UserPreferences userPreferences) {
        _parentWindow = parentWindow;
        _configuration = configuration;
        _windowContext = windowContext;
        _parentModule = parentModule;
        _userPreferences = userPreferences;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        DCFileChooser fileChooser = new DCFileChooser(_userPreferences.getAnalysisJobDirectory());

        OpenAnalysisJobFileChooserAccessory accessory = new OpenAnalysisJobFileChooserAccessory(_windowContext,
                _configuration, fileChooser, Providers.of(this));
        fileChooser.setAccessory(accessory);

        fileChooser.addChoosableFileFilter(FileFilters.ANALYSIS_XML);
        fileChooser.addChoosableFileFilter(FileFilters.ANALYSIS_RESULT_SER);
        fileChooser.setFileFilter(FileFilters.combined("DataCleaner analysis files", FileFilters.ANALYSIS_XML,
                FileFilters.ANALYSIS_RESULT_SER));
        int openFileResult = fileChooser.showOpenDialog((Component) event.getSource());

        if (openFileResult == JFileChooser.APPROVE_OPTION) {
            final FileObject file = fileChooser.getSelectedFileObject();
            openFile(file);
        }
    }

    public static Injector open(FileObject file, AnalyzerBeansConfiguration configuration, Injector injector) {
        final UserPreferences userPreferences = injector.getInstance(UserPreferences.class);
        final OpenAnalysisJobActionListener openAnalysisJobActionListener = new OpenAnalysisJobActionListener(null,
                configuration, null, injector.getInstance(DCModule.class), userPreferences);
        return openAnalysisJobActionListener.openAnalysisJob(file);
    }

    public void openFile(FileObject file) {
        if (file.getName().getBaseName().endsWith(FileFilters.ANALYSIS_RESULT_SER.getExtension())) {
            openAnalysisResult(file, _parentModule);
        } else {
            Injector injector = openAnalysisJob(file);
            final AnalysisJobBuilderWindow window = injector.getInstance(AnalysisJobBuilderWindow.class);
            window.open();

            if (_parentWindow != null && !_parentWindow.isDatastoreSet()) {
                _parentWindow.close();
            }
        }
    }

    public ResultWindow openAnalysisResult(final FileObject fileObject, DCModule parentModule) {
        final AnalysisResult analysisResult;
        try {
            ChangeAwareObjectInputStream is = new ChangeAwareObjectInputStream(fileObject.getContent().getInputStream());
            try {
                is.addClassLoader(ExtensionPackage.getExtensionClassLoader());
                analysisResult = (AnalysisResult) is.readObject();
            } finally {
                FileHelper.safeClose(is);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        final File file = VFSUtils.toFile(fileObject);
        if (file != null) {
            _userPreferences.setAnalysisJobDirectory(file.getParentFile());
            _userPreferences.addRecentJobFile(fileObject);
        }

        final Injector injector = Guice.createInjector(new DCModule(parentModule, null) {
            public FileObject getJobFilename() {
                return fileObject;
            };

            @Override
            public AnalysisResult getAnalysisResult() {
                return analysisResult;
            }

            @Override
            public AnalysisJobBuilder getAnalysisJobBuilder(AnalyzerBeansConfiguration configuration) {
                return null;
            }
        });

        ResultWindow resultWindow = injector.getInstance(ResultWindow.class);
        resultWindow.open();
        return resultWindow;
    }

    /**
     * Opens a job file
     * 
     * @param file
     * @return
     */
    public Injector openAnalysisJob(FileObject file) {
        JaxbJobReader reader = new JaxbJobReader(_configuration);
        try {
            AnalysisJobBuilder ajb = reader.create(file);

            return openAnalysisJob(file, ajb);
        } catch (NoSuchDatastoreException e) {
            if (_windowContext == null) {
                // This can happen in case of single-datastore + job file
                // bootstrapping of DC
                throw e;
            }

            AnalysisJobMetadata metadata = reader.readMetadata(file);
            int result = JOptionPane.showConfirmDialog(null, e.getMessage()
                    + "\n\nDo you wish to open this job as a template?", "Error: " + e.getMessage(),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                OpenAnalysisJobAsTemplateDialog dialog = new OpenAnalysisJobAsTemplateDialog(_windowContext,
                        _configuration, file, metadata, Providers.of(this));
                dialog.setVisible(true);
            }
            return null;
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

        Injector injector = Guice.createInjector(new DCModule(_parentModule, ajb) {
            public FileObject getJobFilename() {
                return fileObject;
            };
        });

        return injector;
    }
}
