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

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Set;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.datacleaner.actions.DefaultRenameComponentActionListener;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link KeyListener} for the {@link JobGraph}.
 */
public class JobGraphKeyListener extends KeyAdapter {

    private static final Logger logger = LoggerFactory.getLogger(JobGraphKeyListener.class);

    private final JobGraphContext _graphContext;
    private final JobGraphActions _actions;

    public JobGraphKeyListener(JobGraphContext graphContext, JobGraphActions actions) {
        _graphContext = graphContext;
        _actions = actions;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
        case KeyEvent.VK_DELETE:
        case KeyEvent.VK_BACK_SPACE:
            onRemoveKey();
            break;
        case KeyEvent.VK_F2:
            onRenameKey();
            break;
        case KeyEvent.VK_F5:
            onRefreshKey();
            break;
        case KeyEvent.VK_ENTER:
            onEnterConfiguration();
            break;
        }
    }

    private void onRefreshKey() {
        _graphContext.getJobGraph().refresh();
    }

    private void onEnterConfiguration() {
        final Set<Object> vertices = _graphContext.getSelectedVertices();
        if (vertices.size() == 1) {
            final Object vertex = vertices.iterator().next();
            if (vertex instanceof ComponentBuilder) {
                final ComponentBuilder componentBuilder = (ComponentBuilder) vertex;
                _actions.showConfigurationDialog(componentBuilder);
            } else if (vertex instanceof Table) {
                final Table table = (Table) vertex;
                _actions.showTableConfigurationDialog(table);
            }
        }
    }

    private void onRenameKey() {
        final Set<Object> vertices = _graphContext.getSelectedVertices();
        if (vertices.size() == 1) {
            final Object vertex = vertices.iterator().next();
            if (vertex instanceof ComponentBuilder) {
                final ComponentBuilder componentBuilder = (ComponentBuilder) vertex;
                final DefaultRenameComponentActionListener actionListener = new DefaultRenameComponentActionListener(
                        componentBuilder, _graphContext);
                actionListener.actionPerformed();
            }
        }
    }

    private void onRemoveKey() {
        final Set<Object> vertices = _graphContext.getSelectedVertices();
        logger.debug("Registered typed DEL. Vertices: {}", vertices);
        if ((vertices != null) && (!vertices.isEmpty())) {
            for (Object vertex : vertices) {
                final AnalysisJobBuilder analysisJobBuilder = _graphContext.getAnalysisJobBuilder(vertex);
                if (vertex instanceof TransformerComponentBuilder) {
                    final TransformerComponentBuilder<?> tjb = (TransformerComponentBuilder<?>) vertex;
                    analysisJobBuilder.removeTransformer(tjb);
                } else if (vertex instanceof AnalyzerComponentBuilder) {
                    final AnalyzerComponentBuilder<?> ajb = (AnalyzerComponentBuilder<?>) vertex;
                    analysisJobBuilder.removeAnalyzer(ajb);
                } else if (vertex instanceof FilterComponentBuilder) {
                    final FilterComponentBuilder<?, ?> fjb = (FilterComponentBuilder<?, ?>) vertex;
                    analysisJobBuilder.removeFilter(fjb);
                } else if (vertex instanceof Table) {
                    final Table table = (Table) vertex;
                    analysisJobBuilder.removeSourceTable(table);
                } else if (vertex instanceof Column) {
                    final Column column = (Column) vertex;
                    analysisJobBuilder.removeSourceColumn(column);
                }
            }
        }
    }
}
