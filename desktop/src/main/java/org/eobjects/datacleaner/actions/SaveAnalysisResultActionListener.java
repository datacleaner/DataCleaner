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
import java.io.OutputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.commons.lang.SerializationUtils;
import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.result.SimpleAnalysisResult;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCFileChooser;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action listener used to fire saving a result to an .analysis.result.dat
 * (serialized) file.
 */
public class SaveAnalysisResultActionListener implements ActionListener {

    private static final Logger logger = LoggerFactory.getLogger(SaveAnalysisResultActionListener.class);

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
                    "Please wait for the job to finish before saving the result", null);
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
                int overwrite = JOptionPane.showConfirmDialog(parent, "Are you sure you want to overwrite the file '"
                        + file.getName() + "'?", "Overwrite existing file?", JOptionPane.YES_NO_OPTION);
                if (overwrite != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            _userPreferences.setAnalysisJobDirectory(file.getParentFile());

            final SimpleAnalysisResult analysisResult;
            if (_result instanceof SimpleAnalysisResult) {
                analysisResult = (SimpleAnalysisResult) _result;
            } else {
                analysisResult = new SimpleAnalysisResult(_result.get().getResultMap());
            }

            final OutputStream out = FileHelper.getOutputStream(file);
            try {
                SerializationUtils.serialize(analysisResult, out);
            } catch (Exception e) {
                logger.error("Error serializing analysis result: " + analysisResult, e);
                WidgetUtils.showErrorMessage("Error writing result to file", e);
            } finally {
                FileHelper.safeClose(out);
            }
        }
    }

}
