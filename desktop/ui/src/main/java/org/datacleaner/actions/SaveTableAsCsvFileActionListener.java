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
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.descriptors.AnalyzerDescriptor;
import org.datacleaner.extension.output.CreateCsvFileAnalyzer;
import org.datacleaner.guice.DCModule;
import org.datacleaner.guice.DCModuleImpl;
import org.datacleaner.guice.InjectorBuilder;
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
 * Provides an action for the user to save a table as a CSV file
 */
public final class SaveTableAsCsvFileActionListener implements ActionListener {

    private final Datastore _datastore;
    private final Table _table;
    private final WindowContext _windowContext;
    private final DCModule _parentModule;
    private final UserPreferences _userPreferences;
    private DataCleanerConfiguration _configuration;

    @Inject
    protected SaveTableAsCsvFileActionListener(final Datastore datastore, final Table table,
            final WindowContext windowContext, final DCModule parentModule, final UserPreferences userPreferences,
            final DataCleanerConfiguration configuration, final InjectorBuilder injectorBuilder) {
        _datastore = datastore;
        _table = table;
        _windowContext = windowContext;
        _parentModule = parentModule;
        _userPreferences = userPreferences;
        _configuration = configuration;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final AnalysisJobBuilder ajb = new AnalysisJobBuilder(_configuration);
        ajb.setDatastore(_datastore);
        ajb.addSourceColumns(_table.getColumns());

        final AnalyzerComponentBuilder<CreateCsvFileAnalyzer> csvOutputAnalyzerBuilder =
                ajb.addAnalyzer(CreateCsvFileAnalyzer.class);
        csvOutputAnalyzerBuilder.addInputColumns(ajb.getSourceColumns());
        final File directory = _userPreferences.getConfiguredFileDirectory();
        csvOutputAnalyzerBuilder.getComponentInstance().setFile(new File(directory, _table.getName() + ".csv"));

        final PropertyWidgetFactory propertyWidgetFactory =
                _parentModule.createChildInjectorForComponent(csvOutputAnalyzerBuilder)
                        .getInstance(PropertyWidgetFactory.class);

        final AnalyzerComponentBuilderPanel presenter =
                new AnalyzerComponentBuilderPanel(csvOutputAnalyzerBuilder, propertyWidgetFactory);

        final AbstractDialog dialog = new AbstractDialog(_windowContext) {
            private static final long serialVersionUID = 1L;

            @Override
            public String getWindowTitle() {
                return "Save " + _table.getName() + " as CSV file";
            }

            @Override
            protected int getDialogWidth() {
                return 600;
            }

            @Override
            protected boolean isWindowResizable() {
                return true;
            }

            @Override
            protected JComponent getDialogContent() {
                final AnalyzerDescriptor<CreateCsvFileAnalyzer> descriptor = csvOutputAnalyzerBuilder.getDescriptor();
                final CloseableTabbedPane tabbedPane = new CloseableTabbedPane(true);
                tabbedPane.addTab(descriptor.getDisplayName(),
                        IconUtils.getDescriptorIcon(descriptor, IconUtils.ICON_SIZE_TAB), presenter.createJComponent());
                tabbedPane.setUnclosableTab(0);
                return tabbedPane;
            }

            @Override
            protected String getBannerTitle() {
                return "Save " + _table.getName() + "\nas CSV file";
            }
        };

        final JButton runButton = WidgetFactory.createPrimaryButton("Run", IconUtils.ACTION_EXECUTE);
        runButton.addActionListener(e12 -> {
            final Injector injector = Guice.createInjector(new DCModuleImpl(_parentModule, ajb));

            final ResultWindow window = injector.getInstance(ResultWindow.class);
            window.open();
            dialog.close();
            window.startAnalysis();
        });

        final JButton closeButton = WidgetFactory.createDefaultButton("Close", IconUtils.ACTION_CLOSE_DARK);
        closeButton.addActionListener(e1 -> dialog.close());

        presenter.addToButtonPanel(runButton);
        presenter.addToButtonPanel(closeButton);

        dialog.open();
    }
}
