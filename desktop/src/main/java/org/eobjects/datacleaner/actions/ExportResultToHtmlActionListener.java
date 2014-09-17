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
import java.io.Writer;
import java.util.concurrent.ExecutionException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.result.html.HtmlAnalysisResultWriter;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCFileChooser;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action listener used to fire an export of an analysis result
 */
public class ExportResultToHtmlActionListener implements ActionListener {

    private static final Logger logger = LoggerFactory.getLogger(ExportResultToHtmlActionListener.class);

    private final Ref<AnalysisResult> _result;
    private final UserPreferences _userPreferences;
    private final AnalyzerBeansConfiguration _configuration;

    public ExportResultToHtmlActionListener(Ref<AnalysisResult> result, AnalyzerBeansConfiguration configuration,
            UserPreferences userPreferences) {
        _result = result;
        _configuration = configuration;
        _userPreferences = userPreferences;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        final AnalysisResult analysisResult = _result.get();
        if (analysisResult == null) {
            WidgetUtils.showErrorMessage("Result not ready",
                    "Please wait for the job to finish before saving the result");
            return;
        }

        final DCFileChooser fileChooser = new DCFileChooser(_userPreferences.getAnalysisJobDirectory());
        fileChooser.setFileFilter(FileFilters.HTML);

        final Component parent;
        if (event.getSource() instanceof Component) {
            parent = (Component) event.getSource();
        } else {
            parent = null;
        }

        final int result = fileChooser.showSaveDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            if (!file.getName().endsWith(FileFilters.HTML.getExtension())) {
                file = new File(file.getParentFile(), file.getName() + FileFilters.HTML.getExtension());
            }

            if (file.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(parent, "Are you sure you want to overwrite the file '"
                        + file.getName() + "'?", "Overwrite existing file?", JOptionPane.YES_NO_OPTION);
                if (overwrite != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            final Writer writer = FileHelper.getBufferedWriter(file);

            // run the actual HTML rendering in the background using a
            // SwingWorker.
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    final HtmlAnalysisResultWriter resultWriter = new HtmlAnalysisResultWriter();
                    try {
                        logger.debug("Begin write to HTML");
                        resultWriter.write(analysisResult, _configuration, writer);
                        logger.debug("End write to HTML");
                    } finally {
                        FileHelper.safeClose(writer);
                    }
                    return null;
                }

                protected void done() {
                    try {
                        get();
                    } catch (ExecutionException e) {
                        logger.error("ExecutionException occurred while getting the result of the HTML rendering", e);
                        final Throwable cause = e.getCause();
                        WidgetUtils.showErrorMessage("Error writing result to HTML page", cause);
                    } catch (InterruptedException e) {
                        logger.warn("Unexpected interrupt in done() method!");
                    }
                };
            }.execute();
        }
    }

}
