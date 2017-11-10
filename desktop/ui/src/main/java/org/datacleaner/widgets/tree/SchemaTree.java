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
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.swing.Icon;
import javax.swing.ImageIcon;
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

import org.apache.metamodel.MetaModelHelper;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.datacleaner.api.ComponentCategory;
import org.datacleaner.api.ComponentSuperCategory;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.connection.SchemaNavigator;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.descriptors.DescriptorProviderListener;
import org.datacleaner.guice.InjectorBuilder;
import org.datacleaner.guice.Nullable;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.DragDropUtils;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.SchemaComparator;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetScreenResolutionAdjuster;
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

public class SchemaTree extends JXTree implements TreeWillExpandListener, TreeCellRenderer, DescriptorProviderListener {
    
    private final WidgetScreenResolutionAdjuster adjuster = WidgetScreenResolutionAdjuster.get();

    class LoadTablesSwingWorker extends SwingWorker<Void, Table> {

        private final DefaultMutableTreeNode _schemaNode;

        public LoadTablesSwingWorker(final DefaultMutableTreeNode schemaNode) {
            _schemaNode = schemaNode;
        }

        public boolean isNeeded() {
            if (_schemaNode.getChildCount() == 1) {
                final DefaultMutableTreeNode child = (DefaultMutableTreeNode) _schemaNode.getChildAt(0);
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
            final List<Table> tables = schema.getTables();
            for (final Table table : tables) {
                final String name = table.getName();
                logger.debug("Building table node: {}", name);
                final DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(table);
                final DefaultMutableTreeNode loadingColumnsNode = new DefaultMutableTreeNode(LOADING_COLUMNS_STRING);
                tableNode.add(loadingColumnsNode);
                _schemaNode.add(tableNode);
            }
            _schemaNode.remove(0);
        }

        @Override
        protected Void doInBackground() throws Exception {
            final Schema schema = (Schema) _schemaNode.getUserObject();
            final List<Table> tables = schema.getTables();
            for (final Table table : tables) {
                final String name = table.getName();
                logger.debug("Publishing table name: {}", name);
                publish(table);
            }
            return null;
        }

        @Override
        protected void process(final List<Table> chunks) {
            for (final Table table : chunks) {
                final DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(table);
                final DefaultMutableTreeNode loadingColumnsNode = new DefaultMutableTreeNode(LOADING_COLUMNS_STRING);
                tableNode.add(loadingColumnsNode);
                _schemaNode.add(tableNode);
            }
            updateUI();
        }

        protected void done() {
            _schemaNode.remove(0);
            updateUI();
        }

    }

    class LoadColumnsSwingWorker extends SwingWorker<Void, Column> {

        private final DefaultMutableTreeNode _tableNode;

        public LoadColumnsSwingWorker(final TreePath path, final DefaultMutableTreeNode tableNode) {
            _tableNode = tableNode;
        }

        @Override
        protected Void doInBackground() throws Exception {
            final Table table = (Table) _tableNode.getUserObject();
            final List<Column> columns = table.getColumns();
            for (final Column column : columns) {
                final String name = column.getName();
                logger.debug("Publishing column name: {}", name);
                publish(column);
            }
            return null;
        }

        protected void process(final List<Column> chunks) {
            for (final Column column : chunks) {
                final DefaultMutableTreeNode columnNode = new DefaultMutableTreeNode(column);
                _tableNode.add(columnNode);
            }
            updateUI();
        }

        protected void done() {
            _tableNode.remove(0);
            updateUI();
        }

    }

    public static final String LOADING_TABLES_STRING = "Loading tables...";
    public static final String LOADING_COLUMNS_STRING = "Loading columns...";
    public static final String UNNAMED_SCHEMA_STRING = "(unnamed schema)";
    public static final String LIBRARY_STRING = "Library";
    public static final String ROOT_NODE_STRING = "Schemas";
    private static final long serialVersionUID = 7763827443642264329L;
    private static final Logger logger = LoggerFactory.getLogger(SchemaTree.class);
    private static final String NO_COMPONENTS_FOUND_SEARCH_RESULT = "No components found matching search criteria.";
    private final Datastore _datastore;
    private final TreeCellRenderer _rendererDelegate;
    private final WindowContext _windowContext;
    private final AnalysisJobBuilder _analysisJobBuilder;
    private final InjectorBuilder _injectorBuilder;
    private final DataCleanerConfiguration _configuration;
    private boolean _includeLibraryNode = true;
    private DatastoreConnection _datastoreConnection;
    private String _searchTerm = "";

