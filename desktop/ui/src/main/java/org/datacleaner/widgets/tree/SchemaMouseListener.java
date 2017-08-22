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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.UpdateableDatastore;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.windows.CreateTableDialog;

final class SchemaMouseListener extends MouseAdapter implements MouseListener {

    private final WindowContext _windowContext;
    private final SchemaTree _schemaTree;
    private final Provider<TableMouseListener> _tableMouseListenerProvider;

    @Inject
    protected SchemaMouseListener(final WindowContext windowContext, final SchemaTree schemaTree,
            final Provider<TableMouseListener> tableMouseListenerProvider) {
        _windowContext = windowContext;
        _schemaTree = schemaTree;
        _tableMouseListenerProvider = tableMouseListenerProvider;
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
        final TreePath path = _schemaTree.getPathForLocation(e.getX(), e.getY());
        if (path == null) {
            return;
        }

        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        final Object userObject = node.getUserObject();
        if (userObject instanceof Schema) {
            final Schema schema = (Schema) userObject;
            final int button = e.getButton();

            if (button == MouseEvent.BUTTON2 || button == MouseEvent.BUTTON3) {
                final JPopupMenu popup = new JPopupMenu();
                popup.setLabel(schema.getName());

                addAddTablesToSourceMenuItem(schema, popup);
                addCreateTableMenuItem(schema, popup);

                popup.show((Component) e.getSource(), e.getX(), e.getY());
            }
        }
    }

    private void addAddTablesToSourceMenuItem(final Schema schema, final JPopupMenu popup) {
        final JMenuItem addTableItem = WidgetFactory
                .createMenuItem("Add all schema tables to source", "images/actions/toggle-source-table.png");
        addTableItem.addActionListener(e -> {
            final TableMouseListener tableMouseListener = _tableMouseListenerProvider.get();
            final List<Table> tables = schema.getTables();
            for (final Table table : tables) {
                tableMouseListener.addTable(table);
            }
        });
        popup.add(addTableItem);
    }

    private void addCreateTableMenuItem(final Schema schema, final JPopupMenu popup) {
        final Datastore datastore = _schemaTree.getDatastore();
        if (CreateTableDialog.isCreateTableAppropriate(datastore, schema)) {
            popup.addSeparator();

            final UpdateableDatastore updateableDatastore = (UpdateableDatastore) datastore;
            final JMenuItem createTableMenuItem =
                    WidgetFactory.createMenuItem("Create table", IconUtils.ACTION_CREATE_TABLE);
            createTableMenuItem.addActionListener(e -> {
                final CreateTableDialog dialog = new CreateTableDialog(_windowContext, updateableDatastore, schema);
                dialog.addListener((datastore1, schema1, tableName) -> _schemaTree.refreshDatastore());
                dialog.open();
            });
            popup.add(createTableMenuItem);
        }
    }
}
