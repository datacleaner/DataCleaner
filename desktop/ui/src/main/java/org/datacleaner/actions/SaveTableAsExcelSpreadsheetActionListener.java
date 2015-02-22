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
import javax.swing.JButton;
import javax.swing.JComponent;

import org.apache.metamodel.schema.Table;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.descriptors.AnalyzerDescriptor;
import org.datacleaner.extension.output.CreateExcelSpreadsheetAnalyzer;
import org.datacleaner.guice.DCModule;
import org.datacleaner.guice.DCModuleImpl;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.panels.AnalyzerComponentBuilderPanel;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.widgets.properties.PropertyWidgetFactory;
import org.datacleaner.widgets.tabs.CloseableTabbedPane;
import org.datacleaner.windows.AbstractDialog;
import org.datacleaner.windows.ResultWindow;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Provides an action for the user to save a table as an Excel spreadsheet
 * 
 * @author Kasper Sørensen
 */
public final class SaveTableAsExcelSpreadsheetActionListener implements ActionListener {

    private final Datastore _datastore;
    private final Table _table;
    private final WindowContext _windowContext;
    private final AnalyzerBeansConfiguration _configuration;
    private final DCModule _parentModule;
    private final UserPreferences _userPreferences;

    @Inject
    protected SaveTableAsExcelSpreadsheetActionListener(Datastore datastore, Table table, WindowContext windowContext,
            AnalyzerBeansConfiguration configuration, UserPreferences userPreferences, DCModule parentModule) {
        _datastore = datastore;
        _table = table;
        _windowContext = windowContext;
        _configuration = configuration;
        _parentModule = parentModule;
        _userPreferences = userPreferences;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final AnalysisJobBuilder ajb = new AnalysisJobBuilder(_configuration);
        ajb.setDatastore(_datastore);
        ajb.addSourceColumns(_table.getColumns());

        final AnalyzerComponentBuilder<CreateExcelSpreadsheetAnalyzer> excelOutputAnalyzerBuilder = ajb
                .addAnalyzer(CreateExcelSpreadsheetAnalyzer.class);
        excelOutputAnalyzerBuilder.addInputColumns(ajb.getSourceColumns());
        File directory = _userPreferences.getConfiguredFileDirectory();
        excelOutputAnalyzerBuilder.getComponentInstance().setFile(new File(directory, _datastore.getName() + ".xlsx"));
        excelOutputAnalyzerBuilder.getComponentInstance().setSheetName(_table.getName());

        final PropertyWidgetFactory propertyWidgetFactory = _parentModule.createChildInjectorForComponent(
                excelOutputAnalyzerBuilder).getInstance(PropertyWidgetFactory.class);

        final AnalyzerComponentBuilderPanel presenter = new AnalyzerComponentBuilderPanel(excelOutputAnalyzerBuilder,
                propertyWidgetFactory);

        final AbstractDialog dialog = new AbstractDialog(_windowContext) {
            private static final long serialVersionUID = 1L;

            @Override
            public String getWindowTitle() {
                return "Save " + _table.getName() + " as Excel spreadsheet";
            }

            @Override
            protected int getDialogWidth() {
                return 600;
            }

            @Override
            protected JComponent getDialogContent() {
                final AnalyzerDescriptor<CreateExcelSpreadsheetAnalyzer> descriptor = excelOutputAnalyzerBuilder
                        .getDescriptor();
                final CloseableTabbedPane tabbedPane = new CloseableTabbedPane(true);
                tabbedPane.addTab(descriptor.getDisplayName(),
                        IconUtils.getDescriptorIcon(descriptor, IconUtils.ICON_SIZE_TAB),
                        presenter.createJComponent());
                tabbedPane.setUnclosableTab(0);
                return tabbedPane;
            }

            @Override
            protected String getBannerTitle() {
                return "Save " + _table.getName() + "\nas Excel spreadsheet file";
            }
        };

        final JButton runButton = WidgetFactory.createPrimaryButton("Run", IconUtils.ACTION_EXECUTE);
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Injector injector = Guice.createInjector(new DCModuleImpl(_parentModule, ajb));

                ResultWindow window = injector.getInstance(ResultWindow.class);
                window.open();
                dialog.close();
                window.startAnalysis();
            }
        });

        final JButton closeButton = WidgetFactory.createDefaultButton("Close", IconUtils.ACTION_CLOSE_DARK);
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.close();
            }
        });

        presenter.addToButtonPanel(runButton);
        presenter.addToButtonPanel(closeButton);

        dialog.open();
    }
}