    @Inject
    protected SchemaTree(final Datastore datastore, @Nullable final AnalysisJobBuilder analysisJobBuilder,
            final DataCleanerConfiguration configuration, final WindowContext windowContext,
            final InjectorBuilder injectorBuilder) {
        super();
        if (datastore == null) {
            throw new IllegalArgumentException("Datastore cannot be null");
        }
        _datastore = datastore;
        _windowContext = windowContext;
        _analysisJobBuilder = analysisJobBuilder;
        _configuration = configuration;
        _injectorBuilder = injectorBuilder;
        _datastoreConnection = datastore.openConnection();
        _rendererDelegate = new DefaultTreeRenderer();

        ToolTipManager.sharedInstance().registerComponent(this);

        setCellRenderer(this);
        setOpaque(false);
        setRootVisible(false);
        setRowHeight(adjuster.adjust(22));
        addTreeWillExpandListener(this);
        setDragEnabled(true);
        setTransferHandler(DragDropUtils.createSourceTransferHandler());
    }

    private static String normalizeStringForMatching(final String str) {
        return StringUtils.replaceWhitespaces(str, "").toLowerCase();
    }

    @Override
    public void addNotify() {
        super.addNotify();

        _configuration.getEnvironment().getDescriptorProvider().addListener(this);

        final Injector injector = _injectorBuilder.with(SchemaTree.class, this).createInjector();

        if (_analysisJobBuilder != null) {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent e) {
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

        _configuration.getEnvironment().getDescriptorProvider().removeListener(this);

        final MouseListener[] mouseListeners = getMouseListeners();
        for (final MouseListener mouseListener : mouseListeners) {
            removeMouseListener(mouseListener);
        }
        _datastoreConnection.close();
    }

    public void expandSelectedData() {
        if (_analysisJobBuilder == null) {
            // do nothing
            return;
        }
        final List<Table> tables = _analysisJobBuilder.getSourceTables();
        for (final Table table : tables) {
            expandTable(table);
        }
    }

    public void expandTable(final Table table) {
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

        final LoadTablesSwingWorker worker = new LoadTablesSwingWorker(schemaNode);
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
        final Schema[] schemas = schemaNavigator.getSchemas();

        // make sure that information schemas are arranged at the top
        Arrays.sort(schemas, new SchemaComparator());

        for (final Schema schema : schemas) {
            final DefaultMutableTreeNode schemaNode = new DefaultMutableTreeNode(schema);
            schemaNode.add(new DefaultMutableTreeNode(LOADING_TABLES_STRING));
            datastoreNode.add(schemaNode);
        }

        if (_includeLibraryNode) {
            final DefaultMutableTreeNode libraryRoot = new DefaultMutableTreeNode(LIBRARY_STRING);
            createLibrary(libraryRoot);
            rootNode.add(libraryRoot);
        }

        final DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
        setModel(treeModel);
    }

    private DefaultMutableTreeNode createLibrary(final DefaultMutableTreeNode libraryRoot) {
        final DescriptorProvider descriptorProvider = _configuration.getEnvironment().getDescriptorProvider();

        final Set<ComponentSuperCategory> superCategories = descriptorProvider.getComponentSuperCategories();
        for (final ComponentSuperCategory superCategory : superCategories) {
            final DefaultMutableTreeNode superCategoryNode = new DefaultMutableTreeNode(superCategory);
            final Collection<? extends ComponentDescriptor<?>> componentDescriptors =
                    descriptorProvider.getComponentDescriptorsOfSuperCategory(superCategory);

            final List<ComponentDescriptor<?>> filteredComponentDescriptors = new ArrayList<>();

            for (final ComponentDescriptor<?> componentDescriptor : componentDescriptors) {
                if (matchesSearchTerm(componentDescriptor)) {
                    filteredComponentDescriptors.add(componentDescriptor);
                }
            }

            if (filteredComponentDescriptors.size() > 0) {
                libraryRoot.add(superCategoryNode);
            }

            final Map<ComponentCategory, DefaultMutableTreeNode> categoryTreeNodes = new HashMap<>();

            final MenuCallback menuCallback = new MenuCallback() {
                @Override
                public void addCategory(final ComponentCategory category) {
                    final DefaultMutableTreeNode treeNode = new SortedDefaultMutableTreeModel(category);
                    categoryTreeNodes.put(category, treeNode);
                    superCategoryNode.add(treeNode);
                }

                @Override
                public void addComponentDescriptor(final ComponentDescriptor<?> descriptor) {
                    boolean placedInSubmenu = false;
                    for (final ComponentCategory category : descriptor.getComponentCategories()) {
                        if (categoryTreeNodes.containsKey(category)) {
                            placedInSubmenu = true;
                            final DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(descriptor);
                            categoryTreeNodes.get(category).add(treeNode);
                        }
                    }

                    if (!placedInSubmenu) {
                        superCategoryNode.add(new DefaultMutableTreeNode(descriptor));
                    }
                }
            };

            final Comparator<ComponentDescriptor<?>> comparatorDescriptor =
                    (o1, o2) -> o1.getDisplayName().compareTo(o2.getDisplayName());

            Collections.sort(filteredComponentDescriptors, comparatorDescriptor);
            DescriptorMenuBuilder.createMenuStructure(menuCallback, filteredComponentDescriptors);

        }
        if (libraryRoot.getChildCount() == 0) {
            libraryRoot.add(new DefaultMutableTreeNode(NO_COMPONENTS_FOUND_SEARCH_RESULT));
        }
        return libraryRoot;
    }

    private boolean matchesSearchTerm(final ComponentDescriptor<?> componentDescriptor) {
        final String searchTerm = normalizeStringForMatching(_searchTerm);
        if (searchTerm.isEmpty()) {
            return true;
        }

        final String displayName = normalizeStringForMatching(componentDescriptor.getDisplayName());
        if (displayName.contains(searchTerm)) {
            return true;
        }

        final String[] aliases = componentDescriptor.getAliases();
        for (String alias : aliases) {
            alias = normalizeStringForMatching(alias);
            if (alias.contains(searchTerm)) {
                return true;
            }
        }

        final Set<ComponentCategory> categories = componentDescriptor.getComponentCategories();
        for (final ComponentCategory category : categories) {
            final String categoryString = normalizeStringForMatching(category.getName());
            if (categoryString.contains(searchTerm)) {
                return true;
            }
        }

        return false;
    }

    public void treeWillCollapse(final TreeExpansionEvent event) throws ExpandVetoException {
        if (event.getPath().getPathCount() == 2) {
            throw new ExpandVetoException(event);
        }
    }

    public void treeWillExpand(final TreeExpansionEvent event) throws ExpandVetoException {
        final TreePath path = event.getPath();
        final DefaultMutableTreeNode lastTreeNode = (DefaultMutableTreeNode) path.getLastPathComponent();
        SwingWorker<?, ?> worker = null;
        if (lastTreeNode.getChildCount() == 1) {
            final DefaultMutableTreeNode firstChildNode = (DefaultMutableTreeNode) lastTreeNode.getChildAt(0);
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

    @Override
    public Component getTreeCellRendererComponent(final JTree tree, Object value, final boolean selected,
            final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
        if (value instanceof DefaultMutableTreeNode) {
            value = ((DefaultMutableTreeNode) value).getUserObject();
        }

        if (value == null) {
            return _rendererDelegate.getTreeCellRendererComponent(tree, "", selected, expanded, leaf, row, hasFocus);
        }

        Component component = null;
        final ImageManager imageManager = ImageManager.get();
        Icon icon = null;

        if (value instanceof Datastore) {
            component = _rendererDelegate
                    .getTreeCellRendererComponent(tree, ((Datastore) value).getName(), selected, expanded, leaf, row,
                            hasFocus);
            icon = IconUtils.getDatastoreIcon((Datastore) value, IconUtils.ICON_SIZE_MENU_ITEM);
        } else if (value instanceof Schema) {
            final Schema schema = ((Schema) value);
            final String schemaName = schema.getName();
            component = _rendererDelegate
                    .getTreeCellRendererComponent(tree, schemaName, selected, expanded, leaf, row, hasFocus);
            icon = imageManager.getImageIcon(IconUtils.MODEL_SCHEMA, IconUtils.ICON_SIZE_MENU_ITEM);
            if (MetaModelHelper.isInformationSchema(schema)) {
                icon = imageManager.getImageIcon(IconUtils.MODEL_SCHEMA_INFORMATION, IconUtils.ICON_SIZE_MENU_ITEM);
            }
        } else if (value instanceof Table) {
            component = _rendererDelegate
                    .getTreeCellRendererComponent(tree, ((Table) value).getName(), selected, expanded, leaf, row,
                            hasFocus);
            icon = imageManager.getImageIcon(IconUtils.MODEL_TABLE, IconUtils.ICON_SIZE_MENU_ITEM);
        } else if (value instanceof Column) {
            final Column column = (Column) value;
            final String columnLabel = column.getName();
            component = _rendererDelegate
                    .getTreeCellRendererComponent(tree, columnLabel, selected, expanded, leaf, row, hasFocus);
            icon = IconUtils.getColumnIcon(column, IconUtils.ICON_SIZE_MENU_ITEM);
        } else if (value instanceof ComponentSuperCategory) {
            final ComponentSuperCategory superCategory = (ComponentSuperCategory) value;
            component = _rendererDelegate
                    .getTreeCellRendererComponent(tree, superCategory.getName(), selected, expanded, leaf, row,
                            hasFocus);
            icon = IconUtils.getComponentSuperCategoryIcon(superCategory, IconUtils.ICON_SIZE_MENU_ITEM);
        } else if (value instanceof ComponentCategory) {
            final ComponentCategory category = (ComponentCategory) value;
            component = _rendererDelegate
                    .getTreeCellRendererComponent(tree, category.getName(), selected, expanded, leaf, row, hasFocus);
            icon = IconUtils.getComponentCategoryIcon(category, IconUtils.ICON_SIZE_MENU_ITEM);
        } else if (value instanceof ComponentDescriptor<?>) {
            final ComponentDescriptor<?> descriptor = (ComponentDescriptor<?>) value;
            component = _rendererDelegate
                    .getTreeCellRendererComponent(tree, descriptor.getDisplayName(), selected, expanded, leaf, row,
                            hasFocus);
            icon = IconUtils.getDescriptorIcon(descriptor, IconUtils.ICON_SIZE_MENU_ITEM, false);
        } else if (value instanceof String) {
            if (LIBRARY_STRING.equals(value)) {
                icon = imageManager.getImageIcon(IconUtils.MODEL_COMPONENT_LIBRARY, IconUtils.ICON_SIZE_MENU_ITEM);
            } else if (NO_COMPONENTS_FOUND_SEARCH_RESULT.equals(value)) {
                // "empty" icon -> no icon
                icon = new ImageIcon();
            }
            component = _rendererDelegate
                    .getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        }

        if (component == null) {
            throw new IllegalArgumentException("Unexpected value: " + value + " of class: " + (value == null
                    ? "<null>"
                    : value.getClass().getName()));
        }

        final boolean opaque = hasFocus || selected;
        if (component instanceof JComponent) {
            ((JComponent) component).setOpaque(opaque);
        }

        if (icon != null) {
            if (component instanceof WrappingIconPanel) {
                final WrappingIconPanel wip = (WrappingIconPanel) component;
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
        final DCToolTip toolTip = new DCToolTip(this, panel);
        toolTip.addPropertyChangeListener("tiptext", new PropertyChangeListener() {
            private String oldToolText = "";

            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                if (oldToolText.equals(evt.getNewValue())) {
                    return;
                }

                panel.removeAll();

                final String description = (String) evt.getNewValue();
                if (!StringUtils.isNullOrEmpty(description)) {
                    final String[] lines = description.split("\n");

                    for (int i = 0; i < lines.length; i++) {
                        final String line = lines[i];

                        final DCLabel label = DCLabel.brightMultiLine(line);
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
    public String getToolTipText(final MouseEvent event) {
        final TreePath path = getPathForLocation(event.getX(), event.getY());
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
    public void onDescriptorsUpdated(final DescriptorProvider descriptorProvider) {
        WidgetUtils.invokeSwingAction(() -> {
            final TreeNode root = (TreeNode) getModel().getRoot();
            final DefaultMutableTreeNode libraryNode = (DefaultMutableTreeNode) root.getChildAt(1);
            libraryNode.removeAllChildren();
            createLibrary(libraryNode);
            final DefaultTreeModel model = (DefaultTreeModel) getModel();
            model.reload(libraryNode);
            expandStandardPaths();
        });
    }


    /**
     * Refreshes the tree's contents
     */
    public void refreshDatastore() {
        updateTree();
        expandStandardPaths();
    }

    public void filter(final String searchTerm) {
        _searchTerm = searchTerm;
        updateTree();
        expandStandardPaths();

        // Expand only when search is active
        if (!searchTerm.equals("")) {
            final TreeNode root = (TreeNode) getModel().getRoot();
            final DefaultMutableTreeNode libraryNode = (DefaultMutableTreeNode) root.getChildAt(1);
            final Enumeration<?> depthFirstEnumeration = libraryNode.depthFirstEnumeration();
            while (depthFirstEnumeration.hasMoreElements()) {
                final DefaultMutableTreeNode node = (DefaultMutableTreeNode) depthFirstEnumeration.nextElement();
                final TreePath treePath = new TreePath(node.getPath());
                expandPath(treePath);
            }
        }
    }

    public void setIncludeLibraryNode(final boolean includeLibraryNode) {
        _includeLibraryNode = includeLibraryNode;
    }

}
