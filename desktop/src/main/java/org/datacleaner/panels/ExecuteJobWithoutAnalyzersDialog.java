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

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.datacleaner.configuration.AnalyzerBeansConfigurationImpl;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerJobBuilder;
import org.datacleaner.api.Analyzer;
import org.datacleaner.bootstrap.DCWindowContext;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.extension.output.CreateCsvFileAnalyzer;
import org.datacleaner.extension.output.CreateExcelSpreadsheetAnalyzer;
import org.datacleaner.guice.DCModule;
import org.datacleaner.guice.InjectorBuilder;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.user.UserPreferencesImpl;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.LookAndFeelManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.windows.AbstractDialog;
import org.datacleaner.windows.ResultWindow;
import org.jdesktop.swingx.VerticalLayout;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * A panel that presents options for the user to execute a job that has no
 * {@link Analyzer}s configured.
 */
public class ExecuteJobWithoutAnalyzersDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    private final AnalysisJobBuilder _analysisJobBuilder;
    private final InjectorBuilder _injectorBuilder;
    private final UserPreferences _userPreferences;

    public ExecuteJobWithoutAnalyzersDialog(InjectorBuilder injectorBuilder, WindowContext windowContext,
            AnalysisJobBuilder analysisJobBuilder, UserPreferences userPreferences) {
        super(windowContext, ImageManager.get().getImage("images/window/banner-execute.png"));
        _injectorBuilder = injectorBuilder;
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
        final DCLabel text1 = DCLabel.brightMultiLine("Your job does not contain any analysis components!");

        final DCLabel text2 = DCLabel
                .brightMultiLine("Would you like to run the current job and write the output data somewhere?");

        final JButton writeCsvButton = createButton("Write a CSV file", IconUtils.CSV_IMAGEPATH);
        writeCsvButton.addActionListener(createWriteDataActionListener(CreateCsvFileAnalyzer.class, ".csv"));

        final JButton writeExcelButton = createButton("Write an Excel spreadsheet", IconUtils.EXCEL_IMAGEPATH);
        writeExcelButton
                .addActionListener(createWriteDataActionListener(CreateExcelSpreadsheetAnalyzer.class, ".xlsx"));

        final DCLabel text3 = DCLabel.brightMultiLine("... Or cancel and modify the job?");

        final JButton cancelButton = createButton("Cancel", IconUtils.ACTION_REMOVE);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });

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
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final AnalysisJob copyAnalysisJob = _analysisJobBuilder.toAnalysisJob(false);
                final AnalysisJobBuilder copyAnalysisJobBuilder = new AnalysisJobBuilder(
                        _analysisJobBuilder.getConfiguration(), copyAnalysisJob);

                final AnalyzerJobBuilder<? extends Analyzer<?>> analyzer = copyAnalysisJobBuilder
                        .addAnalyzer(analyzerClass);
                analyzer.addInputColumns(copyAnalysisJobBuilder.getAvailableInputColumns(Object.class));

                final String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

                analyzer.setConfiguredProperty("File",
                        createFile("datacleaner-" + formattedDate + "-output", filenameExtension));

                final ConfiguredPropertyDescriptor sheetNameProperty = analyzer.getDescriptor().getConfiguredProperty(
                        "Sheet name");
                if (sheetNameProperty != null) {
                    analyzer.setConfiguredProperty(sheetNameProperty, "data");
                }

                final Injector injector = _injectorBuilder.with(AnalysisJobBuilder.class, copyAnalysisJobBuilder)
                        .createInjector();
                final ResultWindow resultWindow = injector.getInstance(ResultWindow.class);
                resultWindow.open();
                ExecuteJobWithoutAnalyzersDialog.this.close();
                resultWindow.startAnalysis();
            }
        };
    }

    private JButton createButton(String text, String imagePath) {
        JButton button = WidgetFactory.createButton(text, imagePath);
        button.addMouseListener(new ButtonHoverMouseListener());
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        return button;
    }

    private File createFile(String filenamePrefix, String extension) {
        final File directory = _userPreferences.getSaveDatastoreDirectory();
        int attempt = 0;
        while (true) {
            final String filename;
            if (attempt == 0) {
                filename = filenamePrefix + extension;
            } else {
                filename = filenamePrefix + "_" + attempt + extension;
            }

            File candidate = new File(directory, filename);
            if (!candidate.exists()) {
                return candidate;
            }

            attempt++;
        }
    }

    public static void main(String[] args) {
        LookAndFeelManager.get().init();

        DCWindowContext windowContext = new DCWindowContext(new AnalyzerBeansConfigurationImpl(),
                new UserPreferencesImpl(null), null);
        InjectorBuilder injectorBuilder = Guice.createInjector(new DCModule()).getInstance(InjectorBuilder.class);

        UserPreferences userPreferences = new UserPreferencesImpl(null);
        ExecuteJobWithoutAnalyzersDialog dialog = new ExecuteJobWithoutAnalyzersDialog(injectorBuilder, windowContext,
                null, userPreferences);
        dialog.open();
    }
}
