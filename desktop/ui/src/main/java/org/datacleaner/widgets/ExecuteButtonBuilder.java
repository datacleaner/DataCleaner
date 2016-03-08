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
import java.util.List;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

import org.apache.metamodel.util.Action;
import org.datacleaner.actions.RunAnalysisActionListener;
import org.datacleaner.guice.DCModule;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.panels.ExecuteJobWithoutAnalyzersDialog;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.ExecuteButtonOptions.ExecutionMenuItem;
import org.datacleaner.windows.AnalysisJobBuilderWindow;

/**
 * A builder for the "Execute" button in the job builder window of DataCleaner
 */
public class ExecuteButtonBuilder {

    private final JButton _mainButton;
    private final JButton _alternativesButton;
    private final AnalysisJobBuilder _analysisJobBuilder;
    private final AnalysisJobBuilderWindow _window;

    public ExecuteButtonBuilder(AnalysisJobBuilderWindow window) {
        _window = window;
        _analysisJobBuilder = window.getAnalysisJobBuilder();

        _mainButton = WidgetFactory.createToolbarButton("Execute", IconUtils.MENU_EXECUTE);
        _alternativesButton = WidgetFactory.createToolbarButton(WidgetUtils.CHAR_CARET_DOWN, null);
        _alternativesButton.setFont(WidgetUtils.FONT_FONTAWESOME);

        _mainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                execute(_analysisJobBuilder);
            }
        });

        _alternativesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final JPopupMenu menu = new JPopupMenu();

                final Action<AnalysisJobBuilder> executeAction = new Action<AnalysisJobBuilder>() {
                    @Override
                    public void run(AnalysisJobBuilder jobBuilder) throws Exception {
                        execute(jobBuilder);
                    }
                };
                final List<ExecutionMenuItem> menuItems = ExecuteButtonOptions.getMenuItems();
                for (ExecutionMenuItem item : menuItems) {
                    if (item instanceof ExecuteButtonOptions.Separator) {
                        menu.addSeparator();
                    } else {
                        final JMenuItem menuItem = WidgetFactory.createMenuItem(item.getText(), item.getIconPath());
                        final ActionListener actionListener = item.createActionListener(_analysisJobBuilder,
                                executeAction, _window);
                        if (actionListener == null) {
                            menuItem.setEnabled(false);
                        } else {
                            menuItem.addActionListener(actionListener);
                        }
                        menu.add(menuItem);
                    }
                }

                final int horizontalPosition = -1 * menu.getPreferredSize().width + _alternativesButton.getWidth();
                menu.show(_alternativesButton, horizontalPosition, _alternativesButton.getHeight());
            }
        });
    }

    public void setEnabled(boolean enabled) {
        _mainButton.setEnabled(enabled);
        _alternativesButton.setEnabled(enabled);
    }

    public void addComponentsToToolbar(JToolBar toolBar) {
        toolBar.add(_mainButton);
        toolBar.add(DCLabel.bright("|"));
        toolBar.add(_alternativesButton);
    }

    private void execute(final AnalysisJobBuilder analysisJobBuilder) {
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

        final RunAnalysisActionListener runAnalysis = new RunAnalysisActionListener(dcModule, analysisJobBuilder);
        runAnalysis.run();
    }

}
