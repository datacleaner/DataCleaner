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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.Ref;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.result.AnalysisResult;
import org.datacleaner.result.save.AnalysisResultSaveHandler;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.FileFilters;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCFileChooser;

/**
 * Action listener used to fire saving a result to an .analysis.result.dat
 * (serialized) file.
 */
public class SaveAnalysisResultActionListener implements ActionListener {

    private final Ref<AnalysisResult> _result;
    private final UserPreferences _userPreferences;

    public SaveAnalysisResultActionListener(Ref<AnalysisResult> result, UserPreferences userPreferences) {
        _result = result;
        _userPreferences = userPreferences;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (_result.get() == null) {
            WidgetUtils.showErrorMessage("Result not ready",
                    "Please wait for the job to finish before saving the result");
            return;
        }

        final DCFileChooser fileChooser = new DCFileChooser(_userPreferences.getAnalysisJobDirectory());
        fileChooser.setFileFilter(FileFilters.ANALYSIS_RESULT_SER);

        final Component parent;
        if (event.getSource() instanceof Component) {
            parent = (Component) event.getSource();
        } else {
            parent = null;
        }

        final int result = fileChooser.showSaveDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            if (!file.getName().endsWith(FileFilters.ANALYSIS_RESULT_SER.getExtension())) {
                file = new File(file.getParentFile(), file.getName() + FileFilters.ANALYSIS_RESULT_SER.getExtension());
            }

            if (file.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(parent,
                        "Are you sure you want to overwrite the file '" + file.getName() + "'?",
                        "Overwrite existing file?", JOptionPane.YES_NO_OPTION);
                if (overwrite != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            _userPreferences.setAnalysisJobDirectory(file.getParentFile());

            final AnalysisResultSaveHandler saveHandler = new AnalysisResultSaveHandler(_result.get(),
                    new FileResource(file));
            final boolean success = saveHandler.saveAttempt();
            if (!success) {
                final AnalysisResult safeAnalysisResult = saveHandler.createSafeAnalysisResult();
                if (safeAnalysisResult == null) {
                    WidgetUtils.showErrorMessage("Error writing result to file!", "See the log for error details.");
                } else {
                    final Map<ComponentJob, AnalyzerResult> unsafeResultElements = saveHandler
                            .getUnsafeResultElements();

                    final StringBuilder details = new StringBuilder();
                    details.append(unsafeResultElements.size()
                            + " of the result elements encountered an error while saving.\n");

                    for (ComponentJob componentJob : unsafeResultElements.keySet()) {
                        final String componentJobLabel = LabelUtils.getLabel(componentJob);
                        details.append('\n');
                        details.append(" - ");
                        details.append(componentJobLabel);
                    }
                    details.append("\n\nSee the log for error details.");
                    details.append("\n\nDo you want to save the result without these elements?");

                    final int confirmation = JOptionPane.showConfirmDialog(null, details.toString(),
                            "Error writing result to file!", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
                    if (confirmation == JOptionPane.OK_OPTION) {

                        saveHandler.saveWithoutUnsafeResultElements();
                    }
                }
            }
        }
    }

}
