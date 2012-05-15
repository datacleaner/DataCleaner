/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.widgets.tree;

import java.awt.Component;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.util.SchemaNavigator;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.guice.InjectorBuilder;
import org.eobjects.datacleaner.guice.Nullable;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.SchemaComparator;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Schema;
import org.eobjects.metamodel.schema.Table;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;
import org.jdesktop.swingx.renderer.WrappingIconPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

public class SchemaTree extends JXTree implements TreeWillExpandListener, TreeCellRenderer {

    private static final long serialVersionUID = 7763827443642264329L;

    private static final Logger logger = LoggerFactory.getLogger(SchemaTree.class);

    public static final String LOADING_TABLES_STRING = "Loading tables...";
    public static final String LOADING_COLUMNS_STRING = "Loading columns...";
    public static final String UNNAMED_SCHEMA_STRING = "(unnamed schema)";
    public static final String ROOT_NODE_STRING = "Schemas";

    private final Datastore _datastore;
    private final DatastoreConnection _datastoreConnection;
    private final TreeCellRenderer _rendererDelegate;
    private final WindowContext _windowContext;
    private final AnalysisJobBuilder _analysisJobBuilder;
    private final InjectorBuilder _injectorBuilder;

    @Inject
    protected SchemaTree(final Datastore datastore, @Nullable AnalysisJobBuilder analysisJobBuilder,
            WindowContext windowContext, InjectorBuilder injectorBuilder) {
        super();
        if (datastore == null) {
            throw new IllegalArgumentException("Datastore cannot be null");
        }
        _datastore = datastore;
        _windowContext = windowContext;
        _analysisJobBuilder = analysisJobBuilder;
        _injectorBuilder = injectorBuilder;
        _datastoreConnection = datastore.openConnection();
        _rendererDelegate = new DefaultTreeRenderer();
        setCellRenderer(this);
        setOpaque(false);
        addTreeWillExpandListener(this);
    }

    @Override
    public void addNotify() {
        super.addNotify();

        Injector injector = _injectorBuilder.with(SchemaTree.class, this).createInjector();

        if (_analysisJobBuilder != null) {
            addMouseListener(injector.getInstance(SchemaMouseListener.class));
            addMouseListener(injector.getInstance(TableMouseListener.class));
            addMouseListener(injector.getInstance(ColumnMouseListener.class));
        }
        updateTree();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        MouseListener[] mouseListeners = getMouseListeners();
        for (MouseListener mouseListener : mouseListeners) {
            removeMouseListener(mouseListener);
        }
        _datastoreConnection.close();
    }

    public WindowContext getWindowContext() {
        return _windowContext;
    }

    private void updateTree() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        rootNode.setUserObject(_datastoreConnection.getDatastore());
        SchemaNavigator schemaNavigator = _datastoreConnection.getSchemaNavigator();
        schemaNavigator.refreshSchemas();
        Schema[] schemas = schemaNavigator.getSchemas();

        // make sure that information schemas are arranged at the top
        Arrays.sort(schemas, new SchemaComparator());

