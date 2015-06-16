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
package org.datacleaner.widgets.tree;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.inject.Inject;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.datacleaner.actions.PreviewSourceDataActionListener;
import org.datacleaner.actions.QueryActionListener;
import org.datacleaner.actions.QuickAnalysisActionListener;
import org.datacleaner.actions.SaveTableAsCsvFileActionListener;
import org.datacleaner.actions.SaveTableAsExcelSpreadsheetActionListener;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.UpdateableDatastore;
import org.datacleaner.guice.InjectorBuilder;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.windows.DropTableDialog;

import com.google.inject.Injector;

final class TableMouseListener extends MouseAdapter implements MouseListener {

    private final AnalysisJobBuilder _analysisJobBuilder;
    private final SchemaTree _schemaTree;
    private final InjectorBuilder _injectorBuilder;
    private final WindowContext _windowContext;

    @Inject
    protected TableMouseListener(WindowContext windowContext, SchemaTree schemaTree,
            AnalysisJobBuilder analysisJobBuilder, InjectorBuilder injectorBuilder) {
        _windowContext = windowContext;
        _schemaTree = schemaTree;
        _analysisJobBuilder = analysisJobBuilder;
        _injectorBuilder = injectorBuilder;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        final TreePath path = _schemaTree.getPathForLocation(e.getX(), e.getY());
        if (path == null) {
            return;
        }
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        final Object userObject = node.getUserObject();
        if (userObject instanceof Table) {
            final Table table = (Table) userObject;

            int button = e.getButton();

            if (button == MouseEvent.BUTTON1 && e.getClickCount() > 1) {
                // double click = add table
                addTable(table);
            } else if (button == MouseEvent.BUTTON2 || button == MouseEvent.BUTTON3) {
                // right click = open popup menu

                boolean enableAddTable = false;
                boolean enableRemoveTable = false;

                final Column[] columns = table.getColumns();
                for (Column column : columns) {
                    if (_analysisJobBuilder.containsSourceColumn(column)) {
                        enableRemoveTable = true;
                    } else {
                        enableAddTable = true;
                    }
                    if (enableAddTable && enableRemoveTable) {
                        break;
                    }
                }

                final Injector injector = _injectorBuilder.with(Table.class, table).with(Column[].class, null)
                        .createInjector();

                final JPopupMenu popup = new JPopupMenu();
                popup.setLabel(table.getName());

                if (enableAddTable) {
                    addAddTableToSourceMenuItem(table, popup);
                }

                if (enableRemoveTable) {
                    addRemoveTableFromSourceMenuItem(table, popup);
                }

                addQuickAnalysisMenuItem(injector, popup);
                addQueryTableMenuItem(table, popup);
                addSaveTableAsExcelMenuItem(popup, injector);
                addSaveTableAsCsvMenuItem(popup, injector);
                addPreviewTableMenuItem(columns, popup);
                addDropTableMenuItem(table, popup);

                popup.show((Component) e.getSource(), e.getX(), e.getY());
            }
        }
    }

    private void addQueryTableMenuItem(final Table table, final JPopupMenu popup) {
        final JMenuItem queryMenuItem = WidgetFactory.createMenuItem("Ad-hoc query", IconUtils.MODEL_QUERY);
        queryMenuItem.addActionListener(new QueryActionListener(_schemaTree.getWindowContext(), _analysisJobBuilder,
                table));
        popup.add(queryMenuItem);
    }

    private void addQuickAnalysisMenuItem(final Injector injector, final JPopupMenu popup) {
        final JMenuItem quickAnalysisMenuItem = WidgetFactory.createMenuItem("Quick analysis",
                IconUtils.MODEL_QUICK_ANALYSIS);

        final QuickAnalysisActionListener quickAnalysisActionListener = injector
                .getInstance(QuickAnalysisActionListener.class);
        quickAnalysisMenuItem.addActionListener(quickAnalysisActionListener);
        popup.add(quickAnalysisMenuItem);
    }

    private void addRemoveTableFromSourceMenuItem(final Table table, final JPopupMenu popup) {
        final JMenuItem removeTableItem = WidgetFactory.createMenuItem("Remove table from source",
                "images/actions/toggle-source-table.png");
        removeTableItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeTable(table);
            }
        });
        popup.add(removeTableItem);
    }

    private void addAddTableToSourceMenuItem(final Table table, final JPopupMenu popup) {
        final JMenuItem addTableItem = WidgetFactory.createMenuItem("Add table to source",
                "images/actions/toggle-source-table.png");
        addTableItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTable(table);
            }
        });
        popup.add(addTableItem);
    }

    private void addSaveTableAsExcelMenuItem(final JPopupMenu popup, Injector injector) {
        final JMenuItem saveAsExcelFileMenuItem = WidgetFactory.createMenuItem("Save table as Excel spreadsheet",
                IconUtils.COMPONENT_TYPE_WRITE_DATA);
        final SaveTableAsExcelSpreadsheetActionListener saveTableAsExcelSpreadsheetActionListener = injector
                .getInstance(SaveTableAsExcelSpreadsheetActionListener.class);
        saveAsExcelFileMenuItem.addActionListener(saveTableAsExcelSpreadsheetActionListener);
        popup.add(saveAsExcelFileMenuItem);
    }

    private void addSaveTableAsCsvMenuItem(final JPopupMenu popup, Injector injector) {
        final JMenuItem saveAsCsvFileMenuItem = WidgetFactory.createMenuItem("Save table as CSV file",
                IconUtils.COMPONENT_TYPE_WRITE_DATA);
        final SaveTableAsCsvFileActionListener saveTableAsCsvFileActionListener = injector
                .getInstance(SaveTableAsCsvFileActionListener.class);
        saveAsCsvFileMenuItem.addActionListener(saveTableAsCsvFileActionListener);
        popup.add(saveAsCsvFileMenuItem);
    }

    private void addPreviewTableMenuItem(final Column[] columns, final JPopupMenu popup) {
        final JMenuItem previewMenuItem = WidgetFactory.createMenuItem("Preview table", IconUtils.ACTION_PREVIEW);
        previewMenuItem.addActionListener(new PreviewSourceDataActionListener(_schemaTree.getWindowContext(),
                _schemaTree.getDatastore(), columns));
        popup.add(previewMenuItem);
    }

    private void addDropTableMenuItem(final Table table, final JPopupMenu popup) {
        final Datastore datastore = _schemaTree.getDatastore();
        if (datastore instanceof UpdateableDatastore) {
            popup.addSeparator();

            final UpdateableDatastore updateableDatastore = (UpdateableDatastore) datastore;
            final JMenuItem dropTableMenuItem = WidgetFactory.createMenuItem("Drop table", IconUtils.ACTION_DROP_TABLE);
            dropTableMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    final DropTableDialog dialog = new DropTableDialog(_windowContext, updateableDatastore, table,
                            _schemaTree);
                    dialog.open();
                }
            });
            popup.add(dropTableMenuItem);
        }
    }

    /**
     * toggles whether or not the column is in the source selection
     */
    public void addTable(Table table) {
        Column[] columns = table.getColumns();
        for (Column column : columns) {
            if (!_analysisJobBuilder.containsSourceColumn(column)) {
                _analysisJobBuilder.addSourceColumn(column);
            }
        }
    }

    /**
     * toggles whether or not the column is in the source selection
     */
    public void removeTable(Table table) {
        Column[] columns = table.getColumns();
        for (Column column : columns) {
            _analysisJobBuilder.removeSourceColumn(column);
        }
    }
}
