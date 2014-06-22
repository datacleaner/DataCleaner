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
package org.eobjects.datacleaner.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.bootstrap.DCWindowContext;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.user.UserPreferencesImpl;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.LookAndFeelManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.windows.AbstractDialog;
import org.jdesktop.swingx.VerticalLayout;

/**
 * A panel that presents options for the user to execute a job that has no
 * {@link Analyzer}s configured.
 */
public class ExecuteJobWithoutAnalyzersDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    private final AnalysisJobBuilder _analysisJobBuilder;

    public ExecuteJobWithoutAnalyzersDialog(WindowContext windowContext, AnalysisJobBuilder analysisJobBuilder) {
        // TODO: get better banner bg.
        super(windowContext, ImageManager.get().getImage("images/window/banner-datastores.png"));
        _analysisJobBuilder = analysisJobBuilder;
    }

    @Override
    public String getWindowTitle() {
        return "Write data?";
    }

    @Override
    protected String getBannerTitle() {
        return "Write data?";
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

        final JButton writeCsvButton = WidgetFactory.createButton("Write a CSV file", IconUtils.CSV_IMAGEPATH);

        final JButton writeExcelButton = WidgetFactory.createButton("Write an Excel spreadsheet",
                IconUtils.EXCEL_IMAGEPATH);
        
        final DCLabel text3 = DCLabel
                .brightMultiLine("... Or cancel and modify the job?");

        final JButton cancelButton = WidgetFactory.createButton("Cancel", IconUtils.ACTION_REMOVE);
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

    public static void main(String[] args) {
        LookAndFeelManager.get().init();

        DCWindowContext windowContext = new DCWindowContext(new AnalyzerBeansConfigurationImpl(),
                new UserPreferencesImpl(null), null);
        ExecuteJobWithoutAnalyzersDialog dialog = new ExecuteJobWithoutAnalyzersDialog(windowContext, null);
        dialog.open();
    }
}
