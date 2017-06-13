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

import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.datacleaner.actions.DefaultRenameComponentActionListener;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;

/**
 * Key-bindings manager for the {@link JobGraph}.
 */
public class JobGraphBindingsManager {
    private abstract static class JobGraphBindingAction implements Action {
        private boolean _enabled = true;

        @Override
        public Object getValue(final String key) {
            return null;
        }

        @Override
        public void putValue(final String key, final Object value) {
        }

        @Override
        public boolean isEnabled() {
            return _enabled;
        }

        @Override
        public void setEnabled(final boolean enabled) {
            _enabled = enabled;
        }

        @Override
        public void addPropertyChangeListener(final PropertyChangeListener listener) {
        }

        @Override
        public void removePropertyChangeListener(final PropertyChangeListener listener) {
        }
    }

    private static final int INPUT_MAP_CONDITION = WHEN_IN_FOCUSED_WINDOW;
    private static final String KEY_DELETE = "DELETE";
    private static final String KEY_BACKSPACE = "BACK_SPACE";
    private static final String KEY_F2 = "F2";
    private static final String KEY_F5 = "F5";
    private static final String KEY_ENTER = "ENTER";
    private final JobGraphContext _graphContext;
    private final JobGraphActions _actions;
    private final JComponent _component;

    public JobGraphBindingsManager(final JobGraphContext graphContext, final JobGraphActions actions,
            final JComponent component) {
        _graphContext = graphContext;
        _actions = actions;
        _component = component;
    }

    public void register() {
        registerDeleteAction();
        registerRenameAction();
        registerRefreshAction();
        registerEnterConfigurationAction();
    }

    private void registerRefreshAction() {
        registerAction(KEY_F5, KeyStroke.getKeyStroke(KEY_F5), new JobGraphBindingAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                _graphContext.getJobGraph().refresh();
            }
        });
    }

    private void registerEnterConfigurationAction() {
        registerAction(KEY_ENTER, KeyStroke.getKeyStroke(KEY_ENTER), new JobGraphBindingAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Set<Object> vertices = _graphContext.getSelectedVertices();

                if (vertices.size() != 1) {
                    return;
                }

                final Object vertex = vertices.iterator().next();

                if (vertex instanceof ComponentBuilder) {
                    final ComponentBuilder componentBuilder = (ComponentBuilder) vertex;
                    _actions.showConfigurationDialog(componentBuilder);
                } else if (vertex instanceof Table) {
                    final Table table = (Table) vertex;
                    _actions.showTableConfigurationDialog(table);
                }
            }
        });
    }

    private void registerRenameAction() {
        registerAction(KEY_F2, KeyStroke.getKeyStroke(KEY_F2), new JobGraphBindingAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Set<Object> vertices = _graphContext.getSelectedVertices();

                if (vertices.size() != 1) {
                    return;
                }

                final Object vertex = vertices.iterator().next();

                if (vertex instanceof ComponentBuilder) {
                    final ComponentBuilder componentBuilder = (ComponentBuilder) vertex;
                    final DefaultRenameComponentActionListener actionListener =
                            new DefaultRenameComponentActionListener(componentBuilder, _graphContext);
                    actionListener.actionPerformed();
                }
            }
        });
    }

    private void registerDeleteAction() {
        registerAction(KEY_DELETE, KeyStroke.getKeyStroke(KEY_DELETE), new JobGraphBindingAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Set<Object> vertices = _graphContext.getSelectedVertices();

                if ((vertices == null) || vertices.isEmpty()) {
                    return;
                }

                for (final Object vertex : vertices) {
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
        });
        // register BACKSPACE key to the same action
        _component.getInputMap(INPUT_MAP_CONDITION).put(KeyStroke.getKeyStroke(KEY_BACKSPACE), KEY_DELETE);
    }

    private void registerAction(final String actionKey, final KeyStroke keyStroke, final Action action) {
        _component.getInputMap(INPUT_MAP_CONDITION).put(keyStroke, actionKey);
        _component.getActionMap().put(actionKey, action);
    }
}
