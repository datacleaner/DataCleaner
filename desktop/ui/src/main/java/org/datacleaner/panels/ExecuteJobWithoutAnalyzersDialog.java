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
package org.datacleaner.panels;

import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;

import org.apache.metamodel.util.FileResource;
import org.datacleaner.actions.RunAnalysisActionListener;
import org.datacleaner.api.Analyzer;
import org.datacleaner.bootstrap.DCWindowContext;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.extension.output.CreateCsvFileAnalyzer;
import org.datacleaner.extension.output.CreateExcelSpreadsheetAnalyzer;
import org.datacleaner.guice.DCModule;
import org.datacleaner.guice.DCModuleImpl;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.user.UserPreferencesImpl;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.LookAndFeelManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.windows.AbstractDialog;
import org.jdesktop.swingx.VerticalLayout;

/**
 * A panel that presents options for the user to execute a job that has no
 * {@link Analyzer}s configured.
 */
public class ExecuteJobWithoutAnalyzersDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    private final AnalysisJobBuilder _analysisJobBuilder;
    private final UserPreferences _userPreferences;
    private final DCModule _dcModule;

    public ExecuteJobWithoutAnalyzersDialog(final DCModule dcModule, final WindowContext windowContext,
            final AnalysisJobBuilder analysisJobBuilder, final UserPreferences userPreferences) {
        super(windowContext, ImageManager.get().getImage("images/window/banner-execute.png"));
        setBackgroundColor(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        _dcModule = dcModule;
        _analysisJobBuilder = analysisJobBuilder;
        _userPreferences = userPreferences;
    }

    @Override
    public String getWindowTitle() {
        return "Execute without analyzers?";
    }

    @Override
    protected String getBannerTitle() {
        return "Execute without analyzers?";
    }

    @Override
    protected int getDialogWidth() {
        return 400;
    }

    @Override
    protected boolean isWindowResizable() {
        return true;
    }

    @Override
    protected JComponent getDialogContent() {
        final DCLabel text1 = DCLabel.darkMultiLine("Your job does not contain any analysis components!");

        final DCLabel text2 =
                DCLabel.darkMultiLine("Would you like to run the current job and write the output data somewhere?");

        final JButton writeCsvButton = createButton("Write a CSV file", IconUtils.CSV_IMAGEPATH);
        writeCsvButton.addActionListener(createWriteDataActionListener(CreateCsvFileAnalyzer.class, ".csv"));

        final JButton writeExcelButton = createButton("Write an Excel spreadsheet", IconUtils.EXCEL_IMAGEPATH);
        writeExcelButton
                .addActionListener(createWriteDataActionListener(CreateExcelSpreadsheetAnalyzer.class, ".xlsx"));

        final DCLabel text3 = DCLabel.darkMultiLine("... Or cancel and modify the job?");

        final JButton cancelButton = createButton("Cancel", IconUtils.ACTION_REMOVE_DARK);
        cancelButton.addActionListener(e -> close());

        final DCPanel panel = new DCPanel();
        panel.setLayout(new VerticalLayout(10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.add(text1);
        panel.add(text2);
        panel.add(writeCsvButton);
        panel.add(writeExcelButton);
        panel.add(text3);
        panel.add(cancelButton);
        panel.setPreferredSize(getDialogWidth(), 250);

        return panel;
    }

    private ActionListener createWriteDataActionListener(final Class<? extends Analyzer<?>> analyzerClass,
            final String filenameExtension) {
        return e -> {
            final AnalysisJob copyAnalysisJob = _analysisJobBuilder.toAnalysisJob(false);
            final AnalysisJobBuilder copyAnalysisJobBuilder =
                    new AnalysisJobBuilder(_analysisJobBuilder.getConfiguration(), copyAnalysisJob);

            final AnalyzerComponentBuilder<? extends Analyzer<?>> analyzer =
                    copyAnalysisJobBuilder.addAnalyzer(analyzerClass);

            analyzer.addInputColumns(copyAnalysisJobBuilder.getAvailableInputColumns(Object.class));

            final String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

            final FileResource resource = createResource("datacleaner-" + formattedDate + "-output", filenameExtension);
            if (analyzerClass == CreateExcelSpreadsheetAnalyzer.class) {
                final File file = resource.getFile();
                analyzer.setConfiguredProperty("File", file);
            } else {
                analyzer.setConfiguredProperty("File", resource);
            }

            final ConfiguredPropertyDescriptor sheetNameProperty =
                    analyzer.getDescriptor().getConfiguredProperty("Sheet name");
            if (sheetNameProperty != null) {
                analyzer.setConfiguredProperty(sheetNameProperty, "data");
            }

            final RunAnalysisActionListener runAnalysis =
                    new RunAnalysisActionListener(_dcModule, copyAnalysisJobBuilder);
            ExecuteJobWithoutAnalyzersDialog.this.close();
            runAnalysis.run();
        };
    }

    private JButton createButton(final String text, final String imagePath) {
        return WidgetFactory.createDefaultButton(text, imagePath);
    }

    private FileResource createResource(final String filenamePrefix, final String extension) {
        final File directory = _userPreferences.getSaveDatastoreDirectory();
        int attempt = 0;
        while (true) {
            final String filename;
            if (attempt == 0) {
                filename = filenamePrefix + extension;
            } else {
                filename = filenamePrefix + "_" + attempt + extension;
            }

            final File file = new File(directory, filename);
            final FileResource resourceCandidate = new FileResource(file);
            if (!resourceCandidate.isExists()) {
                return resourceCandidate;
            }
            attempt++;
        }
    }

    public static void main(final String[] args) {
        LookAndFeelManager.get().init();

        final DCWindowContext windowContext =
                new DCWindowContext(new DataCleanerConfigurationImpl(), new UserPreferencesImpl(null), null);

        final UserPreferences userPreferences = new UserPreferencesImpl(null);
        final ExecuteJobWithoutAnalyzersDialog dialog =
                new ExecuteJobWithoutAnalyzersDialog(new DCModuleImpl(), windowContext, null, userPreferences);
        dialog.open();
    }
}
