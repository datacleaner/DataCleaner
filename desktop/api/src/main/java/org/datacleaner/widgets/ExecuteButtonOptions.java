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

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.metamodel.util.Action;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.job.concurrent.SingleThreadedTaskRunner;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.PreviewUtils;
import org.datacleaner.util.SourceColumnFinder;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.windows.AnalysisJobBuilderWindow;

/**
 * This class provides an API for hooking in "Execute" button options.
 *
 * Each hook is represented as an {@link ExecutionMenuItem} that will be shown
 * when the user clicks the caret symbol next to the "Execute" button.
 */
public class ExecuteButtonOptions {

    interface ExecutionMenuItem {

        String getText();

        String getIconPath();

        /**
         * Creates the action listener for the menu item. This method will be
         * called each time the menu is popping up.
         *
         * If the method returns null then the menu item will be marked as
         * disabled. This may be useful if certain functionality doesn't always
         * apply to the given job.
         *
         * @param analysisJobBuilder
         *            the current job builder object
         * @param executeAction
         *            an {@link Action} that can be invoked with any job builder
         *            to execute it.
         * @return
         */
        ActionListener createActionListener(AnalysisJobBuilder analysisJobBuilder,
                Action<AnalysisJobBuilder> executeAction, AnalysisJobBuilderWindow analysisJobBuilderWindow);

    }

    private static abstract class SimpleExecutionMenuItem implements ExecutionMenuItem {

        private final String _text;
        private final String _iconPath;

        public SimpleExecutionMenuItem(String text, String iconPath) {
            _text = text;
            _iconPath = iconPath;
        }

        @Override
        public final String getIconPath() {
            return _iconPath;
        }

        @Override
        public final String getText() {
            return _text;
        }

        @Override
        public final ActionListener createActionListener(final AnalysisJobBuilder analysisJobBuilder,
                final Action<AnalysisJobBuilder> executeAction, AnalysisJobBuilderWindow analysisJobBuilderWindow) {
            return event -> {
                try {
                    run(analysisJobBuilder, executeAction, analysisJobBuilderWindow);
                } catch (Exception e) {
                    WidgetUtils.showErrorMessage("Unexpected error",
                            "An error occurred while executing job in mode '" + getText() + "'", e);
                }
            };
        }

        protected abstract void run(AnalysisJobBuilder analysisJobBuilder, Action<AnalysisJobBuilder> executeAction,
                AnalysisJobBuilderWindow analysisJobBuilderWindow) throws Exception;
    }

    /**
     * Special purpose {@link ExecutionMenuItem} that represent a separator in
     * the menu.
     */
    public static class Separator implements ExecutionMenuItem {
        @Override
        public String getText() {
            return null;
        }

        @Override
        public String getIconPath() {
            return null;
        }

        @Override
        public ActionListener createActionListener(AnalysisJobBuilder analysisJobBuilder,
                Action<AnalysisJobBuilder> executeAction, AnalysisJobBuilderWindow analysisJobBuilderWindow) {
            return null;
        }
    }

    private static final List<ExecutionMenuItem> MENU_ITEMS = new ArrayList<>();

    static {
        // initialize the default menu items
        addMenuItem(new SimpleExecutionMenuItem("Run normally", IconUtils.ACTION_EXECUTE) {
            @Override
            protected void run(AnalysisJobBuilder analysisJobBuilder, Action<AnalysisJobBuilder> executeAction,
                    AnalysisJobBuilderWindow analysisJobBuilderWindow) throws Exception {
                executeAction.run(analysisJobBuilder);
            }
        });

        addMenuItem(new SimpleExecutionMenuItem("Run first N records", IconUtils.ACTION_PREVIEW) {
            @Override
            protected void run(AnalysisJobBuilder analysisJobBuilder, Action<AnalysisJobBuilder> executeAction,
                    AnalysisJobBuilderWindow analysisJobBuilderWindow) throws Exception {
                final Integer maxRows = WidgetFactory.showMaxRowsDialog(100);

                if (maxRows != null) {
                    final AnalysisJob jobCopy = analysisJobBuilder.toAnalysisJob(false);
                    final AnalysisJobBuilder jobBuilderCopy = new AnalysisJobBuilder(
                            analysisJobBuilder.getConfiguration(), jobCopy);

                    final Set<ComponentBuilder> analyzers = jobBuilderCopy.getComponentBuilders().stream()
                            .filter(o -> o instanceof AnalyzerComponentBuilder)
                            .collect(Collectors.toSet());

                    SourceColumnFinder scf = new SourceColumnFinder();
                    scf.addSources(jobBuilderCopy);

                    final Set<ComponentBuilder> filtered =
                            analyzers.stream().filter(acb -> PreviewUtils.hasFilterPresent(scf, acb)).collect(Collectors.toSet());

                    if (filtered.size() == 0) {
                        PreviewUtils.limitJobRows(jobBuilderCopy, jobBuilderCopy.getComponentBuilders(), maxRows);
                    } else {
                        PreviewUtils.limitJobRows(jobBuilderCopy, jobBuilderCopy.getFilterComponentBuilders(), maxRows);

                        final Set<ComponentBuilder> unfilteredAnalyzers =
                                analyzers.stream().filter(acb -> !PreviewUtils.hasFilterPresent(scf, acb)).collect(Collectors.toSet());

                        // TODO: This may risk running through more input rows than intended, but alternative is worse.
                        // TODO: Transformers implementing HasAnalyzerResult will _not_ undergo special filtering.
                        PreviewUtils.limitJobRows(jobBuilderCopy, unfilteredAnalyzers, maxRows);

                    }

                    executeAction.run(jobBuilderCopy);
                }
            }
        });

        addMenuItem(new SimpleExecutionMenuItem("Run single-threaded", IconUtils.MODEL_ROW) {
            @Override
            protected void run(AnalysisJobBuilder analysisJobBuilder, Action<AnalysisJobBuilder> executeAction,
                    AnalysisJobBuilderWindow analysisJobBuilderWindow) throws Exception {
                final DataCleanerConfiguration baseConfiguration = analysisJobBuilder.getConfiguration();
                final DataCleanerConfigurationImpl configuration = new DataCleanerConfigurationImpl(baseConfiguration)
                        .withEnvironment(new DataCleanerEnvironmentImpl(baseConfiguration.getEnvironment())
                                .withTaskRunner(new SingleThreadedTaskRunner()));

                final AnalysisJob jobCopy = analysisJobBuilder.toAnalysisJob(false);
                final AnalysisJobBuilder jobBuilderCopy = new AnalysisJobBuilder(configuration, jobCopy);

                executeAction.run(jobBuilderCopy);
            }
        });
    }

    public static void addMenuItem(ExecutionMenuItem menuItem) {
        MENU_ITEMS.add(menuItem);
    }

    public static void addMenuItem(int index, ExecutionMenuItem menuItem) {
        MENU_ITEMS.add(index, menuItem);
    }

    public static void removeMenuItem(ExecutionMenuItem menuItem) {
        MENU_ITEMS.remove(menuItem);
    }

    public static void removeMenuItem(int index) {
        MENU_ITEMS.remove(index);
    }

    public static List<ExecutionMenuItem> getMenuItems() {
        return Collections.unmodifiableList(MENU_ITEMS);
    }

    private ExecuteButtonOptions() {
        // prevent instantiation
    }
}