        for (Schema schema : schemas) {
            DefaultMutableTreeNode schemaNode = new DefaultMutableTreeNode(schema);
            schemaNode.add(new DefaultMutableTreeNode(LOADING_TABLES_STRING));
            rootNode.add(schemaNode);
        }
        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
        setModel(treeModel);
    }

    public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
        // Do nothing
    }

    public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
        TreePath path = event.getPath();
        DefaultMutableTreeNode lastTreeNode = (DefaultMutableTreeNode) path.getLastPathComponent();
        SwingWorker<?, ?> worker = null;
        if (lastTreeNode.getChildCount() == 1) {
            DefaultMutableTreeNode firstChildNode = (DefaultMutableTreeNode) lastTreeNode.getChildAt(0);
            if (firstChildNode.getUserObject() == LOADING_TABLES_STRING) {
                // Load a schema's tables
                worker = new LoadTablesSwingWorker(lastTreeNode);
            } else if (firstChildNode.getUserObject() == LOADING_COLUMNS_STRING) {
                // Load a table's columns
                worker = new LoadColumnsSwingWorker(path, lastTreeNode);
            }
        }

        if (worker != null) {
            worker.execute();
        }
    }

    class LoadTablesSwingWorker extends SwingWorker<Void, Table> {

        private final DefaultMutableTreeNode _schemaNode;

        public LoadTablesSwingWorker(DefaultMutableTreeNode schemaNode) {
            _schemaNode = schemaNode;
        }

        @Override
        protected Void doInBackground() throws Exception {
            Schema schema = (Schema) _schemaNode.getUserObject();
            Table[] tables = schema.getTables();
            for (Table table : tables) {
                String name = table.getName();
                logger.debug("Publishing table name: {}", name);
                publish(table);
            }
            return null;
        }

        @Override
        protected void process(List<Table> chunks) {
            for (Table table : chunks) {
                DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(table);
                DefaultMutableTreeNode loadingColumnsNode = new DefaultMutableTreeNode(LOADING_COLUMNS_STRING);
                tableNode.add(loadingColumnsNode);
                _schemaNode.add(tableNode);
            }
            updateUI();
        }

        protected void done() {
            _schemaNode.remove(0);
            updateUI();
        };
    }

    class LoadColumnsSwingWorker extends SwingWorker<Void, Column> {

        private final DefaultMutableTreeNode _tableNode;

        public LoadColumnsSwingWorker(TreePath path, DefaultMutableTreeNode tableNode) {
            _tableNode = tableNode;
        }

        @Override
        protected Void doInBackground() throws Exception {
            Table table = (Table) _tableNode.getUserObject();
            Column[] columns = table.getColumns();
            for (Column column : columns) {
                String name = column.getName();
                logger.debug("Publishing column name: {}", name);
                publish(column);
            }
            return null;
        }

        protected void process(List<Column> chunks) {
            for (Column column : chunks) {
                DefaultMutableTreeNode columnNode = new DefaultMutableTreeNode(column);
                _tableNode.add(columnNode);
            }
            updateUI();
        };

        protected void done() {
            _tableNode.remove(0);
            updateUI();
        };
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {
        if (value instanceof DefaultMutableTreeNode) {
            value = ((DefaultMutableTreeNode) value).getUserObject();
        }

        Component component = null;
        ImageManager imageManager = ImageManager.getInstance();
        Icon icon = null;

        if (value instanceof Datastore) {
            component = _rendererDelegate.getTreeCellRendererComponent(tree, ((Datastore) value).getName(), selected,
                    expanded, leaf, row, hasFocus);
            icon = IconUtils.getDatastoreIcon((Datastore) value, IconUtils.ICON_SIZE_SMALL);
        } else if (value instanceof Schema) {
            Schema schema = ((Schema) value);
            String schemaName = schema.getName();
            component = _rendererDelegate.getTreeCellRendererComponent(tree, schemaName, selected, expanded, leaf, row,
                    hasFocus);
            icon = imageManager.getImageIcon("images/model/schema.png", IconUtils.ICON_SIZE_SMALL);
            if (SchemaComparator.isInformationSchema(schema)) {
                icon = imageManager.getImageIcon("images/model/schema_information.png", IconUtils.ICON_SIZE_SMALL);
            }
        } else if (value instanceof Table) {
            component = _rendererDelegate.getTreeCellRendererComponent(tree, ((Table) value).getName(), selected,
                    expanded, leaf, row, hasFocus);
            icon = imageManager.getImageIcon("images/model/table.png", IconUtils.ICON_SIZE_SMALL);
        } else if (value instanceof Column) {
            Column column = (Column) value;
            String columnLabel = column.getName();
            component = _rendererDelegate.getTreeCellRendererComponent(tree, columnLabel, selected, expanded, leaf,
                    row, hasFocus);
            icon = IconUtils.getColumnIcon(column, IconUtils.ICON_SIZE_SMALL);
        } else if (value instanceof String) {
            component = _rendererDelegate.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row,
                    hasFocus);
        }

        if (component == null) {
            throw new IllegalArgumentException("Unexpected value: " + value + " of class: "
                    + (value == null ? "<null>" : value.getClass().getName()));
        }

        final boolean opaque = hasFocus || selected;
        if (component instanceof JComponent) {
            ((JComponent) component).setOpaque(opaque);
        }

        if (icon != null) {
            if (component instanceof WrappingIconPanel) {
                WrappingIconPanel wip = (WrappingIconPanel) component;
                wip.setIcon(icon);
                wip.getComponent().setOpaque(opaque);
            } else {
                logger.warn("Rendered TreeNode Component was not a WrappingIconPanel, cannot set icon!");
            }
        }

        return component;
    }

    public Datastore getDatastore() {
        return _datastore;
    }
}