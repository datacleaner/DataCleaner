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
package org.datacleaner.widgets.visualization;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;

import org.apache.metamodel.schema.Table;
import org.datacleaner.api.Renderer;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.panels.ComponentBuilderPresenter;
import org.datacleaner.panels.ComponentBuilderPresenterRenderingFormat;
import org.datacleaner.result.renderer.RendererFactory;
import org.datacleaner.windows.ComponentConfigurationDialog;
import org.datacleaner.windows.SourceTableConfigurationDialog;

/**
 * Implements various user actions for use in the {@link JobGraph}.
 */
public class JobGraphActions {

    private final JobGraphContext _graphContext;
    private final WindowContext _windowContext;
    private final Map<ComponentBuilder, ComponentConfigurationDialog> _componentConfigurationDialogs;
    private final Map<Table, SourceTableConfigurationDialog> _tableConfigurationDialogs;
    private final RendererFactory _presenterRendererFactory;

    public JobGraphActions(JobGraphContext graphContext, WindowContext windowContext,
            RendererFactory presenterRendererFactory,
            Map<ComponentBuilder, ComponentConfigurationDialog> componentConfigurationDialogs,
            Map<Table, SourceTableConfigurationDialog> tableConfigurationDialogs) {
        _graphContext = graphContext;
        _windowContext = windowContext;
        _presenterRendererFactory = presenterRendererFactory;
        _componentConfigurationDialogs = componentConfigurationDialogs;
        _tableConfigurationDialogs = tableConfigurationDialogs;
    }

    public void showConfigurationDialog(final ComponentBuilder componentBuilder) {
        final ComponentConfigurationDialog existingDialog = _componentConfigurationDialogs.get(componentBuilder);
        if (existingDialog != null) {
            existingDialog.toFront();
            return;
        }

        @SuppressWarnings("unchecked")
        final Renderer<ComponentBuilder, ? extends ComponentBuilderPresenter> renderer = (Renderer<ComponentBuilder, ? extends ComponentBuilderPresenter>) _presenterRendererFactory
                .getRenderer(componentBuilder, ComponentBuilderPresenterRenderingFormat.class);

        if (renderer != null) {
            final ComponentBuilderPresenter presenter = renderer.render(componentBuilder);

            final ComponentConfigurationDialog dialog = new ComponentConfigurationDialog(_windowContext,
                    componentBuilder, _graphContext.getAnalysisJobBuilder(), presenter);
            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    _componentConfigurationDialogs.remove(componentBuilder);
                    _graphContext.getJobGraph().refresh();
                }
            });
            _componentConfigurationDialogs.put(componentBuilder, dialog);
            dialog.open();
        }
    }

    public void showTableConfigurationDialog(final Table table) {
        final SourceTableConfigurationDialog existingDialog = _tableConfigurationDialogs.get(table);
        if (existingDialog != null) {
            existingDialog.toFront();
            return;
        }

        SourceTableConfigurationDialog dialog = new SourceTableConfigurationDialog(_windowContext,
                _graphContext.getAnalysisJobBuilder(), table);

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                _tableConfigurationDialogs.remove(table);
            }
        });
        _tableConfigurationDialogs.put(table, dialog);

        dialog.open();
    }
}
