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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.datacleaner.api.ComponentCategory;
import org.datacleaner.api.ComponentSuperCategory;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.connection.SchemaNavigator;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ComponentDescriptorsUpdatedListener;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.guice.InjectorBuilder;
import org.datacleaner.guice.Nullable;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.DragDropUtils;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.SchemaComparator;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.DescriptorMenuBuilder;
import org.datacleaner.widgets.DescriptorMenuBuilder.MenuCallback;
import org.datacleaner.widgets.tooltip.DCToolTip;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;
import org.jdesktop.swingx.renderer.WrappingIconPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

public class SchemaTree extends JXTree implements TreeWillExpandListener, TreeCellRenderer,
        ComponentDescriptorsUpdatedListener {

    private static final long serialVersionUID = 7763827443642264329L;

    private static final Logger logger = LoggerFactory.getLogger(SchemaTree.class);

    public static final String LOADING_TABLES_STRING = "Loading tables...";
    public static final String LOADING_COLUMNS_STRING = "Loading columns...";
    public static final String UNNAMED_SCHEMA_STRING = "(unnamed schema)";
    public static final String LIBRARY_STRING = "Library";
    public static final String ROOT_NODE_STRING = "Schemas";

    private final Datastore _datastore;
    private final DatastoreConnection _datastoreConnection;
    private final TreeCellRenderer _rendererDelegate;
    private final WindowContext _windowContext;
    private final AnalysisJobBuilder _analysisJobBuilder;
    private final InjectorBuilder _injectorBuilder;

    private String _searchTerm = "";

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
        _analysisJobBuilder.getConfiguration().getEnvironment().getDescriptorProvider()
                .addComponentDescriptorsUpdatedListener(this);

        ToolTipManager.sharedInstance().registerComponent(this);

        setCellRenderer(this);
        setOpaque(false);
        setRootVisible(false);
        setRowHeight(22);
        addTreeWillExpandListener(this);
        setDragEnabled(true);
        setTransferHandler(DragDropUtils.createSourceTransferHandler());
    }

    @Override
    public void addNotify() {
        super.addNotify();

        final Injector injector = _injectorBuilder.with(SchemaTree.class, this).createInjector();

        if (_analysisJobBuilder != null) {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON2 || e.getButton() == MouseEvent.BUTTON3) {
                        setSelectionPath(getPathForLocation(e.getX(), e.getY()));
                    }
                }
            });
            addMouseListener(injector.getInstance(SchemaMouseListener.class));
            addMouseListener(injector.getInstance(TableMouseListener.class));
            addMouseListener(injector.getInstance(ColumnMouseListener.class));
            addMouseListener(injector.getInstance(ComponentDescriptorMouseListener.class));
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

    public void expandSelectedData() {
        final List<Table> tables = _analysisJobBuilder.getSourceTables();
        for (Table table : tables) {
            expandTable(table);
        }
    }

    public void expandTable(Table table) {
        final DefaultMutableTreeNode treeNode = getTreeNode(table);
        if (treeNode == null) {
            return;
        }
        final TreeNode[] pathElements = treeNode.getPath();
        final TreePath path = new TreePath(pathElements);
        expandPath(path);
    }

    public void expandStandardPaths() {
        final TreeNode root = (TreeNode) getModel().getRoot();
        final DefaultMutableTreeNode schemaNode = (DefaultMutableTreeNode) root.getChildAt(0);
        final DefaultMutableTreeNode libraryNode = (DefaultMutableTreeNode) root.getChildAt(1);

        expandPath(new TreePath(schemaNode.getPath()));
        expandPath(new TreePath(libraryNode.getPath()));
    }

    public DefaultMutableTreeNode getTreeNode(final Schema schema) {
        if (schema == null) {
            return null;
        }

        final TreeNode root = (TreeNode) getModel().getRoot();
        for (int i = 0; i < root.getChildCount(); i++) {
            final DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getChildAt(0).getChildAt(i);
            final Object userObject = child.getUserObject();
            if (schema.equals(userObject)) {
                return child;
            }
        }
        return null;
    }

    public DefaultMutableTreeNode getTreeNode(final Table table) {
        if (table == null) {
            return null;
        }

        final DefaultMutableTreeNode schemaNode = getTreeNode(table.getSchema());
        if (schemaNode == null) {
            return null;
        }

        LoadTablesSwingWorker worker = new LoadTablesSwingWorker(schemaNode);
        worker.executeBlockingly();

        for (int i = 0; i < schemaNode.getChildCount(); i++) {
            final DefaultMutableTreeNode child = (DefaultMutableTreeNode) schemaNode.getChildAt(i);
            final Object userObject = child.getUserObject();
            if (table.equals(userObject)) {
                return child;
            }
        }
        return null;
    }

    public WindowContext getWindowContext() {
        return _windowContext;
    }

    private void updateTree() {

        final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();

        final DefaultMutableTreeNode datastoreNode = new DefaultMutableTreeNode();
        rootNode.add(datastoreNode);

        datastoreNode.setUserObject(_datastoreConnection.getDatastore());
        final SchemaNavigator schemaNavigator = _datastoreConnection.getSchemaNavigator();
        schemaNavigator.refreshSchemas();
        Schema[] schemas = schemaNavigator.getSchemas();

        // make sure that information schemas are arranged at the top
        Arrays.sort(schemas, new SchemaComparator());

        for (final Schema schema : schemas) {
            final DefaultMutableTreeNode schemaNode = new DefaultMutableTreeNode(schema);
            schemaNode.add(new DefaultMutableTreeNode(LOADING_TABLES_STRING));
            datastoreNode.add(schemaNode);
        }

        DefaultMutableTreeNode libraryRoot = new DefaultMutableTreeNode(LIBRARY_STRING);
        createLibrary(libraryRoot);
        rootNode.add(libraryRoot);

        final DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
        setModel(treeModel);
    }

    private DefaultMutableTreeNode createLibrary(final DefaultMutableTreeNode libraryRoot) {
        final DescriptorProvider descriptorProvider = _analysisJobBuilder.getConfiguration().getEnvironment()
                .getDescriptorProvider();

        final Set<ComponentSuperCategory> superCategories = descriptorProvider.getComponentSuperCategories();
        for (ComponentSuperCategory superCategory : superCategories) {
            final DefaultMutableTreeNode schemaNode = new DefaultMutableTreeNode(superCategory);
            libraryRoot.add(schemaNode);
            final Collection<? extends ComponentDescriptor<?>> componentDescriptors = descriptorProvider
                    .getComponentDescriptorsOfSuperCategory(superCategory);

            final List<ComponentDescriptor<?>> filteredComponentDescriptors = new ArrayList<>();

            for (ComponentDescriptor<?> componentDescriptor : componentDescriptors) {
                final String displayName = componentDescriptor.getDisplayName();
                if (displayName.contains(_searchTerm)) {
                    filteredComponentDescriptors.add(componentDescriptor);
                }
            }

            final Map<ComponentCategory, DefaultMutableTreeNode> categoryTreeNodes = new HashMap<>();

            MenuCallback menuCallback = new MenuCallback() {
                @Override
                public void addCategory(ComponentCategory category) {
                    final DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(category);
                    categoryTreeNodes.put(category, treeNode);
                    schemaNode.add(treeNode);
                }

                @Override
                public void addComponentDescriptor(ComponentDescriptor<?> descriptor) {
                    boolean placedInSubmenu = false;
                    for (ComponentCategory category : descriptor.getComponentCategories()) {
                        if (categoryTreeNodes.containsKey(category)) {
                            placedInSubmenu = true;
                            final DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(descriptor);
                            categoryTreeNodes.get(category).add(treeNode);
                        }
                    }

                    if (!placedInSubmenu) {
                        schemaNode.add(new DefaultMutableTreeNode(descriptor));
                    }
                }
            };

            DescriptorMenuBuilder.createMenuStructure(menuCallback, filteredComponentDescriptors, true);

        }
        return libraryRoot;
    }

    public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
        if (event.getPath().getPathCount() == 2) {
            throw new ExpandVetoException(event);
        }
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

        public boolean isNeeded() {
            if (_schemaNode.getChildCount() == 1) {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) _schemaNode.getChildAt(0);
                if (LOADING_TABLES_STRING.equals(child.getUserObject())) {
                    return true;
                }
            }
            return false;
        }

        public void executeBlockingly() {
            if (!isNeeded()) {
                return;
            }
            final Schema schema = (Schema) _schemaNode.getUserObject();
            final Table[] tables = schema.getTables();
            for (Table table : tables) {
                String name = table.getName();
                logger.debug("Building table node: {}", name);
                DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(table);
                DefaultMutableTreeNode loadingColumnsNode = new DefaultMutableTreeNode(LOADING_COLUMNS_STRING);
                tableNode.add(loadingColumnsNode);
                _schemaNode.add(tableNode);
            }
            _schemaNode.remove(0);
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

        if (value == null) {
            return _rendererDelegate.getTreeCellRendererComponent(tree, "", selected, expanded, leaf, row, hasFocus);
        }

        Component component = null;
        ImageManager imageManager = ImageManager.get();
        Icon icon = null;

        if (value instanceof Datastore) {
            component = _rendererDelegate.getTreeCellRendererComponent(tree, ((Datastore) value).getName(), selected,
                    expanded, leaf, row, hasFocus);
            icon = IconUtils.getDatastoreIcon((Datastore) value, IconUtils.ICON_SIZE_MENU_ITEM);
        } else if (value instanceof Schema) {
            Schema schema = ((Schema) value);
            String schemaName = schema.getName();
            component = _rendererDelegate.getTreeCellRendererComponent(tree, schemaName, selected, expanded, leaf, row,
                    hasFocus);
            icon = imageManager.getImageIcon(IconUtils.MODEL_SCHEMA, IconUtils.ICON_SIZE_MENU_ITEM);
            if (SchemaComparator.isInformationSchema(schema)) {
                icon = imageManager.getImageIcon(IconUtils.MODEL_SCHEMA_INFORMATION, IconUtils.ICON_SIZE_MENU_ITEM);
            }
        } else if (value instanceof Table) {
            component = _rendererDelegate.getTreeCellRendererComponent(tree, ((Table) value).getName(), selected,
                    expanded, leaf, row, hasFocus);
            icon = imageManager.getImageIcon(IconUtils.MODEL_TABLE, IconUtils.ICON_SIZE_MENU_ITEM);
        } else if (value instanceof Column) {
            Column column = (Column) value;
            String columnLabel = column.getName();
            component = _rendererDelegate.getTreeCellRendererComponent(tree, columnLabel, selected, expanded, leaf,
                    row, hasFocus);
            icon = IconUtils.getColumnIcon(column, IconUtils.ICON_SIZE_MENU_ITEM);
        } else if (value instanceof ComponentSuperCategory) {
            ComponentSuperCategory superCategory = (ComponentSuperCategory) value;
            component = _rendererDelegate.getTreeCellRendererComponent(tree, superCategory.getName(), selected,
                    expanded, leaf, row, hasFocus);
            icon = IconUtils.getComponentSuperCategoryIcon(superCategory, IconUtils.ICON_SIZE_MENU_ITEM);
        } else if (value instanceof ComponentCategory) {
            ComponentCategory category = (ComponentCategory) value;
            component = _rendererDelegate.getTreeCellRendererComponent(tree, category.getName(), selected, expanded,
                    leaf, row, hasFocus);
            icon = IconUtils.getComponentCategoryIcon(category, IconUtils.ICON_SIZE_MENU_ITEM);
        } else if (value instanceof ComponentDescriptor<?>) {
            ComponentDescriptor<?> descriptor = (ComponentDescriptor<?>) value;
            component = _rendererDelegate.getTreeCellRendererComponent(tree, descriptor.getDisplayName(), selected,
                    expanded, leaf, row, hasFocus);
            icon = IconUtils.getDescriptorIcon(descriptor, IconUtils.ICON_SIZE_MENU_ITEM, false);
        } else if (value instanceof String) {
            if (LIBRARY_STRING.equals(value)) {
                icon = imageManager.getImageIcon(IconUtils.MODEL_COMPONENT_LIBRARY, IconUtils.ICON_SIZE_MENU_ITEM);
            }
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

    @Override
    public JToolTip createToolTip() {
        final DCPanel panel = new DCPanel();
        panel.setOpaque(true);
        panel.setBackground(WidgetUtils.BG_COLOR_DARK);
        panel.setBorder(WidgetUtils.BORDER_THIN);
        panel.setLayout(new VerticalLayout());
        DCToolTip toolTip = new DCToolTip(this, panel);
        toolTip.addPropertyChangeListener("tiptext", new PropertyChangeListener() {
            private String oldToolText = "";

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (oldToolText.equals(evt.getNewValue())) {
                    return;
                }

                panel.removeAll();

                String description = (String) evt.getNewValue();
                if (!StringUtils.isNullOrEmpty(description)) {
                    String[] lines = description.split("\n");

                    for (int i = 0; i < lines.length; i++) {
                        String line = lines[i];

                        DCLabel label = DCLabel.brightMultiLine(line);
                        label.setBorder(new EmptyBorder(0, 4, 4, 0));
                        label.setMaximumWidth(350);

                        panel.add(label);
                    }
                }
            }
        });

        return toolTip;

    }

    @Override
    public String getToolTipText(MouseEvent event) {
        TreePath path = getPathForLocation(event.getX(), event.getY());
        if (path == null) {
            return null;
        }

        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        final Object userObject = node.getUserObject();
        if (userObject instanceof ComponentSuperCategory) {
            return ((ComponentSuperCategory) userObject).getDescription();
        } else if (userObject instanceof ComponentDescriptor<?>) {
            return ((ComponentDescriptor<?>) userObject).getDescription();
        }

        return null;
    }

    public Datastore getDatastore() {
        return _datastore;
    }

    @Override
    public void componentDescriptorsUpdated() {
        final TreeNode root = (TreeNode) getModel().getRoot();
        final DefaultMutableTreeNode libraryNode = (DefaultMutableTreeNode) root.getChildAt(1);
        libraryNode.removeAllChildren();
        createLibrary(libraryNode);
        DefaultTreeModel model = (DefaultTreeModel) getModel();
        model.reload(libraryNode);
        expandStandardPaths();
    }

    public void filter(String searchTerm) {
        _searchTerm = searchTerm;
        updateTree();
        expandStandardPaths();

        final TreeNode root = (TreeNode) getModel().getRoot();
        final DefaultMutableTreeNode libraryNode = (DefaultMutableTreeNode) root.getChildAt(1);
        Enumeration<?> depthFirstEnumeration = libraryNode
                .depthFirstEnumeration();
        while (depthFirstEnumeration.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) depthFirstEnumeration.nextElement();
            TreePath treePath = new TreePath(node.getPath());
            expandPath(treePath);
        }
    }
}
