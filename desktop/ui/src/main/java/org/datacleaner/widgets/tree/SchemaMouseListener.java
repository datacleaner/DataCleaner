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
import javax.inject.Provider;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.metamodel.MetaModelHelper;
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
    protected SchemaMouseListener(WindowContext windowContext, SchemaTree schemaTree, Provider<TableMouseListener> tableMouseListenerProvider) {
        _windowContext = windowContext;
        _schemaTree = schemaTree;
        _tableMouseListenerProvider = tableMouseListenerProvider;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        TreePath path = _schemaTree.getPathForLocation(e.getX(), e.getY());
        if (path == null) {
            return;
        }

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        Object userObject = node.getUserObject();
        if (userObject instanceof Schema) {
            final Schema schema = (Schema) userObject;
            int button = e.getButton();

            if (button == MouseEvent.BUTTON2 || button == MouseEvent.BUTTON3) {
                JPopupMenu popup = new JPopupMenu();
                popup.setLabel(schema.getName());
                JMenuItem addTableItem = WidgetFactory.createMenuItem("Add all schema tables to source",
                        "images/actions/toggle-source-table.png");
                addTableItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        TableMouseListener tableMouseListener = _tableMouseListenerProvider.get();
                        Table[] tables = schema.getTables();
                        for (Table table : tables) {
                            tableMouseListener.addTable(table);
                        }
                    }
                });
                popup.add(addTableItem);

                final Datastore datastore = _schemaTree.getDatastore();
                if (datastore instanceof UpdateableDatastore) {
                    if (!MetaModelHelper.isInformationSchema(schema)) {
                        popup.addSeparator();
                        
                        final UpdateableDatastore updateableDatastore = (UpdateableDatastore) datastore;
                        final JMenuItem dropTableMenuItem = WidgetFactory.createMenuItem("Create table", IconUtils.ACTION_CREATE_TABLE);
                        dropTableMenuItem.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                final CreateTableDialog dialog = new CreateTableDialog(_windowContext, updateableDatastore,
                                        schema, _schemaTree);
                                dialog.open();
                            }
                        });
                        popup.add(dropTableMenuItem);
                    }
                }

                popup.show((Component) e.getSource(), e.getX(), e.getY());
            }
        }
    }
}
