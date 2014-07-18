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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JComponent;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.guice.DCModule;
import org.eobjects.datacleaner.guice.InjectorBuilder;
import org.eobjects.datacleaner.output.beans.CreateExcelSpreadsheetAnalyzer;
import org.eobjects.datacleaner.panels.AnalyzerJobBuilderPanel;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;
import org.eobjects.datacleaner.widgets.tabs.CloseableTabbedPane;
import org.eobjects.datacleaner.windows.AbstractDialog;
import org.eobjects.datacleaner.windows.ResultWindow;
import org.eobjects.metamodel.schema.Table;

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
    private final InjectorBuilder _injectorBuilder;

    @Inject
    protected SaveTableAsExcelSpreadsheetActionListener(Datastore datastore, Table table, WindowContext windowContext,
            AnalyzerBeansConfiguration configuration, UserPreferences userPreferences, DCModule parentModule,
            InjectorBuilder injectorBuilder) {
        _datastore = datastore;
        _table = table;
        _windowContext = windowContext;
        _configuration = configuration;
        _parentModule = parentModule;
        _userPreferences = userPreferences;
        _injectorBuilder = injectorBuilder;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final AnalysisJobBuilder ajb = new AnalysisJobBuilder(_configuration);
        ajb.setDatastore(_datastore);
        ajb.addSourceColumns(_table.getColumns());

        final AnalyzerJobBuilder<CreateExcelSpreadsheetAnalyzer> excelOutputAnalyzerBuilder = ajb
                .addAnalyzer(CreateExcelSpreadsheetAnalyzer.class);
        excelOutputAnalyzerBuilder.addInputColumns(ajb.getSourceColumns());
        File directory = _userPreferences.getConfiguredFileDirectory();
        excelOutputAnalyzerBuilder.getConfigurableBean().setFile(new File(directory, _datastore.getName() + ".xlsx"));
        excelOutputAnalyzerBuilder.getConfigurableBean().setSheetName(_table.getName());

        final PropertyWidgetFactory propertyWidgetFactory = _injectorBuilder.with(
                PropertyWidgetFactory.TYPELITERAL_BEAN_JOB_BUILDER, excelOutputAnalyzerBuilder).getInstance(
                PropertyWidgetFactory.class);

        final AnalyzerJobBuilderPanel presenter = new AnalyzerJobBuilderPanel(excelOutputAnalyzerBuilder, false,
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
                final AnalyzerBeanDescriptor<CreateExcelSpreadsheetAnalyzer> descriptor = excelOutputAnalyzerBuilder
                        .getDescriptor();
                final CloseableTabbedPane tabbedPane = new CloseableTabbedPane(true);
                tabbedPane.addTab(descriptor.getDisplayName(),
                        IconUtils.getDescriptorIcon(descriptor, IconUtils.ICON_SIZE_LARGE),
                        presenter.createJComponent());
                tabbedPane.setUnclosableTab(0);
                return tabbedPane;
            }

            @Override
            protected String getBannerTitle() {
                return "Save " + _table.getName() + "\nas Excel spreadsheet file";
            }
        };

        final JButton runButton = new JButton("Run", ImageManager.get().getImageIcon("images/actions/execute.png"));
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Injector injector = Guice.createInjector(new DCModule(_parentModule, ajb));

                ResultWindow window = injector.getInstance(ResultWindow.class);
                window.open();
                dialog.dispose();
                window.startAnalysis();
            }
        });

        presenter.addToButtonPanel(runButton);

        dialog.setVisible(true);
    }
}
