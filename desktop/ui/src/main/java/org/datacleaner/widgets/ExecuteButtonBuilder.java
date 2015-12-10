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
package org.datacleaner.widgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

import org.datacleaner.actions.RunAnalysisActionListener;
import org.datacleaner.components.maxrows.MaxRowsFilter;
import org.datacleaner.components.maxrows.MaxRowsFilter.Category;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.guice.DCModule;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.FilterOutcome;
import org.datacleaner.job.SimpleComponentRequirement;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.job.concurrent.SingleThreadedTaskRunner;
import org.datacleaner.panels.ExecuteJobWithoutAnalyzersDialog;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.windows.AnalysisJobBuilderWindow;

/**
 * A builder for the "Execute" button in the job builder window of DataCleaner
 */
public class ExecuteButtonBuilder {

    private final JButton _executeButton;
    private final JButton _executionAlternativesButton;
    private final AnalysisJobBuilder _analysisJobBuilder;
    private final AnalysisJobBuilderWindow _window;

    public ExecuteButtonBuilder(AnalysisJobBuilderWindow window) {
        _window = window;
        _analysisJobBuilder = window.getAnalysisJobBuilder();

        _executeButton = WidgetFactory.createToolbarButton("Execute", IconUtils.MENU_EXECUTE);
        _executionAlternativesButton = WidgetFactory.createToolbarButton(WidgetUtils.CHAR_CARET_DOWN, null);
        _executionAlternativesButton.setFont(WidgetUtils.FONT_FONTAWESOME);

        _executeButton.addActionListener(execute(_analysisJobBuilder));

        _executionAlternativesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JMenuItem executeNormallyMenutItem = WidgetFactory.createMenuItem("Run normally",
                        IconUtils.ACTION_EXECUTE);
                executeNormallyMenutItem.addActionListener(execute(_analysisJobBuilder));

                final JMenuItem executePreviewMenuItem = WidgetFactory.createMenuItem("Run first N records",
                        IconUtils.ACTION_PREVIEW);
                executePreviewMenuItem.addActionListener(executePreview());

                final JMenuItem executeSingleThreadedMenuItem = WidgetFactory.createMenuItem("Run single-threaded",
                        IconUtils.MODEL_ROW);
                executeSingleThreadedMenuItem.addActionListener(executeSingleThreaded());

                final JPopupMenu menu = new JPopupMenu();
                menu.add(executeNormallyMenutItem);
                menu.addSeparator();
                menu.add(executePreviewMenuItem);
                menu.add(executeSingleThreadedMenuItem);

                final int horizontalPosition = -1 * menu.getPreferredSize().width
                        + _executionAlternativesButton.getWidth();
                menu.show(_executionAlternativesButton, horizontalPosition, _executionAlternativesButton.getHeight());
            }
        });
    }

    public void setEnabled(boolean enabled) {
        _executeButton.setEnabled(enabled);
        _executionAlternativesButton.setEnabled(enabled);
    }

    public void addComponentsToToolbar(JToolBar toolBar) {
        toolBar.add(_executeButton);
        toolBar.add(DCLabel.bright("|"));
        toolBar.add(_executionAlternativesButton);
    }

    private ActionListener executeSingleThreaded() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final DataCleanerConfiguration baseConfiguration = _window.getConfiguration();
                final DataCleanerConfigurationImpl configuration = new DataCleanerConfigurationImpl(baseConfiguration)
                        .withEnvironment(new DataCleanerEnvironmentImpl(baseConfiguration.getEnvironment())
                                .withTaskRunner(new SingleThreadedTaskRunner()));

                final AnalysisJob jobCopy = _analysisJobBuilder.toAnalysisJob(false);
                final AnalysisJobBuilder jobBuilderCopy = new AnalysisJobBuilder(configuration, jobCopy);

                execute(jobBuilderCopy).actionPerformed(e);
            }
        };
    }

    private ActionListener executePreview() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Integer maxRows = WidgetFactory.showMaxRowsDialog(100);

                if (maxRows != null) {
                    final AnalysisJob jobCopy = _analysisJobBuilder.toAnalysisJob(false);
                    final AnalysisJobBuilder jobBuilderCopy = new AnalysisJobBuilder(_window.getConfiguration(),
                            jobCopy);
                    final FilterComponentBuilder<MaxRowsFilter, Category> maxRowsFilter = jobBuilderCopy
                            .addFilter(MaxRowsFilter.class);
                    maxRowsFilter.getComponentInstance().setMaxRows(maxRows.intValue());
                    maxRowsFilter.addInputColumn(jobBuilderCopy.getSourceColumns().get(0));
                    final FilterOutcome filterOutcome = maxRowsFilter.getFilterOutcome(MaxRowsFilter.Category.VALID);
                    final Collection<ComponentBuilder> componentBuilders = jobBuilderCopy.getComponentBuilders();
                    for (ComponentBuilder componentBuilder : componentBuilders) {
                        if (componentBuilder != maxRowsFilter && componentBuilder.getComponentRequirement() == null) {
                            componentBuilder.setComponentRequirement(new SimpleComponentRequirement(filterOutcome));
                        }
                    }

                    execute(jobBuilderCopy).actionPerformed(e);
                }
            }
        };
    }

    private ActionListener execute(final AnalysisJobBuilder analysisJobBuilder) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final DCModule dcModule = _window.getDCModule();
                if (analysisJobBuilder.getResultProducingComponentBuilders().isEmpty()) {
                    if (analysisJobBuilder.getConsumedOutputDataStreamsJobBuilders().isEmpty()) {
                        // Present choices to user to write file somewhere,
                        // and then run a copy of the job based on that.
                        ExecuteJobWithoutAnalyzersDialog executeJobWithoutAnalyzersPanel = new ExecuteJobWithoutAnalyzersDialog(
                                dcModule, _window.getWindowContext(), analysisJobBuilder, _window.getUserPreferences());
                        executeJobWithoutAnalyzersPanel.open();
                        return;
                    }
                }

                final RunAnalysisActionListener runAnalysis = new RunAnalysisActionListener(dcModule,
                        analysisJobBuilder);
                runAnalysis.run();
            }
        };
    }

}
